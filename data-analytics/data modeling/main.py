# main.py
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import pandas as pd
from typing import List # Import List

app = FastAPI(title="Crowd & Muse-Score ML API")

# ---------------------------------------------------------------------------
# 1. Load the trained pipeline
# ---------------------------------------------------------------------------
try:
    MODEL_PATH = "xgboost_model.pkl"
    model = joblib.load(MODEL_PATH)
except Exception as exc:
    raise RuntimeError(f"Could not load model at {MODEL_PATH}: {exc}")

# ---------------------------------------------------------------------------
# 2. Pydantic schemas
# ---------------------------------------------------------------------------
class PredictionRequest(BaseModel):
    latitude: float
    longitude: float
    hour: int
    month: int
    day: int
    cultural_activity_prefered: str

class PredictionResponse(BaseModel):
    muse_score: float | None = None
    estimated_crowd_number: int
    crowd_score: float
    creative_activity_score: float

# ---------------------------------------------------------------------------
# 3. Inference endpoint (Modified for batch prediction)
# ---------------------------------------------------------------------------
@app.post("/predict_batch", response_model=List[PredictionResponse]) # New endpoint and response_model
def predict_batch(reqs: List[PredictionRequest]): # Accepts a list of requests
    """
    Run the XGBoost pipeline and return model predictions + null muse_score for a batch of requests.
    """
    try:
        # Convert list of Pydantic models to a list of dictionaries
        data_for_df = [r.dict() for r in reqs]
        
        # Create DataFrame from the list of dictionaries
        df = pd.DataFrame(data_for_df)

        # Ensure column order matches training data if necessary, though pandas usually handles this
        df = df[["latitude", "longitude", "hour", "month", "day", "cultural_activity_prefered"]]
        
        # Rename columns to match model's expected input if they are different
        df.columns = ["Latitude", "Longitude", "Hour", "Month", "Day", "Cultural_activity_prefered"]

        preds = model.predict(df) # Predict for the entire batch

        # Convert predictions back to a list of PredictionResponse objects
        response_list = []
        for pred in preds:
            response_list.append(PredictionResponse(
                muse_score=None, # muse_score is calculated in Java
                estimated_crowd_number=int(round(pred[0])),
                crowd_score=float(pred[1]),
                creative_activity_score=float(pred[2])
            ))
        return response_list

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))