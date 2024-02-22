/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.util.reflection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.internal.util.reflection.GenericMetadataSupport.inferFrom;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class GenericMetadataSupportTest {

    interface GenericsSelfReference<T extends GenericsSelfReference<T>> {

        T self();
    }

    interface UpperBoundedTypeWithClass<E extends Number & Comparable<E>> {

        E get();
    }

    interface UpperBoundedTypeWithInterfaces<E extends Comparable<E> & Cloneable> {

        E get();
    }

    interface ListOfNumbers extends List<Number> {
    }

    interface AnotherListOfNumbers extends ListOfNumbers {
    }

    abstract class ListOfNumbersImpl implements ListOfNumbers {
    }

    abstract class AnotherListOfNumbersImpl extends ListOfNumbersImpl {
    }

    interface ListOfAnyNumbers<N extends Number & Cloneable> extends List<N> {
    }

    interface GenericsNest<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {

        // override with fixed ParameterizedType
        Set<Number> remove(Object key);

        List<? super Integer> returning_wildcard_with_class_lower_bound();

        List<? super K> returning_wildcard_with_typeVar_lower_bound();

        List<? extends K> returning_wildcard_with_typeVar_upper_bound();

        K returningK();

        <O extends K> List<O> paramType_with_type_params();

        <S, T extends S> T two_type_params();

        <O extends K> O typeVar_with_type_params();
    }

    static class StringList extends ArrayList<String> {
    }

    public interface TopInterface<T> {

        T generic();
    }

    public interface MiddleInterface<T> extends TopInterface<T> {
    }

    public class OwningClassWithDeclaredUpperBounds<T extends List<String> & Comparable<String> & Cloneable> {

        public abstract class AbstractInner implements MiddleInterface<T> {
        }
    }

    public class OwningClassWithNoDeclaredUpperBounds<T> {

        public abstract class AbstractInner implements MiddleInterface<T> {
        }
    }

    @Test
    public void typeVariable_of_self_type() {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsSelfReference.class).resolveGenericReturnType(firstNamedMethod("self", GenericsSelfReference.class));
        assertThat(genericMetadata.rawType()).isEqualTo(GenericsSelfReference.class);
    }

    @Test
    public void can_get_raw_type_from_Class() {
        assertThat(inferFrom(ListOfAnyNumbers.class).rawType()).isEqualTo(ListOfAnyNumbers.class);
        assertThat(inferFrom(ListOfNumbers.class).rawType()).isEqualTo(ListOfNumbers.class);
        assertThat(inferFrom(GenericsNest.class).rawType()).isEqualTo(GenericsNest.class);
        assertThat(inferFrom(StringList.class).rawType()).isEqualTo(StringList.class);
    }

    @Test
    public void can_get_raw_type_from_ParameterizedType() {
        assertThat(inferFrom(ListOfAnyNumbers.class.getGenericInterfaces()[0]).rawType()).isEqualTo(List.class);
        assertThat(inferFrom(ListOfNumbers.class.getGenericInterfaces()[0]).rawType()).isEqualTo(List.class);
        assertThat(inferFrom(GenericsNest.class.getGenericInterfaces()[0]).rawType()).isEqualTo(Map.class);
        assertThat(inferFrom(StringList.class.getGenericSuperclass()).rawType()).isEqualTo(ArrayList.class);
    }

    @Test
    public void can_get_type_variables_from_Class() {
        assertThat(inferFrom(GenericsNest.class).actualTypeArguments().keySet()).hasSize(1).extracting(TypeVariable::getName).contains("K");
        assertThat(inferFrom(ListOfNumbers.class).actualTypeArguments().keySet()).isEmpty();
        assertThat(inferFrom(ListOfAnyNumbers.class).actualTypeArguments().keySet()).hasSize(1).extracting(TypeVariable::getName).contains("N");
        assertThat(inferFrom(Map.class).actualTypeArguments().keySet()).hasSize(2).extracting(TypeVariable::getName).contains("K", "V");
        assertThat(inferFrom(Serializable.class).actualTypeArguments().keySet()).isEmpty();
        assertThat(inferFrom(StringList.class).actualTypeArguments().keySet()).isEmpty();
    }

    @Test
    public void can_resolve_type_variables_from_ancestors() throws Exception {
        Method listGet = List.class.getMethod("get", int.class);
        assertThat(inferFrom(AnotherListOfNumbers.class).resolveGenericReturnType(listGet).rawType()).isEqualTo(Number.class);
        assertThat(inferFrom(AnotherListOfNumbersImpl.class).resolveGenericReturnType(listGet).rawType()).isEqualTo(Number.class);
    }

    @Test
    public void can_get_type_variables_from_ParameterizedType() {
        assertThat(inferFrom(GenericsNest.class.getGenericInterfaces()[0]).actualTypeArguments().keySet()).hasSize(2).extracting(TypeVariable::getName).contains("K", "V");
        assertThat(inferFrom(ListOfAnyNumbers.class.getGenericInterfaces()[0]).actualTypeArguments().keySet()).hasSize(1).extracting(TypeVariable::getName).contains("E");
        assertThat(inferFrom(Integer.class.getGenericInterfaces()[0]).actualTypeArguments().keySet()).hasSize(1).extracting(TypeVariable::getName).contains("T");
        assertThat(inferFrom(StringBuilder.class.getGenericInterfaces()[0]).actualTypeArguments().keySet()).isEmpty();
        assertThat(inferFrom(StringList.class).actualTypeArguments().keySet()).isEmpty();
    }

    @Test
    public void typeVariable_return_type_of____iterator____resolved_to_Iterator_and_type_argument_to_String() {
        GenericMetadataSupport genericMetadata = inferFrom(StringList.class).resolveGenericReturnType(firstNamedMethod("iterator", StringList.class));
        assertThat(genericMetadata.rawType()).isEqualTo(Iterator.class);
        assertThat(genericMetadata.actualTypeArguments().values()).contains(String.class);
    }

    @Test
    public void typeVariable_return_type_of____get____resolved_to_Set_and_type_argument_to_Number() {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("get", GenericsNest.class));
        assertThat(genericMetadata.rawType()).isEqualTo(Set.class);
        assertThat(genericMetadata.actualTypeArguments().values()).contains(Number.class);
    }

    @Test
    public void bounded_typeVariable_return_type_of____returningK____resolved_to_Comparable_and_with_BoundedType() {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("returningK", GenericsNest.class));
        assertThat(genericMetadata.rawType()).isEqualTo(Comparable.class);
        GenericMetadataSupport extraInterface_0 = inferFrom(genericMetadata.extraInterfaces().get(0));
        assertThat(extraInterface_0.rawType()).isEqualTo(Cloneable.class);
    }

    @Test
    public void fixed_ParamType_return_type_of____remove____resolved_to_Set_and_type_argument_to_Number() {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("remove", GenericsNest.class));
        assertThat(genericMetadata.rawType()).isEqualTo(Set.class);
        assertThat(genericMetadata.actualTypeArguments().values()).contains(Number.class);
    }

    @Test
    public void paramType_return_type_of____values____resolved_to_Collection_and_type_argument_to_Parameterized_Set() {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("values", GenericsNest.class));
        assertThat(genericMetadata.rawType()).isEqualTo(Collection.class);
        GenericMetadataSupport fromTypeVariableE = inferFrom(typeVariableValue(genericMetadata.actualTypeArguments(), "E"));
        assertThat(fromTypeVariableE.rawType()).isEqualTo(Set.class);
        assertThat(fromTypeVariableE.actualTypeArguments().values()).contains(Number.class);
    }

    @Test
    public void paramType_with_type_parameters_return_type_of____paramType_with_type_params____resolved_to_Collection_and_type_argument_to_Parameterized_Set() {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("paramType_with_type_params", GenericsNest.class));
        assertThat(genericMetadata.rawType()).isEqualTo(List.class);
        Type firstBoundOfE = ((GenericMetadataSupport.TypeVarBoundedType) typeVariableValue(genericMetadata.actualTypeArguments(), "E")).firstBound();
        assertThat(inferFrom(firstBoundOfE).rawType()).isEqualTo(Comparable.class);
    }

    @Test
    public void typeVariable_with_type_parameters_return_type_of____typeVar_with_type_params____resolved_K_hence_to_Comparable_and_with_BoundedType() {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("typeVar_with_type_params", GenericsNest.class));
        assertThat(genericMetadata.rawType()).isEqualTo(Comparable.class);
        GenericMetadataSupport extraInterface_0 = inferFrom(genericMetadata.extraInterfaces().get(0));
        assertThat(extraInterface_0.rawType()).isEqualTo(Cloneable.class);
    }

    @Test
    public void class_return_type_of____append____resolved_to_StringBuilder_and_type_arguments() {
        GenericMetadataSupport genericMetadata = inferFrom(StringBuilder.class).resolveGenericReturnType(firstNamedMethod("append", StringBuilder.class));
        assertThat(genericMetadata.rawType()).isEqualTo(StringBuilder.class);
        assertThat(genericMetadata.actualTypeArguments()).isEmpty();
    }

    @Test
    public void paramType_with_wildcard_return_type_of____returning_wildcard_with_class_lower_bound____resolved_to_List_and_type_argument_to_Integer() {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("returning_wildcard_with_class_lower_bound", GenericsNest.class));
        assertThat(genericMetadata.rawType()).isEqualTo(List.class);
        GenericMetadataSupport.BoundedType boundedType = (GenericMetadataSupport.BoundedType) typeVariableValue(genericMetadata.actualTypeArguments(), "E");
        assertThat(boundedType.firstBound()).isEqualTo(Integer.class);
        assertThat(boundedType.interfaceBounds()).isEmpty();
    }

    @Test
    public void paramType_with_wildcard_return_type_of____returning_wildcard_with_typeVar_lower_bound____resolved_to_List_and_type_argument_to_Integer() {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("returning_wildcard_with_typeVar_lower_bound", GenericsNest.class));
        assertThat(genericMetadata.rawType()).isEqualTo(List.class);
        GenericMetadataSupport.BoundedType boundedType = (GenericMetadataSupport.BoundedType) typeVariableValue(genericMetadata.actualTypeArguments(), "E");
        assertThat(inferFrom(boundedType.firstBound()).rawType()).isEqualTo(Comparable.class);
        assertThat(boundedType.interfaceBounds()).contains(Cloneable.class);
    }

    @Test
    public void paramType_with_wildcard_return_type_of____returning_wildcard_with_typeVar_upper_bound____resolved_to_List_and_type_argument_to_Integer() {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("returning_wildcard_with_typeVar_upper_bound", GenericsNest.class));
        assertThat(genericMetadata.rawType()).isEqualTo(List.class);
        GenericMetadataSupport.BoundedType boundedType = (GenericMetadataSupport.BoundedType) typeVariableValue(genericMetadata.actualTypeArguments(), "E");
        assertThat(inferFrom(boundedType.firstBound()).rawType()).isEqualTo(Comparable.class);
        assertThat(boundedType.interfaceBounds()).contains(Cloneable.class);
    }

    @Test
    public void can_extract_raw_type_from_bounds_on_terminal_typeVariable() {
        assertThat(inferFrom(OwningClassWithDeclaredUpperBounds.AbstractInner.class).resolveGenericReturnType(firstNamedMethod("generic", OwningClassWithDeclaredUpperBounds.AbstractInner.class)).rawType()).isEqualTo(List.class);
        assertThat(inferFrom(OwningClassWithNoDeclaredUpperBounds.AbstractInner.class).resolveGenericReturnType(firstNamedMethod("generic", OwningClassWithNoDeclaredUpperBounds.AbstractInner.class)).rawType()).isEqualTo(Object.class);
    }

    @Test
    public void can_extract_interface_type_from_bounds_on_terminal_typeVariable() {
        assertThat(inferFrom(OwningClassWithDeclaredUpperBounds.AbstractInner.class).resolveGenericReturnType(firstNamedMethod("generic", OwningClassWithDeclaredUpperBounds.AbstractInner.class)).rawExtraInterfaces()).containsExactly(Comparable.class, Cloneable.class);
        assertThat(inferFrom(OwningClassWithDeclaredUpperBounds.AbstractInner.class).resolveGenericReturnType(firstNamedMethod("generic", OwningClassWithDeclaredUpperBounds.AbstractInner.class)).extraInterfaces()).containsExactly(parameterizedTypeOf(Comparable.class, null, String.class), Cloneable.class);
        assertThat(inferFrom(OwningClassWithNoDeclaredUpperBounds.AbstractInner.class).resolveGenericReturnType(firstNamedMethod("generic", OwningClassWithNoDeclaredUpperBounds.AbstractInner.class)).extraInterfaces()).isEmpty();
    }

    private ParameterizedType parameterizedTypeOf(final Class<?> rawType, final Class<?> ownerType, final Type... actualTypeArguments) {
        return new ParameterizedType() {

            @Override
            public Type[] getActualTypeArguments() {
                return actualTypeArguments;
            }

            @Override
            public Type getRawType() {
                return rawType;
            }

            @Override
            public Type getOwnerType() {
                return ownerType;
            }

            @SuppressWarnings("EqualsHashCode")
            public boolean equals(Object other) {
                if (other instanceof ParameterizedType) {
                    ParameterizedType otherParamType = (ParameterizedType) other;
                    if (this == otherParamType) {
                        return true;
                    } else {
                        return equals(ownerType, otherParamType.getOwnerType()) && equals(rawType, otherParamType.getRawType()) && Arrays.equals(actualTypeArguments, otherParamType.getActualTypeArguments());
                    }
                } else {
                    return false;
                }
            }

            private boolean equals(Object a, Object b) {
                return (a == b) || (a != null && a.equals(b));
            }
        };
    }

    private Type typeVariableValue(Map<TypeVariable<?>, Type> typeVariables, String typeVariableName) {
        for (Map.Entry<TypeVariable<?>, Type> typeVariableTypeEntry : typeVariables.entrySet()) {
            if (typeVariableTypeEntry.getKey().getName().equals(typeVariableName)) {
                return typeVariableTypeEntry.getValue();
            }
        }
        fail("'" + typeVariableName + "' was not found in " + typeVariables);
        // unreachable
        return null;
    }

    private Method firstNamedMethod(String methodName, Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            boolean protect_against_different_jdk_ordering_avoiding_bridge_methods = !method.isBridge();
            if (method.getName().contains(methodName) && protect_against_different_jdk_ordering_avoiding_bridge_methods) {
                return method;
            }
        }
        throw new IllegalStateException("The method : '" + methodName + "' do not exist in '" + clazz.getSimpleName() + "'");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_typeVariable_of_self_type() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::typeVariable_of_self_type, this.description("typeVariable_of_self_type"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_get_raw_type_from_Class() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_get_raw_type_from_Class, this.description("can_get_raw_type_from_Class"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_get_raw_type_from_ParameterizedType() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_get_raw_type_from_ParameterizedType, this.description("can_get_raw_type_from_ParameterizedType"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_get_type_variables_from_Class() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_get_type_variables_from_Class, this.description("can_get_type_variables_from_Class"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_resolve_type_variables_from_ancestors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_resolve_type_variables_from_ancestors, this.description("can_resolve_type_variables_from_ancestors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_get_type_variables_from_ParameterizedType() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_get_type_variables_from_ParameterizedType, this.description("can_get_type_variables_from_ParameterizedType"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_typeVariable_return_type_of____iterator____resolved_to_Iterator_and_type_argument_to_String() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::typeVariable_return_type_of____iterator____resolved_to_Iterator_and_type_argument_to_String, this.description("typeVariable_return_type_of____iterator____resolved_to_Iterator_and_type_argument_to_String"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_typeVariable_return_type_of____get____resolved_to_Set_and_type_argument_to_Number() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::typeVariable_return_type_of____get____resolved_to_Set_and_type_argument_to_Number, this.description("typeVariable_return_type_of____get____resolved_to_Set_and_type_argument_to_Number"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bounded_typeVariable_return_type_of____returningK____resolved_to_Comparable_and_with_BoundedType() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::bounded_typeVariable_return_type_of____returningK____resolved_to_Comparable_and_with_BoundedType, this.description("bounded_typeVariable_return_type_of____returningK____resolved_to_Comparable_and_with_BoundedType"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fixed_ParamType_return_type_of____remove____resolved_to_Set_and_type_argument_to_Number() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fixed_ParamType_return_type_of____remove____resolved_to_Set_and_type_argument_to_Number, this.description("fixed_ParamType_return_type_of____remove____resolved_to_Set_and_type_argument_to_Number"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_paramType_return_type_of____values____resolved_to_Collection_and_type_argument_to_Parameterized_Set() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::paramType_return_type_of____values____resolved_to_Collection_and_type_argument_to_Parameterized_Set, this.description("paramType_return_type_of____values____resolved_to_Collection_and_type_argument_to_Parameterized_Set"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_paramType_with_type_parameters_return_type_of____paramType_with_type_params____resolved_to_Collection_and_type_argument_to_Parameterized_Set() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::paramType_with_type_parameters_return_type_of____paramType_with_type_params____resolved_to_Collection_and_type_argument_to_Parameterized_Set, this.description("paramType_with_type_parameters_return_type_of____paramType_with_type_params____resolved_to_Collection_and_type_argument_to_Parameterized_Set"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_typeVariable_with_type_parameters_return_type_of____typeVar_with_type_params____resolved_K_hence_to_Comparable_and_with_BoundedType() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::typeVariable_with_type_parameters_return_type_of____typeVar_with_type_params____resolved_K_hence_to_Comparable_and_with_BoundedType, this.description("typeVariable_with_type_parameters_return_type_of____typeVar_with_type_params____resolved_K_hence_to_Comparable_and_with_BoundedType"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_class_return_type_of____append____resolved_to_StringBuilder_and_type_arguments() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::class_return_type_of____append____resolved_to_StringBuilder_and_type_arguments, this.description("class_return_type_of____append____resolved_to_StringBuilder_and_type_arguments"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_paramType_with_wildcard_return_type_of____returning_wildcard_with_class_lower_bound____resolved_to_List_and_type_argument_to_Integer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::paramType_with_wildcard_return_type_of____returning_wildcard_with_class_lower_bound____resolved_to_List_and_type_argument_to_Integer, this.description("paramType_with_wildcard_return_type_of____returning_wildcard_with_class_lower_bound____resolved_to_List_and_type_argument_to_Integer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_paramType_with_wildcard_return_type_of____returning_wildcard_with_typeVar_lower_bound____resolved_to_List_and_type_argument_to_Integer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::paramType_with_wildcard_return_type_of____returning_wildcard_with_typeVar_lower_bound____resolved_to_List_and_type_argument_to_Integer, this.description("paramType_with_wildcard_return_type_of____returning_wildcard_with_typeVar_lower_bound____resolved_to_List_and_type_argument_to_Integer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_paramType_with_wildcard_return_type_of____returning_wildcard_with_typeVar_upper_bound____resolved_to_List_and_type_argument_to_Integer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::paramType_with_wildcard_return_type_of____returning_wildcard_with_typeVar_upper_bound____resolved_to_List_and_type_argument_to_Integer, this.description("paramType_with_wildcard_return_type_of____returning_wildcard_with_typeVar_upper_bound____resolved_to_List_and_type_argument_to_Integer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_extract_raw_type_from_bounds_on_terminal_typeVariable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_extract_raw_type_from_bounds_on_terminal_typeVariable, this.description("can_extract_raw_type_from_bounds_on_terminal_typeVariable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_extract_interface_type_from_bounds_on_terminal_typeVariable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_extract_interface_type_from_bounds_on_terminal_typeVariable, this.description("can_extract_interface_type_from_bounds_on_terminal_typeVariable"));
        }

        private GenericMetadataSupportTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new GenericMetadataSupportTest();
        }

        @java.lang.Override
        public GenericMetadataSupportTest implementation() {
            return this.implementation;
        }
    }
}
