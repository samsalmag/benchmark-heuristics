import matplotlib.pyplot as plt
import scipy.stats as stats
import json

rxjava_dict = {}
root_path = r"benchmarks\results"
with open(root_path + r'\rxjava_dict.json', 'r') as json_file:
    rxjava_dict = json.load(json_file)


# Aggregating all iteration times into a single list
all_iteration_times = [float(time) for benchmark in rxjava_dict.values() for fork in benchmark.values() for time in fork]

# Histogram
# plt.hist(all_iteration_times, bins=30, alpha=0.7, edgecolor='black', range=(0, 300))
# plt.title('Histogram of All Iteration Times')
# plt.xlabel('Iteration Time')
# plt.ylabel('Frequency')
# plt.xlim(0, 300)
# plt.show()

# Calculate variance
# variance = np.var(all_iteration_times, ddof=1)  # Use ddof=1 for sample variance
# print(f'Variance of all benchmark iteration times: {variance}')
