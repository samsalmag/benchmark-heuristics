/*
 * SonarQube
 * Copyright (C) 2009-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.scanner.scan.filesystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.notifications.AnalysisWarnings;
import org.sonar.api.utils.PathUtils;
import org.sonar.scanner.issue.ignore.IgnoreIssuesFilter;
import org.sonar.scanner.issue.ignore.pattern.IssueExclusionPatternInitializer;
import org.sonar.scanner.issue.ignore.scanner.IssueExclusionsLoader;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetadataGeneratorTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private StatusDetection statusDetection = mock(StatusDetection.class);

    private MetadataGenerator generator;

    @Before
    public void setUp() {
        FileMetadata metadata = new FileMetadata(mock(AnalysisWarnings.class));
        IssueExclusionsLoader issueExclusionsLoader = new IssueExclusionsLoader(mock(IssueExclusionPatternInitializer.class), mock(IgnoreIssuesFilter.class), mock(AnalysisWarnings.class));
        generator = new MetadataGenerator(statusDetection, metadata, issueExclusionsLoader);
    }

    @Test
    public void should_detect_charset_from_BOM() {
        Path basedir = Paths.get("src/test/resources/org/sonar/scanner/scan/filesystem/");
        assertThat(createInputFileWithMetadata(basedir.resolve("without_BOM.txt")).charset()).isEqualTo(StandardCharsets.US_ASCII);
        assertThat(createInputFileWithMetadata(basedir.resolve("UTF-8.txt")).charset()).isEqualTo(StandardCharsets.UTF_8);
        assertThat(createInputFileWithMetadata(basedir.resolve("UTF-16BE.txt")).charset()).isEqualTo(StandardCharsets.UTF_16BE);
        assertThat(createInputFileWithMetadata(basedir.resolve("UTF-16LE.txt")).charset()).isEqualTo(StandardCharsets.UTF_16LE);
        assertThat(createInputFileWithMetadata(basedir.resolve("UTF-32BE.txt")).charset()).isEqualTo(MetadataGenerator.UTF_32BE);
        assertThat(createInputFileWithMetadata(basedir.resolve("UTF-32LE.txt")).charset()).isEqualTo(MetadataGenerator.UTF_32LE);
    }

    private DefaultInputFile createInputFileWithMetadata(Path filePath) {
        return createInputFileWithMetadata(filePath.getParent(), filePath.getFileName().toString());
    }

    private DefaultInputFile createInputFileWithMetadata(Path baseDir, String relativePath) {
        DefaultInputFile inputFile = new TestInputFileBuilder("struts", relativePath).setModuleBaseDir(baseDir).build();
        generator.setMetadata("module", inputFile, StandardCharsets.US_ASCII);
        return inputFile;
    }

    @Test
    public void start_with_bom() throws Exception {
        Path tempFile = temp.newFile().toPath();
        FileUtils.write(tempFile.toFile(), "\uFEFFfoo\nbar\r\nbaz", StandardCharsets.UTF_8, true);
        DefaultInputFile inputFile = createInputFileWithMetadata(tempFile);
        assertThat(inputFile.lines()).isEqualTo(3);
        assertThat(inputFile.nonBlankLines()).isEqualTo(3);
        assertThat(inputFile.md5Hash()).isEqualTo(md5Hex("foo\nbar\nbaz"));
        assertThat(inputFile.originalLineStartOffsets()).containsOnly(0, 4, 9);
        assertThat(inputFile.originalLineEndOffsets()).containsOnly(3, 7, 12);
    }

    @Test
    public void use_default_charset_if_detection_fails() throws IOException {
        Path tempFile = temp.newFile().toPath();
        byte invalidWindows1252 = (byte) 129;
        byte[] b = { (byte) 0xDF, (byte) 0xFF, (byte) 0xFF, invalidWindows1252 };
        FileUtils.writeByteArrayToFile(tempFile.toFile(), b);
        DefaultInputFile inputFile = createInputFileWithMetadata(tempFile);
        assertThat(inputFile.charset()).isEqualTo(StandardCharsets.US_ASCII);
    }

    @Test
    public void non_existing_file_should_throw_exception() {
        try {
            createInputFileWithMetadata(Paths.get(""), "non_existing");
            Assert.fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).endsWith("Unable to read file " + Paths.get("").resolve("non_existing").toAbsolutePath());
            assertThat(e.getCause()).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    public void complete_input_file() throws Exception {
        // file system
        Path baseDir = temp.newFolder().toPath().toRealPath(LinkOption.NOFOLLOW_LINKS);
        Path srcFile = baseDir.resolve("src/main/java/foo/Bar.java");
        FileUtils.touch(srcFile.toFile());
        FileUtils.write(srcFile.toFile(), "single line");
        // status
        DefaultInputFile inputFile = createInputFileWithMetadata(baseDir, "src/main/java/foo/Bar.java");
        when(statusDetection.status("foo", inputFile, "6c1d64c0b3555892fe7273e954f6fb5a")).thenReturn(InputFile.Status.ADDED);
        assertThat(inputFile.type()).isEqualTo(InputFile.Type.MAIN);
        assertThat(inputFile.file()).isEqualTo(srcFile.toFile());
        assertThat(inputFile.absolutePath()).isEqualTo(PathUtils.sanitize(srcFile.toAbsolutePath().toString()));
        assertThat(inputFile.key()).isEqualTo("struts:src/main/java/foo/Bar.java");
        assertThat(inputFile.relativePath()).isEqualTo("src/main/java/foo/Bar.java");
        assertThat(inputFile.lines()).isOne();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_detect_charset_from_BOM() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_detect_charset_from_BOM, this.description("should_detect_charset_from_BOM"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_start_with_bom() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::start_with_bom, this.description("start_with_bom"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_use_default_charset_if_detection_fails() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::use_default_charset_if_detection_fails, this.description("use_default_charset_if_detection_fails"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_non_existing_file_should_throw_exception() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::non_existing_file_should_throw_exception, this.description("non_existing_file_should_throw_exception"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_complete_input_file() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::complete_input_file, this.description("complete_input_file"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().temp, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private MetadataGeneratorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MetadataGeneratorTest();
        }

        @java.lang.Override
        public MetadataGeneratorTest implementation() {
            return this.implementation;
        }
    }
}
