import os
import random
from datetime import datetime

# Randomly selects 'num_tests' amount of junit tests from a file containing all a project's tests
def select_random_junit_tests(project_tests_path, num_tests):
    with open(project_tests_path, "r") as f:
        lines = f.readlines()

    lines = [line.strip() for line in lines]            # Remove newline characters from the end of each line
    enumerated_lines = list(enumerate(lines, start=1))  # Create a list of tuples containing (row_number, line_content)
    random.shuffle(enumerated_lines)                    # Shuffle the lines randomly
    return enumerated_lines[:num_tests]                 # Take the first 'num_tests' lines and return it

# Creates a txt file containing all selected unit tests from a given project, 
# the test class paths, and a command to run the benchmarks generated from the unit tests
def generate_output(project1_input, project2_input, project3_input):
    print("Starting selection and output of junit tests from projects...")
    
    selected_tests = []
    current_time = datetime.now().time()
    date_str = datetime.today().strftime("%y%m%d")
    time_str = str(current_time.hour) + "h" + \
               str(current_time.minute) + "m" + \
               str(current_time.second) + "s"
               

    for project in [project1_input, project2_input, project3_input]:
        project_tests_path, num_tests, jmhjar_name = project
        project_name = extract_project_name(project_tests_path)
        selected_tests = select_random_junit_tests(project_tests_path, num_tests)
        jmh_command = get_jmh_base_command(jmhjar_name)


        print(f"{project_name}...", end="")
        with open(os.path.join(os.path.dirname(__file__), "output", f"{project_name}_SELECTED_{date_str}-{time_str}.txt"), "w") as f:
            f.write(f"# {project_name} | SELECTED {num_tests} TESTS \n# ROW IN 'ALL TESTS' FILE | TEST NR. | TEST METHOD PATH \n")
            i = 1
            for row_number, row_content in selected_tests:
                row_number_str = str(row_number).rjust(7)
                f.write(f"{row_number_str} |     {i}.    {row_content[:7]}{row_content[7:]}\n")
                i += 1
                jmh_command += ' ' + get_benchmark_path(row_content)
            f.write("\n")

            # Print class paths
            class_names = []
            for row_number, row_content in selected_tests:
                row_content = row_content[:row_content.rfind(".")]
                class_names.append(row_content)
            f.write("# CLASS PATHS\n")
            for class_name in list(dict.fromkeys(class_names)):
                f.write(f"{class_name}\n")
            
            # Print jmh jar command
            f.write("\n# JMH COMMAND - RUN SELECTED BENCHMARKS\n" + jmh_command + "\n")

            print("done!")
    print("ALL DONE!")

# Extracts project name from a given tests files' 'file_path'
def extract_project_name(file_path):
    start_index = file_path.find("scripts\\output\\") + len("scripts\\output\\")
    end_index = file_path.find("_ALL.txt", start_index)
    if end_index != -1:
        return file_path[start_index:end_index]
    else:
        return file_path[start_index:]

# Returns the path to a single unit test's genereted ju2jmh benchmark
def get_benchmark_path(unit_test_path):
    last_period_index = unit_test_path.rfind('.') + 1  # Find position after the last occurrence of period ('.')
    
    if last_period_index != -1:
        # Split the string into two parts at the last period
        path_before_period = unit_test_path[:last_period_index]
        path_after_period = unit_test_path[last_period_index:]
        
        benchmark_path = path_before_period + "_Benchmark.benchmark_" + path_after_period   # Insert "_Benchmark.benchmark_" between the two parts
        return benchmark_path
    else:
        return unit_test_path

# Get jmh base command (jmh command without selected junit tests) based on given jmh jar name
def get_jmh_base_command(jmhjar_name):
    return f"java -jar \"{jmhjar_name}\" -f 1 -wi 0 -i 1 -r 100ms -foe true"

project1_tests_path = r"scripts\output\eclipse-collections-11.1.0_ALL.txt"   # Path to txt with ALL Eclipse Collections tests
project2_tests_path = r"scripts\output\RxJava-3.1.8_ALL.txt"                 # Path to txt with ALL RxJava tests
project3_tests_path = r"scripts\output\stubby4j-7.6.0_ALL.txt"               # Path to txt with ALL stubby4j tests

num_tests = 30  # Number of junit tests to select from each project
project1_jmhjar_name = "EMPTY"
project2_jmhjar_name = "rxjava-3.0.0-SNAPSHOT-jmh.jar"
project3_jmhjar_name = "EMPTY"

generate_output((project1_tests_path, num_tests, project1_jmhjar_name), 
                (project2_tests_path, num_tests, project2_jmhjar_name), 
                (project3_tests_path, num_tests, project3_jmhjar_name))
