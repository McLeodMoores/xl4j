/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Type converter to convert from Shorts to Excel strings and back again. This converter has a low priority, as it is more likely that
 * conversion to and from XLNumber is required.
 */
public final class ShortXLStringTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = -1;

  /**
   * Default constructor.
   */
  public ShortXLStringTypeConverter() {
    super(Short.class, XLString.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    // cast here is for consistent behaviour with other converters
    return XLString.of(((Short) from).toString());
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return Short.valueOf(((XLString) from).getValue());
  }
}
