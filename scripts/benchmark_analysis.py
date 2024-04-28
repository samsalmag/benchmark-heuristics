import matplotlib.pyplot as plt
import scipy.stats as stats
import json
import numpy as np
import pandas as pd
import seaborn as sns
import sys
import random
import pandas as pd
from scipy.stats import zscore

# Reads in a json file from path and returns it
def read_json_file(file_path):
    with open(file_path, 'r') as json_file:
            return json.load(json_file)

categories = {
    'IO': ['java.io', 'java.nio'],  # Example I/O-related packages
    # 'Thread': ['java.lang.Thread'], # find thread, TODO: verify to make sure it works
    'java.lang': ['java.lang'],
    'java.util': ['java.util'],
    'org.junit': ['org.junit'],
    'concurrency': ['java.util.concurrent']  # Example Concurrency-related packages
}

# 'Fixes' a parsed benchmark json file, to rearrange structure and remove cols not used (e.g. individual method calls)
def create_fixed_parsed_json(file_path, output_path, categories):
    parsed_dict = read_json_file(file_path)
    stabilityMetricValue = 'RMAD_coefficient'

    # Calculate number of times each package category is used for each benchmark
    downsample_dict = {}
    thresholdValue = 5
    badStability = {}
    goodStability = {}

    for key, value in parsed_dict.items():
        # Remove stats dictionary (move up one layer)
        stats = parsed_dict[key].pop('stats', {})
        # Move contents of the stats dictionary to one layer above
        parsed_dict[key].update(stats)

        category_counts = {category: 0 for category in categories}
        package_accesses = value.get('packageAccesses', {})
        sorted_categories = sorted(categories.items(), key=lambda x: len(x[1][0]), reverse=True)
        nrOwnPackages = 0
        nrTotalPackages = 0
        nrObjInstantiations = 0
        obj_accesses = value.get('objectInstantiations', {})
        for objInstantiations, count in obj_accesses.items():
            nrObjInstantiations += count

        for package, count in package_accesses.items():
            nrTotalPackages += count
            if package.startswith('io.reactivex'):
                nrOwnPackages += count

            for category, patterns in sorted_categories:
                if any(package.startswith(pat) for pat in patterns):
                    category_counts[category] += count
                    break
        
        # Change structure of dictionary, remove and add some keys
        # parsed_dict[key].update({'nrObjInstantiations' : nrObjInstantiations})
        parsed_dict[key].update(category_counts)
        parsed_dict[key].update({'nrOwnPackages' : nrOwnPackages, 'nrTotalPackages' : nrTotalPackages, 'NrObjInstantiations' : nrObjInstantiations})

        parsed_dict[key].pop("methodCalls", None)
        parsed_dict[key].pop("objectInstantiations", None)
        parsed_dict[key].pop("packageAccesses", None)
    with open(output_path, 'w') as json_file:
        json.dump(parsed_dict, json_file)

# Takes parsed json file as input and a thresholdValue to use (% percentage of threshold for what is considered good stability)
# returns a downsampled dict of the input json file, containing an equal nr of good and bad stability benchmarks
def downsample(file_path, thresholdValue=5):
    parsed_dict = read_json_file(file_path)
    file_name = file_path.split('\\')[-1]
    file_name = file_name.split('_')[0]
    downsample_dict = {}
    badStability = {}
    goodStability = {}

    for key, value in parsed_dict.items():
        if parsed_dict[key]['stabilityMetricValue'] >= thresholdValue:
            badStability[key] = parsed_dict[key]
        else:
            goodStability[key] = parsed_dict[key]
    print(f"{file_name} nr good stability: {len(goodStability)}, nr bad stability: {len(badStability)}")
    if len(goodStability) > len(badStability):
        random_goodStability = dict(random.sample(list(goodStability.items()), len(badStability)))
        downsample_dict = badStability
        downsample_dict.update(random_goodStability)
    else:
        random_badStability = dict(random.sample(list(badStability.items()), len(goodStability)))
        downsample_dict = goodStability
        downsample_dict.update(random_badStability)
    return downsample_dict

