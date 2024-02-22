/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.matchers;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

public class CapturingArgumentsTest extends TestBase {

    private static class Person {

        private final Integer age;

        public Person(Integer age) {
            this.age = age;
        }

        public int getAge() {
            return age;
        }
    }

    private static class BulkEmailService {

        private final EmailService service;

        public BulkEmailService(EmailService service) {
            this.service = service;
        }

        public void email(Integer... personId) {
            for (Integer i : personId) {
                Person person = new Person(i);
                service.sendEmailTo(person);
            }
        }
    }

    interface EmailService {

        boolean sendEmailTo(Person person);
    }

    private final EmailService emailService = mock(EmailService.class);

    private final BulkEmailService bulkEmailService = new BulkEmailService(emailService);

    private final IMethods mock = mock(IMethods.class);

    @Captor
    private ArgumentCaptor<List<?>> listCaptor;

    @Test
    public void should_allow_assertions_on_captured_argument() {
        // given
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        // when
        bulkEmailService.email(12);
        // then
        verify(emailService).sendEmailTo(argument.capture());
        assertEquals(12, argument.getValue().getAge());
    }

    @Test
    public void should_allow_assertions_on_all_captured_arguments() {
        // given
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        // when
        bulkEmailService.email(11, 12);
        // then
        verify(emailService, times(2)).sendEmailTo(argument.capture());
        assertEquals(11, argument.getAllValues().get(0).getAge());
        assertEquals(12, argument.getAllValues().get(1).getAge());
    }

    @Test
    public void should_allow_assertions_on_last_argument() {
        // given
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        // when
        bulkEmailService.email(11, 12, 13);
        // then
        verify(emailService, times(3)).sendEmailTo(argument.capture());
        assertEquals(13, argument.getValue().getAge());
    }

    @Test
    public void should_print_captor_matcher() {
        // given
        ArgumentCaptor<Person> person = ArgumentCaptor.forClass(Person.class);
        try {
            // when
            verify(emailService).sendEmailTo(person.capture());
            fail();
        } catch (WantedButNotInvoked e) {
            // then
            assertThat(e).hasMessageContaining("<Capturing argument: Person>");
        }
    }

    @Test
    public void should_allow_assertions_on_captured_null() {
        // given
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        // when
        emailService.sendEmailTo(null);
        // then
        verify(emailService).sendEmailTo(argument.capture());
        assertNull(argument.getValue());
    }

