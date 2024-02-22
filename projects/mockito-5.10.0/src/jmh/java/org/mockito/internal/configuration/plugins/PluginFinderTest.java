/*
 * Copyright (c) 2017 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.configuration.plugins;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.io.IOUtil;
import org.mockito.plugins.PluginSwitch;
import org.mockitoutil.TestBase;

public class PluginFinderTest extends TestBase {

    @Mock
    PluginSwitch switcher;

    @InjectMocks
    PluginFinder finder;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void empty_resources() {
        assertNull(finder.findPluginClass(Collections.<URL>emptyList()));
    }

    @Test
    public void no_valid_impl() throws Exception {
        File f = tmp.newFile();
        // when
        IOUtil.writeText("  \n  ", f);
        // then
        assertNull(finder.findPluginClass(asList(f.toURI().toURL())));
    }

    @Test
    public void single_implementation() throws Exception {
        File f = tmp.newFile();
        when(switcher.isEnabled("foo.Foo")).thenReturn(true);
        // when
        IOUtil.writeText("  foo.Foo  ", f);
        // then
        assertEquals("foo.Foo", finder.findPluginClass(asList(f.toURI().toURL())));
    }

    @Test
    public void single_implementation_disabled() throws Exception {
        File f = tmp.newFile();
        when(switcher.isEnabled("foo.Foo")).thenReturn(false);
        // when
        IOUtil.writeText("  foo.Foo  ", f);
        // then
        assertEquals(null, finder.findPluginClass(asList(f.toURI().toURL())));
    }

    @Test
    public void multiple_implementations_only_one_enabled() throws Exception {
        File f1 = tmp.newFile();
        File f2 = tmp.newFile();
        when(switcher.isEnabled("Bar")).thenReturn(true);
        // when
        IOUtil.writeText("Foo", f1);
        IOUtil.writeText("Bar", f2);
        // then
        assertEquals("Bar", finder.findPluginClass(asList(f1.toURI().toURL(), f2.toURI().toURL())));
    }

    @Test
    public void multiple_implementations_only_one_useful() throws Exception {
        File f1 = tmp.newFile();
        File f2 = tmp.newFile();
        when(switcher.isEnabled(anyString())).thenReturn(true);
        // when
        IOUtil.writeText("   ", f1);
        IOUtil.writeText("X", f2);
        // then
        assertEquals("X", finder.findPluginClass(asList(f1.toURI().toURL(), f2.toURI().toURL())));
    }

    @Test
    public void multiple_empty_implementations() throws Exception {
        File f1 = tmp.newFile();
        File f2 = tmp.newFile();
        when(switcher.isEnabled(anyString())).thenReturn(true);
        // when
        IOUtil.writeText("   ", f1);
        IOUtil.writeText("\n", f2);
        // then
        assertEquals(null, finder.findPluginClass(asList(f1.toURI().toURL(), f2.toURI().toURL())));
    }

    @Test
    public void problems_loading_impl() throws Exception {
        String fileName = "xxx";
        File f = tmp.newFile(fileName);
        // when
        IOUtil.writeText("Bar", f);
        when(switcher.isEnabled(anyString())).thenThrow(new RuntimeException("Boo!"));
        try {
            // when
            finder.findPluginClass(asList(f.toURI().toURL()));
            // then
            fail();
        } catch (Exception e) {
            assertThat(e).hasMessageContaining(fileName);
            assertThat(e.getCause()).hasMessage("Boo!");
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends org.mockitoutil.TestBase._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty_resources() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty_resources, this.description("empty_resources"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_no_valid_impl() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::no_valid_impl, this.description("no_valid_impl"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_single_implementation() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::single_implementation, this.description("single_implementation"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_single_implementation_disabled() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::single_implementation_disabled, this.description("single_implementation_disabled"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multiple_implementations_only_one_enabled() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multiple_implementations_only_one_enabled, this.description("multiple_implementations_only_one_enabled"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multiple_implementations_only_one_useful() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multiple_implementations_only_one_useful, this.description("multiple_implementations_only_one_useful"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multiple_empty_implementations() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::multiple_empty_implementations, this.description("multiple_empty_implementations"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_problems_loading_impl() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::problems_loading_impl, this.description("problems_loading_impl"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().tmp, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private PluginFinderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new PluginFinderTest();
        }

        @java.lang.Override
        public PluginFinderTest implementation() {
            return this.implementation;
        }
    }
}
