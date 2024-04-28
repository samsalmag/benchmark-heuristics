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

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SensorIdTest {

    @Test
    public void equals_and_hashCode_depend_on_all_fields() {
        SensorId s1 = new SensorId("a", "b");
        SensorId s2 = new SensorId("a", "b");
        SensorId s3 = new SensorId(null, "b");
        SensorId s4 = new SensorId("a", "a");
        assertThat(s1).isEqualTo(s1).isEqualTo(s2).isNotEqualTo(s3).isNotEqualTo(s4).hasSameHashCodeAs(s2);
    }

    @Test
    public void constructor_fails_if_sensorName_is_null() {
        assertThatThrownBy(() -> new SensorId("p1", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void getters_are_correct() {
        SensorId s1 = new SensorId("a", "b");
        assertThat(s1.getSensorName()).isEqualTo("b");
        assertThat(s1.getPluginKey()).isEqualTo("a");
    }

    @Test
    public void toString_supports_all_values() {
        SensorId s = new SensorId(null, "a");
        assertThat(s).hasToString("a");
        s = new SensorId("a", "b");
        assertThat(s).hasToString("b [a]");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_equals_and_hashCode_depend_on_all_fields() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::equals_and_hashCode_depend_on_all_fields, this.description("equals_and_hashCode_depend_on_all_fields"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_constructor_fails_if_sensorName_is_null() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::constructor_fails_if_sensorName_is_null, this.description("constructor_fails_if_sensorName_is_null"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getters_are_correct() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::getters_are_correct, this.description("getters_are_correct"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toString_supports_all_values() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toString_supports_all_values, this.description("toString_supports_all_values"));
        }

        private SensorIdTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SensorIdTest();
        }

        @java.lang.Override
        public SensorIdTest implementation() {
            return this.implementation;
        }
    }
}
