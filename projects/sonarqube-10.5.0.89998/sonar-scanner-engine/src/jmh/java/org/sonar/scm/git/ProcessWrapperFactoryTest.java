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
package org.sonar.scm.git;

import java.io.IOException;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTester;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProcessWrapperFactoryTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public LogTester logTester = new LogTester();

    private final ProcessWrapperFactory underTest = new ProcessWrapperFactory();

    @Test
    public void should_log_error_output_in_debug_mode() throws IOException {
        logTester.setLevel(Level.DEBUG);
        var root = temp.newFolder().toPath();
        var processWrapper = underTest.create(root, v -> {
        }, Map.of("LANG", "en_US"), "git", "blame");
        assertThatThrownBy(processWrapper::execute).isInstanceOf(IllegalStateException.class);
        assertThat(logTester.logs(Level.DEBUG).get(0)).startsWith("fatal:");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_log_error_output_in_debug_mode() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_log_error_output_in_debug_mode, this.description("should_log_error_output_in_debug_mode"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().temp, statement, description);
            statement = this.applyRule(this.implementation().logTester, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private ProcessWrapperFactoryTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ProcessWrapperFactoryTest();
        }

        @java.lang.Override
        public ProcessWrapperFactoryTest implementation() {
            return this.implementation;
        }
    }
}
