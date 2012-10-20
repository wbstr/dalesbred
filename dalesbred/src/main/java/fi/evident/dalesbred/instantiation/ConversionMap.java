/*
 * Copyright (c) 2012 Evident Solutions Oy
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

package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.evident.dalesbred.utils.Primitives.isAssignableByBoxing;
import static fi.evident.dalesbred.utils.Primitives.wrap;

final class ConversionMap {

    private final Map<Class<?>, List<TypeConversion<?,?>>> mappings = new HashMap<Class<?>, List<TypeConversion<?, ?>>>();

    void register(@NotNull TypeConversion<?, ?> coercion) {
        Class<?> source = wrap(coercion.getSource());

        List<TypeConversion<?,?>> items = mappings.get(source);
        if (items == null) {
            items = new ArrayList<TypeConversion<?, ?>>();
            mappings.put(source, items);
        }

        items.add(coercion);
    }

    @Nullable
    <S,T> TypeConversion<S,T> findConversion(@NotNull Class<S> source, @NotNull Class<T> target) {
        for (Class<? super S> cl = wrap(source); cl != null; cl = cl.getSuperclass()) {
            List<TypeConversion<?,?>> candidates = mappings.get(cl);
            if (candidates != null)
                for (TypeConversion<?,?> coercion : candidates)
                    if (isAssignableByBoxing(target, coercion.getTarget()))
                        return coercion.cast(source, target);
        }

        return null;
    }
}