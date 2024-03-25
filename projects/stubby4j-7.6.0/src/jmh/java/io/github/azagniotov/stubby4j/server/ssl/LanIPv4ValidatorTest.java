package io.github.azagniotov.stubby4j.server.ssl;

import org.junit.Test;
import static com.google.common.truth.Truth.assertThat;

public class LanIPv4ValidatorTest {

    @Test
    public void isPrivateIp() throws Exception {
        assertThat(LanIPv4Validator.isPrivateIp("10.0.0.1")).isTrue();
        assertThat(LanIPv4Validator.isPrivateIp("10.255.255.255")).isTrue();
        assertThat(LanIPv4Validator.isPrivateIp("192.168.0.1")).isTrue();
        assertThat(LanIPv4Validator.isPrivateIp("192.168.255.255")).isTrue();
        assertThat(LanIPv4Validator.isPrivateIp("172.16.0.0")).isTrue();
        assertThat(LanIPv4Validator.isPrivateIp("172.16.0.1")).isTrue();
        assertThat(LanIPv4Validator.isPrivateIp("172.31.255.255")).isTrue();
    }

    @Test
    public void isNotPrivateIp() throws Exception {
        assertThat(LanIPv4Validator.isPrivateIp("9.0.0.1")).isFalse();
        assertThat(LanIPv4Validator.isPrivateIp("10.255.255.256")).isFalse();
        assertThat(LanIPv4Validator.isPrivateIp("192.169.0.1")).isFalse();
        assertThat(LanIPv4Validator.isPrivateIp("192.167.255.255")).isFalse();
        assertThat(LanIPv4Validator.isPrivateIp("172.15.0.0")).isFalse();
        assertThat(LanIPv4Validator.isPrivateIp("172.15.0.1")).isFalse();
        assertThat(LanIPv4Validator.isPrivateIp("172.32.255.255")).isFalse();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isPrivateIp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isPrivateIp, this.description("isPrivateIp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isNotPrivateIp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isNotPrivateIp, this.description("isNotPrivateIp"));
        }

        private LanIPv4ValidatorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new LanIPv4ValidatorTest();
        }

        @java.lang.Override
        public LanIPv4ValidatorTest implementation() {
            return this.implementation;
        }
    }
}
