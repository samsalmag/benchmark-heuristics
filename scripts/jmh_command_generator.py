import os
import random
from datetime import datetime
import sys

# Creates a txt file containing the whole jmh jar command split into multiple smaller commands
def create_jmh_commands_txt(nrCommands, file_path):
    jmh_command = ""
    with open(file_path, "r") as f:
         for line in f:
             if line.startswith('java -jar'):
                 jmh_command = line

    words = jmh_command.split()
    # Extract the base command (string) and all test arguments (list)
    for i, word in enumerate(words):
        if '$' in word:
            jmh_base = ' '.join(words[:i])
            test_args = words[i:]
            break
    
    # Create all smaller jmh commands
    jmh_commands = []
    part_length = len(test_args) // nrCommands
    remainder = len(test_args) % nrCommands
    start = 0
    for i in range(nrCommands):
        # Add remainder to the first i commands needing it
        end = start + part_length + (1 if i < remainder else 0)
        print(f"START: {start}, END: {end}")
        # Add jmh base to each command
        jmh_new_base = jmh_base.replace("output", f"output{i+1}")
        jmh_commands.append(jmh_new_base + " ")
        # jmh_commands.append(jmh_base + " ")
        # Add specified test arguments with a space between them
        jmh_commands[i] += ' '.join(test_args[start:end])
        start = end

    # jmhjar_name = words[2].replace('"', '').replace('jmh-OPC.jar', '')
    jmhjar_name = file_path.split('\\')[-1].split('.')[0]
    for i, command in enumerate(jmh_commands):
        with open(os.path.join(os.path.dirname(__file__), "output", f"{jmhjar_name}_jmh_command{i+1}.txt"), "w") as f:
            f.write(f"{command}")

# rxjava_path = r"test-selections\RxJava-3.1.8_SELECTED_OPC_240228-10h34m41s.txt"
root_path = r"benchmarks\results\onlyRxJava"
rxjava_path = root_path + r"\RxJava_SELECTED_10000.txt"
stubby_path = r"benchmarks\results\testStubby.txt"
create_jmh_commands_txt(5, rxjava_path)