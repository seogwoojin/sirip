# api.py
import base64
import json
import openai
from fastapi import FastAPI, File, UploadFile
from pydantic import BaseModel
from typing import Optional

from reward_optimizer import (
    load_event_data,
    RewardMLModel,
    find_best_reward,
    build_prompt,
    generate_explanation,
)

app = FastAPI()
model: Optional[RewardMLModel] = None


# -----------------------------
# Models
# -----------------------------
class EventRequest(BaseModel):
    title: str
    event_type: str
    organizer_type: str
    target_major: str
    target_grade: str
    weekday: str
    brand_score: float
    date_gap: int
    target_participants: float


class TextRequest(BaseModel):
    text: str
    target_participants: float


# -----------------------------
# Startup
# -----------------------------
@app.on_event("startup")
def startup_event():
    global model
    df = load_event_data("event_logs_realistic_300.csv")
    m = RewardMLModel()
    print("hereJJJ")
    m.fit(df)
    model = m


# -----------------------------
# Common LLM Call
# -----------------------------
def call_llm(prompt: str, image_base64: Optional[str] = None):
    messages = [{"role": "system", "content": "You are an assistant that extracts structured data for event optimization."}]
    if image_base64:
        messages.append({
            "role": "user",
            "content": [
                {"type": "text", "text": prompt},
                {"type": "image_url", "image_url": f"data:image/jpeg;base64,{image_base64}"}
            ]
        })
    else:
        messages.append({"role": "user", "content": prompt})

    response = openai.ChatCompletion.create(
        model="gpt-5-nano",
        messages=messages,
        response_format={"type": "json_object"},
    )
    return json.loads(response.choices[0].message["content"])


# -----------------------------
# /optimize
# -----------------------------
@app.post("/optimize")
def optimize(req: EventRequest):
    if model is None:
        return {"error": "model not initialized"}

    features = req.dict(exclude={"target_participants"})
    best_reward, expected_participants = find_best_reward(model, features, req.target_participants)

    # prompt = build_prompt(features, best_reward, expected_participants, req.target_participants)
    # explanation = generate_explanation(prompt)
    for r in [0, 1000, 2000, 4000, 8000]:
        print(r, model.predict_participants(features, r))
    return {
        "features": features,
        "recommended_reward": float(best_reward),
        "expected_participants": float(expected_participants),
        "target_participants": float(req.target_participants)
    }

# @app.get("/analyze") 
# def analazy():
#     for r in [0, 1000, 2000, 4000, 8000]:
#         print(r, model.predict_participants(features, r))
#     return 

# -----------------------------
# /analyze_text
# -----------------------------
@app.post("/analyze_text")
def analyze_text(req: TextRequest):
    if model is None:
        return {"error": "model not initialized"}

    prompt = f"""
    다음 공지문에서 아래 항목을 JSON으로 추출해줘:
    - title
    - event_type
    - organizer_type
    - target_major
    - target_grade
    - weekday
    - brand_score (1~5)
    - promotion_channels (정수)
    - promotion_intensity (0~1.0)
    - date_gap (행사일까지 남은 일수)
    공지문:
    {req.text}
    """

    features = call_llm(prompt)
    best_reward, expected_participants = find_best_reward(model, features, req.target_participants)

    # 설명 추가
    explanation_prompt = build_prompt(features, best_reward, expected_participants, req.target_participants)
    explanation = generate_explanation(explanation_prompt)

    return {
        "features": features,
        "recommended_reward": float(best_reward),
        "expected_participants": float(expected_participants),
        "target_participants": float(req.target_participants),
        "explanation": explanation,
    }


# -----------------------------
# /analyze_poster
# -----------------------------
@app.post("/analyze_poster")
async def analyze_poster(file: UploadFile = File(...), target_participants: float = 100.0):
    if model is None:
        return {"error": "model not initialized"}

    image_bytes = await file.read()
    base64_image = base64.b64encode(image_bytes).decode("utf-8")

    prompt = """
    다음 포스터에서 행사의 주요 정보를 JSON으로 추출해줘:
    {
      "title": "",
      "event_type": "",
      "organizer_type": "",
      "target_major": "",
      "target_grade": "",
      "weekday": "",
      "brand_score": 1~5,
      "promotion_channels": 1~5,
      "promotion_intensity": 0~1.0,
      "date_gap": ""
    }
    """

    features = call_llm(prompt, image_base64=base64_image)
    best_reward, expected_participants = find_best_reward(model, features, target_participants)

    # 설명 추가
    explanation_prompt = build_prompt(features, best_reward, expected_participants, target_participants)
    explanation = generate_explanation(explanation_prompt)

    return {
        "features": features,
        "recommended_reward": float(best_reward),
        "expected_participants": float(expected_participants),
        "target_participants": float(target_participants),
        "explanation": explanation,
    }

class EvaluationResult(BaseModel):
    event_id: int
    title: str
    true_reward: float
    recommended_reward: float
    reward_relative_error: float
    actual_attended: float
    expected_attended: float
    attendance_relative_error: float
    success: bool
    
import pandas as pd
from typing import List

class EvaluationSummary(BaseModel):
    total_events: int
    success_count: int
    success_rate: float
    avg_reward_relative_error: float
    avg_attendance_relative_error: float
    details: List[EvaluationResult]


