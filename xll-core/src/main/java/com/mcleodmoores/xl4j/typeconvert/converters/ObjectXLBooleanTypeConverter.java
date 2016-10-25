/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLBoolean;

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
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (from instanceof Boolean) {
      return XLBoolean.from((Boolean) from);
    }
    throw new Excel4JRuntimeException("Could not convert from " + from.getClass() + " to XLBoolean");
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (from instanceof XLBoolean) {
      return ((XLBoolean) from).getValue();
    }
    throw new Excel4JRuntimeException("Could not convert from " + from.getClass() + " to boolean");
  }
}
