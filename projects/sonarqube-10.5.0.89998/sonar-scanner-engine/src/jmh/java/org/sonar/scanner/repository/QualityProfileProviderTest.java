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
package org.sonar.scanner.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.DateUtils;
import org.sonar.api.testfixtures.log.LogTester;
import org.sonar.scanner.bootstrap.ScannerProperties;
import org.sonar.scanner.rule.QualityProfiles;
import org.sonarqube.ws.Qualityprofiles.SearchWsResponse.QualityProfile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class QualityProfileProviderTest {

    @Rule
    public LogTester logTester = new LogTester();

    private QualityProfilesProvider qualityProfileProvider;

    private QualityProfileLoader loader = mock(QualityProfileLoader.class);

    private ScannerProperties props = mock(ScannerProperties.class);

    private List<QualityProfile> response;

    @Before
    public void setUp() {
        qualityProfileProvider = new QualityProfilesProvider();
        when(props.getProjectKey()).thenReturn("project");
        response = new ArrayList<>(1);
        response.add(QualityProfile.newBuilder().setKey("profile").setName("profile").setLanguage("lang").setRulesUpdatedAt(DateUtils.formatDateTime(new Date())).build());
    }

    @Test
    public void testProvide() {
        when(loader.load("project")).thenReturn(response);
        QualityProfiles qps = qualityProfileProvider.provide(loader, props);
        assertResponse(qps);
        verify(loader).load("project");
        verifyNoMoreInteractions(loader);
    }

    @Test
    public void testProfileProp() {
        when(loader.load("project")).thenReturn(response);
        QualityProfiles qps = qualityProfileProvider.provide(loader, props);
        assertResponse(qps);
        verify(loader).load("project");
        verifyNoMoreInteractions(loader);
    }

    private void assertResponse(QualityProfiles qps) {
        assertThat(qps.findAll()).extracting("key").containsExactly("profile");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testProvide() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testProvide, this.description("testProvide"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testProfileProp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testProfileProp, this.description("testProfileProp"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().logTester, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private QualityProfileProviderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new QualityProfileProviderTest();
        }

        @java.lang.Override
        public QualityProfileProviderTest implementation() {
            return this.implementation;
        }
    }
}
