package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Doubles to Excel Numbers and back again.
 */
public final class DoubleXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public DoubleXLNumberTypeConverter() {
    super(Double.class, XLNumber.class);
  }
  
  @Override
  public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
    return XLNumber.of((Double) from);
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
    return (double) ((XLNumber) from).getValue();
  }
}
