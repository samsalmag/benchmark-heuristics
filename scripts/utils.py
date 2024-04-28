import json
import os
import sys

# Python script containing utility methods, facilitates other scripts

# Reads in a json file from path and returns it
def read_json_file(file_path):
    with open(file_path, 'r') as json_file:
            return json.load(json_file)
    
# Reads in a list of paths to json files, merges these and creates a new json file in path output_file 
def combine_json_files(file_paths, output_file):
    combined_json = {}
    for path in file_paths:
        if os.path.exists(path):
            combined_json.update(read_json_file(path)) # Merge data
        else:
            print(f"Warning: {path} does not exist, stopping merging of json files.")
            sys.exit(1)
    
    with open(output_file, 'w') as json_out:
        json.dump(combined_json, json_out, indent=4)