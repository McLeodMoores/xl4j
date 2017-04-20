/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;
import java.math.BigInteger;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Type converter to convert from Longs to Excel Numbers and back again.
 */
public final class BigIntegerXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public BigIntegerXLNumberTypeConverter() {
    super(BigInteger.class, XLNumber.class);
  }

  @Override
  public XLValue toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of(((BigInteger) from).doubleValue());
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return BigInteger.valueOf((long) ((XLNumber) from).getValue());
  }
}
