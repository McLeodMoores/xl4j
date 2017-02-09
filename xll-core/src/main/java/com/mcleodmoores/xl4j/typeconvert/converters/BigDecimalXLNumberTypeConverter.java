/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.values.XLNumber;

/**
 * Type converter to convert from BigDecimals to XLNumbers and back again.
 */
public final class BigDecimalXLNumberTypeConverter extends AbstractTypeConverter {

  /**
   * Default constructor.
   */
  public BigDecimalXLNumberTypeConverter() {
    super(BigDecimal.class, XLNumber.class);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of(((BigDecimal) from).doubleValue());
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return BigDecimal.valueOf(((XLNumber) from).getValue());
  }
}
