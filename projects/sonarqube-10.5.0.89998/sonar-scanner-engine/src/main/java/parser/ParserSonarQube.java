/*
 * SonarQube
 * Copyright (C) 2009-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package parser;

import io.github.samsalmag.benchmarkheuristics.parser.BenchmarkParser;
import io.github.samsalmag.benchmarkheuristics.parser.Parser;

import java.util.Arrays;

public class ParserSonarQube {

    public static void main(String[] args) {

        Parser parser = new Parser(1000,
            "projects\\sonarqube-10.5.0.89998\\sonar-scanner-engine\\src\\main\\java\\",
            "projects\\sonarqube-10.5.0.89998\\sonar-scanner-engine\\src\\test\\java\\",
                Arrays.asList("org.sonar.batch", "org.sonar.scanner", "org.sonar.scm"),
                "projects\\sonarqube-10.5.0.89998\\sonar-scanner-engine\\dependencies\\");

        BenchmarkParser benchmarkParser = new BenchmarkParser("projects\\sonarqube-10.5.0.89998\\sonar-scanner-engine\\src\\test\\java\\",
                                                        "benchmarks\\results\\run2\\sonarqube-random_RMAD.json");

        // Split 750 benchmarks into 3 separate json files
        double successPercentage1 = benchmarkParser.parseBenchmarks(parser, 0, 249, "benchmarks\\results\\sonarqube-random_parsedBenchmarks1.json");
        double successPercentage2 = benchmarkParser.parseBenchmarks(parser, 250, 499, "benchmarks\\results\\sonarqube-random_parsedBenchmarks2.json");
        double successPercentage3 = benchmarkParser.parseBenchmarks(parser, 500, 750, "benchmarks\\results\\sonarqube-random_parsedBenchmarks3.json");

        System.out.println("TOTAL SUCCESS RATE: " + ((successPercentage1 + successPercentage2 + successPercentage3) / 3) + "%\n");
    }
}