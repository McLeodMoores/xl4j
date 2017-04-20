/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Type converter to convert from Objects to Excel booleans and back again.
 */
public final class ObjectXLBooleanTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = -7;

  /**
   * Default constructor.
   */
  public ObjectXLBooleanTypeConverter() {
    super(Object.class, XLBoolean.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (from instanceof Boolean) {
      return XLBoolean.from((Boolean) from);
    }
    throw new XL4JRuntimeException("Could not convert from " + from.getClass() + " to XLBoolean");
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (from instanceof XLBoolean) {
      return ((XLBoolean) from).getValue();
    }
    throw new XL4JRuntimeException("Could not convert from " + from.getClass() + " to boolean");
  }
}
