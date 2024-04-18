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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CharsetDetectorTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void should_detect_charset_from_BOM() {
        Path basedir = Paths.get("src/test/resources/org/sonar/scanner/scan/filesystem/");
        assertThat(detectCharset(basedir.resolve("without_BOM.txt"), US_ASCII)).isEqualTo(US_ASCII);
        assertThat(detectCharset(basedir.resolve("UTF-8.txt"), US_ASCII)).isEqualTo(UTF_8);
        assertThat(detectCharset(basedir.resolve("UTF-16BE.txt"), US_ASCII)).isEqualTo(UTF_16BE);
        assertThat(detectCharset(basedir.resolve("UTF-16LE.txt"), US_ASCII)).isEqualTo(UTF_16LE);
        assertThat(detectCharset(basedir.resolve("UTF-32BE.txt"), US_ASCII)).isEqualTo(MetadataGenerator.UTF_32BE);
        assertThat(detectCharset(basedir.resolve("UTF-32LE.txt"), US_ASCII)).isEqualTo(MetadataGenerator.UTF_32LE);
    }

    @Test
    public void should_read_files_from_BOM() throws IOException {
        Path basedir = Paths.get("src/test/resources/org/sonar/scanner/scan/filesystem/");
        assertThat(readFile(basedir.resolve("without_BOM.txt"), US_ASCII)).isEqualTo("without BOM");
        assertThat(readFile(basedir.resolve("UTF-8.txt"), US_ASCII)).isEqualTo("UTF-8");
        assertThat(readFile(basedir.resolve("UTF-16BE.txt"), US_ASCII)).isEqualTo("UTF-16BE");
        assertThat(readFile(basedir.resolve("UTF-16LE.txt"), US_ASCII)).isEqualTo("UTF-16LE");
        assertThat(readFile(basedir.resolve("UTF-32BE.txt"), US_ASCII)).isEqualTo("UTF-32BE");
        assertThat(readFile(basedir.resolve("UTF-32LE.txt"), US_ASCII)).isEqualTo("UTF-32LE");
    }

    @Test
    public void always_try_utf8() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // this is a valid 2 byte UTF-8.
        out.write(194);
        out.write(128);
        Path filePath = temp.newFile().toPath();
        Files.write(filePath, out.toByteArray());
        assertThat(detectCharset(filePath, UTF_16)).isEqualTo(UTF_8);
    }

    @Test
    public void fail_if_file_doesnt_exist() {
        assertThatThrownBy(() -> detectCharset(Paths.get("non_existing"), UTF_8)).isInstanceOf(IllegalStateException.class).hasMessage("Unable to read file " + Paths.get("non_existing").toAbsolutePath());
    }

    @Test
    public void no_encoding_found() throws IOException {
        Path filePath = temp.newFile().toPath();
        byte[] b = new byte[4096];
        new Random().nextBytes(b);
        // avoid accidental BOM matching
        b[0] = 1;
        // avoid UTF-8 / UTF-16
        b[100] = 0;
        b[101] = 0;
        b[102] = 0;
        b[103] = 0;
        // invalid in win-1258
        b[200] = (byte) 129;
        Files.write(filePath, b);
        CharsetDetector detector = new CharsetDetector(filePath, UTF_8);
        assertThat(detector.run()).isFalse();
        assertThat(detector.charset()).isNull();
    }

    private String readFile(Path file, Charset defaultEncoding) throws IOException {
        CharsetDetector detector = new CharsetDetector(file, defaultEncoding);
        assertThat(detector.run()).isTrue();
        List<String> readLines = IOUtils.readLines(new InputStreamReader(detector.inputStream(), detector.charset()));
        return StringUtils.join(readLines, "\n");
    }

    private Charset detectCharset(Path file, Charset defaultEncoding) {
        CharsetDetector detector = new CharsetDetector(file, defaultEncoding);
        assertThat(detector.run()).isTrue();
        return detector.charset();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_detect_charset_from_BOM() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_detect_charset_from_BOM, this.description("should_detect_charset_from_BOM"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_read_files_from_BOM() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_read_files_from_BOM, this.description("should_read_files_from_BOM"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_always_try_utf8() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::always_try_utf8, this.description("always_try_utf8"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fail_if_file_doesnt_exist() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fail_if_file_doesnt_exist, this.description("fail_if_file_doesnt_exist"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_no_encoding_found() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::no_encoding_found, this.description("no_encoding_found"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().temp, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private CharsetDetectorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CharsetDetectorTest();
        }

        @java.lang.Override
        public CharsetDetectorTest implementation() {
            return this.implementation;
        }
    }
}
