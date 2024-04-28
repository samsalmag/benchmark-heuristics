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
package org.sonar.scanner.ci.vendors;

import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTester;
import org.sonar.api.utils.System2;
import org.sonar.scanner.ci.CiConfiguration;
import org.sonar.scanner.ci.CiVendor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CirrusCiTest {

    private System2 system = mock(System2.class);

    private CiVendor underTest = new CirrusCi(system);

    @Rule
    public LogTester logs = new LogTester();

    @Test
    public void getName() {
        assertThat(underTest.getName()).isEqualTo("CirrusCI");
    }

    @Test
    public void isDetected() {
        setEnvVariable("CIRRUS_CI", "true");
        assertThat(underTest.isDetected()).isTrue();
        setEnvVariable("CIRRUS_CI", null);
        assertThat(underTest.isDetected()).isFalse();
    }

    @Test
    public void configuration_of_pull_request() {
        setEnvVariable("CIRRUS_PR", "1234");
        setEnvVariable("CIRRUS_BASE_SHA", "abd12fc");
        setEnvVariable("CIRRUS_CHANGE_IN_REPO", "fd355db");
        assertThat(underTest.loadConfiguration().getScmRevision()).hasValue("fd355db");
    }

    @Test
    public void configuration_of_branch() {
        setEnvVariable("CIRRUS_CHANGE_IN_REPO", "abd12fc");
        assertThat(underTest.loadConfiguration().getScmRevision()).hasValue("abd12fc");
    }

    @Test
    public void log_warning_if_missing_commit_variable() {
        setEnvVariable("CIRRUS_PR", "1234");
        CiConfiguration configuration = underTest.loadConfiguration();
        assertThat(configuration.getScmRevision()).isEmpty();
        assertThat(logs.logs(Level.WARN)).contains("Missing environment variable CIRRUS_CHANGE_IN_REPO");
    }

    private void setEnvVariable(String key, @Nullable String value) {
        when(system.envVariable(key)).thenReturn(value);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getName() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::getName, this.description("getName"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDetected() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isDetected, this.description("isDetected"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_configuration_of_pull_request() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::configuration_of_pull_request, this.description("configuration_of_pull_request"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_configuration_of_branch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::configuration_of_branch, this.description("configuration_of_branch"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_log_warning_if_missing_commit_variable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::log_warning_if_missing_commit_variable, this.description("log_warning_if_missing_commit_variable"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().logs, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private CirrusCiTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CirrusCiTest();
        }

        @java.lang.Override
        public CirrusCiTest implementation() {
            return this.implementation;
        }
    }
}
