/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.yieldcurve.converters;

import java.lang.reflect.Type;

import com.jimmoores.quandl.util.ArgumentChecker;
import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.values.XLString;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;

/**
 * Converts XLString to BusinessDayConvention and vice versa.
 */
public class BusinessDayConventionXLStringTypeConverter extends AbstractTypeConverter {

  /**
   * Default constructor.
   */
  public BusinessDayConventionXLStringTypeConverter() {
    super(BusinessDayConvention.class, XLString.class);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLString.of(((BusinessDayConvention) from).getName());
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return BusinessDayConventionFactory.of(((XLString) from).getValue());
  }
}
