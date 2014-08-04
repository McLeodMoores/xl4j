package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractScalarTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter for general objects into XLObject handles.  Note the lower priority,
 * which means all the other converters get a crack at doing something nicer first.
 */
public class EnumXLStringTypeConverter extends AbstractScalarTypeConverter {

  private static final int ENUM_CONVERTER_PRIORITY = 7;

  /**
   * Default constructor.
   */
  public EnumXLStringTypeConverter() {
    super(Enum.class, XLString.class, ENUM_CONVERTER_PRIORITY);
  }

  @Override
  public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final Enum<?> enumeration = (Enum<?>) from;
    return XLString.of(enumeration.name());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
    ArgumentChecker.notNull(expectedClass, "expectedClass");
    ArgumentChecker.notNull(from, "from");
    final XLString string = (XLString) from;
    final Class<? extends Enum> enumClass = (Class<? extends Enum>) expectedClass;
    return Enum.valueOf(enumClass, string.getValue());
  }

}
