package io.github.azagniotov.stubby4j.utils;

import com.google.api.client.http.HttpMethods;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static com.google.common.truth.Truth.assertThat;

public class ReflectionUtilsTest {

    private StubRequest.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new StubRequest.Builder();
    }

    @Test
    public void shouldGetObjectPropertiesAndValues() throws Exception {
        final StubRequest stubRequest = builder.withMethod(HttpMethods.POST).build();
        final Map<String, String> properties = ReflectionUtils.getProperties(stubRequest);
        assertThat("[POST]").isEqualTo(properties.get("method"));
        assertThat(properties.get("url")).isNull();
        assertThat(properties.get("post")).isNull();
        assertThat(properties.get("headers")).isNull();
    }

    @Test
    public void shouldSetValueOnObjectProperty_WhenCorrectPropertyNameGiven() throws Exception {
        final StubRequest stubRequest = builder.build();
        assertThat(stubRequest.getUrl()).isNull();
        final Map<String, Object> values = new HashMap<>();
        values.put("url", "google.com");
        ReflectionUtils.injectObjectFields(stubRequest, values);
        assertThat(stubRequest.getUrl()).isEqualTo("google.com");
    }

    @Test
    public void shouldNotSetValueOnObjectProperty_WhenIncorrectPropertyNameGiven() throws Exception {
        final StubRequest stubRequest = builder.build();
        assertThat(stubRequest.getUrl()).isNull();
        final Map<String, Object> values = new HashMap<>();
        values.put("nonExistentProperty", "google.com");
        ReflectionUtils.injectObjectFields(stubRequest, values);
        assertThat(stubRequest.getUrl()).isNull();
    }

    @Test
    public void shouldReturnNullWhenClassHasNoDeclaredMethods() throws Exception {
        final Object result = ReflectionUtils.getPropertyValue(new MethodLessInterface() {
        }, "somePropertyName");
        assertThat(result).isNull();
    }

    @Test
    public void shouldReturnPropertyValueWhenClassHasDeclaredMethods() throws Exception {
        final String expectedMethodValue = "cheburashka";
        final Object result = ReflectionUtils.getPropertyValue(new MethodFulInterface() {

            @Override
            public String getName() {
                return expectedMethodValue;
            }
        }, "name");
        assertThat(result).isEqualTo(expectedMethodValue);
    }

    private interface MethodLessInterface {
    }

    private interface MethodFulInterface {

        String getName();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldGetObjectPropertiesAndValues() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldGetObjectPropertiesAndValues, this.description("shouldGetObjectPropertiesAndValues"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldSetValueOnObjectProperty_WhenCorrectPropertyNameGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldSetValueOnObjectProperty_WhenCorrectPropertyNameGiven, this.description("shouldSetValueOnObjectProperty_WhenCorrectPropertyNameGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotSetValueOnObjectProperty_WhenIncorrectPropertyNameGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotSetValueOnObjectProperty_WhenIncorrectPropertyNameGiven, this.description("shouldNotSetValueOnObjectProperty_WhenIncorrectPropertyNameGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnNullWhenClassHasNoDeclaredMethods() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnNullWhenClassHasNoDeclaredMethods, this.description("shouldReturnNullWhenClassHasNoDeclaredMethods"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnPropertyValueWhenClassHasDeclaredMethods() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnPropertyValueWhenClassHasDeclaredMethods, this.description("shouldReturnPropertyValueWhenClassHasDeclaredMethods"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        private ReflectionUtilsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ReflectionUtilsTest();
        }

        @java.lang.Override
        public ReflectionUtilsTest implementation() {
            return this.implementation;
        }
    }
}
