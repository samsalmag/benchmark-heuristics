package io.github.azagniotov.stubby4j.handlers.strategy;

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.DefaultResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.NotFoundResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StubsResponseHandlingStrategyFactoryTest {

    private static final byte[] EMPTY_BYTES = {};

    @Mock
    private StubResponse mockStubResponse;

    @Test
    public void shouldReturnNotFoundResponseHandlingStrategyWhen404ResponseHasNoBody() throws Exception {
        when(mockStubResponse.getHttpStatusCode()).thenReturn(HttpStatus.Code.NOT_FOUND);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(EMPTY_BYTES);
        StubResponseHandlingStrategy handlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(mockStubResponse);
        assertThat(handlingStrategy).isInstanceOf(NotFoundResponseHandlingStrategy.class);
    }

    @Test
    public void shouldReturnDefaultResponseHandlingStrategyWhen404ResponseHasNoBody() throws Exception {
        when(mockStubResponse.getHttpStatusCode()).thenReturn(HttpStatus.Code.NOT_FOUND);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn("something".getBytes());
        StubResponseHandlingStrategy handlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(mockStubResponse);
        assertThat(handlingStrategy).isInstanceOf(DefaultResponseHandlingStrategy.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnNotFoundResponseHandlingStrategyWhen404ResponseHasNoBody() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnNotFoundResponseHandlingStrategyWhen404ResponseHasNoBody, this.description("shouldReturnNotFoundResponseHandlingStrategyWhen404ResponseHasNoBody"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnDefaultResponseHandlingStrategyWhen404ResponseHasNoBody() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnDefaultResponseHandlingStrategyWhen404ResponseHasNoBody, this.description("shouldReturnDefaultResponseHandlingStrategyWhen404ResponseHasNoBody"));
        }

        private StubsResponseHandlingStrategyFactoryTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new StubsResponseHandlingStrategyFactoryTest();
        }

        @java.lang.Override
        public StubsResponseHandlingStrategyFactoryTest implementation() {
            return this.implementation;
        }
    }
}
