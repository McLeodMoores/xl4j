/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.yieldcurve.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Converts XLStrings to DayCount and vice versa.
 */
public class DayCountXLStringTypeConverter extends AbstractTypeConverter {

  /**
   * Default constructor.
   */
  public DayCountXLStringTypeConverter() {
    super(DayCount.class, XLString.class);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLString.of(((DayCount) from).getName());
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return DayCountFactory.of(((XLString) from).getValue());
  }
}
