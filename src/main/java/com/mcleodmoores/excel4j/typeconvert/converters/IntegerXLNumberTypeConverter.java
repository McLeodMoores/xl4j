package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from integers to Excel Numbers and back again.
 */
public final class IntegerXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  // REVIEW emcleod 21-7-2014 inconsistent with the Double, Long and Short converters
  public IntegerXLNumberTypeConverter() {
    super(Integer.TYPE, XLNumber.class);
  }

  @Override
  public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
    return XLNumber.of((Integer) from);
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
    return (int) ((XLNumber) from).getValue();
  }
}
