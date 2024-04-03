import matplotlib.pyplot as plt
import scipy.stats as stats
import json
import numpy as np
import pandas as pd
import seaborn as sns

# ADD IN NEW RMAD VALUE TO USE:
# with open(root_path + r'\rxjava_MAD.json', 'r') as json_file:
    # rmads_new = json.load(json_file)
# for key in rxjava_parsed:
#     for key2 in rmads_new:
#         substring = '_Benchmark.benchmark_'
#         index = key2[0].find(substring)
#         benchmark_name = key2[0][:index] + key2[0][index + len(substring):]
#         if benchmark_name == key:
#             rxjava_parsed[key]['RMAD_new'] = key2[1]

categories = {
    'IO': ['java.io', 'java.nio'],  # Example I/O-related packages
    'java.lang': ['java.lang'],
    'java.util': ['java.util'],
    'org.junit': ['org.junit'],
    'concurrency': ['java.util.concurrent']  # Example Concurrency-related packages
}

# Plots the correlation between column 'RMAD' with other columns of the inputted json dictionary
# takes as input: path to a json file containing data for source code features and rmad values of each benchmark,
# and categories for how to structure package counts (etc. java.util)
def plot_correlation(file_path, categories):
    parsed_dict = read_json_file(file_path)
    # Calculate number of times each package category is used for each benchmark
    for key, value in parsed_dict.items():
        category_counts = {category: 0 for category in categories}
        package_accesses = value.get('packageAccesses', {})
        sorted_categories = sorted(categories.items(), key=lambda x: len(x[1][0]), reverse=True)
        for package, count in package_accesses.items():
            for category, patterns in sorted_categories:
                if any(package.startswith(pat) for pat in patterns):
                    category_counts[category] += count
                    break
        parsed_dict[key].update(category_counts)

    sorted_items = sorted(parsed_dict.items(), key=lambda x: x[1]['RMAD'], reverse=True)
    # sorted_items = sorted_items[:100] # select how many and which benchmarks should be used (based on RMAD order)
    parsed_dict = dict(sorted_items)
    data = pd.DataFrame.from_dict(parsed_dict, orient='index')
    columns_to_drop = ['filePath', 'methodName', 'methodCalls', 'objectInstantiations', 'packageAccesses', 'logicalLinesOfCodeJunitTest', 'logicalLinesOfCode']
    data = data.drop(columns_to_drop, axis=1)

    # RENAME COLUMNS
    data = data.rename(columns={'numConditionals': 'Conditionals', 'numLoops' : 'Loops', 'numNestedLoops' : 'NestedLoops', 'numMethodCalls' : 'MethodCalls', 'recursiveMethodCalls' : 'MethodCalls No Java', 'linesOfCode' : 'LOC', 'linesOfCodeJunitTest' : 'LOC JUnit test', 'IO' : 'java.io', 'concurrency' : 'java.util.concurrent'})
    # SWITCH ORDER OF COLUMNS (will be order of the columns shown in plot), STUPID WAY TO DO IT PLEASE FIX:
    cols = list(data.columns)
    MethodCallsNoJava_pos, LOCJUnit_pos, LOC_pos, MethodCalls_pos = cols.index('MethodCalls No Java'), cols.index('LOC JUnit test'), cols.index('LOC'), cols.index('MethodCalls')
    cols[LOC_pos], cols[MethodCalls_pos] = cols[MethodCalls_pos], cols[LOC_pos]
    MethodCallsNoJava_pos, LOCJUnit_pos, LOC_pos, MethodCalls_pos = cols.index('MethodCalls No Java'), cols.index('LOC JUnit test'), cols.index('LOC'), cols.index('MethodCalls')
    cols[LOCJUnit_pos], cols[MethodCalls_pos] = cols[MethodCalls_pos], cols[LOCJUnit_pos]
    MethodCallsNoJava_pos, LOCJUnit_pos, LOC_pos, MethodCalls_pos = cols.index('MethodCalls No Java'), cols.index('LOC JUnit test'), cols.index('LOC'), cols.index('MethodCalls')
    cols[MethodCallsNoJava_pos], cols[MethodCalls_pos] = cols[MethodCalls_pos], cols[MethodCallsNoJava_pos]
    data = data[cols]

    corr_to_rmad = data.corr(method='pearson')['RMAD'].drop('RMAD')  # Drop the correlation of RMAD with itself
    corr_to_rmad_df = corr_to_rmad.to_frame().reset_index().rename(columns={'index': 'Variable', 'RMAD': 'Correlation'})
    plt.figure(figsize=(10, 6))
    sns.barplot(x='Correlation', y='Variable', data=corr_to_rmad_df, palette='coolwarm')
    plt.title('RxJava feature correlation with RMAD')
    plt.xlabel("Pearson's correlation coefficient")
    plt.ylabel('Variables')
    plt.subplots_adjust(left=0.2, right=0.75)
    plt.show()

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

# Reads in a json file from path and returns it
def read_json_file(file_path):
    with open(file_path, 'r') as json_file:
            return json.load(json_file)