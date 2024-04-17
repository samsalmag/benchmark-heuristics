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

public class ParserSonarQube {

    public static void main(String[] args) {

        Parser parser = new Parser(
            "projects\\sonarqube-10.5.0.89998\\sonar-scanner-engine\\src\\main\\java\\",
            "projects\\sonarqube-10.5.0.89998\\sonar-scanner-engine\\src\\test\\java\\",
            "sonar");

        BenchmarkParser benchmarkParser = new BenchmarkParser("projects\\sonarqube-10.5.0.89998\\sonar-scanner-engine\\src\\test\\java\\",
                                                        "benchmarks\\results\\run2\\sonarqube-random_RMAD.json");

        // Split 750 benchmarks into 3 separate json files
        benchmarkParser.parseBenchmarks(parser, 0, 249, "benchmarks\\results\\sonarqube_parsedBenchmarks1_run2.json");
        benchmarkParser.parseBenchmarks(parser, 250, 499, "benchmarks\\results\\sonarqube_parsedBenchmarks1_run2.json");
        benchmarkParser.parseBenchmarks(parser, 500, 750, "benchmarks\\results\\sonarqube_parsedBenchmarks1_run2.json");
    }
}
