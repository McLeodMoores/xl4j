package com.mcleodmoores.excel4j.typeconvert.converters;

import java.math.BigInteger;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Longs to Excel Numbers and back again.
 */
public final class BigIntegerXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public BigIntegerXLNumberTypeConverter() {
    super(Long.class, XLNumber.class);
  }

  @Override
  public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
    return XLNumber.of(((BigInteger) from).doubleValue());
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
    return BigInteger.valueOf((long) ((XLNumber) from).getValue());
  }
}
