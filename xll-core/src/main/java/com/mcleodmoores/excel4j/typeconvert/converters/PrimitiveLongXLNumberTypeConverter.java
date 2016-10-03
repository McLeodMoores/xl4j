package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from longs to Excel Numbers and back again.
 */
public final class PrimitiveLongXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public PrimitiveLongXLNumberTypeConverter() {
    super(Long.TYPE, XLNumber.class);
  }

  @Override
  public XLValue toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of((Long) from);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return (long) ((XLNumber) from).getValue();
  }
}