    @Test
    public void should_allow_construction_of_captor_for_parameterized_type_in_a_convenient_way() {
        // the test passes if this expression compiles
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Person>> argument = ArgumentCaptor.forClass(List.class);
        assertNotNull(argument);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_allow_construction_of_captor_for_a_more_specific_type() {
        // the test passes if this expression compiles
        ArgumentCaptor<List<?>> argument = ArgumentCaptor.forClass(ArrayList.class);
        assertNotNull(argument);
    }

    @Test
    public void should_allow_capturing_for_stubbing() {
        // given
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        when(emailService.sendEmailTo(argument.capture())).thenReturn(false);
        // when
        emailService.sendEmailTo(new Person(10));
        // then
        assertEquals(10, argument.getValue().getAge());
    }

    @Test
    public void should_capture_when_stubbing_only_when_entire_invocation_matches() {
        // given
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        when(mock.simpleMethod(argument.capture(), eq(2))).thenReturn("blah");
        // when
        mock.simpleMethod("foo", 200);
        mock.simpleMethod("bar", 2);
        // then
        assertThat(argument.getAllValues()).containsOnly("bar");
    }

    @Test
    public void should_say_something_smart_when_misused() {
        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        try {
            argument.getValue();
            fail();
        } catch (MockitoException expected) {
        }
    }

    @Test
    public void should_capture_when_full_arg_list_matches() {
        // given
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        // when
        mock.simpleMethod("foo", 1);
        mock.simpleMethod("bar", 2);
        // then
        verify(mock).simpleMethod(captor.capture(), eq(1));
        assertEquals(1, captor.getAllValues().size());
        assertEquals("foo", captor.getValue());
    }

    @Test
    public void should_capture_int_by_creating_captor_with_primitive_wrapper() {
        // given
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        // when
        mock.intArgumentMethod(10);
        // then
        verify(mock).intArgumentMethod(argument.capture());
        assertEquals(10, (int) argument.getValue());
    }

    @Test
    public void should_capture_int_by_creating_captor_with_primitive() {
        // given
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(int.class);
        // when
        mock.intArgumentMethod(10);
        // then
        verify(mock).intArgumentMethod(argument.capture());
        assertEquals(10, (int) argument.getValue());
    }

    @Test
    public void should_not_capture_int_by_creating_captor_with_primitive() {
        // given
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(int.class);
        // when
        mock.forObject(10L);
        // then
        verify(mock, never()).forObject(argument.capture());
    }

    @Test
    public void should_capture_byte_vararg_by_creating_captor_with_primitive() {
        // given
        ArgumentCaptor<Byte> argumentCaptor = ArgumentCaptor.forClass(byte.class);
        // when
        mock.varargsbyte((byte) 1);
        // then
        verify(mock).varargsbyte(argumentCaptor.capture());
        assertEquals((byte) 1, (byte) argumentCaptor.getValue());
        assertThat(argumentCaptor.getAllValues()).containsExactly((byte) 1);
    }

    @Test
    public void should_capture_byte_vararg_by_creating_captor_with_primitive_2_args() {
        // given
        ArgumentCaptor<Byte> argumentCaptor = ArgumentCaptor.forClass(byte.class);
        // when
        mock.varargsbyte((byte) 1, (byte) 2);
        // then
        verify(mock).varargsbyte(argumentCaptor.capture(), argumentCaptor.capture());
        assertEquals((byte) 2, (byte) argumentCaptor.getValue());
        assertThat(argumentCaptor.getAllValues()).containsExactly((byte) 1, (byte) 2);
    }

    @Test
    public void should_capture_byte_vararg_by_creating_captor_with_primitive_array() {
        // given
        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
        // when
        mock.varargsbyte();
        mock.varargsbyte((byte) 1, (byte) 2);
        // then
        verify(mock, times(2)).varargsbyte(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).containsExactly(new byte[] { 1, 2 });
        assertThat(argumentCaptor.getAllValues()).containsExactly(new byte[] {}, new byte[] { 1, 2 });
    }

    @Test
    public void should_capture_byte_vararg_by_creating_captor_with_primitive_wrapper() {
        // given
        ArgumentCaptor<Byte> argumentCaptor = ArgumentCaptor.forClass(Byte.class);
        // when
        mock.varargsbyte((byte) 1);
        // then
        verify(mock).varargsbyte(argumentCaptor.capture());
        assertEquals((byte) 1, (byte) argumentCaptor.getValue());
        assertThat(argumentCaptor.getAllValues()).containsExactly((byte) 1);
    }

    @Test
    public void should_not_capture_empty_vararg_with_single_captor() {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        mock.mixedVarargs(42);
        // then
        verify(mock, never()).mixedVarargs(any(), argumentCaptor.capture());
    }

    @Test
    public void should_capture_single_vararg_with_single_captor() {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        mock.mixedVarargs(42, "a");
        // then
        verify(mock).mixedVarargs(any(), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo("a");
    }

    @Test
    public void should_not_capture_multiple_vararg_with_single_captor() {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        mock.mixedVarargs(42, "a", "b");
        // then
        verify(mock, never()).mixedVarargs(any(), argumentCaptor.capture());
    }

    @Test
    public void should_capture_multiple_vararg_with_multiple_captor() {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        mock.mixedVarargs(42, "a", "b");
        // then
        verify(mock).mixedVarargs(any(), argumentCaptor.capture(), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo("b");
        assertThat(argumentCaptor.getAllValues()).isEqualTo(asList("a", "b"));
    }

    @Test
    public void should_not_capture_multiple_vararg_some_null_with_single_captor() {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        mock.mixedVarargs(42, "a", null);
        // then
        verify(mock, never()).mixedVarargs(any(), argumentCaptor.capture());
    }

    @Test
    public void should_capture_empty_vararg_with_array_captor() {
        // given
        ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
        // when
        mock.mixedVarargs(42);
        // then
        verify(mock).mixedVarargs(any(), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(new String[] {});
        assertThat(argumentCaptor.getAllValues()).containsExactly(new String[] {});
    }

    @Test
    public void should_capture_single_vararg_with_array_captor() {
        // given
        ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
        // when
        mock.mixedVarargs(42, "a");
        // then
        verify(mock).mixedVarargs(any(), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(new String[] { "a" });
        assertThat(argumentCaptor.getAllValues()).containsExactly(new String[] { "a" });
    }

    @Test
    public void should_capture_multiple_vararg_with_array_captor() {
        // given
        ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
        // when
        mock.mixedVarargs(42, "a", "b", "c");
        // then
        verify(mock).mixedVarargs(any(), argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues()).containsExactly(new String[] { "a", "b", "c" });
    }

    @Test
    public void should_capture_multiple_vararg_some_null_with_array_captor() {
        // given
        ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
        // when
        mock.mixedVarargs(42, "a", null, "c");
        // then
        verify(mock).mixedVarargs(any(), argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues()).containsExactly(new String[] { "a", null, "c" });
    }

    @Test
    public void should_capture_multiple_invocations_with_captor() {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        mock.mixedVarargs(42, "a", "b");
        mock.mixedVarargs(42, "c", "d");
        // then
        verify(mock, times(2)).mixedVarargs(any(), argumentCaptor.capture(), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo("d");
        assertThat(argumentCaptor.getAllValues()).containsExactly("a", "b", "c", "d");
    }

    @Test
    public void should_capture_multiple_invocations_with_array_captor() {
        // given
        ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
        // when
        mock.mixedVarargs(42, "a", "b");
        mock.mixedVarargs(42, "c", "d");
        // then
        verify(mock, times(2)).mixedVarargs(any(), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(new String[] { "c", "d" });
        assertThat(argumentCaptor.getAllValues()).containsExactly(new String[] { "a", "b" }, new String[] { "c", "d" });
    }

    @Test
    public void should_capture_one_arg_even_when_using_vararg_captor_on_nonvararg_method() {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        mock.simpleMethod("a", 2);
        // then
        verify(mock).simpleMethod(argumentCaptor.capture(), eq(2));
        assertThat(argumentCaptor.getAllValues()).containsExactly("a");
    }

    @Test
    public void captures_correctly_when_captor_used_multiple_times() {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        mock.mixedVarargs(42, "a", "b", "c");
        // then
        // this is only for backwards compatibility. It does not make sense in real to do so.
        verify(mock).mixedVarargs(any(), argumentCaptor.capture(), argumentCaptor.capture(), argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues()).containsExactly("a", "b", "c");
    }

    @Test
    public void captures_correctly_when_captor_used_on_pure_vararg_method() {
        // given
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        mock.varargs(42, "capturedValue");
        // then
        verify(mock).varargs(eq(42), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).contains("capturedValue");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_capture_by_type() {
        // When:
        mock.simpleMethod(Set.of());
        mock.simpleMethod(new ArrayList<>(0));
        // Then:
        ArgumentCaptor<Collection<?>> captor = ArgumentCaptor.forClass(ArrayList.class);
        verify(mock).simpleMethod(captor.capture());
        assertThat(captor.getAllValues()).containsExactly(List.of());
    }

    @Test
    public void should_capture_by_type_using_annotation() {
        // When:
        mock.simpleMethod(Set.of());
        mock.simpleMethod(new ArrayList<>(0));
        // Then:
        verify(mock).simpleMethod(listCaptor.capture());
        assertThat(listCaptor.getAllValues()).containsExactly(List.of());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_always_capture_nulls() {
        // When:
        mock.simpleMethod((Set<?>) null);
        mock.simpleMethod((List<?>) null);
        // Then:
        ArgumentCaptor<Collection<?>> captor = ArgumentCaptor.forClass(ArrayList.class);
        verify(mock, times(2)).simpleMethod(captor.capture());
        assertThat(captor.getAllValues()).containsExactly(null, null);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends org.mockitoutil.TestBase._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_allow_assertions_on_captured_argument() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_allow_assertions_on_captured_argument, this.description("should_allow_assertions_on_captured_argument"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_allow_assertions_on_all_captured_arguments() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_allow_assertions_on_all_captured_arguments, this.description("should_allow_assertions_on_all_captured_arguments"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_allow_assertions_on_last_argument() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_allow_assertions_on_last_argument, this.description("should_allow_assertions_on_last_argument"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_print_captor_matcher() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_print_captor_matcher, this.description("should_print_captor_matcher"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_allow_assertions_on_captured_null() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_allow_assertions_on_captured_null, this.description("should_allow_assertions_on_captured_null"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_allow_construction_of_captor_for_parameterized_type_in_a_convenient_way() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_allow_construction_of_captor_for_parameterized_type_in_a_convenient_way, this.description("should_allow_construction_of_captor_for_parameterized_type_in_a_convenient_way"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_allow_construction_of_captor_for_a_more_specific_type() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_allow_construction_of_captor_for_a_more_specific_type, this.description("should_allow_construction_of_captor_for_a_more_specific_type"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_allow_capturing_for_stubbing() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_allow_capturing_for_stubbing, this.description("should_allow_capturing_for_stubbing"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_when_stubbing_only_when_entire_invocation_matches() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_when_stubbing_only_when_entire_invocation_matches, this.description("should_capture_when_stubbing_only_when_entire_invocation_matches"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_say_something_smart_when_misused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_say_something_smart_when_misused, this.description("should_say_something_smart_when_misused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_when_full_arg_list_matches() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_when_full_arg_list_matches, this.description("should_capture_when_full_arg_list_matches"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_int_by_creating_captor_with_primitive_wrapper() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_int_by_creating_captor_with_primitive_wrapper, this.description("should_capture_int_by_creating_captor_with_primitive_wrapper"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_int_by_creating_captor_with_primitive() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_int_by_creating_captor_with_primitive, this.description("should_capture_int_by_creating_captor_with_primitive"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_not_capture_int_by_creating_captor_with_primitive() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_not_capture_int_by_creating_captor_with_primitive, this.description("should_not_capture_int_by_creating_captor_with_primitive"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_byte_vararg_by_creating_captor_with_primitive() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_byte_vararg_by_creating_captor_with_primitive, this.description("should_capture_byte_vararg_by_creating_captor_with_primitive"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_byte_vararg_by_creating_captor_with_primitive_2_args() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_byte_vararg_by_creating_captor_with_primitive_2_args, this.description("should_capture_byte_vararg_by_creating_captor_with_primitive_2_args"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_byte_vararg_by_creating_captor_with_primitive_array() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_byte_vararg_by_creating_captor_with_primitive_array, this.description("should_capture_byte_vararg_by_creating_captor_with_primitive_array"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_byte_vararg_by_creating_captor_with_primitive_wrapper() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_byte_vararg_by_creating_captor_with_primitive_wrapper, this.description("should_capture_byte_vararg_by_creating_captor_with_primitive_wrapper"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_not_capture_empty_vararg_with_single_captor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_not_capture_empty_vararg_with_single_captor, this.description("should_not_capture_empty_vararg_with_single_captor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_single_vararg_with_single_captor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_single_vararg_with_single_captor, this.description("should_capture_single_vararg_with_single_captor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_not_capture_multiple_vararg_with_single_captor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_not_capture_multiple_vararg_with_single_captor, this.description("should_not_capture_multiple_vararg_with_single_captor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_multiple_vararg_with_multiple_captor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_multiple_vararg_with_multiple_captor, this.description("should_capture_multiple_vararg_with_multiple_captor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_not_capture_multiple_vararg_some_null_with_single_captor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_not_capture_multiple_vararg_some_null_with_single_captor, this.description("should_not_capture_multiple_vararg_some_null_with_single_captor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_empty_vararg_with_array_captor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_empty_vararg_with_array_captor, this.description("should_capture_empty_vararg_with_array_captor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_single_vararg_with_array_captor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_single_vararg_with_array_captor, this.description("should_capture_single_vararg_with_array_captor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_multiple_vararg_with_array_captor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_multiple_vararg_with_array_captor, this.description("should_capture_multiple_vararg_with_array_captor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_multiple_vararg_some_null_with_array_captor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_multiple_vararg_some_null_with_array_captor, this.description("should_capture_multiple_vararg_some_null_with_array_captor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_multiple_invocations_with_captor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_multiple_invocations_with_captor, this.description("should_capture_multiple_invocations_with_captor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_multiple_invocations_with_array_captor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_multiple_invocations_with_array_captor, this.description("should_capture_multiple_invocations_with_array_captor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_one_arg_even_when_using_vararg_captor_on_nonvararg_method() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_one_arg_even_when_using_vararg_captor_on_nonvararg_method, this.description("should_capture_one_arg_even_when_using_vararg_captor_on_nonvararg_method"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_captures_correctly_when_captor_used_multiple_times() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::captures_correctly_when_captor_used_multiple_times, this.description("captures_correctly_when_captor_used_multiple_times"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_captures_correctly_when_captor_used_on_pure_vararg_method() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::captures_correctly_when_captor_used_on_pure_vararg_method, this.description("captures_correctly_when_captor_used_on_pure_vararg_method"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_by_type() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_by_type, this.description("should_capture_by_type"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_capture_by_type_using_annotation() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_capture_by_type_using_annotation, this.description("should_capture_by_type_using_annotation"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_always_capture_nulls() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_always_capture_nulls, this.description("should_always_capture_nulls"));
        }

        private CapturingArgumentsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CapturingArgumentsTest();
        }

        @java.lang.Override
        public CapturingArgumentsTest implementation() {
            return this.implementation;
        }
    }
}
