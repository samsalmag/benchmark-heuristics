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
package org.sonar.scanner.scan.branch;

import org.junit.Before;
import org.junit.Test;
import org.sonar.scanner.bootstrap.ScannerProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectBranchesProviderTest {

    private ProjectBranchesProvider provider = new ProjectBranchesProvider();

    private ProjectBranchesLoader mockLoader;

    private ProjectBranches mockBranches;

    private ScannerProperties scannerProperties;

    @Before
    public void setUp() {
        mockLoader = mock(ProjectBranchesLoader.class);
        mockBranches = mock(ProjectBranches.class);
        scannerProperties = mock(ScannerProperties.class);
    }

    @Test
    public void should_use_loader() {
        when(scannerProperties.getProjectKey()).thenReturn("key");
        when(mockLoader.load("key")).thenReturn(mockBranches);
        ProjectBranches branches = provider.provide(mockLoader, scannerProperties);
        assertThat(branches).isSameAs(mockBranches);
    }

    @Test
    public void should_return_default_if_no_loader() {
        ProjectBranches branches = provider.provide(null, scannerProperties);
        assertThat(branches.isEmpty()).isTrue();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_use_loader() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_use_loader, this.description("should_use_loader"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_return_default_if_no_loader() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_return_default_if_no_loader, this.description("should_return_default_if_no_loader"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        private ProjectBranchesProviderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ProjectBranchesProviderTest();
        }

        @java.lang.Override
        public ProjectBranchesProviderTest implementation() {
            return this.implementation;
        }
    }
}
