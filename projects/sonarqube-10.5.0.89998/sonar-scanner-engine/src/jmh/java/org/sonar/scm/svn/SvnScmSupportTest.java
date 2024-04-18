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
package org.sonar.scm.svn;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.scm.svn.SvnScmSupport.newSvnClientManager;

public class SvnScmSupportTest {

    private SvnConfiguration config = mock(SvnConfiguration.class);

    @Test
    public void getObjects_shouldNotBeEmpty() {
        assertThat(SvnScmSupport.getObjects()).isNotEmpty();
    }

    @Test
    public void newSvnClientManager_whenPasswordConfigured_shouldNotReturnNull() {
        when(config.password()).thenReturn("password");
        when(config.passPhrase()).thenReturn("passPhrase");
        assertThat(newSvnClientManager(config)).isNotNull();
    }

    @Test
    public void newSvnClientManager_whenPasswordNotConfigured_shouldNotReturnNull() {
        assertThat(config.password()).isNull();
        assertThat(config.passPhrase()).isNull();
        assertThat(newSvnClientManager(config)).isNotNull();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getObjects_shouldNotBeEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::getObjects_shouldNotBeEmpty, this.description("getObjects_shouldNotBeEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_newSvnClientManager_whenPasswordConfigured_shouldNotReturnNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::newSvnClientManager_whenPasswordConfigured_shouldNotReturnNull, this.description("newSvnClientManager_whenPasswordConfigured_shouldNotReturnNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_newSvnClientManager_whenPasswordNotConfigured_shouldNotReturnNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::newSvnClientManager_whenPasswordNotConfigured_shouldNotReturnNull, this.description("newSvnClientManager_whenPasswordNotConfigured_shouldNotReturnNull"));
        }

        private SvnScmSupportTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SvnScmSupportTest();
        }

        @java.lang.Override
        public SvnScmSupportTest implementation() {
            return this.implementation;
        }
    }
}
