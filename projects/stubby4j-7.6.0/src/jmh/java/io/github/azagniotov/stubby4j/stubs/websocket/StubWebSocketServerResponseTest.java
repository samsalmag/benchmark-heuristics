package io.github.azagniotov.stubby4j.stubs.websocket;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketMessageType.TEXT;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.ONCE;
import static io.github.azagniotov.stubby4j.utils.FileUtils.tempFileFromString;
import static io.github.azagniotov.stubby4j.utils.StringUtils.getBytesUtf8;

@RunWith(MockitoJUnitRunner.class)
public class StubWebSocketServerResponseTest {

    private StubWebSocketServerResponse.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new StubWebSocketServerResponse.Builder();
    }

    @Test
    public void stubbedWebSocketServerResponseHasExpectedDefaults() throws Exception {
        final StubWebSocketServerResponse webSocketServerResponse = builder.build();
        assertThat(webSocketServerResponse.getDelay()).isEqualTo(0);
        assertThat(webSocketServerResponse.getPolicy()).isEqualTo(ONCE);
        assertThat(webSocketServerResponse.getMessageType()).isEqualTo(TEXT);
    }

    @Test
    public void returnsBodyAsExpectedBytesWhenOnlyBodyStubbed() throws Exception {
        final StubWebSocketServerResponse socketServerResponse = builder.withBody("OK").build();
        assertThat(socketServerResponse.getBodyAsBytes()).isEqualTo(getBytesUtf8("OK"));
    }

    @Test
    public void returnsBodyAsExpectedBytesWhenOnlyFileStubbed() throws Exception {
        final StubWebSocketServerResponse socketServerResponse = builder.withFile(tempFileFromString("Apple")).build();
        assertThat(socketServerResponse.getBodyAsBytes()).isEqualTo(getBytesUtf8("Apple"));
    }

    @Test
    public void returnsBodyAsExpectedFileBytesWhenBothBodyAndFileStubbed() throws Exception {
        final StubWebSocketServerResponse socketServerResponse = builder.withBody("OK").withFile(tempFileFromString("Banana")).build();
        assertThat(socketServerResponse.getBodyAsBytes()).isEqualTo(getBytesUtf8("Banana"));
    }

    @Test
    public void returnsBodyAsExpectedStringWhenOnlyBodyStubbed() throws Exception {
        final StubWebSocketServerResponse socketServerResponse = builder.withBody("OK").build();
        assertThat(socketServerResponse.getBodyAsString()).isEqualTo("OK");
    }

    @Test
    public void returnsBodyAsExpectedStringWhenOnlyFileStubbed() throws Exception {
        final StubWebSocketServerResponse socketServerResponse = builder.withFile(tempFileFromString("Apple")).build();
        assertThat(socketServerResponse.getBodyAsString()).isEqualTo("Apple");
    }

    @Test
    public void returnsBodyAsExpectedStringWhenBothBodyAndFileStubbed() throws Exception {
        final StubWebSocketServerResponse socketServerResponse = builder.withBody("OK").withFile(tempFileFromString("Banana")).build();
        assertThat(socketServerResponse.getBodyAsString()).isEqualTo("Banana");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedWebSocketServerResponseHasExpectedDefaults() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedWebSocketServerResponseHasExpectedDefaults, this.description("stubbedWebSocketServerResponseHasExpectedDefaults"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_returnsBodyAsExpectedBytesWhenOnlyBodyStubbed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::returnsBodyAsExpectedBytesWhenOnlyBodyStubbed, this.description("returnsBodyAsExpectedBytesWhenOnlyBodyStubbed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_returnsBodyAsExpectedBytesWhenOnlyFileStubbed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::returnsBodyAsExpectedBytesWhenOnlyFileStubbed, this.description("returnsBodyAsExpectedBytesWhenOnlyFileStubbed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_returnsBodyAsExpectedFileBytesWhenBothBodyAndFileStubbed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::returnsBodyAsExpectedFileBytesWhenBothBodyAndFileStubbed, this.description("returnsBodyAsExpectedFileBytesWhenBothBodyAndFileStubbed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_returnsBodyAsExpectedStringWhenOnlyBodyStubbed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::returnsBodyAsExpectedStringWhenOnlyBodyStubbed, this.description("returnsBodyAsExpectedStringWhenOnlyBodyStubbed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_returnsBodyAsExpectedStringWhenOnlyFileStubbed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::returnsBodyAsExpectedStringWhenOnlyFileStubbed, this.description("returnsBodyAsExpectedStringWhenOnlyFileStubbed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_returnsBodyAsExpectedStringWhenBothBodyAndFileStubbed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::returnsBodyAsExpectedStringWhenBothBodyAndFileStubbed, this.description("returnsBodyAsExpectedStringWhenBothBodyAndFileStubbed"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        private StubWebSocketServerResponseTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new StubWebSocketServerResponseTest();
        }

        @java.lang.Override
        public StubWebSocketServerResponseTest implementation() {
            return this.implementation;
        }
    }
}
