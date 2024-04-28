from sklearn.linear_model import LinearRegression, Lasso, Ridge
from sklearn.model_selection import train_test_split, cross_validate, cross_val_score, KFold, RepeatedKFold, StratifiedKFold, GridSearchCV, learning_curve, ShuffleSplit
from sklearn.metrics import mean_squared_error, r2_score, mean_absolute_error, make_scorer
import json
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.pipeline import Pipeline, make_pipeline
from sklearn.preprocessing import MinMaxScaler, StandardScaler, PolynomialFeatures
from sklearn.feature_selection import VarianceThreshold
from sklearn.dummy import DummyRegressor
import sys
from utils import read_json_file
import statsmodels.api as sm
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor
from sklearn.tree import DecisionTreeRegressor
from sklearn.neighbors import KNeighborsRegressor
from sklearn.svm import SVR
from sklearn.inspection import permutation_importance

# Tries to find the relation between features and the dependent variable (stability)
def find_relation(file_paths):
    dfs = []
    for i in range(len(file_paths)):
        json_dict = read_and_format_json(file_paths[i])
        # data = normalize(json_dict, scaler) # normalize
        data = pd.DataFrame.from_dict(json_dict, orient='index')
        dfs.append(data)
    df = pd.concat(dfs, ignore_index=True) # combine all dfs to one single df

    y = df['stabilityMetricValue']
    X = df[['linesOfCodeJunitTest']]
    X = sm.add_constant(X)
    model = sm.OLS(y, X)
    results = model.fit()
    print(results.summary())
    # df = df[['stabilityMetricValue', 'linesOfCodeJunitTest']]
    # correlation_matrix = df.corr()
    # print(correlation_matrix)

def random_forest(df, scaler, normalizeIndividual):
    y = df['stabilityMetricValue'] # Target
    X = df.drop('stabilityMetricValue', axis=1) # select all features except stabilityMetricValue
    # X = df[['linesOfCodeJunitTest', 'IO']]  # Features to use
    # X = df.drop(['stabilityMetricValue', 'numNestedLoops', 'nrOwnPackages', 'numLoops'], axis=1)  # Excluding these features
    # X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3)
    model = RandomForestRegressor()
    print_cross_validation_stats(X, y, model)
    # model.fit(X_train, y_train)
    # y_pred = model.predict(X_test)
    # print_model_stats(y_test, y_pred, "Forest")

    # importances = model.feature_importances_
    # feature_importances = pd.DataFrame({
    #     'Feature': X.columns,
    #     'Importance': importances
    # }).sort_values(by='Importance', ascending=False)
    # print(feature_importances)

# # TODO: check if this method works correctly
# # Helper (util) getter method for removing the stability metric and then normalizing, then adding back the stability
# def normalize_without_stability(data, scaler):
#     data_features = data.drop('stabilityMetricValue', axis=1)
#     data_stability = data['stabilityMetricValue']
#     data_normalized = scaler.fit_transform(data_features)
#     data_normalized = pd.DataFrame(data_normalized, columns=data_features.columns, index=data_features.index)
#     data_normalized['stabilityMetricValue'] = data_stability
#     data_normalized = pd.DataFrame(data_normalized, columns=data.columns, index=data.index)
#     return data_normalized

# # Normalizes the data from an inputted json dictionary, using a normalization option (default Z-score)
# def normalize(dict, scaler):
#     df = pd.DataFrame.from_dict(dict, orient='index')
#     X = df.drop(columns=['stabilityMetricValue'])
#     y = df['stabilityMetricValue']
#     X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
#     X_train_normalized = scaler.fit_transform(X_train) if not scaler else scaler.transform(X_train)
#     X_test_normalized = scaler.transform(X_test)
#     return X_train_normalized, X_test_normalized, y_train, y_test

# Formats an inputted json file path to remove columns/features not being used, returns a formatted dictionary
def read_and_format_json(path):
    input_dict = read_json_file(path)
    # unstable_dict = {} # dictionary containing only unstable tests (above certain threshold)
    for key, value in input_dict.items():
        # Remove these columns
        value.pop('filePath')
        value.pop('methodName')
        # if value['stabilityMetricValue'] > 5:
            # unstable_dict[key] = value
    # return unstable_dict
    return input_dict

# Creates a test and train split for the inputted json dictionary
def split_data(dict):
    df = pd.DataFrame.from_dict(dict, orient='index')
    X = df.drop(columns=['stabilityMetricValue'])
    y = df['stabilityMetricValue']
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)
    return X_train, X_test, y_train, y_test

# Normalizes inputted X_train and X_test data
def normalize_data(X_train, X_test, scaler=None):
    if scaler is None:
        scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)
    return pd.DataFrame(X_train_scaled), pd.DataFrame(X_test_scaled)

