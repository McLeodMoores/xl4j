/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import java.lang.reflect.Field;

import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * A class holding the converter required to convert the value of a field to the appropriate type.
 */
public class ObjectFieldInvoker extends AbstractFieldInvoker {
  private final TypeConverter _converter;

  /**
   * @param field
   *          the field, not null
   * @param converter
   *          the result converter, not null
   */
  public ObjectFieldInvoker(final Field field, final TypeConverter converter) {
    super(field);
    _converter = ArgumentChecker.notNull(converter, "converter");
  }

  @Override
  public XLValue invoke(final Object object) {
    try {
      return (XLValue) _converter.toXLValue(getField().get(object));
    } catch (final IllegalAccessException e) {
      throw new Excel4JRuntimeException("Error getting field", e);
    }
  }
}
