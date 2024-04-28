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
package org.sonar.scanner.scan;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.CoreProperties;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.notifications.AnalysisWarnings;
import org.sonar.api.testfixtures.log.LogTester;
import org.sonar.batch.bootstrapper.EnvironmentInformation;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.sonar.scanner.scan.DeprecatedPropertiesWarningGenerator.LOGIN_WARN_MESSAGE;
import static org.sonar.scanner.scan.DeprecatedPropertiesWarningGenerator.PASSWORD_WARN_MESSAGE;
import static org.sonar.scanner.scan.DeprecatedPropertiesWarningGenerator.SCANNER_DOTNET_WARN_MESSAGE;

public class DeprecatedPropertiesWarningGeneratorTest {

    @Rule
    public LogTester logger = new LogTester();

    private final MapSettings settings = new MapSettings();

    private final AnalysisWarnings analysisWarnings = Mockito.spy(AnalysisWarnings.class);

    private final EnvironmentInformation environmentInformation = Mockito.mock(EnvironmentInformation.class);

    private final DeprecatedPropertiesWarningGenerator underTest = new DeprecatedPropertiesWarningGenerator(settings.asConfig(), analysisWarnings, environmentInformation);

    @Before
    public void setUp() throws Exception {
        settings.removeProperty(CoreProperties.LOGIN);
        settings.removeProperty(CoreProperties.PASSWORD);
        when(environmentInformation.getKey()).thenReturn("ScannerCLI");
    }

    @Test
    public void execute_whenUsingLogin_shouldAddWarning() {
        settings.setProperty(CoreProperties.LOGIN, "test");
        underTest.execute();
        verify(analysisWarnings, times(1)).addUnique(LOGIN_WARN_MESSAGE);
        Assertions.assertThat(logger.logs(Level.WARN)).contains(LOGIN_WARN_MESSAGE);
    }

    @Test
    public void execute_whenUsingPassword_shouldAddWarning() {
        settings.setProperty(CoreProperties.LOGIN, "test");
        settings.setProperty(CoreProperties.PASSWORD, "winner winner chicken dinner");
        underTest.execute();
        verify(analysisWarnings, times(1)).addUnique(PASSWORD_WARN_MESSAGE);
        Assertions.assertThat(logger.logs(Level.WARN)).contains(PASSWORD_WARN_MESSAGE);
    }

    @Test
    public void execute_whenUsingLoginAndDotNetScanner_shouldAddWarning() {
        settings.setProperty(CoreProperties.LOGIN, "test");
        when(environmentInformation.getKey()).thenReturn("ScannerMSBuild");
        underTest.execute();
        verify(analysisWarnings, times(1)).addUnique(LOGIN_WARN_MESSAGE + SCANNER_DOTNET_WARN_MESSAGE);
        Assertions.assertThat(logger.logs(Level.WARN)).contains(LOGIN_WARN_MESSAGE + SCANNER_DOTNET_WARN_MESSAGE);
    }

    @Test
    public void execute_whenUsingPasswordAndDotNetScanner_shouldAddWarning() {
        settings.setProperty(CoreProperties.LOGIN, "test");
        settings.setProperty(CoreProperties.PASSWORD, "winner winner chicken dinner");
        when(environmentInformation.getKey()).thenReturn("ScannerMSBuild");
        underTest.execute();
        verify(analysisWarnings, times(1)).addUnique(PASSWORD_WARN_MESSAGE + SCANNER_DOTNET_WARN_MESSAGE);
        Assertions.assertThat(logger.logs(Level.WARN)).contains(PASSWORD_WARN_MESSAGE + SCANNER_DOTNET_WARN_MESSAGE);
    }

    @Test
    public void execute_whenNotUsingLoginOrPassword_shouldNotAddWarning() {
        underTest.execute();
        verifyNoInteractions(analysisWarnings);
        Assertions.assertThat(logger.logs(Level.WARN)).isEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_execute_whenUsingLogin_shouldAddWarning() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::execute_whenUsingLogin_shouldAddWarning, this.description("execute_whenUsingLogin_shouldAddWarning"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_execute_whenUsingPassword_shouldAddWarning() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::execute_whenUsingPassword_shouldAddWarning, this.description("execute_whenUsingPassword_shouldAddWarning"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_execute_whenUsingLoginAndDotNetScanner_shouldAddWarning() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::execute_whenUsingLoginAndDotNetScanner_shouldAddWarning, this.description("execute_whenUsingLoginAndDotNetScanner_shouldAddWarning"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_execute_whenUsingPasswordAndDotNetScanner_shouldAddWarning() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::execute_whenUsingPasswordAndDotNetScanner_shouldAddWarning, this.description("execute_whenUsingPasswordAndDotNetScanner_shouldAddWarning"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_execute_whenNotUsingLoginOrPassword_shouldNotAddWarning() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::execute_whenNotUsingLoginOrPassword_shouldNotAddWarning, this.description("execute_whenNotUsingLoginOrPassword_shouldNotAddWarning"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().logger, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private DeprecatedPropertiesWarningGeneratorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DeprecatedPropertiesWarningGeneratorTest();
        }

        @java.lang.Override
        public DeprecatedPropertiesWarningGeneratorTest implementation() {
            return this.implementation;
        }
    }
}
