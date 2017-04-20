/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Type converter to convert from Objects to Excel strings and back again.
 */
public final class ObjectXLStringTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = -7;

  /**
   * Default constructor.
   */
  public ObjectXLStringTypeConverter() {
    super(Object.class, XLString.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (from instanceof String) {
      return XLString.of((String) from);
    }
    throw new XL4JRuntimeException("Could not convert from " + from.getClass() + " to XLString");
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (from instanceof XLString) {
      return ((XLString) from).getValue();
    }
    throw new XL4JRuntimeException("Could not convert from " + from.getClass() + " to String");
  }
}
