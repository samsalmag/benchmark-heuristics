import os
import re

path_1 = r"projects\eclipse-collections-11.1.0\unit-tests\src\test\java\org\eclipse\collections\impl"   # Eclipse Collections
path_2 = r"projects\RxJava-3.1.8\src\test\java\io\reactivex\rxjava3"                                    # RxJava
path_3 = r"projects\stubby4j-7.6.0\src\test\java\io\github\azagniotov\stubby4j"                         # stubby4j

def find_java_files(directory):
    java_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))
    return java_files

def find_junit_tests(java_file_path):
    junit_tests = []
    with open(java_file_path, "r") as file:
        content = file.read()
        # Find all methods annotated with @Test
        test_annotations = re.findall(r"@Test\s*.*?public\s+void\s+([a-zA-Z0-9_]+)\s*\(\s*\)", content, re.DOTALL)
        junit_tests.extend(test_annotations)
        
    return junit_tests

def find_all_junit_tests(directory):
    junit_tests = []
    java_files = find_java_files(directory)
    for file in java_files:
        test_methods = find_junit_tests(file)
        for test in test_methods:
            file_no_extension = file[:-5]
            junit_tests.append(file_no_extension + "\\" + test)
    return junit_tests

def generate_txt(junit_tests, test_directory):
    file_name = extract_project_name(test_directory) + ".txt"
    txt_path = os.path.join(os.path.dirname(__file__), "output", file_name)
    if not os.path.exists(os.path.dirname(txt_path)):
        os.makedirs(os.path.dirname(txt_path))
    with open(txt_path, "w") as f:
        for test_path in junit_tests:
            proccessed_test_path = process_path(test_path)
            f.write(proccessed_test_path + "\n")
    return txt_path

def process_path(path):
    # Replace slashes with periods
    path = path.replace("\\", ".")
    # Find the index of the folder named "test" followed by a folder named "java"
    index_test = path.find(".test.java.")
    # If such a pattern is found, include everything after the "java" folder
    if index_test != -1:
        path = path[index_test + len(".test.java."):]
    
    return path

def extract_project_name(file_path):
    start_index = file_path.find("projects\\") + len("projects\\")
    end_index = file_path.find("\\", start_index)
    if end_index != -1:
        return file_path[start_index:end_index]
    else:
        return file_path[start_index:]

def export_junit_tests(test_directory):
    junit_tests = find_all_junit_tests(test_directory)
    generate_txt(junit_tests, test_directory)

print("Exporting first path...")
export_junit_tests(path_1)  # Eclipse Collections
print("Exporting second path...")
export_junit_tests(path_2)  # RxJava
print("Exporting third path...")
export_junit_tests(path_3)  # stubby4j
