/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Type converter to convert from ints to Excel strings and back again. This converter has a low priority, as it is more likely that
 * conversion to and from XLNumber is required.
 */
public final class PrimitiveIntegerXLStringTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = -1;

  /**
   * Default constructor.
   */
  public PrimitiveIntegerXLStringTypeConverter() {
    super(Integer.TYPE, XLString.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    // cast here is for consistent behaviour with other converters
    return XLString.of(((Integer) from).toString());
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return Integer.parseInt(((XLString) from).getValue());
  }
}
