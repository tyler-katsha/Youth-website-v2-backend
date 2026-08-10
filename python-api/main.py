import tempfile
import traceback
import os
import requests

from fastapi import FastAPI,HTTPException
from nudenet import NudeDetector
from pydantic import BaseModel

print("RUNNING FILE:", __file__)
print("CURRENT DIRECTORY:", os.getcwd())

class PredictionRequest(BaseModel):
    path:str

app = FastAPI()
detector = NudeDetector()

print("App:",app)
print("Detector:",detector)


@app.get("/greeting")
def greeting():
    return {
        "message": "Hello from python to Spring boot"
    }


EXPLICIT_CLASSES = {
    "FEMALE_BREAST_EXPOSED",
    "FEMALE_GENITALIA_EXPOSED",
    "MALE_GENITALIA_EXPOSED",
    "ANUS_EXPOSED",
    "BUTTOCKS_EXPOSED",
}
@app.post("/predict")
async def predict(request: PredictionRequest):

    try:

        response = requests.get(request.path)


        # response.raise_for_status()

        suffix = os.path.splitext(request.path)[1] or ".jpg"

        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            tmp.write(response.content)
            temp_path = tmp.name

        detections = detector.detect(temp_path)

        print(detections)

        os.remove(temp_path)

        return {
            "approved": True,
            "detections": detections
        }


    except Exception as e:
        print(e)
        traceback.print_exc()
        raise HTTPException(status_code=500,detail=traceback.format_exc())

@app.get("/test")
def test():
    return {
        "test":"New endpoint test"
    }

print("\n=== REGISTERED ROUTES ===")
for route in app.routes:
    print(route.path, route.methods)