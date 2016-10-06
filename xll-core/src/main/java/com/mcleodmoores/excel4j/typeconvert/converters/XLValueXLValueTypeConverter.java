/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from <code>XLValue</code> to <code>XLValue</code> and back again.
 */
public final class XLValueXLValueTypeConverter extends AbstractTypeConverter {
  /** The priority of this converter */
  private static final int PRIORITY = 6;

  /**
   * Default constructor.
   */
  public XLValueXLValueTypeConverter() {
    super(XLValue.class, XLValue.class, PRIORITY);
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
