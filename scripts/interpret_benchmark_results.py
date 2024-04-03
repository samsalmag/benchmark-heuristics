import os
import statistics
import re
import json

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
                #iteration_data = line.strip().split(': ')[1]
                iteration_data = line.split(' ')[4]
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
def calculate_rmad_benchmark(forks_dict):
    # Flatten list of forks to get one list of all benchmark iteration times
    all_fork_iterations = [float(time) for fork in forks_dict.values() for time in fork]
    median = statistics.median(all_fork_iterations)
    # Computing distance between iteration times and median of all iterations
    deviations = [abs(time - median) for time in all_fork_iterations]
    MAD = statistics.median(deviations)
    mad_coefficient = {
        -1: 1.4826,
        5: 1.803927,
        10: 1.624681,
        20: 1.545705,
        30: 1.523031}
    RMAD_new = (mad_coefficient[5] * MAD / median * 100)
    # RMAD = MAD / median if median != 0 else 0
    # return RMAD_new
    return MAD

# Calculates and returns a sorted list of pairs containing benchmarkname and its RMAD value
def get_rmad_all_benchmarks(benchmark_dict):
    rmad_benchmarks = []
    for benchmark, forks in benchmark_dict.items():
        rmad_benchmarks.append((benchmark, calculate_rmad_benchmark(forks)))
    
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
    
root_path = r"benchmarks\results"
# file_paths_mockito = [root_path + r"\mockito-output1.txt", root_path + r"\mockito-output2.txt"]
# file_paths_rxjava = [root_path + r"\rxjava-output1.txt", root_path + r"\rxjava-output2.txt", root_path + r"\rxjava-output3.txt"]
file_paths_stubby = [root_path + r"\stubby4j-output1.txt"]
results = []
for path in file_paths_stubby:
    remove_errors_txt(path)
    path_no_error_file = path.split(".")[0] +"_removed_errors.txt"
    results.append(read_results(path_no_error_file))

# rxjava_dict = {}
# for dictionary in results:
#     rxjava_dict.update(dictionary)

stubby_dict = {}
stubby_dict.update(results[0])
rmads = get_rmad_all_benchmarks(stubby_dict)

with open(root_path + r'\stubby4j_RMAD.json', 'w') as json_file:
    json.dump(rmads, json_file)
