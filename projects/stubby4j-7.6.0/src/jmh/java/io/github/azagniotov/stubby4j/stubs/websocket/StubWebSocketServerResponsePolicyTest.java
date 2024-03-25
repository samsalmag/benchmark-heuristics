package io.github.azagniotov.stubby4j.stubs.websocket;

import org.junit.Test;
import static com.google.common.truth.Truth.assertThat;

public class StubWebSocketServerResponsePolicyTest {

    @Test
    public void returnsTrueOnKnownProperties() throws Exception {
        // Double negative logic
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("once")).isFalse();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("push")).isFalse();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("fragmentation")).isFalse();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("ping")).isFalse();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("disconnect")).isFalse();
    }

    @Test
    public void returnsFalseOnUnknownProperties() throws Exception {
        // Double negative logic
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("apple")).isTrue();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("")).isTrue();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_returnsTrueOnKnownProperties() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::returnsTrueOnKnownProperties, this.description("returnsTrueOnKnownProperties"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_returnsFalseOnUnknownProperties() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::returnsFalseOnUnknownProperties, this.description("returnsFalseOnUnknownProperties"));
        }

        private StubWebSocketServerResponsePolicyTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new StubWebSocketServerResponsePolicyTest();
        }

        @java.lang.Override
        public StubWebSocketServerResponsePolicyTest implementation() {
            return this.implementation;
        }
    }
}
