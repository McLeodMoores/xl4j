/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import java.lang.reflect.Field;

import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * A class holding the converter required to convert the value of a field to the appropriate type.
 */
public class ObjectFieldGetter extends AbstractFieldGetter {
  private final TypeConverter _converter;

  /**
   * @param field
   *          the field, not null
   * @param converter
   *          the result converter, not null
   */
  public ObjectFieldGetter(final Field field, final TypeConverter converter) {
    super(field);
    _converter = ArgumentChecker.notNull(converter, "converter");
  }

  @Override
  public XLValue get(final Object object) {
    try {
      return (XLValue) _converter.toXLValue(getField().get(object));
    } catch (final IllegalAccessException e) {
      throw new XL4JRuntimeException("Error getting field " + getFieldName() + " from " + getFieldDeclaringClass(), e);
    }
  }
}
