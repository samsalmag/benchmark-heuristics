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
package org.sonar.batch.bootstrapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class BatchTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    @Test
    public void testBuilder() {
        System.out.println(FORMATTER.format(LocalDate.parse("2019-05-02").atStartOfDay(ZoneId.systemDefault())));
        Batch batch = newBatch();
        assertNotNull(batch);
    }

    private Batch newBatch() {
        return Batch.builder().setEnvironment(new EnvironmentInformation("Gradle", "1.0")).addComponent("fake").build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfNullComponents() {
        Batch.builder().setEnvironment(new EnvironmentInformation("Gradle", "1.0")).setComponents(null).build();
    }

    @Test
    public void shouldDisableLoggingConfiguration() {
        Batch batch = Batch.builder().setEnvironment(new EnvironmentInformation("Gradle", "1.0")).addComponent("fake").setEnableLoggingConfiguration(false).build();
        assertNull(batch.getLoggingConfiguration());
    }

    @Test
    public void loggingConfigurationShouldBeEnabledByDefault() {
        assertNotNull(newBatch().getLoggingConfiguration());
    }

    @Test
    public void shoudSetLogListener() {
        LogOutput logOutput = mock(LogOutput.class);
        Batch batch = Batch.builder().setLogOutput(logOutput).build();
        assertThat(batch.getLoggingConfiguration().getLogOutput()).isEqualTo(logOutput);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testBuilder() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testBuilder, this.description("testBuilder"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFailIfNullComponents() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::shouldFailIfNullComponents, this.description("shouldFailIfNullComponents"), java.lang.IllegalStateException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDisableLoggingConfiguration() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDisableLoggingConfiguration, this.description("shouldDisableLoggingConfiguration"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_loggingConfigurationShouldBeEnabledByDefault() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::loggingConfigurationShouldBeEnabledByDefault, this.description("loggingConfigurationShouldBeEnabledByDefault"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shoudSetLogListener() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shoudSetLogListener, this.description("shoudSetLogListener"));
        }

        private BatchTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new BatchTest();
        }

        @java.lang.Override
        public BatchTest implementation() {
            return this.implementation;
        }
    }
}
