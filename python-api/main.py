import tempfile
import traceback
import os
import requests

from fastapi import FastAPI,HTTPException
from nudenet import NudeDetector
from pydantic import BaseModel

class PredictionRequest(BaseModel):
    path:str

app = FastAPI()
detector = NudeDetector()

EXPLICIT_CLASSES = {
    "FEMALE_BREAST_EXPOSED",
    "FEMALE_GENITALIA_EXPOSED",
    "MALE_GENITALIA_EXPOSED",
    "ANUS_EXPOSED",
    "BUTTOCKS_EXPOSED",
}
@app.post("/predict")
async def predict(request: PredictionRequest):
    temp_path = None
    try:

        response = requests.get(request.path)

        response.raise_for_status()

        suffix = os.path.splitext(request.path)[1] or ".jpg"

        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            tmp.write(response.content)
            temp_path = tmp.name

        detections = detector.detect(temp_path)

        approved = all(d['class'] in EXPLICIT_CLASSES for d in detections)
        os.remove(temp_path)

        return {
            "approved": approved,
            "detections": detections
        }


    except Exception:
        traceback.print_exc()
        raise HTTPException(status_code=500,detail=traceback.format_exc())

app.get("/healthz")
async def health_check():
    return {"status": "UP"}
