def keep_specific_methods_in_inner_class(java_file_path, method_names):
    with open(java_file_path, 'r') as file:
        content = file.readlines()

    # Initialize variables to track the class and method positions
    class_start = -1
    class_end = -1
    brace_count = 0
    inside_class = False

    # New approach: Process lines to correctly identify class start and end
    processed_lines = []
    for line in content:
        if 'class _Benchmark' in line:
            class_start = len(processed_lines)
            inside_class = True
            brace_count += 1  # Counting the opening brace of the class
            processed_lines.append(line)
        elif inside_class:
            # Count braces to find the actual end of the class
            brace_count += line.count('{') - line.count('}')
            if brace_count == 0:
                inside_class = False
                class_end = len(processed_lines)
            processed_lines.append(line)
        else:
            processed_lines.append(line)

    # If the class was found, process its content
    if class_start != -1 and class_end != -1:
        inner_class_lines = processed_lines[class_start:class_end]
        new_inner_class_content = process_inner_class(inner_class_lines, method_names)
        # Replace the old inner class content with the new one
        processed_lines = processed_lines[:class_start] + new_inner_class_content + processed_lines[class_end:]
    else:
        print(f"Inner class '_Benchmark' not found : {java_file_path}")
        return

    # Write the modified content back to the file or a new file
    with open(java_file_path, 'w') as file:
        file.writelines(processed_lines)

def process_inner_class(lines, keep_method_name):
    new_lines = []
    method_start = -1
    brace_count = 0
    annotation_line = None

    for i, line in enumerate(lines):
        if 'public void' in line and 'benchmark' in line:
            if keep_method_name in line:
                # Add the annotation line if it exists
                if annotation_line is not None:
                    new_lines.append(annotation_line)
                    annotation_line = None
                new_lines.append(line)
                method_start = len(new_lines) - 1
            else:
                if method_start == -1:  # If not currently in a method to keep
                    brace_count = 1  # Assume this is a start of a method
                    annotation_line = None  # Reset annotation line as it's not needed
                else:
                    new_lines.append(line)  # Part of the method to keep
        elif method_start != -1:
            brace_count += line.count('{') - line.count('}')
            if brace_count == 0:
                method_start = -1  # Reset at the end of the method
            new_lines.append(line)  # Part of the method to keep
        elif brace_count > 0:
            brace_count += line.count('{') - line.count('}')
        else:
            if '@org.openjdk.jmh.annotations.Benchmark' in line:
                annotation_line = line  # Temporarily hold the annotation line
            else:
                # Add any pending annotation line if the next line isn't a method to remove
                if annotation_line is not None and 'public void' not in lines[min(i+1, len(lines)-1)]:
                    new_lines.append(annotation_line)
                    annotation_line = None
                new_lines.append(line)

    new_lines = remove_excessive_blank_lines(new_lines)
    return new_lines

def remove_excessive_blank_lines(lines):
    cleaned_lines = []
    previous_line_blank = False

    for line in lines:
        current_line_blank = not line.strip()  # Check if the current line is blank
        # Add the line if it's not blank or if the previous line was not blank
        if not current_line_blank or (current_line_blank and not previous_line_blank):
            cleaned_lines.append(line)
        previous_line_blank = current_line_blank

    return cleaned_lines

# Example usage
#jmh_path = r"projects\RxJava-3.1.8\src\jmh\java\io"
#java_files = find_java_files(jmh_path)
#print("HEJ")

path = r"scripts\output\RxJava-3.1.8_BENCHMARKS_240227-11h27m31s.txt"

with open(path, "r") as f:
        lines = f.readlines()

lines = [line.strip() for line in lines]            # Remove newline characters from the end of each line
for line in lines:
    if line == "":
        continue
    words = line.split(" ")
    path_base = r"projects\RxJava-3.1.8\src\jmh\java"
    file_path = path_base + f"\\{words[0]}"
    method_name = words[1]
    keep_specific_methods_in_inner_class(file_path, method_name)

#java_file_path = r"projects\RxJava-3.1.8\src\jmh\java\io\reactivex\rxjava3\completable\CompletableTest.java"
#method_names = "benchmark_andThenSingleNever"
#keep_specific_methods_in_inner_class(java_file_path, method_names)