# Gets train and test splits from the inputted json file paths, normalizes the datasets individually if normalize parameter is set
def get_train_test_split(file_paths, normalize_individual=True):
    train_dfs, test_dfs, y_trains, y_tests = [], [], [], []
    # Scaler to use, can also use MinMaxScaler()
    scaler = StandardScaler() if not normalize_individual else None
    # Aggregate and split data first
    for path in file_paths:
        json_dict = read_and_format_json(path)
        X_train, X_test, y_train, y_test = split_data(json_dict)
        if normalize_individual:
            X_train, X_test = normalize_data(X_train, X_test)
        train_dfs.append(X_train)
        test_dfs.append(X_test)
        y_trains.append(y_train)
        y_tests.append(y_test)
    # Concatenate all training and testing data
    df_train = pd.concat(train_dfs, ignore_index=True)
    df_test = pd.concat(test_dfs, ignore_index=True)
    y_train = pd.concat(y_trains, ignore_index=True)
    y_test = pd.concat(y_tests, ignore_index=True)
    # Normalize all datasets unless done individually
    if not normalize_individual:
        df_train, df_test = normalize_data(df_train, df_test, scaler)

    return df_train, df_test, y_train, y_test

def learning_curve_plot(X_train, y_train, kfold, model, modelName):
    scoring = 'neg_root_mean_squared_error'
    # Learning curve
    train_sizes, train_scores, validation_scores = learning_curve(
        estimator=model,
        X=X_train,
        y=y_train,
        train_sizes=np.linspace(0.1, 1.0, 6),  # Generates evenly spaced points from 10% to 100% of the training set size
        cv=kfold,
        scoring=scoring,
        n_jobs=-1  # Use all available cores
    )
    
    # Calculate mean and standard deviation for training and validation scores
    train_mean = np.mean(-train_scores, axis=1)
    train_std = np.std(-train_scores, axis=1)
    validation_mean = np.mean(-validation_scores, axis=1)
    validation_std = np.std(-validation_scores, axis=1)
    # Plot learning curve
    plt.figure(figsize=(8, 6))
    plt.plot(train_sizes, train_mean, 'o-', color="red", label='Training score')
    plt.fill_between(train_sizes, train_mean - train_std, train_mean + train_std, color="red", alpha=0.15)
    plt.plot(train_sizes, validation_mean, 'o-', color="green", label='Validation score')
    plt.fill_between(train_sizes, validation_mean - validation_std, validation_mean + validation_std, color="green", alpha=0.15)
    plt.title(f'Learning Curve for {modelName}')
    plt.xlabel('Training Set Size')
    plt.ylabel('Score (RMSE)')
    plt.legend(loc='best')
    plt.grid(True)
    plt.show()

# Prints model cross validation stats about its predictions (mean squared error etc.)
def print_cross_validation_stats(X_train, X_test, y_train, y_test, model, modelName="?"):
    # kf = KFold(n_splits=5, shuffle=True)
    kf = RepeatedKFold(n_splits=5, n_repeats=10) # uses repeated kfold
    # learning_curve_plot(X_train, y_train, kf, model, modelName) # show learning curve plots
    scores = cross_validate(model, X_train, y_train, cv=kf, scoring={
        'rmse': 'neg_root_mean_squared_error',
        'mae': 'neg_mean_absolute_error',
        'r2': 'r2'
    })

    # Average cross-validation scores on the training set
    avg_rmse_train = -np.mean(scores['test_rmse'])
    avg_mae_train = -np.mean(scores['test_mae'])
    avg_r2_train = np.mean(scores['test_r2'])
    print(f"{modelName} model (Training Set) RMSE: {avg_rmse_train:.3f}, MAE: {avg_mae_train:.3f}, R-squared: {avg_r2_train:.3f}")
    # Evaluate the model on the test set
    model.fit(X_train, y_train)
    y_pred_test = model.predict(X_test)
    rmse_test = mean_squared_error(y_test, y_pred_test, squared=False)
    mae_test = mean_absolute_error(y_test, y_pred_test)
    r2_test = r2_score(y_test, y_pred_test)
    # Print the evaluation metrics
    print(f"{modelName} model (Test Set) RMSE: {rmse_test:.3f}, MAE: {mae_test:.3f}, R-squared: {r2_test:.3f}")

