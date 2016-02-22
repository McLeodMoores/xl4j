package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLNumber;

/**
 * Type converter to convert from BigDecimals to XLNumbers and back again.
 */
public final class BigDecimalXLNumberTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = 11;
  /**
   * Default constructor.
   */
  public BigDecimalXLNumberTypeConverter() {
    super(BigDecimal.class, XLNumber.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of(((BigDecimal) from).doubleValue());
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return BigDecimal.valueOf(((XLNumber) from).getValue());
  }
}
