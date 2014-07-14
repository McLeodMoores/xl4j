package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Excel Numbers and back again.
 * TODO: refactor out into lots of more specialized converters for better performance.
 */
public final class IntegerXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public IntegerXLNumberTypeConverter() {
    super(Integer.class, XLNumber.class);
  }

  @Override
  public XLValue toXLValue(final Object from) {
    return XLNumber.of((Integer) from);
  }

  @Override
  public Object toJavaObject(final XLValue from) {
    return (int) ((XLNumber) from).getValue();
  }
}
