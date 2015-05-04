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

package org.dalesbred.internal.result;

import org.dalesbred.EmptyResultException;
import org.dalesbred.NonUniqueResultException;
import org.dalesbred.result.ResultSetProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A {@link ResultSetProcessor} that adapts another a list-producing processor to produce only single result.
 */
public final class UniqueResultSetProcessor<T> implements ResultSetProcessor<T> {

    @NotNull
    private final ResultSetProcessor<List<T>> resultSetProcessor;

    public UniqueResultSetProcessor(@NotNull ResultSetProcessor<List<T>> resultSetProcessor) {
        this.resultSetProcessor = resultSetProcessor;
    }

    @Override
    @Nullable
    public T process(@NotNull ResultSet resultSet) throws SQLException {
        List<T> results = resultSetProcessor.process(resultSet);

        if (results.size() == 1)
            return results.get(0);
        else if (results.isEmpty())
            throw new EmptyResultException();
        else
            throw new NonUniqueResultException(results.size());
    }
}