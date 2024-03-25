package io.github.azagniotov.stubby4j.stubs;

import org.eclipse.jetty.http.HttpStatus.Code;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StubHttpLifecycleBuilderTest {

    private static final String SOME_RESOURCE_URI = "/some/resource/uri";

    private static final String AUTHORIZATION_HEADER_BASIC = "Basic Ym9iOnNlY3JldA==";

    private static final String AUTHORIZATION_HEADER_BASIC_INVALID = "Basic 888888888888==";

    private static final String AUTHORIZATION_HEADER_BEARER = "Bearer Ym9iOnNlY3JldA==";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private StubRequest.Builder requestBuilder;

    private StubResponse.Builder responseBuilder;

    private StubHttpLifecycle.Builder httpCycleBuilder;

    @Before
    public void setUp() throws Exception {
        requestBuilder = new StubRequest.Builder();
        responseBuilder = new StubResponse.Builder();
        httpCycleBuilder = new StubHttpLifecycle.Builder();
    }

    @Test
    public void shouldFindStubHttpLifecycleEqual_WhenComparedToItself() throws Exception {
        final StubHttpLifecycle expectedStubHttpLifecycle = httpCycleBuilder.build();
        final boolean assertionResult = expectedStubHttpLifecycle.equals(expectedStubHttpLifecycle);
        assertThat(assertionResult).isTrue();
    }

    @Test
    public void shouldFindStubHttpLifecycleNotEqual_WhenComparedToDifferentInstanceClass() throws Exception {
        final StubHttpLifecycle expectedStubHttpLifecycle = httpCycleBuilder.build();
        final Object assertingObject = StubResponse.okResponse();
        final boolean assertionResult = expectedStubHttpLifecycle.equals(assertingObject);
        assertThat(assertionResult).isFalse();
    }

    @Test
    public void shouldReturnStubResponse_WhenNoSequenceResponses() throws Exception {
        final StubResponse stubResponse = responseBuilder.withHttpStatusCode(Code.CREATED).withBody("SELF").build();
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withResponse(stubResponse).build();
        assertThat(stubHttpLifecycle.getResponse(true)).isEqualTo(stubResponse);
    }

    @Test
    public void shouldReturnDescription_WhenDescription() {
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withDescription("wibble").build();
        assertThat(stubHttpLifecycle.getDescription()).isEqualTo("wibble");
    }

    @Test
    public void shouldThrow_WhenResponseObjectIsNotStubResponseType() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Trying to set response of the wrong type");
        httpCycleBuilder.withResponse(8).build();
    }

    @Test
    public void shouldThrow_WhenResponseObjectIsNotCollectionType() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Trying to set response of the wrong type");
        httpCycleBuilder.withResponse(new HashMap<>()).build();
    }

    @Test
    public void shouldReturnDefaultStubResponse_WhenNoSequenceResponsePresentInTheList() throws Exception {
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withResponse(new LinkedList<>()).build();
        final StubResponse actualStubbedResponse = stubHttpLifecycle.getResponse(true);
        assertThat(actualStubbedResponse.getHttpStatusCode()).isEqualTo(Code.OK);
        assertThat(actualStubbedResponse.getBody()).isEmpty();
    }

    @Test
    public void shouldReturnSequenceResponse_WhenOneSequenceResponsePresent() throws Exception {
        final StubResponse stubResponse = responseBuilder.withHttpStatusCode(Code.CREATED).withBody("SELF").build();
        final Code expectedStatus = Code.OK;
        final String expectedBody = "This is a sequence response #1";
        final List<StubResponse> sequence = new LinkedList<StubResponse>() {

            {
                add(responseBuilder.withHttpStatusCode(expectedStatus).withBody(expectedBody).build());
            }
        };
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withResponse(sequence).build();
        final StubResponse actualStubbedResponse = stubHttpLifecycle.getResponse(true);
        assertThat(actualStubbedResponse).isNotEqualTo(stubResponse);
        assertThat(actualStubbedResponse.getHttpStatusCode()).isEqualTo(expectedStatus);
        assertThat(actualStubbedResponse.getBody()).isEqualTo(expectedBody);
        assertThat(stubHttpLifecycle.getNextSequencedResponseId()).isEqualTo(0);
    }

    @Test
    public void shouldReturnSecondSequenceResponseAfterSecondCall_WhenTwoSequenceResponsePresent() throws Exception {
        final StubResponse stubResponse = responseBuilder.withHttpStatusCode(Code.CREATED).withBody("SELF").build();
        final Code expectedStatus = Code.INTERNAL_SERVER_ERROR;
        final String expectedBody = "This is a sequence response #2";
        final List<StubResponse> sequence = new LinkedList<StubResponse>() {

            {
                add(responseBuilder.withHttpStatusCode(Code.OK).withBody("This is a sequence response #1").build());
                add(responseBuilder.withHttpStatusCode(expectedStatus).withBody(expectedBody).build());
            }
        };
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withResponse(sequence).build();
        // Do not remove this stubbing, even if this is an unused variable
        final StubResponse irrelevantStubbedResponse = stubHttpLifecycle.getResponse(true);
        final StubResponse actualStubbedResponse = stubHttpLifecycle.getResponse(true);
        assertThat(actualStubbedResponse).isNotEqualTo(stubResponse);
        assertThat(actualStubbedResponse.getHttpStatusCode()).isEqualTo(expectedStatus);
        assertThat(actualStubbedResponse.getBody()).isEqualTo(expectedBody);
        assertThat(stubHttpLifecycle.getNextSequencedResponseId()).isEqualTo(0);
    }

    @Test
    public void shouldRequireBasicAuthorization() throws Exception {
        final StubRequest stubRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build();
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withRequest(stubRequest).build();
        assertThat(stubHttpLifecycle.isAuthorizationRequired()).isTrue();
    }

    @Test
    public void shouldRequireBearerAuthorization() throws Exception {
        final StubRequest stubRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BEARER).build();
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withRequest(stubRequest).build();
        assertThat(stubHttpLifecycle.isAuthorizationRequired()).isTrue();
    }

    @Test
    public void shouldGetRawBasicAuthorizationHttpHeader() throws Exception {
        final StubRequest stubRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BASIC).build();
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withRequest(stubRequest).build();
        assertThat(AUTHORIZATION_HEADER_BASIC).isEqualTo(stubHttpLifecycle.getRawHeaderAuthorization());
    }

    @Test
    public void shouldGetRawBearerAuthorizationHttpHeader() throws Exception {
        final StubRequest stubRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BEARER).build();
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withRequest(stubRequest).build();
        assertThat(AUTHORIZATION_HEADER_BEARER).isEqualTo(stubHttpLifecycle.getRawHeaderAuthorization());
    }

    @Test
    public void shouldNotAuthorizeViaBasic_WhenAssertingAuthorizationHeaderBasicIsNotSet() throws Exception {
        final StubRequest assertingStubRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).build();
        final StubHttpLifecycle assertingStubHttpLifecycle = httpCycleBuilder.withRequest(assertingStubRequest).build();
        final StubRequest stubbedStubRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build();
        final StubHttpLifecycle stubbedStubHttpLifecycle = httpCycleBuilder.withRequest(stubbedStubRequest).build();
        assertThat(stubbedStubHttpLifecycle.isIncomingRequestUnauthorized(assertingStubHttpLifecycle)).isTrue();
    }

    @Test
    public void shouldNotAuthorizeViaBasic_WhenAuthorizationHeaderBasicIsNotTheSame() throws Exception {
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC_INVALID).build();
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build();
        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());
        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isTrue();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBasicIsNotTheSame() throws Exception {
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC_INVALID).build();
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build();
        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());
        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);
        verify(spyAssertingStubHttpLifecycle).getRawHeaderAuthorization();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedHeaderAuthorization(any(StubbableAuthorizationType.class));
        verify(spyStubbedStubHttpLifecycle, never()).getRawHeaderAuthorization();
        verify(spyStubbedStubHttpLifecycle).getStubbedHeaderAuthorization(StubbableAuthorizationType.BASIC);
    }

    @Test
    public void shouldAuthorizeViaBasic_WhenAuthorizationHeaderBasicEquals() throws Exception {
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BASIC).build();
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build();
        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());
        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isFalse();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBasicEquals() throws Exception {
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BASIC).build();
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build();
        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());
        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);
        verify(spyAssertingStubHttpLifecycle).getRawHeaderAuthorization();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedHeaderAuthorization(any(StubbableAuthorizationType.class));
        verify(spyStubbedStubHttpLifecycle, never()).getRawHeaderAuthorization();
        verify(spyStubbedStubHttpLifecycle).getStubbedHeaderAuthorization(StubbableAuthorizationType.BASIC);
    }

    @Test
    public void shouldNotAuthorizeViaBearer_WhenAssertingAuthorizationHeaderBearerIsNotSet() throws Exception {
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).build();
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build();
        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());
        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isTrue();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBearerIsNotSet() throws Exception {
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).build();
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build();
        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());
        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);
        verify(spyAssertingStubHttpLifecycle).getRawHeaderAuthorization();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedHeaderAuthorization(any(StubbableAuthorizationType.class));
        verify(spyStubbedStubHttpLifecycle, never()).getRawHeaderAuthorization();
        verify(spyStubbedStubHttpLifecycle).getStubbedHeaderAuthorization(StubbableAuthorizationType.BEARER);
    }

    @Test
    public void shouldNotAuthorizeViaBearer_WhenAuthorizationHeaderBearerIsNotTheSame() throws Exception {
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer("Bearer Ym9iOnNlY3JldA==").build();
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, "Bearer 888888888888==").build();
        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());
        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isTrue();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBearerIsNotTheSame() throws Exception {
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer("Bearer Ym9iOnNlY3JldA==").build();
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, "Bearer 888888888888==").build();
        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());
        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);
        verify(spyAssertingStubHttpLifecycle).getRawHeaderAuthorization();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedHeaderAuthorization(any(StubbableAuthorizationType.class));
        verify(spyStubbedStubHttpLifecycle, never()).getRawHeaderAuthorization();
        verify(spyStubbedStubHttpLifecycle).getStubbedHeaderAuthorization(StubbableAuthorizationType.BEARER);
    }

    @Test
    public void shouldAuthorizeViaBearer_WhenAuthorizationHeaderBearerEquals() throws Exception {
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build();
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BEARER).build();
        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());
        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isFalse();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBearerEquals() throws Exception {
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build();
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build();
        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());
        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);
        verify(spyAssertingStubHttpLifecycle).getRawHeaderAuthorization();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedHeaderAuthorization(any(StubbableAuthorizationType.class));
        verify(spyStubbedStubHttpLifecycle, never()).getRawHeaderAuthorization();
        verify(spyStubbedStubHttpLifecycle).getStubbedHeaderAuthorization(StubbableAuthorizationType.BEARER);
    }

    @Test
    public void shouldReturnAjaxResponseContent_WhenStubTypeRequest() throws Exception {
        final String expectedPost = "this is a POST";
        final StubRequest stubRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withPost(expectedPost).build();
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withRequest(stubRequest).build();
        final String actualPost = stubHttpLifecycle.getAjaxResponseContent(StubTypes.REQUEST, "post");
        assertThat(expectedPost).isEqualTo(actualPost);
    }

    @Test
    public void shouldReturnAjaxResponseContent_WhenStubTypeResponse() throws Exception {
        final String expectedBody = "this is a response body";
        final StubResponse stubResponse = responseBuilder.withHttpStatusCode(Code.CREATED).withBody(expectedBody).build();
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withResponse(stubResponse).build();
        final String actualBody = stubHttpLifecycle.getAjaxResponseContent(StubTypes.RESPONSE, "body");
        assertThat(expectedBody).isEqualTo(actualBody);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFindStubHttpLifecycleEqual_WhenComparedToItself() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFindStubHttpLifecycleEqual_WhenComparedToItself, this.description("shouldFindStubHttpLifecycleEqual_WhenComparedToItself"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFindStubHttpLifecycleNotEqual_WhenComparedToDifferentInstanceClass() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFindStubHttpLifecycleNotEqual_WhenComparedToDifferentInstanceClass, this.description("shouldFindStubHttpLifecycleNotEqual_WhenComparedToDifferentInstanceClass"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnStubResponse_WhenNoSequenceResponses() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnStubResponse_WhenNoSequenceResponses, this.description("shouldReturnStubResponse_WhenNoSequenceResponses"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnDescription_WhenDescription() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnDescription_WhenDescription, this.description("shouldReturnDescription_WhenDescription"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldThrow_WhenResponseObjectIsNotStubResponseType() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldThrow_WhenResponseObjectIsNotStubResponseType, this.description("shouldThrow_WhenResponseObjectIsNotStubResponseType"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldThrow_WhenResponseObjectIsNotCollectionType() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldThrow_WhenResponseObjectIsNotCollectionType, this.description("shouldThrow_WhenResponseObjectIsNotCollectionType"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnDefaultStubResponse_WhenNoSequenceResponsePresentInTheList() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnDefaultStubResponse_WhenNoSequenceResponsePresentInTheList, this.description("shouldReturnDefaultStubResponse_WhenNoSequenceResponsePresentInTheList"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnSequenceResponse_WhenOneSequenceResponsePresent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnSequenceResponse_WhenOneSequenceResponsePresent, this.description("shouldReturnSequenceResponse_WhenOneSequenceResponsePresent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnSecondSequenceResponseAfterSecondCall_WhenTwoSequenceResponsePresent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnSecondSequenceResponseAfterSecondCall_WhenTwoSequenceResponsePresent, this.description("shouldReturnSecondSequenceResponseAfterSecondCall_WhenTwoSequenceResponsePresent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldRequireBasicAuthorization() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldRequireBasicAuthorization, this.description("shouldRequireBasicAuthorization"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldRequireBearerAuthorization() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldRequireBearerAuthorization, this.description("shouldRequireBearerAuthorization"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldGetRawBasicAuthorizationHttpHeader() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldGetRawBasicAuthorizationHttpHeader, this.description("shouldGetRawBasicAuthorizationHttpHeader"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldGetRawBearerAuthorizationHttpHeader() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldGetRawBearerAuthorizationHttpHeader, this.description("shouldGetRawBearerAuthorizationHttpHeader"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotAuthorizeViaBasic_WhenAssertingAuthorizationHeaderBasicIsNotSet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotAuthorizeViaBasic_WhenAssertingAuthorizationHeaderBasicIsNotSet, this.description("shouldNotAuthorizeViaBasic_WhenAssertingAuthorizationHeaderBasicIsNotSet"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotAuthorizeViaBasic_WhenAuthorizationHeaderBasicIsNotTheSame() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotAuthorizeViaBasic_WhenAuthorizationHeaderBasicIsNotTheSame, this.description("shouldNotAuthorizeViaBasic_WhenAuthorizationHeaderBasicIsNotTheSame"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyBehaviour_WhenAuthorizationHeaderBasicIsNotTheSame() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyBehaviour_WhenAuthorizationHeaderBasicIsNotTheSame, this.description("shouldVerifyBehaviour_WhenAuthorizationHeaderBasicIsNotTheSame"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldAuthorizeViaBasic_WhenAuthorizationHeaderBasicEquals() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldAuthorizeViaBasic_WhenAuthorizationHeaderBasicEquals, this.description("shouldAuthorizeViaBasic_WhenAuthorizationHeaderBasicEquals"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyBehaviour_WhenAuthorizationHeaderBasicEquals() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyBehaviour_WhenAuthorizationHeaderBasicEquals, this.description("shouldVerifyBehaviour_WhenAuthorizationHeaderBasicEquals"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotAuthorizeViaBearer_WhenAssertingAuthorizationHeaderBearerIsNotSet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotAuthorizeViaBearer_WhenAssertingAuthorizationHeaderBearerIsNotSet, this.description("shouldNotAuthorizeViaBearer_WhenAssertingAuthorizationHeaderBearerIsNotSet"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyBehaviour_WhenAuthorizationHeaderBearerIsNotSet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyBehaviour_WhenAuthorizationHeaderBearerIsNotSet, this.description("shouldVerifyBehaviour_WhenAuthorizationHeaderBearerIsNotSet"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotAuthorizeViaBearer_WhenAuthorizationHeaderBearerIsNotTheSame() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotAuthorizeViaBearer_WhenAuthorizationHeaderBearerIsNotTheSame, this.description("shouldNotAuthorizeViaBearer_WhenAuthorizationHeaderBearerIsNotTheSame"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyBehaviour_WhenAuthorizationHeaderBearerIsNotTheSame() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyBehaviour_WhenAuthorizationHeaderBearerIsNotTheSame, this.description("shouldVerifyBehaviour_WhenAuthorizationHeaderBearerIsNotTheSame"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldAuthorizeViaBearer_WhenAuthorizationHeaderBearerEquals() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldAuthorizeViaBearer_WhenAuthorizationHeaderBearerEquals, this.description("shouldAuthorizeViaBearer_WhenAuthorizationHeaderBearerEquals"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyBehaviour_WhenAuthorizationHeaderBearerEquals() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyBehaviour_WhenAuthorizationHeaderBearerEquals, this.description("shouldVerifyBehaviour_WhenAuthorizationHeaderBearerEquals"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnAjaxResponseContent_WhenStubTypeRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnAjaxResponseContent_WhenStubTypeRequest, this.description("shouldReturnAjaxResponseContent_WhenStubTypeRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnAjaxResponseContent_WhenStubTypeResponse() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnAjaxResponseContent_WhenStubTypeResponse, this.description("shouldReturnAjaxResponseContent_WhenStubTypeResponse"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().expectedException, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private StubHttpLifecycleBuilderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new StubHttpLifecycleBuilderTest();
        }

        @java.lang.Override
        public StubHttpLifecycleBuilderTest implementation() {
            return this.implementation;
        }
    }
}
