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
package org.sonar.scanner.repository;

import java.util.Map;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class MultiModuleProjectRepositoryTest {

    private MultiModuleProjectRepository repository;

    @Before
    public void setUp() {
        SingleProjectRepository repository1 = new SingleProjectRepository(Maps.newHashMap("/Abc.java", new FileData("123", "456")));
        SingleProjectRepository repository2 = new SingleProjectRepository(Maps.newHashMap("/Def.java", new FileData("567", "321")));
        Map<String, SingleProjectRepository> moduleRepositories = Maps.newHashMap("module1", repository1);
        moduleRepositories.put("module2", repository2);
        repository = new MultiModuleProjectRepository(moduleRepositories);
    }

    @Test
    public void test_file_data_when_module_and_file_exist() {
        FileData fileData = repository.fileData("module2", "/Def.java");
        assertNotNull(fileData);
        assertThat(fileData.hash()).isEqualTo("567");
        assertThat(fileData.revision()).isEqualTo("321");
    }

    @Test
    public void test_file_data_when_module_does_not_exist() {
        FileData fileData = repository.fileData("unknown", "/Def.java");
        assertThat(fileData).isNull();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_file_data_when_module_and_file_exist() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_file_data_when_module_and_file_exist, this.description("test_file_data_when_module_and_file_exist"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_file_data_when_module_does_not_exist() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_file_data_when_module_does_not_exist, this.description("test_file_data_when_module_does_not_exist"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        private MultiModuleProjectRepositoryTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MultiModuleProjectRepositoryTest();
        }

        @java.lang.Override
        public MultiModuleProjectRepositoryTest implementation() {
            return this.implementation;
        }
    }
}
