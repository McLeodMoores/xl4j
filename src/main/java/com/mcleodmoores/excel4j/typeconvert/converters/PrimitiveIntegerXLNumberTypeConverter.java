package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractScalarTypeConverter;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from integers to Excel Numbers and back again.
 */
public final class PrimitiveIntegerXLNumberTypeConverter extends AbstractScalarTypeConverter {
  /**
   * Default constructor.
   */
  public PrimitiveIntegerXLNumberTypeConverter() {
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
