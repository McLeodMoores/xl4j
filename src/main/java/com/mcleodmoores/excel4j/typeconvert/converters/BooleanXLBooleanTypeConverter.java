package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Excel Booleans and back again.
 */
public final class BooleanXLBooleanTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public BooleanXLBooleanTypeConverter() {
    super(Boolean.class, XLBoolean.class);
  }

  @Override
  public XLValue toXLValue(final Object from) {
    return XLBoolean.from((Boolean) from);
  }

  @Override
  public Object toJavaObject(final XLValue from) {
    return (boolean) ((XLBoolean) from).getValue();
  }
}
