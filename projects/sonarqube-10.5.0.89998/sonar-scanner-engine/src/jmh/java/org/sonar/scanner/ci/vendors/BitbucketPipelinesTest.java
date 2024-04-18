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
import org.junit.Test;
import org.sonar.api.utils.System2;
import org.sonar.scanner.ci.CiVendor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BitbucketPipelinesTest {

    private System2 system = mock(System2.class);

    private CiVendor underTest = new BitbucketPipelines(system);

    @Test
    public void getName() {
        assertThat(underTest.getName()).isEqualTo("Bitbucket Pipelines");
    }

    @Test
    public void isDetected() {
        setEnvVariable("CI", "true");
        setEnvVariable("BITBUCKET_COMMIT", "bdf12fe");
        assertThat(underTest.isDetected()).isTrue();
        setEnvVariable("CI", "true");
        setEnvVariable("BITBUCKET_COMMIT", null);
        assertThat(underTest.isDetected()).isFalse();
    }

    @Test
    public void configuration_of_pull_request() {
        setEnvVariable("CI", "true");
        setEnvVariable("BITBUCKET_COMMIT", "abd12fc");
        setEnvVariable("BITBUCKET_PR_ID", "1234");
        assertThat(underTest.loadConfiguration().getScmRevision()).hasValue("abd12fc");
    }

    @Test
    public void configuration_of_branch() {
        setEnvVariable("CI", "true");
        setEnvVariable("BITBUCKET_COMMIT", "abd12fc");
        setEnvVariable("BITBUCKET_PR_ID", null);
        assertThat(underTest.loadConfiguration().getScmRevision()).hasValue("abd12fc");
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

        private BitbucketPipelinesTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new BitbucketPipelinesTest();
        }

        @java.lang.Override
        public BitbucketPipelinesTest implementation() {
            return this.implementation;
        }
    }
}
