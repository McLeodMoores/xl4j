package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractScalarTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Excel Strings to Java Strings and back again.
 */
public final class StringXLStringTypeConverter extends AbstractScalarTypeConverter {

  /**
   * Default Constructor.
   */
  public StringXLStringTypeConverter() {
    super(String.class, XLString.class);
  }

  @Override
  public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLString.of((String) from);
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
    ArgumentChecker.notNull(from, "from");
    return ((XLString) from).getValue();
  }

}
