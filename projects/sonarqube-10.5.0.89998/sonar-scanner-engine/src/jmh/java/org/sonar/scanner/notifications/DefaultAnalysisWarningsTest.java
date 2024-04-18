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
package org.sonar.scanner.notifications;

import org.junit.Test;
import org.sonar.api.utils.System2;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultAnalysisWarningsTest {

    private final System2 system2 = mock(System2.class);

    private final DefaultAnalysisWarnings underTest = new DefaultAnalysisWarnings(system2);

    @Test
    public void addUnique_adds_messages_with_timestamp() {
        String warning1 = "dummy warning 1";
        long timestamp1 = 1L;
        String warning2 = "dummy warning 2";
        long timestamp2 = 2L;
        when(system2.now()).thenReturn(timestamp1).thenReturn(timestamp2);
        underTest.addUnique(warning1);
        underTest.addUnique(warning2);
        assertThat(underTest.warnings()).extracting(DefaultAnalysisWarnings.Message::getText, DefaultAnalysisWarnings.Message::getTimestamp).containsExactly(tuple(warning1, timestamp1), tuple(warning2, timestamp2));
    }

    @Test
    public void addUnique_adds_same_message_once() {
        String warning = "dummy warning";
        underTest.addUnique(warning);
        underTest.addUnique(warning);
        assertThat(underTest.warnings()).extracting(DefaultAnalysisWarnings.Message::getText).isEqualTo(singletonList(warning));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addUnique_fails_with_IAE_when_message_is_empty() {
        underTest.addUnique("");
    }

    @Test
    public void addUnique_preserves_order_and_takes_first_unique_item() {
        String warning1 = "dummy warning 1";
        long timestamp1 = 1L;
        String warning2 = "dummy warning 2";
        long timestamp2 = 2L;
        String warning3 = "dummy warning 3";
        long timestamp3 = 3L;
        when(system2.now()).thenReturn(timestamp1).thenReturn(timestamp2).thenReturn(timestamp3);
        underTest.addUnique(warning1);
        underTest.addUnique(warning2);
        underTest.addUnique(warning3);
        underTest.addUnique(warning2);
        assertThat(underTest.warnings()).extracting(DefaultAnalysisWarnings.Message::getText, DefaultAnalysisWarnings.Message::getTimestamp).containsExactly(tuple(warning1, timestamp1), tuple(warning2, timestamp2), tuple(warning3, timestamp3));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addUnique_adds_messages_with_timestamp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addUnique_adds_messages_with_timestamp, this.description("addUnique_adds_messages_with_timestamp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addUnique_adds_same_message_once() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addUnique_adds_same_message_once, this.description("addUnique_adds_same_message_once"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addUnique_fails_with_IAE_when_message_is_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::addUnique_fails_with_IAE_when_message_is_empty, this.description("addUnique_fails_with_IAE_when_message_is_empty"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addUnique_preserves_order_and_takes_first_unique_item() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::addUnique_preserves_order_and_takes_first_unique_item, this.description("addUnique_preserves_order_and_takes_first_unique_item"));
        }

        private DefaultAnalysisWarningsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DefaultAnalysisWarningsTest();
        }

        @java.lang.Override
        public DefaultAnalysisWarningsTest implementation() {
            return this.implementation;
        }
    }
}
