package com.mcleodmoores.excel4j.typeconvert.converters;

import java.math.BigDecimal;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLNumber;

/**
 * Type converter to convert from Doubles to Excel Numbers and back again.
 */
public final class BigDecimalXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public BigDecimalXLNumberTypeConverter() {
    super(BigDecimal.class, XLNumber.class);
  }

  @Override
  public Object toXLValue(final Class<?> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of(((BigDecimal) from).doubleValue());
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return BigDecimal.valueOf(((XLNumber) from).getValue());
  }
}
