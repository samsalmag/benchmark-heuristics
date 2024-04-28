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
package org.sonar.scanner.issue.ignore.pattern;

import org.junit.Test;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.Rule;
import static org.assertj.core.api.Assertions.assertThat;

public class IssuePatternTest {

    @Test
    public void shouldMatchJavaFile() {
        String javaFile = "org/foo/Bar.java";
        assertThat(new IssuePattern("org/foo/Bar.java", "*").matchFile(javaFile)).isTrue();
        assertThat(new IssuePattern("org/foo/*", "*").matchFile(javaFile)).isTrue();
        assertThat(new IssuePattern("**Bar.java", "*").matchFile(javaFile)).isTrue();
        assertThat(new IssuePattern("**", "*").matchFile(javaFile)).isTrue();
        assertThat(new IssuePattern("org/*/?ar.java", "*").matchFile(javaFile)).isTrue();
        assertThat(new IssuePattern("org/other/Hello.java", "*").matchFile(javaFile)).isFalse();
        assertThat(new IssuePattern("org/foo/Hello.java", "*").matchFile(javaFile)).isFalse();
        assertThat(new IssuePattern("org/*/??ar.java", "*").matchFile(javaFile)).isFalse();
        assertThat(new IssuePattern("org/*/??ar.java", "*").matchFile(null)).isFalse();
        assertThat(new IssuePattern("org/*/??ar.java", "*").matchFile("plop")).isFalse();
    }

    @Test
    public void shouldMatchRule() {
        RuleKey rule = Rule.create("checkstyle", "IllegalRegexp", "").ruleKey();
        assertThat(new IssuePattern("*", "*").matchRule(rule)).isTrue();
        assertThat(new IssuePattern("*", "checkstyle:*").matchRule(rule)).isTrue();
        assertThat(new IssuePattern("*", "checkstyle:IllegalRegexp").matchRule(rule)).isTrue();
        assertThat(new IssuePattern("*", "checkstyle:Illegal*").matchRule(rule)).isTrue();
        assertThat(new IssuePattern("*", "*:*Illegal*").matchRule(rule)).isTrue();
        assertThat(new IssuePattern("*", "pmd:IllegalRegexp").matchRule(rule)).isFalse();
        assertThat(new IssuePattern("*", "pmd:*").matchRule(rule)).isFalse();
        assertThat(new IssuePattern("*", "*:Foo*IllegalRegexp").matchRule(rule)).isFalse();
    }

    @Test
    public void toString_should_include_all_fields() {
        assertThat(new IssuePattern("*", "*:Foo*IllegalRegexp")).hasToString("IssuePattern{filePattern=*, rulePattern=*:Foo*IllegalRegexp}");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldMatchJavaFile() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldMatchJavaFile, this.description("shouldMatchJavaFile"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldMatchRule() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldMatchRule, this.description("shouldMatchRule"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toString_should_include_all_fields() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toString_should_include_all_fields, this.description("toString_should_include_all_fields"));
        }

        private IssuePatternTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new IssuePatternTest();
        }

        @java.lang.Override
        public IssuePatternTest implementation() {
            return this.implementation;
        }
    }
}
