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
package org.sonar.scanner.genericcoverage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.event.Level;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.internal.Encryption;
import org.sonar.api.utils.System2;
import org.sonar.api.testfixtures.log.LogTester;
import org.sonar.scanner.config.DefaultConfiguration;
import org.sonar.scanner.deprecated.test.TestPlanBuilder;
import org.sonar.scanner.scan.ProjectConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class GenericTestExecutionSensorTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void logWhenDeprecatedPropsAreUsed() throws IOException {
        File basedir = temp.newFolder();
        File report = new File(basedir, "report.xml");
        FileUtils.write(report, "<unitTest version=\"1\"><file path=\"A.java\"><testCase name=\"test1\" duration=\"500\"/></file></unitTest>", StandardCharsets.UTF_8);
        SensorContextTester context = SensorContextTester.create(basedir);
        Map<String, String> settings = new HashMap<>();
        settings.put(GenericTestExecutionSensor.OLD_UNIT_TEST_REPORT_PATHS_PROPERTY_KEY, "report.xml");
        PropertyDefinitions defs = new PropertyDefinitions(System2.INSTANCE, GenericTestExecutionSensor.properties());
        DefaultConfiguration config = new ProjectConfiguration(defs, new Encryption(null), settings);
        new GenericTestExecutionSensor(mock(TestPlanBuilder.class), config).execute(context);
        assertThat(logTester.logs(Level.WARN)).contains("Using 'unitTest' as root element of the report is deprecated. Please change to 'testExecutions'.", "Property 'sonar.genericcoverage.unitTestReportPaths' is deprecated. Please use 'sonar.testExecutionReportPaths' instead.");
        assertThat(logTester.logs(Level.INFO)).contains("Imported test execution data for 0 files", "Test execution data ignored for 1 unknown files, including:\nA.java");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_logWhenDeprecatedPropsAreUsed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::logWhenDeprecatedPropsAreUsed, this.description("logWhenDeprecatedPropsAreUsed"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().temp, statement, description);
            statement = this.applyRule(this.implementation().logTester, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private GenericTestExecutionSensorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new GenericTestExecutionSensorTest();
        }

        @java.lang.Override
        public GenericTestExecutionSensorTest implementation() {
            return this.implementation;
        }
    }
}
