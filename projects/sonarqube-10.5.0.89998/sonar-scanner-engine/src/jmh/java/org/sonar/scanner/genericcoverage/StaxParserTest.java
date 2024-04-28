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
package org.sonar.scanner.genericcoverage;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.junit.Test;
import javax.xml.stream.XMLStreamException;

public class StaxParserTest {

    @Test
    public void testXMLWithDTD() throws XMLStreamException {
        StaxParser parser = new StaxParser(getTestHandler());
        parser.parse(getClass().getClassLoader().getResourceAsStream("org/sonar/scanner/genericcoverage/xml-dtd-test.xml"));
    }

    @Test
    public void testXMLWithXSD() throws XMLStreamException {
        StaxParser parser = new StaxParser(getTestHandler());
        parser.parse(getClass().getClassLoader().getResourceAsStream("org/sonar/scanner/genericcoverage/xml-xsd-test.xml"));
    }

    private StaxParser.XmlStreamHandler getTestHandler() {
        return new StaxParser.XmlStreamHandler() {

            public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
                rootCursor.advance();
                while (rootCursor.getNext() != null) {
                }
            }
        };
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testXMLWithDTD() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testXMLWithDTD, this.description("testXMLWithDTD"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testXMLWithXSD() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testXMLWithXSD, this.description("testXMLWithXSD"));
        }

        private StaxParserTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new StaxParserTest();
        }

        @java.lang.Override
        public StaxParserTest implementation() {
            return this.implementation;
        }
    }
}
