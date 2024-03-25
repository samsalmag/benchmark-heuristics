package io.github.azagniotov.stubby4j.stubs;

import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.util.Optional;
import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.utils.FileUtils.tempFileFromString;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.BODY;

public class StubResponseBuilderTest {

    private StubResponse.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new StubResponse.Builder();
    }

    @Test
    public void shouldStage_WhenConfigurablePropertyAndFieldValuePresent() throws Exception {
        final String expectedFieldValue = "Hello!";
        final String orElse = "Boo!";
        final Optional<Object> fieldValueOptional = Optional.of(expectedFieldValue);
        builder.stage(BODY, fieldValueOptional);
        assertThat(builder.getStaged(String.class, BODY, orElse)).isEqualTo(expectedFieldValue);
    }

    @Test
    public void shouldNotStage_WhenConfigurablePropertyPresentButFieldValueMissing() throws Exception {
        final String orElse = "Boo!";
        final Optional<Object> fieldValueOptional = Optional.ofNullable(null);
        builder.stage(BODY, fieldValueOptional);
        assertThat(builder.getStaged(String.class, BODY, orElse)).isEqualTo(orElse);
    }

    @Test
    public void shouldReturnDefaultHttpStatusCode_WhenStatusFieldNull() throws Exception {
        assertThat(Code.OK).isEqualTo(builder.getHttpStatusCode());
    }

    @Test
    public void shouldReturnRespectiveHttpStatusCode_WhenStatusFieldSet() throws Exception {
        assertThat(Code.CREATED).isEqualTo(builder.withHttpStatusCode(Code.CREATED).getHttpStatusCode());
    }

    @Test
    public void shouldReturnBody_WhenFileIsNull() throws Exception {
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).withBody("this is some body").build();
        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("this is some body").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnBody_WhenFileIsEmpty() throws Exception {
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).withBody("this is some body").withFile(File.createTempFile("tmp", "tmp")).build();
        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("this is some body").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnEmptyBody_WhenFileAndBodyAreNull() throws Exception {
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).build();
        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnEmptyBody_WhenBodyIsEmpty() throws Exception {
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).withBody("").build();
        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnEmptyBody_WhenBodyIsEmpty_AndFileIsEmpty() throws Exception {
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).withBody("").build();
        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnFile_WhenFileNotEmpty_AndRegardlessOfBody() throws Exception {
        final String expectedResponseBody = "content";
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).withBody("something").withFile(tempFileFromString(expectedResponseBody)).build();
        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat(expectedResponseBody).isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldRequireRecording_WhenBodyStartsWithHttp() throws Exception {
        final String expectedResponseBody = "http://someurl.com";
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).withBody(expectedResponseBody).build();
        assertThat(stubResponse.isRecordingRequired()).isTrue();
    }

    @Test
    public void shouldRequireRecording_WhenBodyStartsWithHttpUpperCase() throws Exception {
        final String expectedResponseBody = "HTtP://someurl.com";
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).withBody(expectedResponseBody).build();
        assertThat(stubResponse.isRecordingRequired()).isTrue();
    }

    @Test
    public void shouldNotRequireRecording_WhenBodyStartsWithHtt() throws Exception {
        final String expectedResponseBody = "htt://someurl.com";
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).withBody(expectedResponseBody).build();
        assertThat(stubResponse.isRecordingRequired()).isFalse();
    }

    @Test
    public void shouldNotRequireRecording_WhenBodyDoesnotStartWithHttp() throws Exception {
        final String expectedResponseBody = "some body content";
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).withBody(expectedResponseBody).build();
        assertThat(stubResponse.isRecordingRequired()).isFalse();
    }

    @Test
    public void shouldFindBodyTokenized_WhenBodyContainsTemplateTokens() throws Exception {
        final String body = "some body with a <% token %>";
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).withBody(body).build();
        assertThat(stubResponse.isBodyContainsTemplateTokens()).isTrue();
    }

    @Test
    public void shouldFindBodyNotTokenized_WhenRawFileIsTemplateFile() throws Exception {
        final String body = "some body";
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).withBody(body).withFile(tempFileFromString("file content with a <% token %>")).build();
        assertThat(stubResponse.isBodyContainsTemplateTokens()).isTrue();
    }

    @Test
    public void shouldFindBodyNotTokenized_WhenRawFileNotTemplateFile() throws Exception {
        final String body = "some body";
        final StubResponse stubResponse = builder.withHttpStatusCode(Code.OK).withBody(body).withFile(tempFileFromString("file content")).build();
        assertThat(stubResponse.isBodyContainsTemplateTokens()).isFalse();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldStage_WhenConfigurablePropertyAndFieldValuePresent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldStage_WhenConfigurablePropertyAndFieldValuePresent, this.description("shouldStage_WhenConfigurablePropertyAndFieldValuePresent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotStage_WhenConfigurablePropertyPresentButFieldValueMissing() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotStage_WhenConfigurablePropertyPresentButFieldValueMissing, this.description("shouldNotStage_WhenConfigurablePropertyPresentButFieldValueMissing"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnDefaultHttpStatusCode_WhenStatusFieldNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnDefaultHttpStatusCode_WhenStatusFieldNull, this.description("shouldReturnDefaultHttpStatusCode_WhenStatusFieldNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnRespectiveHttpStatusCode_WhenStatusFieldSet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnRespectiveHttpStatusCode_WhenStatusFieldSet, this.description("shouldReturnRespectiveHttpStatusCode_WhenStatusFieldSet"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnBody_WhenFileIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnBody_WhenFileIsNull, this.description("shouldReturnBody_WhenFileIsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnBody_WhenFileIsEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnBody_WhenFileIsEmpty, this.description("shouldReturnBody_WhenFileIsEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnEmptyBody_WhenFileAndBodyAreNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnEmptyBody_WhenFileAndBodyAreNull, this.description("shouldReturnEmptyBody_WhenFileAndBodyAreNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnEmptyBody_WhenBodyIsEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnEmptyBody_WhenBodyIsEmpty, this.description("shouldReturnEmptyBody_WhenBodyIsEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnEmptyBody_WhenBodyIsEmpty_AndFileIsEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnEmptyBody_WhenBodyIsEmpty_AndFileIsEmpty, this.description("shouldReturnEmptyBody_WhenBodyIsEmpty_AndFileIsEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnFile_WhenFileNotEmpty_AndRegardlessOfBody() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnFile_WhenFileNotEmpty_AndRegardlessOfBody, this.description("shouldReturnFile_WhenFileNotEmpty_AndRegardlessOfBody"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldRequireRecording_WhenBodyStartsWithHttp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldRequireRecording_WhenBodyStartsWithHttp, this.description("shouldRequireRecording_WhenBodyStartsWithHttp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldRequireRecording_WhenBodyStartsWithHttpUpperCase() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldRequireRecording_WhenBodyStartsWithHttpUpperCase, this.description("shouldRequireRecording_WhenBodyStartsWithHttpUpperCase"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotRequireRecording_WhenBodyStartsWithHtt() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotRequireRecording_WhenBodyStartsWithHtt, this.description("shouldNotRequireRecording_WhenBodyStartsWithHtt"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotRequireRecording_WhenBodyDoesnotStartWithHttp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotRequireRecording_WhenBodyDoesnotStartWithHttp, this.description("shouldNotRequireRecording_WhenBodyDoesnotStartWithHttp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFindBodyTokenized_WhenBodyContainsTemplateTokens() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFindBodyTokenized_WhenBodyContainsTemplateTokens, this.description("shouldFindBodyTokenized_WhenBodyContainsTemplateTokens"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFindBodyNotTokenized_WhenRawFileIsTemplateFile() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFindBodyNotTokenized_WhenRawFileIsTemplateFile, this.description("shouldFindBodyNotTokenized_WhenRawFileIsTemplateFile"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFindBodyNotTokenized_WhenRawFileNotTemplateFile() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFindBodyNotTokenized_WhenRawFileNotTemplateFile, this.description("shouldFindBodyNotTokenized_WhenRawFileNotTemplateFile"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        private StubResponseBuilderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new StubResponseBuilderTest();
        }

        @java.lang.Override
        public StubResponseBuilderTest implementation() {
            return this.implementation;
        }
    }
}
