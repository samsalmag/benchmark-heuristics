package io.github.azagniotov.stubby4j.handlers.strategy;

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.DefaultResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.NotFoundResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.RedirectResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.UnauthorizedResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.junit.Test;
import static com.google.common.truth.Truth.assertThat;

public class HandlingStrategyFactoryTest {

    @Test
    public void shouldIdentifyResponseStrategyForDefaultResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.okResponse();
        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(DefaultResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForNotFoundResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.notFoundResponse();
        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(NotFoundResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForUnauthorizedResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.unauthorizedResponse();
        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(UnauthorizedResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode301() throws Exception {
        final StubResponse stubResponse = new StubResponse.Builder().withHttpStatusCode(Code.MOVED_PERMANENTLY).build();
        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode302() throws Exception {
        final StubResponse stubResponse = new StubResponse.Builder().withHttpStatusCode(Code.MOVED_TEMPORARILY).build();
        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode302_Found() throws Exception {
        final StubResponse stubResponse = new StubResponse.Builder().withHttpStatusCode(Code.FOUND).build();
        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode303() throws Exception {
        final StubResponse stubResponse = new StubResponse.Builder().withHttpStatusCode(Code.SEE_OTHER).build();
        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode307() throws Exception {
        final StubResponse stubResponse = new StubResponse.Builder().withHttpStatusCode(Code.TEMPORARY_REDIRECT).build();
        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode308() throws Exception {
        final StubResponse stubResponse = new StubResponse.Builder().withHttpStatusCode(Code.PERMANENT_REDIRECT).build();
        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldIdentifyResponseStrategyForDefaultResponse() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldIdentifyResponseStrategyForDefaultResponse, this.description("shouldIdentifyResponseStrategyForDefaultResponse"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldIdentifyResponseStrategyForNotFoundResponse() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldIdentifyResponseStrategyForNotFoundResponse, this.description("shouldIdentifyResponseStrategyForNotFoundResponse"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldIdentifyResponseStrategyForUnauthorizedResponse() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldIdentifyResponseStrategyForUnauthorizedResponse, this.description("shouldIdentifyResponseStrategyForUnauthorizedResponse"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode301() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode301, this.description("shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode301"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode302() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode302, this.description("shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode302"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode302_Found() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode302_Found, this.description("shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode302_Found"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode303() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode303, this.description("shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode303"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode307() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode307, this.description("shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode307"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode308() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode308, this.description("shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode308"));
        }

        private HandlingStrategyFactoryTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new HandlingStrategyFactoryTest();
        }

        @java.lang.Override
        public HandlingStrategyFactoryTest implementation() {
            return this.implementation;
        }
    }
}
