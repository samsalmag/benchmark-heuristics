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
package org.sonar.scanner.bootstrap;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.utils.System2;
import org.sonar.api.testfixtures.log.LogTester;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalConfigurationProviderTest {

    @Rule
    public LogTester logTester = new LogTester();

    GlobalServerSettings globalServerSettings;

    ScannerProperties scannerProps;

    @Before
    public void prepare() {
        globalServerSettings = mock(GlobalServerSettings.class);
        scannerProps = new ScannerProperties(Collections.emptyMap());
    }

    @Test
    public void should_load_global_settings() {
        when(globalServerSettings.properties()).thenReturn(ImmutableMap.of("sonar.cpd.cross", "true"));
        GlobalConfiguration globalConfig = new GlobalConfigurationProvider().provide(globalServerSettings, scannerProps, new PropertyDefinitions(System2.INSTANCE));
        assertThat(globalConfig.get("sonar.cpd.cross")).hasValue("true");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_load_global_settings() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_load_global_settings, this.description("should_load_global_settings"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().prepare();
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().logTester, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private GlobalConfigurationProviderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new GlobalConfigurationProviderTest();
        }

        @java.lang.Override
        public GlobalConfigurationProviderTest implementation() {
            return this.implementation;
        }
    }
}
