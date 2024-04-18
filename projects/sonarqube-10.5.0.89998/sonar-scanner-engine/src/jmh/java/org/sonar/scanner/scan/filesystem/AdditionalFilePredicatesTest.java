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
package org.sonar.scanner.scan.filesystem;

import org.junit.Test;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import static org.assertj.core.api.Assertions.assertThat;

public class AdditionalFilePredicatesTest {

    @Test
    public void key() {
        FilePredicate predicate = new AdditionalFilePredicates.KeyPredicate("struts:Action.java");
        InputFile inputFile = new TestInputFileBuilder("struts", "Action.java").build();
        assertThat(predicate.apply(inputFile)).isTrue();
        inputFile = new TestInputFileBuilder("struts", "Filter.java").build();
        assertThat(predicate.apply(inputFile)).isFalse();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_key() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::key, this.description("key"));
        }

        private AdditionalFilePredicatesTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new AdditionalFilePredicatesTest();
        }

        @java.lang.Override
        public AdditionalFilePredicatesTest implementation() {
            return this.implementation;
        }
    }
}
