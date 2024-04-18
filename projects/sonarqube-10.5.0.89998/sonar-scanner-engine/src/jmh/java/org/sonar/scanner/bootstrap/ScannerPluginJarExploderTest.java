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
package org.sonar.scanner.bootstrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.core.platform.ExplodedPlugin;
import org.sonar.core.platform.PluginInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScannerPluginJarExploderTest {

    @ClassRule
    public static TemporaryFolder temp = new TemporaryFolder();

    private File tempDir;

    private ScannerPluginJarExploder underTest;

    @Before
    public void setUp() throws IOException {
        tempDir = temp.newFolder();
        PluginFiles pluginFiles = mock(PluginFiles.class);
        when(pluginFiles.createTempDir()).thenReturn(tempDir);
        underTest = new ScannerPluginJarExploder(pluginFiles);
    }

    @Test
    public void copy_and_extract_libs() throws IOException {
        File jar = loadFile("sonar-checkstyle-plugin-2.8.jar");
        ExplodedPlugin exploded = underTest.explode(PluginInfo.create(jar));
        assertThat(exploded.getKey()).isEqualTo("checkstyle");
        assertThat(exploded.getMain()).isFile().exists();
        assertThat(exploded.getLibs()).extracting(File::getName).containsExactlyInAnyOrder("antlr-2.7.6.jar", "checkstyle-5.1.jar", "commons-cli-1.0.jar");
        assertThat(new File(jar.getParent(), "sonar-checkstyle-plugin-2.8.jar")).exists();
        assertThat(new File(jar.getParent(), "sonar-checkstyle-plugin-2.8.jar_unzip/META-INF/lib/checkstyle-5.1.jar")).exists();
    }

    @Test
    public void extract_only_libs() throws IOException {
        File jar = loadFile("sonar-checkstyle-plugin-2.8.jar");
        underTest.explode(PluginInfo.create(jar));
        assertThat(new File(jar.getParent(), "sonar-checkstyle-plugin-2.8.jar")).exists();
        assertThat(new File(jar.getParent(), "sonar-checkstyle-plugin-2.8.jar_unzip/META-INF/MANIFEST.MF")).doesNotExist();
        assertThat(new File(jar.getParent(), "sonar-checkstyle-plugin-2.8.jar_unzip/org/sonar/plugins/checkstyle/CheckstyleVersion.class")).doesNotExist();
    }

    @Test
    public void retry_on_locked_file() throws IOException {
        File jar = loadFile("sonar-checkstyle-plugin-2.8.jar");
        File lockFile = new File(jar.getParentFile(), jar.getName() + "_unzip.lock");
        try (FileOutputStream out = new FileOutputStream(lockFile)) {
            FileLock lock = out.getChannel().lock();
            try {
                PluginInfo pluginInfo = PluginInfo.create(jar);
                assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> underTest.explode(pluginInfo)).withMessage("Fail to open plugin [checkstyle]: " + jar).withCauseExactlyInstanceOf(IOException.class);
            } finally {
                lock.release();
            }
        }
    }

    private File loadFile(String filename) throws IOException {
        File src = FileUtils.toFile(getClass().getResource(getClass().getSimpleName() + "/" + filename));
        File dest = new File(temp.newFolder(), filename);
        FileUtils.copyFile(src, dest);
        return dest;
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_copy_and_extract_libs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::copy_and_extract_libs, this.description("copy_and_extract_libs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_extract_only_libs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::extract_only_libs, this.description("extract_only_libs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retry_on_locked_file() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::retry_on_locked_file, this.description("retry_on_locked_file"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyClassRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(ScannerPluginJarExploderTest.temp, statement, description);
            statement = super.applyClassRuleFields(statement, description);
            return statement;
        }

        private ScannerPluginJarExploderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ScannerPluginJarExploderTest();
        }

        @java.lang.Override
        public ScannerPluginJarExploderTest implementation() {
            return this.implementation;
        }
    }
}
