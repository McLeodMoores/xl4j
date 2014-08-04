package com.mcleodmoores.excel4j.typeconvert.converters;

import java.math.BigDecimal;

import com.mcleodmoores.excel4j.typeconvert.AbstractScalarTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Doubles to Excel Numbers and back again.
 */
public final class BigDecimalXLNumberTypeConverter extends AbstractScalarTypeConverter {
  /**
   * Default constructor.
   */
  public BigDecimalXLNumberTypeConverter() {
    super(BigDecimal.class, XLNumber.class);
  }

  @Override
  public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of(((BigDecimal) from).doubleValue());
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
    ArgumentChecker.notNull(from, "from");
    return BigDecimal.valueOf(((XLNumber) from).getValue());
  }
}
