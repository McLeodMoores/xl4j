package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLBoolean;

/**
 * Type converter to convert from Excel Booleans and back again.
 */
public final class PrimitiveBooleanXLBooleanTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public PrimitiveBooleanXLBooleanTypeConverter() {
    super(Boolean.TYPE, XLBoolean.class);
  }

  @Override
  public Object toXLValue(final Class<?> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLBoolean.from((boolean) from);
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return (boolean) ((XLBoolean) from).getValue();
  }
}
