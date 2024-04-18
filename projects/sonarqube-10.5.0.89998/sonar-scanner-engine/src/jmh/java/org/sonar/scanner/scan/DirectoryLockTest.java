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

import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DirectoryLockTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private DirectoryLock lock;

    @Before
    public void setUp() {
        lock = new DirectoryLock(temp.getRoot().toPath());
    }

    @Test
    public void tryLock() {
        assertThat(temp.getRoot()).isEmptyDirectory();
        lock.tryLock();
        assertThat(temp.getRoot().toPath().resolve(".sonar_lock")).exists();
        lock.unlock();
    }

    @Test
    public void unlockWithoutLock() {
        lock.unlock();
    }

    @Test
    public void errorTryLock() {
        lock = new DirectoryLock(Paths.get("non", "existing", "path"));
        assertThatThrownBy(() -> lock.tryLock()).isInstanceOf(IllegalStateException.class).hasMessageContaining("Failed to create lock");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryLock() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tryLock, this.description("tryLock"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unlockWithoutLock() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unlockWithoutLock, this.description("unlockWithoutLock"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorTryLock() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorTryLock, this.description("errorTryLock"));
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

        private DirectoryLockTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DirectoryLockTest();
        }

        @java.lang.Override
        public DirectoryLockTest implementation() {
            return this.implementation;
        }
    }
}
