/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLString;

/**
 * Type converter to convert from longs to Excel strings and back again. This converter
 * has a low priority, as it is more likely that conversion to and from XLNumber is required.
 */
public final class PrimitiveLongXLStringTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = -1;

  /**
   * Default constructor.
   */
  public PrimitiveLongXLStringTypeConverter() {
    super(Long.TYPE, XLString.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    // cast here is for consistent behaviour with other converters
    return XLString.of(((Long) from).toString());
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return Long.parseLong(((XLString) from).getValue());
  }
}
