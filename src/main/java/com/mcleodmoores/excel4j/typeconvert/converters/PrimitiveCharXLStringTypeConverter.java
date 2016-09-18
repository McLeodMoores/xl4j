/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLString;

/**
 * Type converter to convert from doubless to Excel strings and back again. This converter
 * has a low priority, as it is more likely that conversion to and from XLNumber is required.
 */
public final class PrimitiveCharXLStringTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = -1;

  /**
   * Default constructor.
   */
  public PrimitiveCharXLStringTypeConverter() {
    super(Character.TYPE, XLString.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    // cast here is for consistent behaviour with other converters
    return XLString.of(((Character) from).toString());
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    XLString xlString = (XLString) from;
    if (xlString.getValue().length() == 1) {
      return Character.valueOf(xlString.getValue().charAt(0));
    }
    throw new Excel4JRuntimeException("Could not convert to char because more than one character");
  }
}
