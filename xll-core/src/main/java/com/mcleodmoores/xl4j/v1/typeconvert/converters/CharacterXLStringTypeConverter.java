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
 * Type converter to convert from Integers to Excel strings and back again. This converter has a low priority, as it is more likely that
 * conversion to and from XLNumber is required.
 */
public final class CharacterXLStringTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = -1;

  /**
   * Default constructor.
   */
  public CharacterXLStringTypeConverter() {
    super(Character.class, XLString.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    // cast here is for consistent behaviour with other converters
    return XLString.of(((Character) from).toString());
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (((XLString) from).getValue().length() == 1) {
      return Character.valueOf(((XLString) from).getValue().charAt(0));
    }
    throw new XL4JRuntimeException("String is too long to convert to Character");
  }
}
