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
package org.sonar.scanner.cache;

import java.util.Optional;
import org.junit.Test;
import org.sonar.api.config.Configuration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.scanner.cache.AnalysisCacheEnabled.PROP_KEY;

public class AnalysisCacheEnabledTest {

    private final Configuration configuration = mock(Configuration.class);

    private final AnalysisCacheEnabled analysisCacheEnabled = new AnalysisCacheEnabled(configuration);

    @Test
    public void enabled_by_default_if_not_pr() {
        assertThat(analysisCacheEnabled.isEnabled()).isTrue();
    }

    @Test
    public void disabled_if_property_set() {
        when(configuration.getBoolean(PROP_KEY)).thenReturn(Optional.of(false));
        assertThat(analysisCacheEnabled.isEnabled()).isFalse();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_enabled_by_default_if_not_pr() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::enabled_by_default_if_not_pr, this.description("enabled_by_default_if_not_pr"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disabled_if_property_set() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disabled_if_property_set, this.description("disabled_if_property_set"));
        }

        private AnalysisCacheEnabledTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new AnalysisCacheEnabledTest();
        }

        @java.lang.Override
        public AnalysisCacheEnabledTest implementation() {
            return this.implementation;
        }
    }
}
