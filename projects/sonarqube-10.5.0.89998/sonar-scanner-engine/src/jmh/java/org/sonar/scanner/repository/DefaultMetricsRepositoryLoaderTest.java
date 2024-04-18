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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.sonar.scanner.WsTestUtil;
import org.sonar.scanner.bootstrap.DefaultScannerWsClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DefaultMetricsRepositoryLoaderTest {

    private static final String WS_URL = "/api/metrics/search?ps=500&p=";

    private DefaultScannerWsClient wsClient;

    private DefaultMetricsRepositoryLoader metricsRepositoryLoader;

    @Before
    public void setUp() throws IOException {
        wsClient = mock(DefaultScannerWsClient.class);
        WsTestUtil.mockReader(wsClient, WS_URL + "1", new StringReader(IOUtils.toString(this.getClass().getResourceAsStream("DefaultMetricsRepositoryLoaderTest/page1.json"))));
        WsTestUtil.mockReader(wsClient, WS_URL + "2", new StringReader(IOUtils.toString(this.getClass().getResourceAsStream("DefaultMetricsRepositoryLoaderTest/page2.json"))));
        metricsRepositoryLoader = new DefaultMetricsRepositoryLoader(wsClient);
    }

    @Test
    public void test() {
        MetricsRepository metricsRepository = metricsRepositoryLoader.load();
        assertThat(metricsRepository.metrics()).hasSize(3);
        WsTestUtil.verifyCall(wsClient, WS_URL + "1");
        WsTestUtil.verifyCall(wsClient, WS_URL + "2");
        verifyNoMoreInteractions(wsClient);
    }

    @Test
    public void testIOError() throws IOException {
        Reader reader = mock(Reader.class);
        when(reader.read(any(char[].class), anyInt(), anyInt())).thenThrow(new IOException());
        WsTestUtil.mockReader(wsClient, reader);
        assertThatThrownBy(() -> metricsRepositoryLoader.load()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testCloseError() throws IOException {
        Reader reader = mock(Reader.class);
        when(reader.read(any(char[].class), anyInt(), anyInt())).thenReturn(-1);
        doThrow(new IOException()).when(reader).close();
        WsTestUtil.mockReader(wsClient, reader);
        assertThatThrownBy(() -> metricsRepositoryLoader.load()).isInstanceOf(IllegalStateException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test, this.description("test"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIOError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIOError, this.description("testIOError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testCloseError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testCloseError, this.description("testCloseError"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        private DefaultMetricsRepositoryLoaderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DefaultMetricsRepositoryLoaderTest();
        }

        @java.lang.Override
        public DefaultMetricsRepositoryLoaderTest implementation() {
            return this.implementation;
        }
    }
}
