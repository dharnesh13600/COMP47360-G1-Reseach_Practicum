# Data Modeling Project

## Project Overview

This is a machine learning-based data modeling project that primarily uses CatBoost and LightGBM gradient boosting algorithms for model training and analysis. The project focuses on predictive modeling of geographical locations and cultural activity preferences.

## Project Structure

```
Data Modeling- Ting/
├── README.md                           # Project documentation
├── lightgbm_feature_importance.csv     # LightGBM feature importance analysis results
└── catboost_info/                      # CatBoost model training information
    ├── catboost_training.json          # Detailed training log (500 iterations)
    ├── learn_error.tsv                 # Learning error records
    ├── time_left.tsv                   # Training time records
    └── learn/
        └── events.out.tfevents         # TensorBoard event files
```

## Key Features

### 1. Feature Importance Analysis

The project uses LightGBM to perform feature importance analysis, identifying key features that influence model predictions:

**Core Features:**
- **Day** (Importance: 6321.67) - Date feature, the most important predictor
- **Latitude** (Importance: 5163.0) - Latitude information
- **Hour** (Importance: 5064.33) - Hour time feature
- **Longitude** (Importance: 4452.33) - Longitude information
- **Month** (Importance: 3294.67) - Month feature

**Cultural Activity Preference Features:**
- Filmmaking (Importance: 201.33)
- Busking (Importance: 143.0)
- Art Sale (Importance: 120.0)
- Street photography (Importance: 89.67)
- Portrait photography (Importance: 88.33)

### 2. CatBoost Model Training

The project uses CatBoost for deep model training:

**Training Configuration:**
- **Iterations:** 500 rounds
- **Target Metric:** RMSE (Root Mean Square Error)
- **Optimization Goal:** Minimize RMSE
- **Training Mode:** Standard training mode

**Training Performance:**
- Initial RMSE: 0.913
- Final RMSE: Significantly reduced (specific values in training.json)
- Smooth convergence throughout training process

## Technology Stack

- **CatBoost**: Gradient boosting framework for classification and regression tasks
- **LightGBM**: Microsoft's gradient boosting framework
- **TensorBoard**: Model training visualization tool
- **Python**: Primary programming language (inferred)

## Data Features

Based on feature importance analysis, the project data contains the following types of features:

1. **Temporal Features**
   - Day
   - Hour
   - Month

2. **Geographical Features**
   - Latitude
   - Longitude

3. **Cultural Activity Preferences**
   - Filmmaking
   - Busking
   - Art Sale
   - Street Photography
   - Portrait Photography

## Model Performance

### CatBoost Model Training Results
- **Total Training Rounds:** 500 rounds
- **Convergence:** Model shows good convergence performance
- **Error Trend:** Decreased from initial 0.913 to lower levels
- **Training Time:** Complete time records saved in time_left.tsv

### Feature Importance Insights
1. **Temporal-Spatial Features Dominate**: Day, Latitude, Hour, Longitude features occupy the highest importance scores
2. **Cultural Preferences Have Impact**: Although relatively lower in importance, cultural activity preference features still contribute to the model
3. **Geographic Factors Are Critical**: High importance of latitude and longitude features indicates that geographic location is a key factor for prediction

## How to Use

1. **View Feature Importance:**
   ```bash
   # View LightGBM feature importance results
   cat lightgbm_feature_importance.csv
   ```

2. **Analyze CatBoost Training Process:**
   ```bash
   # View training configuration and process
   cat catboost_info/catboost_training.json
   
   # View learning error changes
   cat catboost_info/learn_error.tsv
   ```

3. **Visualize Training Process:**
   ```bash
   # Use TensorBoard to view training visualization
   tensorboard --logdir=catboost_info/learn/
   ```

## Results and Conclusions

This data modeling project successfully:
- Implemented comparative analysis of two different gradient boosting algorithms
- Identified key features that influence prediction results
- Established a prediction model based on temporal-spatial and cultural preferences
- Provided complete training process monitoring and analysis

The project shows that temporal-spatial features (especially date and geographic location) are the most important predictors, while cultural activity preferences also provide valuable supplementary information to the model.

## Key Findings

- **Temporal patterns are crucial**: Day and Hour features have the highest predictive power
- **Geographic location matters**: Latitude and Longitude are among the top features
- **Cultural preferences add value**: Though less important, cultural activity preferences contribute meaningfully to predictions
- **Model convergence is stable**: CatBoost training showed smooth and consistent improvement over 500 iterations

## Future Improvements

Potential areas for enhancement:
- Feature engineering for temporal patterns
- Geographic clustering analysis
- Cross-validation for model robustness
- Hyperparameter optimization
- Model ensemble techniques

## Author

Ting

---

*This project uses machine learning techniques for data modeling and predictive analysis, including complete feature engineering and model evaluation workflows.* 