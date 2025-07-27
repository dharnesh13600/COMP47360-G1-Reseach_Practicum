# Machine Learning Model Comparison Project

This project contains complete implementations and comparisons of four different machine learning algorithms for multi-output regression tasks.

## 📁 Project Structure

```
notebooks/
├── ML-Model-CatBoost.ipynb         # CatBoost model implementation
├── ML-Model-LightGBM.ipynb         # LightGBM model implementation  
├── ML-Model-XGBoost-tuned.ipynb    # Tuned XGBoost model implementation
├── ML-Model-random-forest.ipynb    # Random Forest model implementation
└── README.md                       # Project documentation
```

## 🎯 Project Objectives

This project aims to compare the performance of four mainstream machine learning algorithms on the same dataset:
- **CatBoost** - Gradient boosting algorithm developed by Yandex
- **LightGBM** - Efficient gradient boosting framework by Microsoft
- **XGBoost** - Extreme gradient boosting with hyperparameter tuning
- **Random Forest** - Random forest ensemble learning algorithm

## 📊 Dataset Description

### Input Features (X)
- `Latitude` - Latitude coordinates
- `Longitude` - Longitude coordinates  
- `Hour` - Hour (extracted from time)
- `Month` - Month (extracted from date)
- `Day` - Day (extracted from date)
- `Cultural_activity_prefered` - Cultural activity preference (categorical feature)

### Target Variables (y) - Multi-output Regression
- `Total crowd` - Total crowd count
- `Taxi zone crowd score` - Taxi zone crowd score
- `Activity Score` - Activity score

## 🔧 Technical Implementation

### Data Preprocessing
- **Time Feature Engineering**: Extract hour, month, day from time and date fields
- **Categorical Feature Encoding**: Use OneHotEncoder for cultural activity preferences
- **Data Split**: 70% training set, 30% test set (random_state=42)

### Model Configurations

#### 1. CatBoost
- **Features**: Native support for categorical features, no preprocessing required
- **Parameter Optimization**: RandomizedSearchCV (20 iterations)
- **Best Parameters**: 
  - iterations: 500
  - learning_rate: 0.1
  - depth: 9
  - bagging_temperature: 2

#### 2. LightGBM  
- **Features**: Efficient gradient boosting framework
- **Parameter Optimization**: RandomizedSearchCV (30 iterations)
- **Best Parameters**:
  - n_estimators: 800
  - learning_rate: 0.1
  - max_depth: 12
  - num_leaves: 100

#### 3. XGBoost (Tuned Version)
- **Features**: Carefully tuned XGBoost implementation
- **Parameter Optimization**: RandomizedSearchCV (30 iterations)
- **Best Parameters**:
  - n_estimators: 300
  - learning_rate: 0.1
  - max_depth: 7
  - subsample: 1.0

#### 4. Random Forest
- **Features**: Classic ensemble learning algorithm
- **Configuration**: 100 decision trees
- **Cross Validation**: 5-fold cross validation

## 📈 Model Performance Comparison

### Test Set Performance

| Model | Average R² | Average RMSE | Features |
|-------|------------|--------------|----------|
| **CatBoost** | 0.9567 | 229.27 | 🏆 Best overall performance, native categorical support |
| **XGBoost** | 0.9584 | 478.92 | 🎯 Carefully tuned, excellent performance |
| **Random Forest** | 0.9705 | 407.97 | 🌟 Highest R² score, stable and reliable |
| **LightGBM** | 0.9325 | 234.77 | ⚡ Fast training, memory efficient |

### Detailed Performance by Target Variable

#### Total crowd Prediction
- **CatBoost**: R² = 0.9388, RMSE = 687.88
- **LightGBM**: R² = 0.9359, RMSE = 704.11  
- **XGBoost**: R² = 0.9391, RMSE = 1436.47
- **Random Forest**: R² = 0.9558, RMSE = 1223.70

#### Taxi zone crowd score Prediction
- **CatBoost**: R² = 0.9320, RMSE = 0.10
- **LightGBM**: R² = 0.8662, RMSE = 0.14
- **XGBoost**: R² = 0.9377, RMSE = 0.20
- **Random Forest**: R² = 0.9559, RMSE = 0.17

#### Activity Score Prediction
- **CatBoost**: R² = 0.9992, RMSE = 0.03
- **LightGBM**: R² = 0.9953, RMSE = 0.07
- **XGBoost**: R² = 0.9983, RMSE = 0.09
- **Random Forest**: R² = 0.9998, RMSE = 0.03

## 🔍 Feature Importance Analysis

### LightGBM Feature Importance Ranking
1. **Day** - 6321.67 (Most important)
2. **Latitude** - 5163.00
3. **Hour** - 5064.33
4. **Longitude** - 4452.33
5. **Month** - 3294.67
6. **Cultural activity preferences** - Relatively low but still contributing

## 💾 Model Persistence

Each trained model is saved as a pickle file:
- `catboost_model.pkl`
- `lightgbm_model.pkl` 
- `xgboost_model.pkl`
- `random_forest_model.pkl`

## 🚀 Usage

### Environment Requirements
```python
pandas
numpy
scikit-learn
catboost
lightgbm
xgboost
joblib
matplotlib
```

### Quick Start
1. Ensure the data file `final_dataset.csv` is in the same directory
2. Run any model's Jupyter notebook in sequence
3. Models will automatically train, evaluate, and save

### Loading Trained Models
```python
import joblib

# Load any model
model = joblib.load('catboost_model.pkl')

# Make predictions
predictions = model.predict(X_new)
```

## 📝 Conclusions and Recommendations

### Recommended Model Selection

1. **Best Overall Performance**: **CatBoost**
   - Excellent performance across all three target variables
   - Native categorical feature support, simplifies preprocessing
   - Stable training with low overfitting risk

2. **Highest Accuracy Requirements**: **Random Forest**  
   - Highest average R² score (0.9705)
   - Good model interpretability
   - Suitable for scenarios requiring extreme accuracy

3. **Large-scale Data**: **LightGBM**
   - Fastest training speed
   - High memory efficiency
   - Suitable for big datasets and production environments

4. **Customization Needs**: **XGBoost**
   - Rich hyperparameter tuning space
   - Extensive community support
   - Suitable for projects requiring deep customization

### Performance Optimization Suggestions
- All models perform excellently on Activity Score prediction (R² > 0.995)
- Total crowd prediction is relatively most challenging, consider feature engineering optimization
- Geographic location and time features are the most important predictors

## 🔬 Technical Highlights

- **Multi-output Regression**: Simultaneously predict three related target variables
- **Hyperparameter Optimization**: Automatic tuning using RandomizedSearchCV
- **Cross Validation**: Ensures model generalization capability
- **Feature Engineering**: Time series feature extraction and categorical encoding
- **Model Persistence**: Convenient for deployment and reuse

---

*This project demonstrates the application and comparison of modern machine learning algorithms in real prediction tasks, providing data-driven references for selecting appropriate algorithms.* 