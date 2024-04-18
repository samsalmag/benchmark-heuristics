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
package org.sonar.scanner.scan;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultInputModule;
import org.sonar.api.batch.fs.internal.DefaultInputProject;
import org.sonar.scanner.fs.InputModuleHierarchy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkDirectoriesInitializerTest {

    private final WorkDirectoriesInitializer initializer = new WorkDirectoriesInitializer();

    private final InputModuleHierarchy hierarchy = mock(InputModuleHierarchy.class);

    private final DefaultInputProject project = mock(DefaultInputProject.class);

    private final DefaultInputModule root = mock(DefaultInputModule.class);

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private File rootWorkDir;

    private File lock;

    @Before
    public void setUp() throws IOException {
        rootWorkDir = temp.newFolder();
        when(hierarchy.root()).thenReturn(root);
        createFilesToClean(rootWorkDir);
        when(root.getWorkDir()).thenReturn(rootWorkDir.toPath());
        when(project.getWorkDir()).thenReturn(rootWorkDir.toPath());
    }

    private void createFilesToClean(File dir) throws IOException {
        new File(dir, "foo.txt").createNewFile();
        File newFolder = new File(dir, "foo");
        newFolder.mkdir();
        File fileInFolder = new File(newFolder, "test");
        fileInFolder.createNewFile();
        lock = new File(dir, DirectoryLock.LOCK_FILE_NAME);
        lock.createNewFile();
        assertThat(dir.list()).hasSizeGreaterThan(1);
    }

    @Test
    public void execute_doesnt_fail_if_nothing_to_clean() {
        temp.delete();
        initializer.execute(hierarchy);
    }

    @Test
    public void execute_should_clean_root() {
        initializer.execute(project);
        assertThat(rootWorkDir).exists();
        assertThat(lock).exists();
        assertThat(rootWorkDir.list()).containsOnly(DirectoryLock.LOCK_FILE_NAME);
    }

    @Test
    public void execute_on_hierarchy_should_clean_submodules() throws IOException {
        DefaultInputModule moduleA = mock(DefaultInputModule.class);
        DefaultInputModule moduleB = mock(DefaultInputModule.class);
        when(hierarchy.children(root)).thenReturn(Arrays.asList(moduleA));
        when(hierarchy.children(moduleA)).thenReturn(Arrays.asList(moduleB));
        File moduleAWorkdir = new File(rootWorkDir, "moduleA");
        File moduleBWorkdir = new File(moduleAWorkdir, "moduleB");
        when(moduleA.getWorkDir()).thenReturn(moduleAWorkdir.toPath());
        when(moduleB.getWorkDir()).thenReturn(moduleBWorkdir.toPath());
        moduleAWorkdir.mkdir();
        moduleBWorkdir.mkdir();
        new File(moduleAWorkdir, "fooA.txt").createNewFile();
        new File(moduleBWorkdir, "fooB.txt").createNewFile();
        initializer.execute(project);
        initializer.execute(hierarchy);
        assertThat(rootWorkDir).exists();
        assertThat(lock).exists();
        assertThat(rootWorkDir.list()).containsOnly(DirectoryLock.LOCK_FILE_NAME, "moduleA");
        assertThat(moduleAWorkdir).exists();
        assertThat(moduleBWorkdir).exists();
        assertThat(moduleAWorkdir.list()).containsOnly("moduleB");
        assertThat(moduleBWorkdir).isEmptyDirectory();
    }

    @Test
    public void execute_on_hierarchy_should_clean_submodules_expect_submodule_with_same_work_directory_as_root() throws IOException {
        DefaultInputModule moduleA = mock(DefaultInputModule.class);
        DefaultInputModule moduleB = mock(DefaultInputModule.class);
        when(hierarchy.children(root)).thenReturn(List.of(moduleA, moduleB));
        File rootAndModuleAWorkdir = new File(rootWorkDir, "moduleA");
        File moduleBWorkdir = new File(rootAndModuleAWorkdir, "../moduleB");
        when(root.getWorkDir()).thenReturn(rootAndModuleAWorkdir.toPath());
        when(moduleA.getWorkDir()).thenReturn(rootAndModuleAWorkdir.toPath());
        when(moduleB.getWorkDir()).thenReturn(moduleBWorkdir.toPath());
        rootAndModuleAWorkdir.mkdir();
        createFilesToClean(rootAndModuleAWorkdir);
        moduleBWorkdir.mkdir();
        new File(rootAndModuleAWorkdir, "fooA.txt").createNewFile();
        new File(moduleBWorkdir, "fooB.txt").createNewFile();
        initializer.execute(hierarchy);
        assertThat(rootWorkDir).exists();
        assertThat(lock).exists();
        assertThat(rootAndModuleAWorkdir).exists();
        assertThat(rootAndModuleAWorkdir.list()).containsOnly(DirectoryLock.LOCK_FILE_NAME, "fooA.txt", "foo", "foo.txt");
        assertThat(moduleBWorkdir).exists().isEmptyDirectory();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_execute_doesnt_fail_if_nothing_to_clean() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::execute_doesnt_fail_if_nothing_to_clean, this.description("execute_doesnt_fail_if_nothing_to_clean"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_execute_should_clean_root() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::execute_should_clean_root, this.description("execute_should_clean_root"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_execute_on_hierarchy_should_clean_submodules() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::execute_on_hierarchy_should_clean_submodules, this.description("execute_on_hierarchy_should_clean_submodules"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_execute_on_hierarchy_should_clean_submodules_expect_submodule_with_same_work_directory_as_root() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::execute_on_hierarchy_should_clean_submodules_expect_submodule_with_same_work_directory_as_root, this.description("execute_on_hierarchy_should_clean_submodules_expect_submodule_with_same_work_directory_as_root"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().temp, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private WorkDirectoriesInitializerTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new WorkDirectoriesInitializerTest();
        }

        @java.lang.Override
        public WorkDirectoriesInitializerTest implementation() {
            return this.implementation;
        }
    }
}
