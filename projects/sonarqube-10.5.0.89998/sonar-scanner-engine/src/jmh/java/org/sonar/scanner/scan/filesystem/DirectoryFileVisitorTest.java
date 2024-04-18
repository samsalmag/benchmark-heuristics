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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.commons.lang3.SystemUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputModule;
import org.sonar.scanner.fs.InputModuleHierarchy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class DirectoryFileVisitorTest {

    @ClassRule
    public static TemporaryFolder temp = new TemporaryFolder();

    private final DefaultInputModule module = mock();

    private final ModuleExclusionFilters moduleExclusionFilters = mock();

    private final InputModuleHierarchy inputModuleHierarchy = mock();

    private final InputFile.Type type = mock();

    @Test
    public void visit_hidden_file() throws IOException {
        DirectoryFileVisitor.FileVisitAction action = mock(DirectoryFileVisitor.FileVisitAction.class);
        File hidden = temp.newFile(".hidden");
        if (SystemUtils.IS_OS_WINDOWS) {
            Files.setAttribute(hidden.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
        }
        DirectoryFileVisitor underTest = new DirectoryFileVisitor(action, module, moduleExclusionFilters, inputModuleHierarchy, type);
        underTest.visitFile(hidden.toPath(), Files.readAttributes(hidden.toPath(), BasicFileAttributes.class));
        verify(action, never()).execute(any(Path.class));
    }

    @Test
    public void test_visit_file_failed_generic_io_exception() throws IOException {
        DirectoryFileVisitor.FileVisitAction action = mock(DirectoryFileVisitor.FileVisitAction.class);
        File file = temp.newFile("failed");
        DirectoryFileVisitor underTest = new DirectoryFileVisitor(action, module, moduleExclusionFilters, inputModuleHierarchy, type);
        assertThrows(IOException.class, () -> underTest.visitFileFailed(file.toPath(), new IOException()));
    }

    @Test
    public void test_visit_file_failed_file_system_loop_exception() throws IOException {
        DirectoryFileVisitor.FileVisitAction action = mock(DirectoryFileVisitor.FileVisitAction.class);
        File file = temp.newFile("symlink");
        DirectoryFileVisitor underTest = new DirectoryFileVisitor(action, module, moduleExclusionFilters, inputModuleHierarchy, type);
        FileVisitResult result = underTest.visitFileFailed(file.toPath(), new FileSystemLoopException(file.getPath()));
        assertThat(result).isEqualTo(FileVisitResult.CONTINUE);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_visit_hidden_file() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::visit_hidden_file, this.description("visit_hidden_file"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_visit_file_failed_generic_io_exception() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_visit_file_failed_generic_io_exception, this.description("test_visit_file_failed_generic_io_exception"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_visit_file_failed_file_system_loop_exception() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_visit_file_failed_file_system_loop_exception, this.description("test_visit_file_failed_file_system_loop_exception"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyClassRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(DirectoryFileVisitorTest.temp, statement, description);
            statement = super.applyClassRuleFields(statement, description);
            return statement;
        }

        private DirectoryFileVisitorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DirectoryFileVisitorTest();
        }

        @java.lang.Override
        public DirectoryFileVisitorTest implementation() {
            return this.implementation;
        }
    }
}
