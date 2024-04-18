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
package org.sonar.scanner.issue.ignore.pattern;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class LineRangeTest {

    @Test(expected = IllegalArgumentException.class)
    public void lineRangeShouldBeOrdered() {
        new LineRange(25, 12);
    }

    @Test
    public void shouldConvertLineRangeToLines() {
        LineRange range = new LineRange(12, 15);
        assertThat(range.toLines()).containsOnly(12, 13, 14, 15);
    }

    @Test
    public void shouldTestInclusionInRangeOfLines() {
        LineRange range = new LineRange(12, 15);
        assertThat(range.in(3)).isFalse();
        assertThat(range.in(12)).isTrue();
        assertThat(range.in(13)).isTrue();
        assertThat(range.in(14)).isTrue();
        assertThat(range.in(15)).isTrue();
        assertThat(range.in(16)).isFalse();
    }

    @Test
    public void testToString() {
        assertThat(new LineRange(12, 15)).hasToString("[12-15]");
    }

    @Test
    public void testEquals() {
        LineRange range = new LineRange(12, 15);
        assertThat(range).isEqualTo(range).isEqualTo(new LineRange(12, 15)).isNotEqualTo(new LineRange(12, 2000)).isNotEqualTo(new LineRange(1000, 2000)).isNotNull().isNotEqualTo(new StringBuffer());
    }

    @Test
    public void testHashCode() {
        assertThat(new LineRange(12, 15)).hasSameHashCodeAs(new LineRange(12, 15));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lineRangeShouldBeOrdered() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::lineRangeShouldBeOrdered, this.description("lineRangeShouldBeOrdered"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldConvertLineRangeToLines() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldConvertLineRangeToLines, this.description("shouldConvertLineRangeToLines"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldTestInclusionInRangeOfLines() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldTestInclusionInRangeOfLines, this.description("shouldTestInclusionInRangeOfLines"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testToString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testToString, this.description("testToString"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testEquals() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testEquals, this.description("testEquals"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testHashCode() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testHashCode, this.description("testHashCode"));
        }

        private LineRangeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new LineRangeTest();
        }

        @java.lang.Override
        public LineRangeTest implementation() {
            return this.implementation;
        }
    }
}
