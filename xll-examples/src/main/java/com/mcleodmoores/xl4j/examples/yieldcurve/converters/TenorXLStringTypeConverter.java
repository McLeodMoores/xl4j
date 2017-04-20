/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.yieldcurve.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Converts XLStrings to Tenors and vice versa. Note that this method strips the preceding "P"
 * if present.
 */
public class TenorXLStringTypeConverter extends AbstractTypeConverter {

  /**
   * Default constructor.
   */
  public TenorXLStringTypeConverter() {
    super(Tenor.class, XLString.class);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    final Tenor tenor = (Tenor) from;
    if (tenor.isBusinessDayTenor()) {
      return XLString.of(tenor.toFormattedString());
    }
    return XLString.of(tenor.toFormattedString().substring(1));
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    try {
      return Tenor.parse(((XLString) from).getValue());
    } catch (final Exception e) {
      // try with a preceding P
      return Tenor.parse("P" + ((XLString) from).getValue());
    }
  }
}
