import os
import statistics
import re
import json
import sys

# Reads in a json file from path and returns it
def read_json_file(file_path):
    with open(file_path, 'r') as json_file:
            return json.load(json_file)

# Removes any errors from an inputted benchmark file (txt file from a jmh run) 
# Creates a new txt file with failed benchmarks being removed
def remove_errors_txt(file_path):
    lines_no_errors = []
    file_name = file_path.split(".")[0]

    with open(file_path, 'r') as infile, open(file_name+"_removed_errors.txt", 'w') as outfile:
        current_segment = []  # Buffer to store lines of the current benchmark segment
        contains_error = False  # Flag to indicate if the current benchmark segment contains an error
        in_summary = False  # Flag to indicate if the summary section has started

        # -------- remove thread text, if present ----------------
        # thread_pattern = r"^Thread\[.*?\n(?:  at .*\n)*(?:\r?\n)?"
        # file_contents = infile.read()
        # cleaned_text = re.sub(thread_pattern, "", file_contents, flags=re.MULTILINE)
        # outfile.writelines(cleaned_text)

        # -------- remove jdk warning text, if present ----------------
        # jdk_warning_text = "OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended"
        # pattern = re.escape(jdk_warning_text) + r"\s*"
        # file_contents = infile.read()
        # cleaned_text = re.sub(pattern, "", file_contents)
        # outfile.writelines(cleaned_text)

        for line in infile:
            # Check if the summary starts
            if line.startswith('# Run complete. Total time:'):
                in_summary = True
            
            if in_summary:
                # Write everything after the summary start line
                outfile.write(line)
            else:
                # Process benchmark segments before the summary
                if line.startswith('# Benchmark:'):
                    # If entering a new benchmark segment, check if the previous segment had an error
                    if not contains_error and current_segment:
                        outfile.writelines(current_segment)
                    # Reset for the new segment
                    contains_error = False
                    current_segment = []
                
                # Check if the line indicates a failure within a benchmark segment
                if '<failure>' in line:
                    contains_error = True
                
                # Add the current line to the segment buffer
                current_segment.append(line)
        
        # After the loop, if the last benchmark segment before the summary was error-free, write it
        if not contains_error and current_segment and not in_summary:
            outfile.writelines(current_segment)
    
    return lines_no_errors

# Creates and returns a dictionary containing all results of benchmark output (forks, iterations), 
# Input is file path to a cleaned txt file (having any errors removed)
def read_results(file_path):
    results = {} # key: benchmark name, value: dict(key: fork nr, value: list(iteration))

    with open(file_path, 'r') as file:
        current_benchmark = None
        current_fork = None
        for line in file:
            # Check for benchmark name
            if line.startswith('# Benchmark:'):
                current_benchmark = line.split(': ')[1].strip()
                results[current_benchmark] = {}
            # Check for fork number
            elif line.startswith('# Fork:'):
                #current_fork = line.split(': ')[1].strip()
                current_fork = "fork"+line.split(' ')[2]
                # Initialize an empty list for this fork in the current benchmark
                if current_benchmark:
                    results[current_benchmark][current_fork] = []
            # Check for iteration data
            elif line.strip().startswith('Iteration'):
                # iteration_data = line.split(' ')[4] # use this
                iteration_data = line.split(' ')[-2] # use this
                # Append the iteration data to the current fork of the current benchmark
                if current_benchmark and current_fork:
                    results[current_benchmark][current_fork].append(iteration_data)

    return results

# Removes a file from path
def remove_file(file_path):
    if os.path.exists(file_path):
        os.remove(file_path)
    else:
        print(f"The file {file_path} does not exist.")

# Returns RMAD value for a benchmark (Relative median absolute deviation)
# Does it by computing median of the distances of values from median of all iterations,
# i.e Median Absolute Deviation (MAD), and then divides the MAD with median of all iterations to get RMAD
def calculate_rmad_benchmark(forks_dict, stability_metric):
    # Flatten list of forks to get one list of all benchmark iteration times
    # all_fork_iterations = [float(time) for fork in forks_dict.values() for time in fork]
    all_fork_iterations = [float(time.replace(',', '.')) for fork in forks_dict.values() for time in fork]

    median = statistics.median(all_fork_iterations)
    # Computing distance between iteration times and median of all iterations
    deviations = [abs(time - median) for time in all_fork_iterations]
    MAD = statistics.median(deviations)
    mad_coefficient = { # Key is number of iterations used
        -1: 1.4826,
        5: 1.803927,
        10: 1.624681,
        20: 1.545705,
        30: 1.523031}
    if stability_metric == "RMAD_normal":
        RMAD = (mad_coefficient[20] * MAD) / median if median != 0 else 0
        return RMAD
    elif stability_metric == "RMAD_coefficient":
        RMAD_coefficient = (mad_coefficient[20] * MAD / median) * 100
        return RMAD_coefficient
    elif stability_metric == "RMAD_coefficientOneFork":
        fork1 = [float(time.replace(',', '.')) for time in forks_dict["fork1"]]
        median1Fork = statistics.median(fork1)
        # Computing distance between iteration times and median of all iterations
        deviations1Fork = [abs(time - median1Fork) for time in fork1]
        MAD1Fork = statistics.median(deviations1Fork)
        RMAD_oneFork = (mad_coefficient[20] * MAD1Fork / median1Fork) * 100
        return RMAD_oneFork
    
    print("should not get here")
    return MAD

