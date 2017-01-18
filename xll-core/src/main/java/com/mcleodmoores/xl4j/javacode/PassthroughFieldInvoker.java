/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import java.lang.reflect.Field;

import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * A class that passes through the value of a field without further conversion.
 */
public class PassthroughFieldInvoker extends AbstractFieldInvoker {

  /**
   * @param field
   *          the field, not null
   */
  public PassthroughFieldInvoker(final Field field) {
    super(field);
  }

  @Override
  public XLValue invoke(final Object object) {
    try {
      return (XLValue) getField().get(object);
    } catch (final IllegalAccessException e) {
      throw new Excel4JRuntimeException("Error getting field", e);
    }
  }

}
