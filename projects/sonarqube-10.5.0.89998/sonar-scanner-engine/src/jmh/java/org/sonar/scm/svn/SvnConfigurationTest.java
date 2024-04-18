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
package org.sonar.scm.svn;

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.System2;
import org.sonar.core.config.SvnProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SvnConfigurationTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void sanityCheck() throws Exception {
        MapSettings settings = new MapSettings(new PropertyDefinitions(System2.INSTANCE));
        SvnConfiguration config = new SvnConfiguration(settings.asConfig());
        assertThat(config.username()).isNull();
        assertThat(config.password()).isNull();
        settings.setProperty(SvnProperties.USER_PROP_KEY, "foo");
        assertThat(config.username()).isEqualTo("foo");
        settings.setProperty(SvnProperties.PASSWORD_PROP_KEY, "pwd");
        assertThat(config.password()).isEqualTo("pwd");
        settings.setProperty(SvnProperties.PASSPHRASE_PROP_KEY, "pass");
        assertThat(config.passPhrase()).isEqualTo("pass");
        assertThat(config.privateKey()).isNull();
        File fakeKey = temp.newFile();
        settings.setProperty(SvnProperties.PRIVATE_KEY_PATH_PROP_KEY, fakeKey.getAbsolutePath());
        assertThat(config.privateKey()).isEqualTo(fakeKey);
        settings.setProperty(SvnProperties.PRIVATE_KEY_PATH_PROP_KEY, "/not/exists");
        try {
            config.privateKey();
            fail("Expected exception");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("Unable to read private key from ");
        }
    }

    @Test
    public void isEmpty_givenNullProperties_returnTrue() {
        MapSettings settings = new MapSettings(new PropertyDefinitions(System2.INSTANCE));
        SvnConfiguration svnConfiguration = new SvnConfiguration(settings.asConfig());
        assertThat(svnConfiguration.isEmpty()).isTrue();
    }

    @Test
    public void isEmpty_givenNotNullProperties_returnFalse() {
        MapSettings settings = new MapSettings(new PropertyDefinitions(System2.INSTANCE));
        settings.setProperty("sonar.svn.username", "bob");
        SvnConfiguration svnConfiguration = new SvnConfiguration(settings.asConfig());
        assertThat(svnConfiguration.isEmpty()).isFalse();
    }

    @Test
    public void isEmpty_givenAllNotNullProperties_returnFalse() {
        MapSettings settings = new MapSettings(new PropertyDefinitions(System2.INSTANCE));
        settings.setProperty("sonar.svn.username", "bob");
        settings.setProperty("sonar.svn.privateKeyPath", "bob");
        settings.setProperty("sonar.svn.passphrase.secured", "bob");
        SvnConfiguration svnConfiguration = new SvnConfiguration(settings.asConfig());
        assertThat(svnConfiguration.isEmpty()).isFalse();
    }

    @Test
    public void isEmpty_givenHalfNotNullProperties_returnFalse() {
        MapSettings settings = new MapSettings(new PropertyDefinitions(System2.INSTANCE));
        settings.setProperty("sonar.svn.password.secured", "bob");
        settings.setProperty("sonar.svn.passphrase.secured", "bob");
        SvnConfiguration svnConfiguration = new SvnConfiguration(settings.asConfig());
        assertThat(svnConfiguration.isEmpty()).isFalse();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sanityCheck() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sanityCheck, this.description("sanityCheck"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmpty_givenNullProperties_returnTrue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isEmpty_givenNullProperties_returnTrue, this.description("isEmpty_givenNullProperties_returnTrue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmpty_givenNotNullProperties_returnFalse() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isEmpty_givenNotNullProperties_returnFalse, this.description("isEmpty_givenNotNullProperties_returnFalse"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmpty_givenAllNotNullProperties_returnFalse() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isEmpty_givenAllNotNullProperties_returnFalse, this.description("isEmpty_givenAllNotNullProperties_returnFalse"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmpty_givenHalfNotNullProperties_returnFalse() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isEmpty_givenHalfNotNullProperties_returnFalse, this.description("isEmpty_givenHalfNotNullProperties_returnFalse"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().temp, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private SvnConfigurationTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SvnConfigurationTest();
        }

        @java.lang.Override
        public SvnConfigurationTest implementation() {
            return this.implementation;
        }
    }
}
