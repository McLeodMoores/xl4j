/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Type converter to convert from Excel Booleans and back again.
 */
public final class PrimitiveBooleanXLBooleanTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public PrimitiveBooleanXLBooleanTypeConverter() {
    super(Boolean.TYPE, XLBoolean.class);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLBoolean.from((boolean) from);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return (boolean) ((XLBoolean) from).getValue();
  }
}
