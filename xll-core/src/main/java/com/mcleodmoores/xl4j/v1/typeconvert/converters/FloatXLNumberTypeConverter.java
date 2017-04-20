/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Type converter to convert from Floats to Excel Numbers and back again.
 */
public final class FloatXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public FloatXLNumberTypeConverter() {
    super(Float.class, XLNumber.class);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    final Float f = (Float) from;
    if (f.isInfinite()) {
      return XLError.Div0;
    }
    if (f.isNaN()) {
      return XLError.NA;
    }
    return XLNumber.of(f);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return (float) ((XLNumber) from).getValue();
  }
}
