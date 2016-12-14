/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.yieldcurve.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.values.XLString;
import com.opengamma.util.money.Currency;

/**
 * Converts XLString to Currency and vice versa.
 */
public class CurrencyXLStringTypeConverter extends AbstractTypeConverter {

  /**
   * Default constructor.
   */
  public CurrencyXLStringTypeConverter() {
    super(Currency.class, XLString.class);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLString.of(((Currency) from).getCode());
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return Currency.of(((XLString) from).getValue());
  }
}