@app.get("/evaluate", response_model=EvaluationSummary)
def evaluate_model(
    reward_tol: float = 0.2,      # 리워드 상대 오차 허용 비율 (20%)
    attend_tol: float = 0.1,      # 인원 상대 오차 허용 비율 (10%)
    limit: int = 100,             # details에 몇 개까지 포함할지
):
    """
    event_logs_eval_100.csv 기반으로
    - true_reward (데이터셋의 reward_amount)
    - target_participants (데이터셋의 attended_participants)
    에 대해 모델이 추천한 리워드가 얼마나 잘 맞는지 평가.
    """
    if model is None:
        return {
            "total_events": 0,
            "success_count": 0,
            "success_rate": 0.0,
            "avg_reward_relative_error": 0.0,
            "avg_attendance_relative_error": 0.0,
            "details": [],
        }

    # 평가용 데이터셋 로드 (경로는 네가 저장한 파일명에 맞춰 변경)
    df = pd.read_csv("event_logs_eval_100.csv")

    results: list[EvaluationResult] = []
    reward_err_sum = 0.0
    attend_err_sum = 0.0
    success_count = 0

    for _, row in df.iterrows():
        # 모델에 넣을 feature 구성
        features = {
            "title": row["title"],
            "event_type": row["event_type"],
            "organizer_type": row["organizer_type"],
            "target_major": row["target_major"],
            "target_grade": row["target_grade"],
            "weekday": row["weekday"],
            "brand_score": float(row["brand_score"]),
            # 홍보 관련은 버렸으니 고정값이나 무시
            "promotion_channels": 0,
            "promotion_intensity": 0.0,
            "date_gap": int(row["date_gap"]),
        }

        true_reward = float(row["reward_amount"])
        target_participants = float(row["attended_participants"])

        # 최적 리워드/예상 인원 계산
        best_reward, expected_participants = find_best_reward(
            model,
            features,
            target_participants,
        )

        reward_rel_err = abs(best_reward - true_reward) / max(true_reward, 1.0)
        attend_rel_err = abs(expected_participants - target_participants) / max(
            target_participants, 1.0
        )

        success = (reward_rel_err <= reward_tol) and (attend_rel_err <= attend_tol)

        if success:
            success_count += 1

        reward_err_sum += reward_rel_err
        attend_err_sum += attend_rel_err

        results.append(
            EvaluationResult(
                event_id=int(row["event_id"]),
                title=row["title"],
                true_reward=true_reward,
                recommended_reward=float(best_reward),
                reward_relative_error=float(reward_rel_err),
                actual_attended=target_participants,
                expected_attended=float(expected_participants),
                attendance_relative_error=float(attend_rel_err),
                success=success,
            )
        )

    total = len(df)
    avg_reward_err = reward_err_sum / max(total, 1)
    avg_attend_err = attend_err_sum / max(total, 1)

    return EvaluationSummary(
        total_events=total,
        success_count=success_count,
        success_rate=success_count / max(total, 1),
        avg_reward_relative_error=avg_reward_err,
        avg_attendance_relative_error=avg_attend_err,
        details=results[:limit],
    )

import pandas as pd
from pydantic import BaseModel
from typing import List

class RewardFitResult(BaseModel):
    event_id: int
    title: str
    true_reward: float
    predicted_reward: float
    relative_error: float
    match: bool

class RewardFitSummary(BaseModel):
    total: int
    match_count: int
    match_rate: float
    avg_relative_error: float
    top5_most_accurate: List[RewardFitResult]
    top5_most_inaccurate: List[RewardFitResult]


@app.get("/evaluate_reward_fit", response_model=RewardFitSummary)
def evaluate_reward_fit(tolerance: float = 0.2):
    """
    데이터셋의 실제 reward_amount를 기준으로
    모델이 얼마나 정확하게 리워드를 재현하는지 평가한다.
    """
    if model is None:
        raise RuntimeError("Model not initialized.")

    df = pd.read_csv("event_logs_eval_100.csv")

    results = []
    total_err = 0.0
    match_count = 0

    for _, row in df.iterrows():
        features = {
            "title": row["title"],
            "event_type": row["event_type"],
            "organizer_type": row["organizer_type"],
            "target_major": row["target_major"],
            "target_grade": row["target_grade"],
            "weekday": row["weekday"],
            "brand_score": float(row["brand_score"]),
            "promotion_channels": 0,
            "promotion_intensity": 0.0,
            "date_gap": int(row["date_gap"]),
        }

        true_reward = float(row["reward_amount"])
        target_participants = float(row["attended_participants"])

        # 모델이 예측하는 리워드 (목표 인원 기반)
        pred_reward, _ = find_best_reward(model, features, target_participants)

        rel_err = abs(pred_reward - true_reward) / max(true_reward, 1.0)
        total_err += rel_err

        match = rel_err <= tolerance
        if match:
            match_count += 1

        results.append(
            RewardFitResult(
                event_id=int(row["event_id"]),
                title=row["title"],
                true_reward=true_reward,
                predicted_reward=pred_reward,
                relative_error=rel_err,
                match=match,
            )
        )

    results.sort(key=lambda x: x.relative_error)
    top5_good = results[:5]
    top5_bad = results[-5:]

    return RewardFitSummary(
        total=len(df),
        match_count=match_count,
        match_rate=match_count / len(df),
        avg_relative_error=total_err / len(df),
        top5_most_accurate=top5_good,
        top5_most_inaccurate=top5_bad,
    )
