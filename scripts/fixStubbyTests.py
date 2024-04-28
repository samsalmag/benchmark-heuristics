import json
import sys
import random

    
def gen_new_txt(file_path):
    pairs_paths = [('functional-test', 'functionalTest'), ('integration-test', 'integrationTest'), ('load-test', 'loadTest'), ('test', 'test')]

    with open(file_path, 'r') as reader, open(r"benchmarks\fixedStubbyFile.txt", 'w') as writer:
        lines = reader.readlines()
        sum = 0
        for line in lines:
            newLine = ''
            for first, second in pairs_paths:
                if '.'+first+'.' in line:
                    newLine = second + '.' + line
                    break
            if not newLine:
                newLine = 'test.' + line
            
            print(newLine)
            sum += 1
            writer.write(newLine)

stubby_path = r"benchmarks\stubbyjavaTestsNew.txt"
gen_new_txt(stubby_path)