# Plots the correlation between column 'RMAD' with other columns of the inputted json dictionary
# takes as input: path to a json file containing data for source code features and rmad values of each benchmark,
# and categories for how to structure package counts (etc. java.util)
def plot_correlation(file_paths, normalize=True):
    datas = []
    final_dict = {}
    index = 0
    for path in file_paths:
        parsed_dict = read_json_file(path)
        # parsed_dict = downsample(path)
        final_dict.update(parsed_dict) # combine each parsed dict to final dict

    print(f"Final dict length: {len(final_dict)}")
    data = pd.DataFrame.from_dict(final_dict, orient='index')
    # columns_to_drop = ['filePath', 'methodName', 'methodCalls', 'objectInstantiations', 'packageAccesses', 'logicalLinesOfCodeJunitTest', 'logicalLinesOfCode']
    columns_to_drop = ['filePath', 'methodName']
    data = data.drop(columns_to_drop, axis=1)

    # RENAME COLUMNS
    data = data.rename(columns={'numConditionals': 'Conditionals', 'numLoops' : 'Loops', 'numNestedLoops' : 'NestedLoops', 'numMethodCalls' : 'MethodCalls', 'numRecursiveMethodCalls' : 'MethodCalls No Java', 'linesOfCode' : 'LOC', 'linesOfCodeJunitTest' : 'LOC JUnit test', 'IO' : 'java.io', 'concurrency' : 'java.util.concurrent'})
    
    # SWITCH ORDER OF COLUMNS (will be order of the columns shown in plot), STUPID WAY TO DO IT PLEASE FIX:
    cols = list(data.columns)
    MethodCallsNoJava_pos, LOCJUnit_pos, LOC_pos, MethodCalls_pos = cols.index('MethodCalls No Java'), cols.index('LOC JUnit test'), cols.index('LOC'), cols.index('MethodCalls')
    cols[LOC_pos], cols[MethodCalls_pos] = cols[MethodCalls_pos], cols[LOC_pos]
    MethodCallsNoJava_pos, LOCJUnit_pos, LOC_pos, MethodCalls_pos = cols.index('MethodCalls No Java'), cols.index('LOC JUnit test'), cols.index('LOC'), cols.index('MethodCalls')
    cols[LOCJUnit_pos], cols[MethodCalls_pos] = cols[MethodCalls_pos], cols[LOCJUnit_pos]
    MethodCallsNoJava_pos, LOCJUnit_pos, LOC_pos, MethodCalls_pos = cols.index('MethodCalls No Java'), cols.index('LOC JUnit test'), cols.index('LOC'), cols.index('MethodCalls')
    cols[MethodCallsNoJava_pos], cols[MethodCalls_pos] = cols[MethodCalls_pos], cols[MethodCallsNoJava_pos]
    data = data[cols]

    # Compute correlation
    stabilityMetricValue = 'stabilityMetricValue'
    corr_to_rmad = data.corr(method='spearman')[stabilityMetricValue].drop(stabilityMetricValue)  # Drop the correlation of RMAD with itself
    corr_to_rmad_df = corr_to_rmad.to_frame().reset_index().rename(columns={'index': 'Variable', stabilityMetricValue: 'Correlation'})
    plt.figure(figsize=(10, 6))
    sns.barplot(x='Correlation', y='Variable', data=corr_to_rmad_df, palette='coolwarm')
    plt.title(f'RMAD feature correlation')
    plt.xlabel("Pearson's correlation coefficient")
    plt.ylabel('Variables')
    plt.subplots_adjust(left=0.2, right=0.75)
    plt.show()

root_path = r"benchmarks\results\rmads_final\parsed_final"
input_path = root_path + r'\Sonarqube_Fixed_AllMethodsExtracted.json'
input_paths = [root_path + r'\Sonarqube_Fixed_AllMethodsExtracted.json', root_path + r'\Mockito_Fixed_AllMethodsExtracted.json', root_path + r'\RxJava_Fixed_AllMethodsExtracted.json']
# output_path = root_path + r'\Sonarqube_Fixed_AllMethodsExtracted.json'

plot_correlation(input_paths)
# create_fixed_parsed_json(input_path, output_path, categories)

# Plots the distribution of all iterations (is forks * iterations nr of values for each benchmark)
# takes as input path to a json file containing benchmark forks and iterations
def plot_iteration_distribution(file_path):
    benchmark_dict = read_json_file(file_path)
    # Aggregating all iteration times into a single list
    all_iteration_times = [float(time) for benchmark in benchmark_dict.values() for fork in benchmark.values() for time in fork]
    adjusted_times = [time for time in all_iteration_times]
    logbins = np.logspace(np.log10(min(adjusted_times)), np.log10(max(adjusted_times)), num=20)
    xmin = 0
    xmax = 30000
    plt.xscale('log')
    plt.xlim(xmin, xmax)
    plt.hist(all_iteration_times, bins=logbins, alpha=0.7, edgecolor='black')
    plt.hist(adjusted_times, bins=logbins, alpha=0.7, edgecolor='black')
    plt.title('Frequency of iteration times')
    plt.xlabel('Log-scaled iteration time (ns)')
    plt.ylabel('Frequency (nr of iterations)')
    tick_interval = (xmax - xmin) / 10
    ticks = np.arange(xmin, xmax + tick_interval, tick_interval)
    plt.xticks(ticks, rotation=25)
    plt.show()

# Plots distribution of all RMAD values
# takes as input path to a json file containing benchmark rmad values
def plot_RMADs_distribution(file_path):
    RMADs = read_json_file(file_path)
    rmad_values = [pair[1] for pair in RMADs]
    sorted_data = sorted(rmad_values)
    plt.figure(figsize=(8, 6))
    plt.hist(sorted_data, bins=20, color='skyblue', edgecolor='black')  # Adjust the number of bins as needed
    plt.title('Distribution of RMAD Values')
    plt.xlabel('RMAD')
    plt.ylabel('Frequency')
    plt.grid(True)
    plt.show()
