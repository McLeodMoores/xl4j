package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLBoolean;

/**
 * Type converter to convert from Objects to Excel Numbers and back again.
 */
public final class ObjectXLBooleanTypeConverter extends AbstractTypeConverter {
  private static final int PRIORITY = 7;

  /**
   * Default constructor.
   */
  public ObjectXLBooleanTypeConverter() {
    super(Object.class, XLBoolean.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (from instanceof Boolean) {
      return XLBoolean.from((Boolean) from);
    } else {
      throw new Excel4JRuntimeException("could not convert from " + from.getClass() + " to XLNumber");
    }
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return (Boolean) ((XLBoolean) from).getValue();
  }
}
