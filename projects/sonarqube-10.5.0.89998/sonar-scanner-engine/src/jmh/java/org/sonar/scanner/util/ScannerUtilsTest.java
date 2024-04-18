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
package org.sonar.scanner.util;

import org.junit.Test;
import org.sonar.api.impl.utils.ScannerUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class ScannerUtilsTest {

    @Test
    public void encodeForUrl() {
        assertThat(ScannerUtils.encodeForUrl(null)).isEmpty();
        assertThat(ScannerUtils.encodeForUrl("")).isEmpty();
        assertThat(ScannerUtils.encodeForUrl("foo")).isEqualTo("foo");
        assertThat(ScannerUtils.encodeForUrl("foo&bar")).isEqualTo("foo%26bar");
    }

    private static class MyClass {

        @Override
        public String toString() {
            return null;
        }
    }

    @Test
    public void testDescribe() {
        Object withToString = new Object() {

            @Override
            public String toString() {
                return "desc";
            }
        };
        Object withoutToString = new Object();
        assertThat(ScannerUtils.describe(withToString)).isEqualTo(("desc"));
        assertThat(ScannerUtils.describe(withoutToString)).isEqualTo("java.lang.Object");
        assertThat(ScannerUtils.describe(new MyClass())).endsWith("MyClass");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_encodeForUrl() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::encodeForUrl, this.description("encodeForUrl"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testDescribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testDescribe, this.description("testDescribe"));
        }

        private ScannerUtilsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ScannerUtilsTest();
        }

        @java.lang.Override
        public ScannerUtilsTest implementation() {
            return this.implementation;
        }
    }
}
