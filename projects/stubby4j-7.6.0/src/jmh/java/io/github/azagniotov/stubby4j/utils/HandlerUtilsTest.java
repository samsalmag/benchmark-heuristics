package io.github.azagniotov.stubby4j.utils;

import org.eclipse.jetty.http.HttpScheme;
import org.junit.Test;
import static com.google.common.truth.Truth.assertThat;

public class HandlerUtilsTest {

    private final static int HOURS_IN_DAY = 24;

    private final static int SECONDS_IN_MINUTE = 60;

    private final static int MINUTES_IN_HOUR = 60;

    private final static int MILLISECONDS_IN_SECOND = 1000;

    private final static int MILLISECONDS_IN_MINUTE = SECONDS_IN_MINUTE * MILLISECONDS_IN_SECOND;

    private final static int MILLISECONDS_IN_HOUR = MINUTES_IN_HOUR * SECONDS_IN_MINUTE * MILLISECONDS_IN_SECOND;

    private final static int MILLISECONDS_IN_DAY = HOURS_IN_DAY * MINUTES_IN_HOUR * SECONDS_IN_MINUTE * MILLISECONDS_IN_SECOND;

    @Test
    public void shouldEscapeHtmlEntities() throws Exception {
        final String actualEscaped = StringUtils.escapeHtmlEntities("<xmlElement>8</xmlElement>");
        final String expectedEscaped = "&lt;xmlElement&gt;8&lt;/xmlElement&gt;";
        assertThat(actualEscaped).isEqualTo(expectedEscaped);
    }

    @Test
    public void shouldLinkifyUriString() throws Exception {
        final String actualLinkified = HandlerUtils.linkifyRequestUrl(HttpScheme.HTTP.asString(), "/google/1", "localhost", 8888);
        final String expectedLinkified = "<a target='_blank' href='http://localhost:8888/google/1'>http://localhost:8888/google/1</a>";
        assertThat(actualLinkified).isEqualTo(expectedLinkified);
    }

    @Test
    public void shouldLinkifyUriStringForHttps() throws Exception {
        final String actualLinkified = HandlerUtils.linkifyRequestUrl(HttpScheme.HTTPS.asString(), "/google/1", "localhost", 7443);
        final String expectedLinkified = "<a target='_blank' href='https://localhost:7443/google/1'>https://localhost:7443/google/1</a>";
        assertThat(actualLinkified).isEqualTo(expectedLinkified);
    }

    @Test
    public void shouldLinkifyUriStringWithSingleQuotesInQueryString() throws Exception {
        final String actualLinkified = HandlerUtils.linkifyRequestUrl(HttpScheme.HTTP.asString(), "/entity?client_secret=secret&attributes=['id','uuid']", "localhost", 8882);
        final String expectedLinkified = "<a target='_blank' href='http://localhost:8882/entity?client_secret=secret&attributes=[%27id%27,%27uuid%27]'>http://localhost:8882/entity?client_secret=secret&attributes=['id','uuid']</a>";
        assertThat(actualLinkified).isEqualTo(expectedLinkified);
    }

    @Test
    public void shouldGenerateUpTime_OneDayOneHourOneMinuteOneSecond() throws Exception {
        final long timestamp = MILLISECONDS_IN_DAY + MILLISECONDS_IN_HOUR + MILLISECONDS_IN_MINUTE + MILLISECONDS_IN_SECOND;
        final String actualUpTime = HandlerUtils.calculateStubbyUpTime(timestamp);
        assertThat(actualUpTime).isEqualTo("1 day, 1 hour, 1 min, 1 sec");
    }

    @Test
    public void shouldGenerateUpTime_OneDay23Hours59Minutes59Seconds() throws Exception {
        final long timestamp = MILLISECONDS_IN_DAY + (MILLISECONDS_IN_HOUR * 23) + (MILLISECONDS_IN_MINUTE * 59) + (MILLISECONDS_IN_SECOND * 59);
        final String actualUpTime = HandlerUtils.calculateStubbyUpTime(timestamp);
        assertThat(actualUpTime).isEqualTo("1 day, 23 hours, 59 mins, 59 secs");
    }

    @Test
    public void shouldGenerateUpTime_TwoDaysZeroHoursZeroMinutes1Second() throws Exception {
        final long timestamp = (MILLISECONDS_IN_DAY * 2) + MILLISECONDS_IN_SECOND;
        final String actualUpTime = HandlerUtils.calculateStubbyUpTime(timestamp);
        assertThat(actualUpTime).isEqualTo("2 days, 0 hours, 0 mins, 1 sec");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldEscapeHtmlEntities() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldEscapeHtmlEntities, this.description("shouldEscapeHtmlEntities"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldLinkifyUriString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldLinkifyUriString, this.description("shouldLinkifyUriString"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldLinkifyUriStringForHttps() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldLinkifyUriStringForHttps, this.description("shouldLinkifyUriStringForHttps"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldLinkifyUriStringWithSingleQuotesInQueryString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldLinkifyUriStringWithSingleQuotesInQueryString, this.description("shouldLinkifyUriStringWithSingleQuotesInQueryString"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldGenerateUpTime_OneDayOneHourOneMinuteOneSecond() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldGenerateUpTime_OneDayOneHourOneMinuteOneSecond, this.description("shouldGenerateUpTime_OneDayOneHourOneMinuteOneSecond"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldGenerateUpTime_OneDay23Hours59Minutes59Seconds() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldGenerateUpTime_OneDay23Hours59Minutes59Seconds, this.description("shouldGenerateUpTime_OneDay23Hours59Minutes59Seconds"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldGenerateUpTime_TwoDaysZeroHoursZeroMinutes1Second() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldGenerateUpTime_TwoDaysZeroHoursZeroMinutes1Second, this.description("shouldGenerateUpTime_TwoDaysZeroHoursZeroMinutes1Second"));
        }

        private HandlerUtilsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new HandlerUtilsTest();
        }

        @java.lang.Override
        public HandlerUtilsTest implementation() {
            return this.implementation;
        }
    }
}
