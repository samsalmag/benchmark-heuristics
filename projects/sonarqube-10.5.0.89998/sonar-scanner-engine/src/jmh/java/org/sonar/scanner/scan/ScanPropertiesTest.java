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
package org.sonar.scanner.scan;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultInputProject;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.MessageException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScanPropertiesTest {

    private final MapSettings settings = new MapSettings();

    private final DefaultInputProject project = mock(DefaultInputProject.class);

    private final ScanProperties underTest = new ScanProperties(settings.asConfig(), project);

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        when(project.getBaseDir()).thenReturn(temp.newFolder().toPath());
        when(project.getWorkDir()).thenReturn(temp.newFolder().toPath());
    }

    @Test
    public void defaults_if_no_setting_defined() {
        assertThat(underTest.branch()).isEmpty();
        assertThat(underTest.preloadFileMetadata()).isFalse();
        assertThat(underTest.shouldKeepReport()).isFalse();
        assertThat(underTest.metadataFilePath()).isEqualTo(project.getWorkDir().resolve("report-task.txt"));
        underTest.validate();
    }

    @Test
    public void should_define_branch_name() {
        settings.setProperty("sonar.branch.name", "name");
        assertThat(underTest.branch()).isEqualTo(Optional.of("name"));
    }

    @Test
    public void should_define_report_publish_timeout() {
        assertThat(underTest.reportPublishTimeout()).isEqualTo(60);
        settings.setProperty("sonar.ws.report.timeout", "10");
        assertThat(underTest.reportPublishTimeout()).isEqualTo(10);
    }

    @Test
    public void should_define_preload_file_metadata() {
        settings.setProperty("sonar.preloadFileMetadata", "true");
        assertThat(underTest.preloadFileMetadata()).isTrue();
    }

    @Test
    public void should_define_keep_report() {
        settings.setProperty("sonar.scanner.keepReport", "true");
        assertThat(underTest.shouldKeepReport()).isTrue();
    }

    @Test
    public void should_define_metadata_file_path() throws IOException {
        Path path = temp.newFolder().toPath().resolve("report");
        settings.setProperty("sonar.scanner.metadataFilePath", path.toString());
        assertThat(underTest.metadataFilePath()).isEqualTo(path);
    }

    @Test
    public void validate_fails_if_metadata_file_location_is_not_absolute() {
        settings.setProperty("sonar.scanner.metadataFilePath", "relative");
        assertThatThrownBy(() -> underTest.validate()).isInstanceOf(MessageException.class).hasMessage("Property 'sonar.scanner.metadataFilePath' must point to an absolute path: relative");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defaults_if_no_setting_defined() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::defaults_if_no_setting_defined, this.description("defaults_if_no_setting_defined"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_define_branch_name() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_define_branch_name, this.description("should_define_branch_name"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_define_report_publish_timeout() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_define_report_publish_timeout, this.description("should_define_report_publish_timeout"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_define_preload_file_metadata() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_define_preload_file_metadata, this.description("should_define_preload_file_metadata"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_define_keep_report() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_define_keep_report, this.description("should_define_keep_report"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_define_metadata_file_path() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_define_metadata_file_path, this.description("should_define_metadata_file_path"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_validate_fails_if_metadata_file_location_is_not_absolute() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::validate_fails_if_metadata_file_location_is_not_absolute, this.description("validate_fails_if_metadata_file_location_is_not_absolute"));
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

        private ScanPropertiesTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ScanPropertiesTest();
        }

        @java.lang.Override
        public ScanPropertiesTest implementation() {
            return this.implementation;
        }
    }
}
