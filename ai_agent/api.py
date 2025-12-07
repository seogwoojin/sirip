# api.py
import base64
import json
from typing import Optional, Dict, Any, List

import openai
from fastapi import FastAPI, File, UploadFile
from pydantic import BaseModel

from reward_optimizer import (
    load_event_data,
    RewardMLModel,
    find_best_reward,
    build_prompt,
    generate_explanation,
)

# =====================================
# FastAPI 초기화
# =====================================
app = FastAPI()
model: Optional[RewardMLModel] = None


# =====================================
# Request Models
# =====================================
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


# =====================================
# Startup - Load & Fit Model
# =====================================
@app.on_event("startup")
def startup_event():
    global model
    df = load_event_data("event_logs_realistic_300.csv")
    m = RewardMLModel()
    m.fit(df)
    model = m
    print("[Startup] RewardMLModel loaded and fitted.")


# =====================================
# OpenAI Client
# =====================================
from openai import OpenAI
client = OpenAI()


# =====================================
# Feature Normalization
# (LLM이 리스트를 반환하는 문제 방지)
# =====================================
def normalize_features(features: Dict[str, Any]) -> Dict[str, Any]:
    normalized = {}
    for k, v in features.items():
        if isinstance(v, list):
            # 리스트가 들어오면 OneHotEncoder가 깨지므로 문자열로 합침
            normalized[k] = ", ".join(map(str, v))
        else:
            normalized[k] = v
    return normalized


# =====================================
# LLM JSON Schema 강제
# =====================================
EVENT_SCHEMA = {
    "type": "json_schema",
    "json_schema": {
        "name": "event_extraction",
        "strict": True,
        "schema": {
            "type": "object",
            "properties": {
                "title": {"type": "string"},
                "event_type": {"type": "string"},
                "organizer_type": {"type": "string"},
                "target_major": {"type": "string"},
                "target_grade": {"type": "string"},
                "weekday": {"type": "string"},
                "brand_score": {"type": "number"},
                "date_gap": {"type": "number"}
            },
            "required": [
                "title", "event_type", "organizer_type",
                "target_major", "target_grade",
                "weekday", "brand_score", "date_gap"
            ],
            "additionalProperties": False   # ★ 중요 ★
        }
    }
}


# =====================================
# LLM Common Call
# =====================================
def call_llm(prompt: str, image_base64: Optional[str] = None) -> Dict[str, Any]:
    messages = [
        {
            "role": "system",
            "content": (
                "You extract structured event information. "
                "Return ONLY valid JSON according to the schema."
            )
        }
    ]

    if image_base64:
        messages.append({
            "role": "user",
            "content": [
                {"type": "text", "text": prompt},
                {"type": "input_image", "image_url": f"data:image/jpeg;base64,{image_base64}"}
            ],
        })
    else:
        messages.append({"role": "user", "content": prompt})

    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=messages,
        response_format=EVENT_SCHEMA
    )

    extracted = json.loads(response.choices[0].message.content)
    return normalize_features(extracted)


# =====================================
# /optimize
# =====================================
@app.post("/optimize")
def optimize(req: EventRequest):
    if model is None:
        return {"error": "model not initialized"}

    features = req.dict(exclude={"target_participants"})
    best_reward, expected_participants = find_best_reward(model, features, req.target_participants)

    return {
        "features": features,
        "recommended_reward": float(best_reward),
        "expected_participants": float(expected_participants),
        "target_participants": float(req.target_participants),
    }


# =====================================
# /analyze_text
# =====================================
@app.post("/analyze_text")
def analyze_text(req: TextRequest):
    if model is None:
        return {"error": "model not initialized"}

    prompt = f"""
    다음 공지문에서 아래 항목을 JSON으로 추출해줘. 
    반드시 문자열로 반환하고, 배열 형태는 절대 사용하지 마.
    - title
    - event_type
    - organizer_type
    - target_major
    - target_grade
    - weekday (문자열 1개)
    - brand_score (1~5)
    - date_gap (행사일까지 남은 일수)

    공지문:
    {req.text}
    """

    try:
        features = call_llm(prompt)
    except Exception as e:
        return {"error": f"LLM extraction failed: {str(e)}"}

    print(features)
    try:
        best_reward, expected_participants = find_best_reward(
            model, features, req.target_participants
        )
    except Exception as e:
        return {"error": f"Reward optimization failed: {str(e)}"}

    explanation_prompt = build_prompt(features, best_reward, expected_participants, req.target_participants)
    explanation = generate_explanation(explanation_prompt)

    return {
        "features": features,
        "recommended_reward": float(best_reward),
        "expected_participants": float(expected_participants),
        "target_participants": float(req.target_participants),
        "explanation": explanation,
    }


# =====================================
# /analyze_poster
# =====================================
@app.post("/analyze_poster")
async def analyze_poster(file: UploadFile = File(...), target_participants: float = 100.0):
    if model is None:
        return {"error": "model not initialized"}

    image_bytes = await file.read()
    base64_image = base64.b64encode(image_bytes).decode("utf-8")

    prompt = """
    포스터를 보고 아래 정보를 JSON으로 추출해줘.
    모든 필드는 문자열 또는 숫자 1개로만, 배열은 절대 사용하지 마.
    {
      "title": "",
      "event_type": "",
      "organizer_type": "",
      "target_major": "",
      "target_grade": "",
      "weekday": "",
      "brand_score": 1,
      "date_gap": 0
    }
    """

    try:
        features = call_llm(prompt, image_base64=base64_image)
    except Exception as e:
        return {"error": f"LLM extraction failed: {str(e)}"}

    best_reward, expected_participants = find_best_reward(model, features, target_participants)

    explanation_prompt = build_prompt(features, best_reward, expected_participants, target_participants)
    explanation = generate_explanation(explanation_prompt)

    return {
        "features": features,
        "recommended_reward": float(best_reward),
        "expected_participants": float(expected_participants),
        "target_participants": float(target_participants),
        "explanation": explanation,
    }
