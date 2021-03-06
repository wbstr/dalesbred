/*
 * Copyright (c) 2015 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.dalesbred.internal.instantiation;

import org.dalesbred.annotation.DalesbredIgnore;
import org.dalesbred.annotation.DalesbredInstantiator;
import org.dalesbred.annotation.Reflective;
import org.dalesbred.dialect.DefaultDialect;
import org.dalesbred.internal.instantiation.test.InaccessibleClassRef;
import org.dalesbred.internal.utils.TypeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class InstantiatorProviderTest {

    private final InstantiatorProvider instantiatorRegistry = new InstantiatorProvider(new DefaultDialect());

    @Test
    public void everyClassIsAssignableFromItself() {
        assertAssignable(int.class, int.class);
        assertAssignable(Integer.class, Integer.class);
        assertAssignable(Object.class, Object.class);
        assertAssignable(String.class, String.class);
    }

    @Test
    public void primitivesAreAssignableFromWrappers() {
        assertAssignable(int.class, Integer.class);
        assertAssignable(long.class, Long.class);
    }

    @Test
    public void wrappersAreAssignableFromPrimitives() {
        assertAssignable(Integer.class, int.class);
        assertAssignable(Long.class, long.class);
    }

    @Test
    public void findDefaultConstructor() {
        TestClass result = instantiate(TestClass.class, NamedTypeList.builder(0).build());
        assertNotNull(result);
        assertThat(result.calledConstructor, is(1));
    }

    @Test
    public void findConstructedBasedOnType() {
        TestClass result = instantiate(TestClass.class, String.class, "foo");
        assertNotNull(result);
        assertThat(result.calledConstructor, is(2));
    }

    @Test
    public void findBasedOnPrimitiveType() {
        TestClass result = instantiate(TestClass.class, int.class, 3);
        assertNotNull(result);
        assertThat(result.calledConstructor, is(3));
    }

    @Test
    public void findPrimitiveTypedConstructorWithBoxedType() {
        TestClass result = instantiate(TestClass.class, Integer.class, 3);
        assertNotNull(result);
        assertThat(result.calledConstructor, is(3));
    }

    @Test(expected = InstantiationFailureException.class)
    public void findingInstantiatorForInaccessibleClassThrowsNiceException() {
        instantiate(InaccessibleClassRef.INACCESSIBLE_CLASS, int.class, 3);
    }

    @Test(expected = InstantiationFailureException.class)
    public void findingInstantiatorForInaccessibleConstructorThrowsNiceException() {
        instantiate(InaccessibleConstructor.class, int.class, 3);
    }

    @Test
    public void extraFieldsCanBeSpecifiedWithSettersAndFields() {
        NamedTypeList types = NamedTypeList.builder(3).add("arg", String.class).add("propertyWithAccessors", String.class).add("publicField", String.class).build();

        TestClass result = instantiate(TestClass.class, types, "foo", "bar", "baz");
        assertNotNull(result);
        assertThat(result.calledConstructor, is(2));
        assertThat(result.getPropertyWithAccessors(), is("bar"));
        assertThat(result.publicField, is("baz"));
    }

    @Test(expected = InstantiationFailureException.class)
    public void dontUseIgnoredConstructor() {
        instantiate(TestClass.class, createNamedTypeList(int.class, int.class), 0, 0);
    }

    @Test(expected = InstantiationFailureException.class)
    public void explicitConstructorIsUsedInsteadOfValidConstructor() {
        NamedTypeList types = NamedTypeList.builder(1)
                .add("publicField", String.class).build();

        instantiate(TestClassWithExplicitConstructor.class, types, "foo");
    }

    @Test(expected = InstantiationFailureException.class)
    public void explicitConstructorIsUsedInsteadOfValidPropertyAccessor() {
        NamedTypeList types = NamedTypeList.builder(1)
                .add("propertyWithAccessors", String.class).build();

        instantiate(TestClassWithExplicitConstructor.class, types, "bar");
    }

    @Test
    public void explicitPrivateConstructor() {
        NamedTypeList types = NamedTypeList.builder(1)
                .add("publicField", String.class).build();

        TestClassWithExplicitPrivateConstructor result = instantiate(TestClassWithExplicitPrivateConstructor.class, types, "foo");

        assertNotNull(result);
        assertThat(result.calledConstructor, is(2));
        assertThat(result.publicField, is("foo"));
    }

    @Test(expected = InstantiationFailureException.class)
    public void multipleConstructorAnnotationsGivesNiceError() {
        instantiate(TestClassWithMultipleExplicitConstructors.class, int.class, 1);
    }

    @Test
    public void privateClassesCanBeInstantiatedWithExplicitAnnotation() {
        NamedTypeList types = NamedTypeList.builder(1)
                .add("publicField", String.class).build();
        PrivateTestClassWithExplicitInstantiator result = instantiate(PrivateTestClassWithExplicitInstantiator.class, types, "foo");

        assertNotNull(result);
        assertThat(result.publicField, is("foo"));
    }

    public static class TestClass {
        private final int calledConstructor;

        @Reflective
        public String publicField = "";

        private String propertyWithAccessors = "";

        @Reflective
        public TestClass() { calledConstructor = 1; }

        @Reflective
        public TestClass(String s) { calledConstructor = 2; }

        @Reflective
        public TestClass(int x) { calledConstructor = 3; }

        @SuppressWarnings("unused")
        @DalesbredIgnore
        public TestClass(int x, int y) { calledConstructor = 4; }

        public String getPropertyWithAccessors() {
            return propertyWithAccessors;
        }

        @Reflective
        public void setPropertyWithAccessors(String propertyWithAccessors) {
            this.propertyWithAccessors = propertyWithAccessors;
        }
    }

    public static class TestClassWithExplicitConstructor {
        public String publicField = "";

        private String propertyWithAccessors = "";

        @SuppressWarnings("UnusedDeclaration")
        public TestClassWithExplicitConstructor() {}

        @SuppressWarnings("UnusedDeclaration")
        public TestClassWithExplicitConstructor(String publicField) {
            this.publicField = publicField;
        }

        @DalesbredInstantiator
        public TestClassWithExplicitConstructor(int wrongType) {
            this.publicField = String.valueOf(wrongType);
        }

        @SuppressWarnings("unused")
        public String getPropertyWithAccessors() {
            return propertyWithAccessors;
        }

        @Reflective
        public void setPropertyWithAccessors(String propertyWithAccessors) {
            this.propertyWithAccessors = propertyWithAccessors;
        }
    }

    public static class TestClassWithExplicitPrivateConstructor {
        private final int calledConstructor;
        public String publicField = "";

        @SuppressWarnings("UnusedDeclaration")
        public TestClassWithExplicitPrivateConstructor() {
            calledConstructor = 1;
        }

        @DalesbredInstantiator
        private TestClassWithExplicitPrivateConstructor(String publicField) {
            calledConstructor = 2;
            this.publicField = publicField;
        }
    }

    public static class TestClassWithMultipleExplicitConstructors {
        @DalesbredInstantiator
        public TestClassWithMultipleExplicitConstructors() {
        }

        @DalesbredInstantiator
        public TestClassWithMultipleExplicitConstructors(String foo) {
        }
    }

    private static class PrivateTestClassWithExplicitInstantiator {
        public String publicField = "";

        @DalesbredInstantiator
        private PrivateTestClassWithExplicitInstantiator(String publicField) {
            this.publicField = publicField;
        }
    }

    @Nullable
    private <T,V> T instantiate(@NotNull Class<T> cl, @NotNull Class<V> type, V value) {
        return instantiate(cl, createNamedTypeList(type), value);
    }

    @Nullable
    private <T> T instantiate(Class<T> cl, NamedTypeList namedTypeList, Object... values) {
        Instantiator<T> instantiator = instantiatorRegistry.findInstantiator(cl, namedTypeList);
        InstantiatorArguments arguments = new InstantiatorArguments(namedTypeList, values);
        return instantiator.instantiate(arguments);
    }

    @NotNull
    private static NamedTypeList createNamedTypeList(@NotNull Class<?>... types) {
        NamedTypeList.Builder list = NamedTypeList.builder(types.length);
        for (int i = 0; i < types.length; i++)
            list.add("name" + i, types[i]);
        return list.build();
    }

    private static void assertAssignable(@NotNull Class<?> target, @NotNull Class<?> source) {
        assertThat(TypeUtils.isAssignable(target, source), is(true));
    }

    public static class InaccessibleConstructor {

        @Reflective
        InaccessibleConstructor(int x) { }
    }
}
