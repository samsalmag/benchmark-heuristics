package io.github.azagniotov.stubby4j.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.io.IOException;

public class FileUtilsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldNotConvertFileToBytesWhenBadFilenameGiven() throws Exception {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Could not load file from path: bad/file/path");
        FileUtils.binaryFileToBytes(".", "bad/file/path");
    }

    @Test
    public void shouldNotLoadFileFromURWhenBadFilenameGiven() throws Exception {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Could not load file from path: bad/file/path");
        FileUtils.uriToFile("bad/file/path");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotConvertFileToBytesWhenBadFilenameGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotConvertFileToBytesWhenBadFilenameGiven, this.description("shouldNotConvertFileToBytesWhenBadFilenameGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotLoadFileFromURWhenBadFilenameGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotLoadFileFromURWhenBadFilenameGiven, this.description("shouldNotLoadFileFromURWhenBadFilenameGiven"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().expectedException, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private FileUtilsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FileUtilsTest();
        }

        @java.lang.Override
        public FileUtilsTest implementation() {
            return this.implementation;
        }
    }
}
