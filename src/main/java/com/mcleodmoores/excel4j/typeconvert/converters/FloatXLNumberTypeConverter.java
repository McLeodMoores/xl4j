package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Floats to Excel Numbers and back again.
 */
public final class FloatXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public FloatXLNumberTypeConverter() {
    super(Float.class, XLNumber.class);
  }
  
  @Override
  public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
    return XLNumber.of((Float) from);
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
    return (float) ((XLNumber) from).getValue();
  }
}
