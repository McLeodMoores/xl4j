/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLNumber;

/**
 * Type converter to convert from Objects to Excel Numbers and back again.
 */
public final class ObjectXLNumberTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = -7;

  /**
   * Default constructor.
   */
  public ObjectXLNumberTypeConverter() {
    super(Object.class, XLNumber.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (from instanceof Number) {
      final Number num = (Number) from;
      return XLNumber.of(num.doubleValue());
    }
    throw new Excel4JRuntimeException("could not convert from " + from.getClass() + " to XLNumber");
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (from instanceof XLNumber) {
      return ((XLNumber) from).getValue();
    }
    throw new Excel4JRuntimeException("Could not convert from " + from.getClass() + " to double");
  }
}