# Calculates and returns a sorted list of pairs containing benchmarkname and its RMAD value
def get_rmad_all_benchmarks(benchmark_dict):
    rmad_benchmarks = []
    for benchmark, forks in benchmark_dict.items():
        rmad_benchmarks.append((benchmark, calculate_rmad_benchmark(forks, "RMAD_coefficient")))
        rmad_benchmarks.append((benchmark, calculate_rmad_benchmark(forks, "RMAD_coefficientOneFork"))) 
        rmad_benchmarks.append((benchmark, calculate_rmad_benchmark(forks, "RMAD_normal")))
    
    sorted_rmad = sorted(rmad_benchmarks, key=lambda pair: pair[1])
    return sorted_rmad

# Prints a warning if the inputted dictionary doesn't have 5 forks, with each fork having 5 iterations
def validate_benchmark_dictionary(benchmark_dict):
    for key in benchmark_dict:
        forkIndex = 0
        for fork in benchmark_dict[key]:
            forkIndex += 1
            iterList = benchmark_dict[key][fork]
            if len(iterList) != 5:
                print("WARNING, NOT 5 ITERATIONS!!!")
                print(benchmark_dict[key])
                print(key)
        if forkIndex != 5:
            print("WARNING, NOT 5 FORKS!!!")
    
# Reads the results from the summary (end of txt file) where normal distribution is used
def read_results_summary(file_path, benchmark_dict):
    result_dict = {}
    for benchmark, forks in benchmark_dict.items():
        RMAD_coefficient = calculate_rmad_benchmark(forks, "RMAD_coefficient")
        RMAD_coefficientOneFork = calculate_rmad_benchmark(forks, "RMAD_coefficientOneFork")
        RMAD_normal = calculate_rmad_benchmark(forks, "RMAD_normal")
        result_dict[benchmark] = {"RMAD_coefficient" : RMAD_coefficient, 'RMAD_coefficientOneFork' : RMAD_coefficientOneFork, 'RMAD_normal' : RMAD_normal}

    with open(file_path, 'r') as file:
        start_reading = False
        for line in file:
            # Check for start of benchmark summary
            if line.startswith('Benchmark'):
                start_reading = True

            elif start_reading:
                entries = [entry for entry in line.split(' ') if entry]
                entries[0] = entries[0].replace('i.r.r', 'io.reactivex.rxjava3')
                score = float(entries[3].replace(',', '.'))
                errorMargin = float(entries[5].replace(',', '.'))
                cv = (errorMargin / score) * 100

                # result_dict[entries[0]] = {"score" : score, "errorMargin" : errorMargin, "cv" : cv}
                result_dict[entries[0]].update({"score" : score, "errorMargin" : errorMargin, "cv" : cv})
    return result_dict
        

paths = [r"benchmarks\RMADS_LOCAL.json", r"benchmarks\ParsedBenchmarksBest.json"]
rxjava_dict = {}
rmads = read_json_file(paths[0])
parsed = read_json_file(paths[1])

merged_dict = {}

# print(rmads['io.reactivex.rxjava3.internal.functions.ObjectHelperTest.verifyPositiveIntFail'])

# Iterate through the keys in the first dictionary
for key in rmads:
    oldKey = key
    key = key.replace('_Benchmark.benchmark_', "")
    # Check if the key is also in the second dictionary
    if key in parsed:
        # merged_dict[key] = parsed[key] + rmads[oldKey]  # Or use dict2[key], or a combination
        merged_dict[key] = parsed[key].copy()  # Start with a copy of the first dictionary
        merged_dict[key].update(rmads[oldKey])
print(len(merged_dict))

with open(r"benchmarks" + r'\ParsedRMADS_BEST.json', 'w') as json_file:
    json.dump(merged_dict, json_file)
sys.exit()


root_path = r"benchmarks\results"
with open(root_path + r'\newRxjava_parsedBenchmarksCOMBINED.json', 'w') as json_file:
    json.dump(rxjava_dict, json_file)

# root_path = r"benchmarks\results\run2"
root_path = r"benchmarks"
# file_paths_mockito = [root_path + r"\mockito-output1.txt", root_path + r"\mockito-output2.txt"]
file_paths_rxjava = [root_path + r"\rxjava-output1.txt", root_path + r"\rxjava-output2.txt", root_path + r"\rxjava-output3.txt", root_path + r"\rxjava-output4.txt", root_path + r"\rxjava-output5.txt"]
# file_paths_stubby = [root_path + r"\rxjava-outputNEW.txt"]
file_path_local = root_path + r"\rxjava-outputLocal1.txt"
# results = []
# for path in file_paths_rxjava:
#     remove_errors_txt(path)
#     path_no_error_file = path.split(".")[0] +"_removed_errors.txt"
#     results.append(read_results(path_no_error_file))

remove_errors_txt(file_path_local)
path_no_error_file = file_path_local.split(".")[0] +"_removed_errors.txt"
results = read_results(path_no_error_file)
rmads = read_results_summary(path_no_error_file, results)
print(rmads)
with open(root_path + r'\RMADS_LOCAL.json', 'w') as json_file:
    json.dump(rmads, json_file)




# rxjava_dict = {}
# for dictionary in results:
#     rxjava_dict.update(dictionary)

# rmads = get_rmad_all_benchmarks(rxjava_dict)
# nr = 0
# for benchmarks in rmads:
#     if benchmarks[1] >= 10:
#         nr += 1
# print(nr)

# rxjava_dict = {}
# rxjava_dict.update(results[0])
# rmads = get_rmad_all_benchmarks(rxjava_dict)

# with open(root_path + r'\NEW_RXJAVA_RMAD.json', 'w') as json_file:
#     json.dump(rmads, json_file)
