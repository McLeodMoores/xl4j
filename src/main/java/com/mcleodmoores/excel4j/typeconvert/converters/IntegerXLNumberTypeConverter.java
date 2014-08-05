package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLNumber;

/**
 * Type converter to convert from integers to Excel Numbers and back again.
 */
public final class IntegerXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public IntegerXLNumberTypeConverter() {
    super(Integer.class, XLNumber.class);
  }

  @Override
  public Object toXLValue(final Class<?> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of((Integer) from);
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return (int) ((XLNumber) from).getValue();
  }
}
