# reward_optimizer.py

import os
from typing import Dict, Any, Optional, Iterable

import numpy as np
import pandas as pd
from dotenv import load_dotenv
from lightgbm import LGBMRegressor
from openai import OpenAI
from scipy.optimize import minimize_scalar
from sklearn.compose import ColumnTransformer
from sklearn.metrics import r2_score
from sklearn.model_selection import KFold
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

load_dotenv()

client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

# -----------------------------
# 기본 설정
# -----------------------------
CATEGORICAL_COLS = [
    "event_type",
    "organizer_type",
    "target_major",
    "target_grade",
    "weekday",
]
NUMERIC_COLS = [
    "brand_score",
    "date_gap",
]
TARGET_COL = "attended_participants"

# 손실 함수 가중치 (정규화된 값 기준)
ALPHA = 3.0   # 목표와의 오차를 더 강하게
BETA = 0.2    # 비용 비중 줄이기


def load_event_data(csv_path: str) -> pd.DataFrame:
    """
    학습용 이벤트 로그 로드.
    attendance_rate 등 추가 지표는 분석용으로 사용 가능.
    """
    df = pd.read_csv(csv_path)
    df["attendance_rate"] = (
        df["attended_participants"] / df["applied_participants"].clip(lower=1)
    )
    return df


def apply_time_decay(
    reward_amount: float | np.ndarray,
    date_gap: float | np.ndarray,
    time_decay_lambda: float,
) -> float | np.ndarray:
    """
    리워드가 행사일까지 남은 일수에 따라 희석되는 효과를 반영.
    λ는 데이터 기반으로 튜닝한다.
    """
    return reward_amount * np.exp(-time_decay_lambda * date_gap)


class RewardMLModel:
    """
    - LightGBM 기반 참가자 수 예측 모델
    - reward_amount + date_gap → reward_effective 로 변환
    - time_decay_lambda, reward 범위 등을 내부에 보관해서
      최적화 단계에서 활용 가능하게 함
    """

    def __init__(
        self,
        time_decay_candidates: Optional[Iterable[float]] = None,
        n_splits_cv: int = 3,
    ):
        self.pipeline: Optional[Pipeline] = None
        self.time_decay_lambda: float = 0.0
        self.reward_min: float = 0.0
        self.reward_max: float = 8000.0
        self._time_decay_candidates = (
            list(time_decay_candidates)
            if time_decay_candidates is not None
            else [0.0, 0.01, 0.03, 0.05, 0.08]
        )
        self._n_splits_cv = n_splits_cv

    def _build_pipeline(self) -> Pipeline:
        """
        범주형 One-Hot + 수치형 스케일링 후 LGBMRegressor.
        실전에서 안정적인 조합.
        """
        cat_enc = OneHotEncoder(handle_unknown="ignore")
        num_scaler = StandardScaler()
        preproc = ColumnTransformer(
            [
                ("cat", cat_enc, CATEGORICAL_COLS),
                ("num", num_scaler, NUMERIC_COLS + ["reward_amount", "reward_effective"]),
            ]
        )

        model = LGBMRegressor(
            n_estimators=500,
            learning_rate=0.05,
            num_leaves=31,
            subsample=0.8,
            colsample_bytree=0.8,
            reg_lambda=1.0,
            random_state=42,
        )

        return Pipeline(
            [
                ("preprocess", preproc),
                ("model", model),
            ]
        )

    def _cv_score_for_lambda(self, df: pd.DataFrame, lam: float) -> float:
        """
        주어진 time_decay_lambda 에 대해 K-Fold CV R^2 점수 계산.
        """
        df = df.copy()
        df["reward_effective"] = apply_time_decay(
            df["reward_amount"], df["date_gap"], lam
        )
        X = df[CATEGORICAL_COLS + NUMERIC_COLS + ["reward_amount", "reward_effective"]]
        y = df[TARGET_COL]

        # 데이터 개수가 적으면 split 수를 줄임
        n_splits = min(self._n_splits_cv, len(df))
        if n_splits <= 1:
            # CV 의미 없으면 간단히 하나의 모델 점수로 대신
            pipeline = self._build_pipeline()
            pipeline.fit(X, y)
            preds = pipeline.predict(X)
            return r2_score(y, preds)

        kf = KFold(
            n_splits=n_splits,
            shuffle=True,
            random_state=42,
        )

        scores: list[float] = []
        for train_idx, val_idx in kf.split(X):
            X_train, X_val = X.iloc[train_idx], X.iloc[val_idx]
            y_train, y_val = y.iloc[train_idx], y.iloc[val_idx]

            pipeline = self._build_pipeline()
            pipeline.fit(X_train, y_train)
            preds = pipeline.predict(X_val)
            scores.append(r2_score(y_val, preds))
        return float(np.mean(scores))

    def fit(self, df: pd.DataFrame) -> None:
        """
        1) reward_amount 범위 및 통계값 저장
        2) time_decay_lambda 후보들에 대해 CV 점수 비교, 최적 λ 선택
        3) 최적 λ로 reward_effective 생성 후 전체 데이터로 최종 모델 학습
        """
        self.reward_min = float(df["reward_amount"].min())
        self.reward_max = float(df["reward_amount"].max())

        # 1. time_decay_lambda 튜닝
        best_lambda = 0.0
        best_score = -np.inf

        for lam in self._time_decay_candidates:
            score = self._cv_score_for_lambda(df, lam)
            if score > best_score:
                best_score = score
                best_lambda = lam

        self.time_decay_lambda = best_lambda

        # 2. 선택된 λ로 최종 학습
        df = df.copy()
        df["reward_effective"] = apply_time_decay(
            df["reward_amount"], df["date_gap"], self.time_decay_lambda
        )
        X = df[CATEGORICAL_COLS + NUMERIC_COLS + ["reward_amount", "reward_effective"]]
        y = df[TARGET_COL]

        self.pipeline = self._build_pipeline()
        self.pipeline.fit(X, y)

    def predict_participants(
        self, features: Dict[str, Any], reward: float
    ) -> float:
        """
        주어진 feature + reward에 대해 예상 참가자 수 예측.
        음수 예측은 0으로 클리핑.
        """
        if self.pipeline is None:
            raise RuntimeError("Model pipeline is not fitted yet.")

        f = features.copy()
        f["reward_effective"] = apply_time_decay(
            reward, f["date_gap"], self.time_decay_lambda
        )
        f["reward_amount"] = reward
        X = pd.DataFrame([f])[CATEGORICAL_COLS + NUMERIC_COLS + ["reward_amount", "reward_effective"]]
        pred = float(self.pipeline.predict(X)[0])
        return max(pred, 0.0)


