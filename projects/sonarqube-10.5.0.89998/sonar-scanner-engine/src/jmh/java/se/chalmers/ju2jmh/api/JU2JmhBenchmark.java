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
package se.chalmers.ju2jmh.api;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public abstract class JU2JmhBenchmark {

    @FunctionalInterface
    public interface ThrowingRunnable {

        void run() throws Throwable;
    }

    public abstract void createImplementation() throws Throwable;

    public abstract Object implementation();

    public void beforeClass() throws Throwable {
    }

    public void afterClass() throws Throwable {
    }

    public void before() throws Throwable {
    }

    public void after() throws Throwable {
    }

    public Statement applyClassRuleFields(Statement statement, Description description) {
        return statement;
    }

    public Statement applyClassRuleMethods(Statement statement, Description description) {
        return statement;
    }

    public Statement applyRuleFields(Statement statement, Description description) {
        return statement;
    }

    public Statement applyRuleMethods(Statement statement, Description description) {
        return statement;
    }

    public final Description description(String methodName) {
        return Description.createTestDescription(implementation().getClass(), methodName);
    }

    private FrameworkMethod frameworkMethodFromDescription(Description description) {
        FrameworkMethod frameworkMethod;
        try {
            frameworkMethod = new FrameworkMethod(implementation().getClass().getMethod(description.getMethodName()));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return frameworkMethod;
    }

    public final Statement applyRule(TestRule rule, Statement statement, Description description) {
        if (rule.getClass() == Timeout.class) {
            return statement;
        }
        return rule.apply(statement, description);
    }

    public final Statement applyRule(MethodRule rule, Statement statement, Description description) {
        return rule.apply(statement, frameworkMethodFromDescription(description), implementation());
    }

    private static class BeforeAfterStatement extends Statement {

        private final ThrowingRunnable beforeAction;

        private final ThrowingRunnable action;

        private final ThrowingRunnable afterAction;

        private BeforeAfterStatement(ThrowingRunnable beforeAction, ThrowingRunnable action, ThrowingRunnable afterAction) {
            this.beforeAction = beforeAction;
            this.action = action;
            this.afterAction = afterAction;
        }

        @Override
        public void evaluate() throws Throwable {
            beforeAction.run();
            try {
                action.run();
            } finally {
                afterAction.run();
            }
        }
    }

    public final void runBenchmark(ThrowingRunnable benchmark, Description description) throws Throwable {
        Statement statement = new BeforeAfterStatement(this::before, benchmark, this::after);
        statement = applyRuleMethods(statement, description);
        statement = applyRuleFields(statement, description);
        statement = new BeforeAfterStatement(this::beforeClass, statement::evaluate, this::afterClass);
        statement = applyClassRuleMethods(statement, description);
        statement = applyClassRuleFields(statement, description);
        statement.evaluate();
    }

    public final void runExceptionBenchmark(ThrowingRunnable benchmark, Description description, Class<? extends Throwable> expected) throws Throwable {
        ThrowingRunnable exceptionBenchmark = () -> {
            try {
                benchmark.run();
            } catch (Throwable e) {
                if (expected.isInstance(e)) {
                    return;
                }
                throw e;
            }
            throw new AssertionError("Expected " + expected.getCanonicalName() + " but none was thrown");
        };
        runBenchmark(exceptionBenchmark, description);
    }
}
