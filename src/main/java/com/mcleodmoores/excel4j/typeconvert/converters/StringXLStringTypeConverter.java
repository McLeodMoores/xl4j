package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLString;

/**
 * Type converter to convert from Excel Strings to Java Strings and back again.
 */
public final class StringXLStringTypeConverter extends AbstractTypeConverter {

  /**
   * Default Constructor.
   */
  public StringXLStringTypeConverter() {
    super(String.class, XLString.class);
  }

  @Override
  public Object toXLValue(final Class<?> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLString.of((String) from);
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return ((XLString) from).getValue();
  }

}
