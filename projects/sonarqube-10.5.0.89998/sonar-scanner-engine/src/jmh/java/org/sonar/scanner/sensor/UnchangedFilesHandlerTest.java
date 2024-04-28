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
package org.sonar.scanner.sensor;

import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.testfixtures.log.LogTester;
import org.sonar.scanner.scan.branch.BranchConfiguration;
import org.sonar.scanner.scan.branch.DefaultBranchConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class UnchangedFilesHandlerTest {

    private final ExecutingSensorContext executingSensorContext = new ExecutingSensorContext();

    private final Configuration enabledConfig = new MapSettings().setProperty("sonar.unchangedFiles.optimize", true).asConfig();

    private final DefaultInputFile file = mock(DefaultInputFile.class);

    private final BranchConfiguration defaultBranchConfig = new DefaultBranchConfiguration();

    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void log_if_active() {
        new UnchangedFilesHandler(enabledConfig, defaultBranchConfig, executingSensorContext);
        assertThat(logTester.logs()).contains("Optimization for unchanged files enabled");
    }

    @Test
    public void not_active_if_its_pr() {
        logTester.setLevel(Level.DEBUG);
        BranchConfiguration prConfig = branchConfiguration(null, null, true);
        UnchangedFilesHandler handler = new UnchangedFilesHandler(enabledConfig, prConfig, executingSensorContext);
        assertThat(logTester.logs()).contains("Optimization for unchanged files not enabled because it's not an analysis of a branch with a previous analysis");
        handler.markAsUnchanged(file);
        verifyNoInteractions(file);
    }

    @Test
    public void not_active_if_using_different_reference() {
        logTester.setLevel(Level.DEBUG);
        BranchConfiguration differentRefConfig = branchConfiguration("a", "b", false);
        UnchangedFilesHandler handler = new UnchangedFilesHandler(enabledConfig, differentRefConfig, executingSensorContext);
        assertThat(logTester.logs()).contains("Optimization for unchanged files not enabled because it's not an analysis of a branch with a previous analysis");
        handler.markAsUnchanged(file);
        verifyNoInteractions(file);
    }

    @Test
    public void not_active_if_property_not_defined() {
        UnchangedFilesHandler handler = new UnchangedFilesHandler(new MapSettings().asConfig(), defaultBranchConfig, executingSensorContext);
        handler.markAsUnchanged(file);
        verifyNoInteractions(file);
        assertThat(logTester.logs()).isEmpty();
    }

    @Test
    public void mark_file_if_sensor_is_enabled() {
        when(file.status()).thenReturn(InputFile.Status.SAME);
        executingSensorContext.setSensorExecuting(new SensorId("cpp", "CFamily"));
        UnchangedFilesHandler handler = new UnchangedFilesHandler(enabledConfig, defaultBranchConfig, executingSensorContext);
        handler.markAsUnchanged(file);
        verify(file).setMarkedAsUnchanged(true);
    }

    @Test
    public void dont_mark_file_if_sensor_is_not_enabled() {
        executingSensorContext.setSensorExecuting(new SensorId("cpp", "other"));
        UnchangedFilesHandler handler = new UnchangedFilesHandler(enabledConfig, defaultBranchConfig, executingSensorContext);
        handler.markAsUnchanged(file);
        verifyNoInteractions(file);
    }

    @Test
    public void dont_mark_file_if_no_sensor_running() {
        executingSensorContext.clearExecutingSensor();
        UnchangedFilesHandler handler = new UnchangedFilesHandler(enabledConfig, defaultBranchConfig, executingSensorContext);
        handler.markAsUnchanged(file);
        verifyNoInteractions(file);
    }

    @Test
    public void dont_mark_file_is_status_is_not_same() {
        when(file.status()).thenReturn(InputFile.Status.CHANGED);
        executingSensorContext.setSensorExecuting(new SensorId("cpp", "CFamily"));
        UnchangedFilesHandler handler = new UnchangedFilesHandler(enabledConfig, defaultBranchConfig, executingSensorContext);
        handler.markAsUnchanged(file);
        verify(file, never()).setMarkedAsUnchanged(true);
    }

    private BranchConfiguration branchConfiguration(@Nullable String branchName, @Nullable String referenceName, boolean isPullRequest) {
        BranchConfiguration branchConfiguration = mock(BranchConfiguration.class);
        when(branchConfiguration.referenceBranchName()).thenReturn(referenceName);
        when(branchConfiguration.branchName()).thenReturn(branchName);
        when(branchConfiguration.isPullRequest()).thenReturn(isPullRequest);
        return branchConfiguration;
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_log_if_active() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::log_if_active, this.description("log_if_active"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_not_active_if_its_pr() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::not_active_if_its_pr, this.description("not_active_if_its_pr"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_not_active_if_using_different_reference() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::not_active_if_using_different_reference, this.description("not_active_if_using_different_reference"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_not_active_if_property_not_defined() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::not_active_if_property_not_defined, this.description("not_active_if_property_not_defined"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mark_file_if_sensor_is_enabled() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mark_file_if_sensor_is_enabled, this.description("mark_file_if_sensor_is_enabled"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dont_mark_file_if_sensor_is_not_enabled() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dont_mark_file_if_sensor_is_not_enabled, this.description("dont_mark_file_if_sensor_is_not_enabled"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dont_mark_file_if_no_sensor_running() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dont_mark_file_if_no_sensor_running, this.description("dont_mark_file_if_no_sensor_running"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dont_mark_file_is_status_is_not_same() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dont_mark_file_is_status_is_not_same, this.description("dont_mark_file_is_status_is_not_same"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().logTester, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private UnchangedFilesHandlerTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new UnchangedFilesHandlerTest();
        }

        @java.lang.Override
        public UnchangedFilesHandlerTest implementation() {
            return this.implementation;
        }
    }
}
