package io.github.azagniotov.stubby4j.yaml;

import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.google.common.truth.Truth.assertThat;

public class YamlParserTest {

    @Test
    public void isMainYamlHasIncludesFoundAsTrue() {
        final YamlParser yamlParser = new YamlParser();
        final Map<String, List<String>> loadedYamlConfig = new HashMap<>();
        loadedYamlConfig.put("includes", new ArrayList<>());
        assertThat(yamlParser.isMainYamlHasIncludes(loadedYamlConfig)).isTrue();
    }

    @Test
    public void isMainYamlHasIncludesFoundAsFalse() {
        final YamlParser yamlParser = new YamlParser();
        final Map<String, List<String>> loadedYamlConfig = new HashMap<>();
        loadedYamlConfig.put("unexpectedKey", new ArrayList<>());
        assertThat(yamlParser.isMainYamlHasIncludes(loadedYamlConfig)).isFalse();
    }

    @Test
    public void isMainYamlHasIncludesFoundAsFalseWhenNotMap() {
        final YamlParser yamlParser = new YamlParser();
        assertThat(yamlParser.isMainYamlHasIncludes(new ArrayList<>())).isFalse();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isMainYamlHasIncludesFoundAsTrue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isMainYamlHasIncludesFoundAsTrue, this.description("isMainYamlHasIncludesFoundAsTrue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isMainYamlHasIncludesFoundAsFalse() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isMainYamlHasIncludesFoundAsFalse, this.description("isMainYamlHasIncludesFoundAsFalse"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isMainYamlHasIncludesFoundAsFalseWhenNotMap() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isMainYamlHasIncludesFoundAsFalseWhenNotMap, this.description("isMainYamlHasIncludesFoundAsFalseWhenNotMap"));
        }

        private YamlParserTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new YamlParserTest();
        }

        @java.lang.Override
        public YamlParserTest implementation() {
            return this.implementation;
        }
    }
}
