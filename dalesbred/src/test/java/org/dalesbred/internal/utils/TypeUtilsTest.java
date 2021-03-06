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

package org.dalesbred.internal.utils;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import static org.dalesbred.internal.utils.TypeUtils.arrayType;
import static org.dalesbred.internal.utils.TypeUtils.rawType;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypeUtilsTest {

    @Test
    public void rawTypeOfGenericType() throws Exception {
        Field genericField = ExampleClass.class.getField("stringList");
        assertThat(rawType(genericField.getGenericType()), is((Type) List.class));
        assertThat(rawType(genericField.getType()), is((Type) List.class));
    }

    @Test
    public void rawTypeOfArrayType() throws Exception {
        Field genericField = ExampleClass.class.getField("stringArray");
        assertThat(rawType(genericField.getGenericType()), is((Type) String[].class));
        assertThat(rawType(genericField.getType()), is((Type) String[].class));
    }

    @Test
    public void rawTypeOfSimpleType() throws Exception {
        Field genericField = ExampleClass.class.getField("string");
        assertThat(rawType(genericField.getGenericType()), is((Type) String.class));
        assertThat(rawType(genericField.getType()), is((Type) String.class));
    }

    @Test
    public void arrayTypes() {
        assertThat(arrayType(String.class), is((Object) String[].class));
        assertThat(arrayType(Integer.class), is((Object) Integer[].class));
        assertThat(arrayType(int.class), is((Object) int[].class));
    }

    @SuppressWarnings("unused")
    static class ExampleClass {
        public List<String> stringList;
        public String[] stringArray;
        public String string;
    }
}
