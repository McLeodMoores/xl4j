package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Excel Strings to Java Strings and back again.
 */
public final class XLValueArrayXLValueArrayTypeConverter extends AbstractTypeConverter {

  private static final int PRIORITY = 7;

  /**
   * Default Constructor.
   */
  public XLValueArrayXLValueArrayTypeConverter() {
    super(XLValue[].class, XLValue[].class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return from;
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return from;
  }

}
