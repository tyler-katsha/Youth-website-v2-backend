from fastapi import FastAPI
from nudenet import NudeDetector

app = FastAPI()

# Names of likelihood from google.cloud.vision.enums
LIKELIHOOD = (
    "UNKNOWN",
    "VERY_UNLIKELY",
    "UNLIKELY",
    "POSSIBLE",
    "LIKELY",
    "VERY_LIKELY",
)

@app.get("/greeting")
def greeting():
    return {
        "message": "Hello from python to Spring boot"
    }

@app.post("/predict")
async def predict(request):

    detector = NudeDetector()

    return {
        "detections": detector.detect(request.path),
    }

