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

# Finds all junit tests within the given directories
def find_all_junit_tests(directories):
    junit_tests = []
    
    for d in directories:
        java_files = find_java_files(d)
        for file in java_files:
            test_methods = find_junit_tests(file)
            for test in test_methods:
                file_no_extension = file[:-5]
                junit_tests.append(file_no_extension + "\\" + test)
    return junit_tests

# Creates a txt file containing all 'junit_tests' and their paths
def generate_txt(junit_tests, project_name):
    file_name =  project_name + "_ALL" + ".txt"
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
    index_test = path.find(".java.")   # Find the index of the folder named "test" followed by a folder named "java"
    
    # If such a pattern is found, include everything after the "java" folder
    if index_test != -1:
        path = path[index_test + len(".java."):]
    
    return path

# Exports all found junit tests within 'test_directories'
def export_junit_tests(test_directories, project_name):
    junit_tests = find_all_junit_tests(test_directories)
    generate_txt(junit_tests, project_name)

path_1 = (r"projects\mockito-5.10.0\src\test\java\org",)                                            # Mockito
path_2 = (r"projects\RxJava-3.1.8\src\test\java\io\reactivex\rxjava3",)                             # RxJava
path_3 = (r"projects\stubby4j-7.6.0\src\test\java\io\github\azagniotov\stubby4j",)                  # stubby4j test
path_4 = (r"projects\stubby4j-7.6.0\src\functional-test\java\io\github\azagniotov\stubby4j",)       # stubby4j functional-test
path_5 = (r"projects\stubby4j-7.6.0\src\integration-test\java\io\github\azagniotov\stubby4j",)      # stubby4j integration-test
# path_6 = (r"projects\spring-framework-6.0.3\spring-core\src",)      # Spring-framework
# path_6 = (r"projects\sonarqube-10.5.0.89998\sonar-core\src\test\java\org",)      # sonarqube-core
path_7 = (r"projects\sonarqube-10.5.0.89998\sonar-scanner-engine\src\test\java\org",)      # sonarqube-scanner
# path_7 = (r"projects\sonarqube-10.5.0.89998\sonar-duplications\src\test\java\org",)      # sonarqube-duplications
# path_6 = (r"projects\stubby4j-7.6.0\src\load-test\java\io\github\azagniotov\stubby4j",)             # stubby4j load-test                                   

# Mockito
print("Exporting Mockito junit tests...", end="")
# export_junit_tests(path_1, "mockito-5.10.0")  
print("done!")

# RxJava
print("Exporting RxJava junit tests...", end="")
# export_junit_tests(path_2, "RxJava-3.1.8")  
print("done!")

# stubby4j
print("Exporting spring junit tests...", end="")
export_junit_tests(path_7, "sonarqube")
# export_junit_tests(path_3, "stubby4j")
# export_junit_tests(path_4, "stubby4j-functional-test")
# export_junit_tests(path_5, "stubby4j-integration-test")
# export_junit_tests(path_6, "stubby4j-7.6.0-load-test") 
print("done!")

print("ALL DONE!")
