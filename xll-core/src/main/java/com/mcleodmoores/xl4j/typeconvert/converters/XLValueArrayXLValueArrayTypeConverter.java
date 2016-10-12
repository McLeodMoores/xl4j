/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Type converter to convert from <code>XLValue[]</code> to <code>XLValue[]</code> and back again.
 */
public final class XLValueArrayXLValueArrayTypeConverter extends AbstractTypeConverter {
  /** The priority of this converter */
  private static final int PRIORITY = 7;

  /**
   * Default Constructor.
   */
  public XLValueArrayXLValueArrayTypeConverter() {
    super(XLValue[].class, XLValue[].class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return from;
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return from;
  }

}
