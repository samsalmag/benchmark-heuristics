package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import org.eclipse.jetty.server.Request;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractHandlerExtensionTest {

    @Mock
    private Request mockBaseRequest;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private HttpServletResponse mockHttpServletResponse;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ANSITerminal.muteConsole(true);
    }

    @Test
    public void shouldDetermineRequestAsHandledWhenBaseRequestHandled() throws Exception {
        AbstractHandlerExtension abstractHandlerExtension = new DummyHandler();
        when(mockBaseRequest.isHandled()).thenReturn(true);
        final boolean isHandled = abstractHandlerExtension.logAndCheckIsHandled("someName", mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);
        assertThat(isHandled).isTrue();
    }

    @Test
    public void shouldDetermineRequestAsHandledWhenResponseCommitted() throws Exception {
        AbstractHandlerExtension abstractHandlerExtension = new DummyHandler();
        when(mockBaseRequest.isHandled()).thenReturn(false);
        when(mockHttpServletResponse.isCommitted()).thenReturn(true);
        final boolean isHandled = abstractHandlerExtension.logAndCheckIsHandled("someName", mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);
        assertThat(isHandled).isTrue();
    }

    @Test
    public void shouldDetermineRequestAsNotHandled() throws Exception {
        AbstractHandlerExtension abstractHandlerExtension = new DummyHandler();
        when(mockBaseRequest.isHandled()).thenReturn(false);
        when(mockHttpServletResponse.isCommitted()).thenReturn(false);
        final boolean isHandled = abstractHandlerExtension.logAndCheckIsHandled("someName", mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);
        assertThat(isHandled).isFalse();
    }

    private class DummyHandler implements AbstractHandlerExtension {
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDetermineRequestAsHandledWhenBaseRequestHandled() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDetermineRequestAsHandledWhenBaseRequestHandled, this.description("shouldDetermineRequestAsHandledWhenBaseRequestHandled"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDetermineRequestAsHandledWhenResponseCommitted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDetermineRequestAsHandledWhenResponseCommitted, this.description("shouldDetermineRequestAsHandledWhenResponseCommitted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDetermineRequestAsNotHandled() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDetermineRequestAsNotHandled, this.description("shouldDetermineRequestAsNotHandled"));
        }

        @java.lang.Override
        public void beforeClass() throws java.lang.Throwable {
            super.beforeClass();
            AbstractHandlerExtensionTest.beforeClass();
        }

        private AbstractHandlerExtensionTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new AbstractHandlerExtensionTest();
        }

        @java.lang.Override
        public AbstractHandlerExtensionTest implementation() {
            return this.implementation;
        }
    }
}