def loss_function(
    r: float,
    model: RewardMLModel,
    features: Dict[str, Any],
    target: float,
    reward_scale: float,
) -> float:
    """
    실전용 손실 함수:
    - 상대 오차 |예상 - 목표| / max(목표, 1)
    - 비용 비율 (예상 * 리워드) / (reward_scale * max(목표, 1))
    """
    expected = model.predict_participants(features, r)
    target_safe = max(target, 1.0)

    rel_error = abs(expected - target) / target_safe

    # reward_scale ~ 학습 데이터의 최대 리워드 또는 상한값
    denom = max(reward_scale * target_safe, 1e-6)
    rel_cost = (expected * r) / denom

    return ALPHA * rel_error + BETA * rel_cost


def find_best_reward(
    model: RewardMLModel,
    features: Dict[str, Any],
    target: float,
    low: Optional[float] = None,
    high: Optional[float] = None,
):
    """
    - 모델 내부에 저장된 reward_min / reward_max를 기본 탐색 범위로 사용
    - 손실 함수는 비율 기반이므로 데이터 스케일이 바뀌어도 안정적
    """
    if low is None:
        low = max(0.0, getattr(model, "reward_min", 0.0))
    if high is None:
        # 상한에 약간의 buffer를 줘서 탐색 여유 확보
        base_max = getattr(model, "reward_max", 8000.0)
        high = base_max * 1.2

    reward_scale = high  # 비용 정규화 기준

    res = minimize_scalar(
        lambda r: loss_function(r, model, features, target, reward_scale),
        bounds=(low, high),
        method="bounded",
    )
    best_r = float(res.x)
    expected = model.predict_participants(features, best_r)
    return best_r, expected


def build_prompt(
    features: Dict[str, Any],
    reward: float,
    participants: float,
    target: float,
) -> str:
    """
    LLM 설명 생성을 위한 프롬프트.
    """
    return f"""
행사명: {features.get('title', '(제목 없음)')}
유형: {features['event_type']}
주최: {features['organizer_type']}
대상: {features['target_major']} ({features['target_grade']})
남은 모집 일수: {features['date_gap']}
브랜드 점수: {features['brand_score']}
홍보 강도: {features['promotion_intensity']}

추천 리워드 금액: {reward:.0f}원
예상 참여자 수: {participants:.1f}명
목표 인원: {target}

위 정보를 바탕으로,
1) 왜 이 금액이 적절한지,
2) 남은 일수·브랜드 점수·홍보 강도 등의 요인이 어떻게 작용했는지,
3) 예산 대비 효율성 측면에서 간단한 조언을 3~5문장으로 제시해주세요.
""".strip()


def generate_explanation(prompt: str) -> str:
    """
    GPT를 이용해 사람 친화적인 설명 생성.
    실패 시 기본 설명 반환.
    """
    # try:
    #     completion = client.chat.completions.create(
    #         model="gpt-5-nano",
    #         messages=[
    #             {
    #                 "role": "system",
    #                 "content": "당신은 대학 행사 리워드 최적화 전문가입니다.",
    #             },
    #             {"role": "user", "content": prompt},
    #         ],
    #         temperature=0.4,
    #     )
    #     return completion.choices[0].message.content.strip()
    # except Exception:
    return (
            "추천 리워드와 예측 참여자 수를 바탕으로 설정된 금액입니다. "
            "남은 모집 일수, 브랜드 인지도, 홍보 강도 등을 종합적으로 고려했습니다."
        )
