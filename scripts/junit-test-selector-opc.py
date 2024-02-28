import os
import random
from datetime import datetime

# Selects one junit test per test class from project
def select_random_junit_tests(project_tests_path):
    with open(project_tests_path, "r") as f:
        lines = f.readlines()

    lines = [line.strip() for line in lines]            # Remove newline characters from the end of each line
    enumerated_lines = list(enumerate(lines, start=1))  # Create a list of tuples containing (row_number, line_content)
    random.shuffle(enumerated_lines)                    # Shuffle the lines randomly

    # Only save one junit test per class. A dict is used to to relate each class (key) to one junit test (value)
    test_per_class = {}
    for row_number, row_content in enumerated_lines:
        class_path = row_content[:row_content.rfind(".")]

        # If class doesn't exist in dict yet (no test has been chosen for the class), then select a test for the class
        if test_per_class.get(class_path) is None:
            test_per_class[class_path] = (row_number, row_content)

    return list(test_per_class.values())                 # Return the values of the dict (the test paths)

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
        project_tests_path, jmhjar_name = project
        project_name = extract_project_name(project_tests_path)
        selected_tests = select_random_junit_tests(project_tests_path)
        jmh_command = get_jmh_base_command(jmhjar_name)


        print(f"{project_name}...", end="")
        with open(os.path.join(os.path.dirname(__file__), "output", f"{project_name}_SELECTED_OPC_{date_str}-{time_str}.txt"), "w") as f:
            f.write(f"# {project_name} \n# ROW IN 'ALL TESTS' FILE | TEST NR. | TEST METHOD PATH \n")
            i = 1
            for row_number, row_content in selected_tests:
                row_number_str = str(row_number).rjust(7)
                f.write(f"{row_number_str} |     {i}.    {row_content[:7]}{row_content[7:]}\n")
                i += 1
                jmh_command += " " + get_benchmark_path(row_content) + "$"
            f.write("\n")

            # Print class paths
            class_names = []
            for row_number, row_content in selected_tests:
                row_content = row_content[:row_content.rfind(".")]
                class_names.append(row_content)
            f.write("# CLASS PATHS\n")
            for class_name in list(dict.fromkeys(class_names)):
                f.write(f"{class_name}\n")
            
            create_jmh_commands_txt(3, jmh_command)

            # Print jmh jar command
            f.write("\n# JMH COMMAND - RUNS SELECTED BENCHMARKS\n" + jmh_command + "\n")

            print("done!")

        # For benchmark-remover script
        with open(os.path.join(os.path.dirname(__file__), "output", f"{project_name}_BENCHMARKS_{date_str}-{time_str}.txt"), "w") as f:
            for row_number, row_content in selected_tests:
                method_name = row_content[row_content.rfind(".") + 1:]
                row_content = row_content[:row_content.rfind(".")]
                row_content = row_content.replace(".", "\\")          # Replace slashes with periods
                row_content += ".java"
                f.write(f"\n {row_content} {method_name}")

    print("ALL DONE!")

# Creates a txt file containing the whole jmh jar command split into multiple smaller commands
def create_jmh_commands_txt(nrCommands, jmh_command):
    words = jmh_command.split()
    # Extract the base command (string) and all test arguments (list)
    for i, word in enumerate(words):
        if '$' in word:
            jmh_base = ' '.join(words[:i])
            test_args = words[i:]
            print("\n")
            print(jmh_base)
            break
    
    # Create all smaller jmh commands
    jmh_commands = []
    part_length = len(test_args) // nrCommands
    remainder = len(test_args) % nrCommands
    start = 0
    for i in range(nrCommands):
        # Add remainder to the first i commands needing it
        end = start + part_length + (1 if i < remainder else 0)
        # Add jmh base to each command
        jmh_commands.append(jmh_base + " ")
        # Add specified test arguments with a space between them
        jmh_commands[i] += ' '.join(test_args[start:end])
        start = end

    jmhjar_name = words[2].replace('"', '')
    for i, command in enumerate(jmh_commands):
        with open(os.path.join(os.path.dirname(__file__), "output", f"{jmhjar_name}_jmh_command{i+1}.txt"), "w") as f:
            f.write(f"{command}")

 
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
    return f"java -jar \"{jmhjar_name}\" -bm avgt -tu ms -f 5 -wi 5 -i 5 -r 100ms -foe false -o rxjava-output.txt"
    

project1_tests_path = r"scripts\output\mockito-5.10.0_ALL.txt"               # Path to txt with ALL Mockito tests
project2_tests_path = r"scripts\output\RxJava-3.1.8_ALL.txt"                 # Path to txt with ALL RxJava tests
project3_tests_path = r"scripts\output\stubby4j-7.6.0_ALL.txt"               # Path to txt with ALL stubby4j tests

project1_jmhjar_name = "mockito-jmh.jar"
project2_jmhjar_name = "rxjavaOPC.jar"
project3_jmhjar_name = "stubby4j-jmh.jar"

generate_output((project1_tests_path, project1_jmhjar_name), 
                (project2_tests_path, project2_jmhjar_name), 
                (project3_tests_path, project3_jmhjar_name))
