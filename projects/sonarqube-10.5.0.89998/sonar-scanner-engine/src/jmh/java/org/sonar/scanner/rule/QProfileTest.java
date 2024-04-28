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
package org.sonar.scanner.rule;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class QProfileTest {

    @Test
    public void testEquals() {
        QProfile q1 = new QProfile("k1", "name1", null, null);
        QProfile q2 = new QProfile("k1", "name2", null, null);
        QProfile q3 = new QProfile("k3", "name3", null, null);
        assertThat(q1).isEqualTo(q2).isNotEqualTo(q3).isNotNull().isNotEqualTo("str");
        assertThat(q2).isNotEqualTo(q3);
        assertThat(q1).hasSameHashCodeAs(q2);
        assertThat(q1.hashCode()).isNotEqualTo(q3.hashCode());
        assertThat(q2.hashCode()).isNotEqualTo(q3.hashCode());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testEquals() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testEquals, this.description("testEquals"));
        }

        private QProfileTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new QProfileTest();
        }

        @java.lang.Override
        public QProfileTest implementation() {
            return this.implementation;
        }
    }
}
