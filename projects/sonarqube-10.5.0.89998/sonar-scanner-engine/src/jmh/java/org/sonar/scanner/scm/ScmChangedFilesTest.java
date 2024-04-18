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
package org.sonar.scanner.scm;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.sonar.scm.git.ChangedFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ScmChangedFilesTest {

    private ScmChangedFiles scmChangedFiles;

    @Test
    public void testGetter() {
        Path filePath = Paths.get("files");
        ChangedFile file = ChangedFile.of(filePath);
        Set<ChangedFile> files = Set.of(file);
        scmChangedFiles = new ScmChangedFiles(files);
        assertThat(scmChangedFiles.get()).containsOnly(file);
    }

    @Test
    public void testNullable() {
        scmChangedFiles = new ScmChangedFiles(null);
        assertThat(scmChangedFiles.isValid()).isFalse();
        assertThat(scmChangedFiles.get()).isNull();
        assertThatThrownBy(() -> scmChangedFiles.isChanged(Paths.get("files2"))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testConfirm() {
        Path filePath = Paths.get("files");
        Set<ChangedFile> files = Set.of(ChangedFile.of(filePath));
        scmChangedFiles = new ScmChangedFiles(files);
        assertThat(scmChangedFiles.isValid()).isTrue();
        assertThat(scmChangedFiles.isChanged(Paths.get("files"))).isTrue();
        assertThat(scmChangedFiles.isChanged(Paths.get("files2"))).isFalse();
    }

    /**
     * On Windows the Path object (WindowsPath) is the same for file.js and for File.js. We just want to make sure we don't fail in this case.
     */
    @Test
    public void constructor_givenTwoSimiliarFileNames_shouldNotThrowException() {
        ChangedFile first = ChangedFile.of(Path.of("file.js"));
        ChangedFile second = ChangedFile.of(Path.of("File.js"));
        Set<ChangedFile> changedFiles = Stream.of(first, second).collect(Collectors.toSet());
        assertThatNoException().isThrownBy(() -> new ScmChangedFiles(changedFiles));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testGetter() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testGetter, this.description("testGetter"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testNullable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testNullable, this.description("testNullable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testConfirm() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testConfirm, this.description("testConfirm"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_constructor_givenTwoSimiliarFileNames_shouldNotThrowException() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::constructor_givenTwoSimiliarFileNames_shouldNotThrowException, this.description("constructor_givenTwoSimiliarFileNames_shouldNotThrowException"));
        }

        private ScmChangedFilesTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ScmChangedFilesTest();
        }

        @java.lang.Override
        public ScmChangedFilesTest implementation() {
            return this.implementation;
        }
    }
}
