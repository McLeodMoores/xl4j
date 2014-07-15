package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from shorts to Excel Numbers and back again.
 */
public final class ShortXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public ShortXLNumberTypeConverter() {
    super(Short.class, XLNumber.class);
  }

  @Override
  public XLValue toXLValue(final Object from) {
    return XLNumber.of((Short) from);
  }

  @Override
  public Object toJavaObject(final XLValue from) {
    return (short) ((XLNumber) from).getValue();
  }
}
