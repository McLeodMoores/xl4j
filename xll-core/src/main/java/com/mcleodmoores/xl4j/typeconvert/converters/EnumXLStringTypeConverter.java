/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JReflectionUtils;
import com.mcleodmoores.xl4j.values.XLString;

/**
 * Type converter for general objects into XLObject handles. Note the lower priority, which means all the other converters get a crack at
 * doing something nicer first.
 */
public class EnumXLStringTypeConverter extends AbstractTypeConverter {
  /** The converter priority */
  private static final int ENUM_CONVERTER_PRIORITY = 7;

  /**
   * Default constructor.
   */
  public EnumXLStringTypeConverter() {
    super(Enum.class, XLString.class, ENUM_CONVERTER_PRIORITY);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    final Enum<?> enumeration = (Enum<?>) from;
    return XLString.of(enumeration.name());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(expectedType, "expectedClass");
    ArgumentChecker.notNull(from, "from");
    final XLString string = (XLString) from;
    final Class<? extends Enum> enumClass = (Class<? extends Enum>) Excel4JReflectionUtils.reduceToClass(expectedType);
    return Enum.valueOf(enumClass, string.getValue());
  }

}
