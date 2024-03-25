package io.github.azagniotov.stubby4j.utils;

import org.junit.Test;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import static com.google.common.truth.Truth.assertThat;

public class DateTimeUtilsTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ").withZone(ZoneOffset.systemDefault());

    @Test
    public void systemDefault() throws Exception {
        final String localNow = DATE_TIME_FORMATTER.format(Instant.now());
        final String systemDefault = DateTimeUtils.systemDefault();
        assertThat(localNow).isEqualTo(systemDefault);
    }

    @Test
    public void systemDefaultFromMillis() throws Exception {
        final long currentTimeMillis = System.currentTimeMillis();
        final String localNow = DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(currentTimeMillis));
        final String systemDefault = DateTimeUtils.systemDefault(currentTimeMillis);
        assertThat(localNow).isEqualTo(systemDefault);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_systemDefault() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::systemDefault, this.description("systemDefault"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_systemDefaultFromMillis() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::systemDefaultFromMillis, this.description("systemDefaultFromMillis"));
        }

        private DateTimeUtilsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DateTimeUtilsTest();
        }

        @java.lang.Override
        public DateTimeUtilsTest implementation() {
            return this.implementation;
        }
    }
}
