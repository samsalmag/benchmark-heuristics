package io.github.azagniotov.stubby4j.caching;

import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import org.junit.Test;
import java.util.Optional;
import static com.google.common.truth.Truth.assertThat;

public class CacheTest {

    @Test
    public void shouldBuildNoOpCache() throws Exception {
        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(true);
        assertThat(cache).isInstanceOf(NoOpStubHttpLifecycleCache.class);
    }

    @Test
    public void shouldBuildDefaultCache() throws Exception {
        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(false);
        assertThat(cache).isInstanceOf(StubHttpLifecycleCache.class);
    }

    @Test
    public void shouldClearCacheByKey() throws Exception {
        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(false);
        final StubHttpLifecycle stubHttpLifeCycle = new StubHttpLifecycle.Builder().build();
        final String targetKey = "/some/url";
        cache.putIfAbsent(targetKey, stubHttpLifeCycle);
        assertThat(cache.size().get()).isEqualTo(1);
        assertThat(cache.get(targetKey)).isEqualTo(Optional.of(stubHttpLifeCycle));
        assertThat(cache.clearByKey(targetKey)).isTrue();
        assertThat(cache.size().get()).isEqualTo(0);
        assertThat(cache.get(targetKey)).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldNotClearCacheByKey() throws Exception {
        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(false);
        final StubHttpLifecycle stubHttpLifeCycle = new StubHttpLifecycle.Builder().build();
        final String targetHashCodeKey = "-124354548";
        cache.putIfAbsent(targetHashCodeKey, stubHttpLifeCycle);
        assertThat(cache.size().get()).isEqualTo(1);
        assertThat(cache.get(targetHashCodeKey)).isEqualTo(Optional.of(stubHttpLifeCycle));
        assertThat(cache.clearByKey("99999")).isFalse();
        assertThat(cache.size().get()).isEqualTo(1);
        assertThat(cache.get(targetHashCodeKey)).isEqualTo(Optional.of(stubHttpLifeCycle));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldBuildNoOpCache() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldBuildNoOpCache, this.description("shouldBuildNoOpCache"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldBuildDefaultCache() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldBuildDefaultCache, this.description("shouldBuildDefaultCache"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldClearCacheByKey() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldClearCacheByKey, this.description("shouldClearCacheByKey"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotClearCacheByKey() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotClearCacheByKey, this.description("shouldNotClearCacheByKey"));
        }

        private CacheTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CacheTest();
        }

        @java.lang.Override
        public CacheTest implementation() {
            return this.implementation;
        }
    }
}
