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
package org.sonar.scanner.phases;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.sonar.scanner.bootstrap.PostJobExtensionDictionary;
import org.sonar.scanner.postjob.PostJobWrapper;
import org.sonar.scanner.postjob.PostJobsExecutor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PostJobsExecutorTest {

    private PostJobsExecutor executor;

    private PostJobExtensionDictionary selector = mock(PostJobExtensionDictionary.class);

    private PostJobWrapper job1 = mock(PostJobWrapper.class);

    private PostJobWrapper job2 = mock(PostJobWrapper.class);

    @Before
    public void setUp() {
        executor = new PostJobsExecutor(selector);
    }

    @Test
    public void should_execute_post_jobs() {
        when(selector.selectPostJobs()).thenReturn(Arrays.asList(job1, job2));
        when(job1.shouldExecute()).thenReturn(true);
        executor.execute();
        verify(job1).shouldExecute();
        verify(job2).shouldExecute();
        verify(job1).execute();
        verifyNoMoreInteractions(job1, job2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_execute_post_jobs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_execute_post_jobs, this.description("should_execute_post_jobs"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        private PostJobsExecutorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new PostJobsExecutorTest();
        }

        @java.lang.Override
        public PostJobsExecutorTest implementation() {
            return this.implementation;
        }
    }
}
