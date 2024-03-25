package io.github.azagniotov.stubby4j.caching;

import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import org.junit.Test;
import java.util.Optional;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

public class NoOpStubHttpLifecycleCacheTest {

    @Test
    public void putIfAbsentAndGet() {
        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(true);
        assertThat(cache.size().get()).isEqualTo(0);
        final StubHttpLifecycle stubHttpLifeCycle = new StubHttpLifecycle.Builder().build();
        final String targetKey = "/some/url";
        cache.putIfAbsent(targetKey, stubHttpLifeCycle);
        assertThat(cache.size().get()).isEqualTo(0);
        assertThat(cache.get(targetKey)).isEqualTo(Optional.empty());
    }

    @Test
    public void clearByKey() {
        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(true);
        final StubHttpLifecycle stubHttpLifeCycle = new StubHttpLifecycle.Builder().build();
        final String targetKey = "/some/url";
        assertThat(cache.clearByKey(targetKey)).isTrue();
        cache.putIfAbsent(targetKey, stubHttpLifeCycle);
        assertThat(cache.get(targetKey)).isEqualTo(Optional.empty());
        assertThat(cache.clearByKey(targetKey)).isTrue();
    }

    @Test
    public void cache() {
        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(true);
        assertThrows(UnsupportedOperationException.class, cache::cache);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_putIfAbsentAndGet() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::putIfAbsentAndGet, this.description("putIfAbsentAndGet"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearByKey() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::clearByKey, this.description("clearByKey"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cache() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cache, this.description("cache"));
        }

        private NoOpStubHttpLifecycleCacheTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new NoOpStubHttpLifecycleCacheTest();
        }

        @java.lang.Override
        public NoOpStubHttpLifecycleCacheTest implementation() {
            return this.implementation;
        }
    }
}
