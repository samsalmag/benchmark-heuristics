import matplotlib.pyplot as plt
import scipy.stats as stats
import json
import numpy as np
import pandas as pd
import seaborn as sns

RMAD_TO_USE = 'RMAD_new'
RMADS_NOT_USED = ['RMAD']

rxjava_dict = {}
root_path = r"benchmarks\results"
with open(root_path + r'\rxjava_dict.json', 'r') as json_file:
    rxjava_dict = json.load(json_file)

with open(root_path + r'\rxjava_parsedBenchmarks.json', 'r') as json_file:
    rxjava_parsed = json.load(json_file)

with open(root_path + r'\rxjava_new_RMAD.json', 'r') as json_file:
    rmads_new = json.load(json_file)

for key in rxjava_parsed:
    for key2 in rmads_new:
        substring = '_Benchmark.benchmark_'
        index = key2[0].find(substring)
        benchmark_name = key2[0][:index] + key2[0][index + len(substring):]
        if benchmark_name == key:
            rxjava_parsed[key]['RMAD_new'] = key2[1]

categories = {
    'IO': ['java.io', 'java.nio'],  # Example I/O-related packages
    'java.lang': ['java.lang'],
    'java.util': ['java.util'],
    'org.junit': ['org.junit'],
    'concurrency': ['java.util.concurrent']  # Example Concurrency-related packages
}

total_category_counts = {category: 0 for category in categories}

for key, value in rxjava_parsed.items():
    category_counts = {category: 0 for category in categories}
    package_accesses = value.get('packageAccesses', {})
    sorted_categories = sorted(categories.items(), key=lambda x: len(x[1][0]), reverse=True)

    for package, count in package_accesses.items():
        for category, patterns in sorted_categories:
            if any(package.startswith(pat) for pat in patterns):
                category_counts[category] += count
                break

    rxjava_parsed[key].update(category_counts)

sorted_items = sorted(rxjava_parsed.items(), key=lambda x: x[1]['RMAD_new'], reverse=True)
rxjava_parsed = dict(sorted_items)

data = pd.DataFrame.from_dict(rxjava_parsed, orient='index')
columns_to_drop = ['filePath', 'methodName', 'methodCalls', 'objectInstantiations', 'packageAccesses'] + ['RMADS_NOT_USED']
data = data.drop(columns_to_drop, axis=1)

pd.set_option('display.width', None)  # Use maximum width available
pd.set_option('display.max_colwidth', None)

corr_to_rmad = data.corr(method='spearman')['RMAD_new'].drop('RMAD_new')  # Drop the correlation of RMAD with itself
corr_to_rmad_df = corr_to_rmad.to_frame().reset_index().rename(columns={'index': 'Variable', 'RMAD_new': 'Correlation'})
plt.figure(figsize=(10, 6))
sns.barplot(x='Correlation', y='Variable', data=corr_to_rmad_df, palette='coolwarm')
plt.title('Correlation of Variables with RMAD')
plt.xlabel('Correlation Coefficient')
plt.ylabel('Variables')
plt.show()

sns.pairplot(data, x_vars=['numNestedLoops', 'numConditionals', 'numLoops'], y_vars='RMAD_new', height=5, aspect=0.8, kind='reg')
plt.show()

# Aggregating all iteration times into a single list
all_iteration_times = [float(time) for benchmark in rxjava_dict.values() for fork in benchmark.values() for time in fork]

# print(all_iteration_times)
pseudocount = min([time for time in all_iteration_times if time > 0]) / 100
adjusted_times = [time for time in all_iteration_times]
logbins = np.logspace(np.log10(min(adjusted_times)), np.log10(max(adjusted_times)), num=20)

xmin = 0
xmax = 30000
nrbins = 20
bins = np.linspace(xmin, xmax, nrbins)
plt.xscale('log')

# plt.xlim(xmin, xmax)
# plt.hist(all_iteration_times, bins=logbins, alpha=0.7, edgecolor='black')
plt.hist(adjusted_times, bins=logbins, alpha=0.7, edgecolor='black')
plt.title('Frequency of iteration times')
plt.xlabel('Log-scaled iteration time (ns)')
plt.ylabel('Frequency (nr of iterations)')

#tick_interval = (xmax - xmin) / 10
#ticks = np.arange(xmin, xmax + tick_interval, tick_interval)
#plt.xticks(ticks, rotation=25)
plt.show()

# Calculate variance
# variance = np.var(all_iteration_times, ddof=1)  # Use ddof=1 for sample variance
# print(f'Variance of all benchmark iteration times: {variance}')
