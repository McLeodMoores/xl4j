package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractScalarTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Longs to Excel Numbers and back again.
 */
public final class LongXLNumberTypeConverter extends AbstractScalarTypeConverter {
  /**
   * Default constructor.
   */
  public LongXLNumberTypeConverter() {
    super(Long.class, XLNumber.class);
  }

  @Override
  public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of((Long) from);
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
    ArgumentChecker.notNull(from, "from");
    return (long) ((XLNumber) from).getValue();
  }
}