# TEMP METHOD! Using it to get feature importances aswell
def print_cross_validation_stats2(X, y, model, modelName="?"):
    kf = RepeatedKFold(n_splits=5, n_repeats=10, random_state=42)  # Uses repeated k-fold
    feature_importances = np.zeros(X.shape[1])

    for train_index, test_index in kf.split(X):
        X_train, X_test = X.iloc[train_index], X.iloc[test_index]
        y_train, y_test = y.iloc[train_index], y.iloc[test_index]
        model.fit(X_train, y_train)
        result = permutation_importance(model, X_train, y_train, n_repeats=5, random_state=42, n_jobs=-1)
        feature_importances += result.importances_mean / kf.get_n_splits()  # Average over the number of splits

    scores = cross_validate(model, X, y, cv=kf, scoring={
        'rmse': 'neg_root_mean_squared_error',
        'mae': 'neg_mean_absolute_error',
        'r2': 'r2'
    }, n_jobs=-1)
    avg_rmse = -np.mean(scores['test_rmse'])
    avg_mae = -np.mean(scores['test_mae'])
    avg_r2 = np.mean(scores['test_r2'])
    print(f"{modelName} model RMSE: {avg_rmse:.3f}, MAE: {avg_mae:.3f}, R-squared: {avg_r2:.3f}")
    # Plotting feature importances
    plt.figure(figsize=(12, 10))
    feature_names = X.columns  # Get feature names from the DataFrame
    indices = np.argsort(feature_importances)[::-1]  # Sort feature importances
    plt.title(f"Feature Importance for model {modelName}")
    plt.bar(range(len(feature_importances)), feature_importances[indices], align="center", color='lightblue')
    plt.xticks(range(len(feature_importances)), [feature_names[i] for i in indices], rotation=90)  # Rotate feature names for visibility
    plt.xlabel('Feature Name')
    plt.ylabel('Importance')
    plt.tight_layout()
    plt.show()

# Creates and trains models from the chosen inputted json file path files, the normalization scaler,
# and if the inputted datasets should be normalized individually or all together
def create_prediction_models(file_paths, normalizeIndividual=True):
    X_train, X_test, y_train, y_test = get_train_test_split(file_paths, normalizeIndividual) # get dataframe containing the data
    models = [
        ('Linear', LinearRegression()),
        ('Random Forest', RandomForestRegressor()),
        ('Decision Tree', DecisionTreeRegressor()),
        ('Ridge Regression', Ridge()),
        ('Support Vector Regression', SVR()),
        ('Gradient Boosting Regressor', GradientBoostingRegressor()),
        ('Median', DummyRegressor(strategy="median")),
        ('Mean', DummyRegressor(strategy="mean"))
    ]

    # models = [
    #     ('Random Forest', RandomForestRegressor()),
    # ]
    for name, model in models:
        print_cross_validation_stats(X_train, X_test, y_train, y_test, model, name)

# Predicts the value for the model given some input values (dictionary)
def predict_from_input(input, model, scaler):
    trained_features = ['numConditionals', 'numLoops', 'numNestedLoops', 'numMethodCalls', 'numRecursiveMethodCalls', 'linesOfCode', 'linesOfCodeJunitTest', 'logicalLinesOfCode', 'logicalLinesOfCodeJunitTest', 'IO', 'java.lang', 'java.util', 'org.junit', 'concurrency', 'nrOwnPackages', 'nrTotalPackages', 'NrObjInstantiations']
    input_filtered = {key: input_data[key] for key in trained_features if key in input}
    print(input_filtered)
    input_df = pd.DataFrame([input_filtered], columns=trained_features)
    input_df = input_df.astype(float)
    new_input_scaled = scaler.transform(input_df)
    new_input_scaled = pd.DataFrame(new_input_scaled, columns=trained_features)
    # TODO find out why warning when predicting using normalization for combined datasets
    predicted_value = model.predict(new_input_scaled)
    print("Predicted stability metric value:", predicted_value)

# Create a histogram plot for all stability values in the datasets
def plot(file_paths):
    stability_values = []
    for path in file_paths:
        json_dict = read_and_format_json(path)
        for key, value in json_dict.items():
            stability_metric_value = value['stabilityMetricValue']
            stability_values.append(stability_metric_value)

    plt.hist(stability_values, bins=50, color='skyblue', edgecolor='black')
    plt.xlabel('Stability metric value (RMAD)')
    plt.ylabel('Frequency')
    plt.title('Histogram of stability values')
    plt.show()

# TODO: learning curve for random forest

root_path = r"benchmarks\results\rmads_final\parsed_final"
input_paths = [root_path + r'\Sonarqube_Fixed_AllMethodsExtracted.json', root_path + r'\Mockito_Fixed_AllMethodsExtracted.json', root_path + r'\RxJava_Fixed_AllMethodsExtracted.json']
# input_paths = [input_paths[2]]
# find_relation(input_paths)
# plot(input_paths)
create_prediction_models(input_paths, normalizeIndividual=False)
input_data = {"filePath": "io\\reactivex\\rxjava3\\internal\\operators\\observable\\ObservableConcatMapEagerTest.java", "methodName": "arrayDelayErrorMaxConcurrencyErrorDelayed", "stabilityMetricValue": 4.481014765096032, "numConditionals": 234, "numLoops": 9, "numNestedLoops": 0, "numMethodCalls": 1018, "numRecursiveMethodCalls": 180, "linesOfCode": 948, "linesOfCodeJunitTest": 18, "logicalLinesOfCode": 1201, "logicalLinesOfCodeJunitTest": 18, "IO": 0, "java.lang": 795, "java.util": 159, "org.junit": 4, "concurrency": 44, "nrOwnPackages": 265, "nrTotalPackages": 1267, "NrObjInstantiations": 108}
