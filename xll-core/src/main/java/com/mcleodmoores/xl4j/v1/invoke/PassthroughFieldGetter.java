/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import java.lang.reflect.Field;

import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * A class that passes through the value of a field without further conversion.
 */
public class PassthroughFieldGetter extends AbstractFieldGetter {

  /**
   * @param field
   *          the field, not null
   */
  public PassthroughFieldGetter(final Field field) {
    super(field);
  }

  @Override
  public XLValue get(final Object object) {
    try {
      return (XLValue) getField().get(object);
    } catch (final IllegalAccessException e) {
      throw new XL4JRuntimeException("Error getting field", e);
    }
  }

}
