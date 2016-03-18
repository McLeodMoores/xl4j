/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.examples;

import com.mcleodmoores.excel4j.TypeConversionMode;
import com.mcleodmoores.excel4j.XLFunction;

/**
 *
 */
public interface Operation<T> {

  @XLFunction(name = "Add",
              description = "Adds two objects",
              category = "Operation",
              typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  T add(T other);

  T subtract(T other);

  T multiply(T other);

  T divide(T other);

  T scale(double scale);

  T abs();

  T reciprocal();
}
