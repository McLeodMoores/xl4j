package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Longs to Excel Numbers and back again.
 */
public final class PrimitiveLongXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public PrimitiveLongXLNumberTypeConverter() {
    super(Long.TYPE, XLNumber.class);
  }

  @Override
  public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
    return XLNumber.of((Long) from);
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
    return (long) ((XLNumber) from).getValue();
  }
}
