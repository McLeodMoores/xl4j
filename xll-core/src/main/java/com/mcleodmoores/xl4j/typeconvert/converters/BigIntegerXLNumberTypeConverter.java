package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.Type;
import java.math.BigInteger;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLValue;

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
  public XLValue toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of(((BigInteger) from).doubleValue());
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return BigInteger.valueOf((long) ((XLNumber) from).getValue());
  }
}
