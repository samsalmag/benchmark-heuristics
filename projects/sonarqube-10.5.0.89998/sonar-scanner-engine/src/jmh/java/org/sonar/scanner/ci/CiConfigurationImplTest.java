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
package org.sonar.scanner.ci;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CiConfigurationImplTest {

    @Test
    public void getScmRevision() {
        assertThat(new CiConfigurationImpl(null, "test").getScmRevision()).isEmpty();
        assertThat(new CiConfigurationImpl("", "test").getScmRevision()).isEmpty();
        assertThat(new CiConfigurationImpl("   ", "test").getScmRevision()).isEmpty();
        assertThat(new CiConfigurationImpl("a7bdf2d", "test").getScmRevision()).hasValue("a7bdf2d");
    }

    @Test
    public void getNam_for_undetected_ci() {
        assertThat(new CiConfigurationProvider.EmptyCiConfiguration().getCiName()).isEqualTo("undetected");
    }

    @Test
    public void getName_for_detected_ci() {
        assertThat(new CiConfigurationImpl(null, "test").getCiName()).isEqualTo("test");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getScmRevision() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::getScmRevision, this.description("getScmRevision"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getNam_for_undetected_ci() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::getNam_for_undetected_ci, this.description("getNam_for_undetected_ci"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getName_for_detected_ci() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::getName_for_detected_ci, this.description("getName_for_detected_ci"));
        }

        private CiConfigurationImplTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CiConfigurationImplTest();
        }

        @java.lang.Override
        public CiConfigurationImplTest implementation() {
            return this.implementation;
        }
    }
}
