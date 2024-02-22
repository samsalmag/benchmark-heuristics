import os
import re

# Finds all java files within the given directory
def find_java_files(directory):
    java_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))
    return java_files

# Finds all junit tests within a single java file with path 'java_file_path'
def find_junit_tests(java_file_path):
    junit_tests = []
    with open(java_file_path, "r") as file:
        content = file.read()
        # Find all methods annotated with @Test
        test_annotations = re.findall(r"@Test\s*.*?public\s+void\s+([a-zA-Z0-9_]+)\s*\(\s*\)", content, re.DOTALL)
        junit_tests.extend(test_annotations)
        
    return junit_tests

# Finds all junit tests within the given directory
def find_all_junit_tests(directory):
    junit_tests = []
    java_files = find_java_files(directory)
    for file in java_files:
        test_methods = find_junit_tests(file)
        for test in test_methods:
            file_no_extension = file[:-5]
            junit_tests.append(file_no_extension + "\\" + test)
    return junit_tests

# Creates a txt file containing all 'junit_tests' within the directory of 'test_directory'
def generate_txt(junit_tests, test_directory):
    file_name = extract_project_name(test_directory) + "_ALL" + ".txt"
    txt_path = os.path.join(os.path.dirname(__file__), "output", file_name)
    if not os.path.exists(os.path.dirname(txt_path)):
        os.makedirs(os.path.dirname(txt_path))
    with open(txt_path, "w") as f:
        for test_path in junit_tests:
            proccessed_test_path = process_path(test_path)
            f.write(proccessed_test_path + "\n")
    return txt_path

# Process and format the given path
def process_path(path):
    path = path.replace("\\", ".")          # Replace slashes with periods
    index_test = path.find(".test.java.")   # Find the index of the folder named "test" followed by a folder named "java"
    
    # If such a pattern is found, include everything after the "java" folder
    if index_test != -1:
        path = path[index_test + len(".test.java."):]
    
    return path

# Extracts project name from all projects in the 'projects' folder
def extract_project_name(file_path):
    start_index = file_path.find("projects\\") + len("projects\\")
    end_index = file_path.find("\\", start_index)
    if end_index != -1:
        return file_path[start_index:end_index]
    else:
        return file_path[start_index:]

# Exports all found junit tests within 'test_directory'
def export_junit_tests(test_directory):
    junit_tests = find_all_junit_tests(test_directory)
    generate_txt(junit_tests, test_directory)

path_1 = r"benchmark-heuristics\projects\mockito-5.10.0\src\test\java\org"                                                   # Mockito
path_2 = r"benchmark-heuristics\projects\RxJava-3.1.8\src\test\java\io\reactivex\rxjava3"                                    # RxJava
path_3 = r"benchmark-heuristics\projects\stubby4j-7.6.0\src\test\java\io\github\azagniotov\stubby4j"                         # stubby4j

# Mockito
print("Exporting Mockito junit tests...", end="")
export_junit_tests(path_1)  
print("done!")

# RxJava
print("Exporting RxJava junit tests...", end="")
export_junit_tests(path_2)  
print("done!")

# stubby4j
print("Exporting stubby4j junit tests...", end="")
export_junit_tests(path_3)  
print("done!")

print("ALL DONE!")
