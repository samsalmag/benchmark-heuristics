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
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.internal.DefaultInputProject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProjectLockTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private ProjectLock lock;

    private File baseDir;

    private File worDir;

    @Before
    public void setUp() throws IOException {
        baseDir = tempFolder.newFolder();
        worDir = new File(baseDir, ".sonar");
        lock = new ProjectLock(new DefaultInputProject(ProjectDefinition.create().setBaseDir(baseDir).setWorkDir(worDir)));
    }

    @Test
    public void tryLock() {
        Path lockFilePath = worDir.toPath().resolve(DirectoryLock.LOCK_FILE_NAME);
        lock.tryLock();
        assertThat(Files.exists(lockFilePath)).isTrue();
        assertThat(Files.isRegularFile(lockFilePath)).isTrue();
        lock.stop();
        assertThat(Files.exists(lockFilePath)).isTrue();
    }

    @Test
    public void tryLockConcurrently() {
        lock.tryLock();
        assertThatThrownBy(() -> lock.tryLock()).isInstanceOf(IllegalStateException.class).hasMessage("Another SonarQube analysis is already in progress for this project");
    }

    @Test
    public /**
     * If there is an error starting up the scan, we'll still try to unlock even if the lock
     * was never done
     */
    void stopWithoutStarting() {
        lock.stop();
        lock.stop();
    }

    @Test
    public void tryLockTwice() {
        lock.tryLock();
        lock.stop();
        lock.tryLock();
        lock.stop();
    }

    @Test
    public void unLockWithNoLock() {
        lock.stop();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryLock() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryLock, this.description("tryLock"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryLockConcurrently() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryLockConcurrently, this.description("tryLockConcurrently"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stopWithoutStarting() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stopWithoutStarting, this.description("stopWithoutStarting"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryLockTwice() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryLockTwice, this.description("tryLockTwice"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unLockWithNoLock() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unLockWithNoLock, this.description("unLockWithNoLock"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().tempFolder, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private ProjectLockTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ProjectLockTest();
        }

        @java.lang.Override
        public ProjectLockTest implementation() {
            return this.implementation;
        }
    }
}
