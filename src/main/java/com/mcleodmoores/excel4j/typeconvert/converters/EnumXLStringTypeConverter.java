package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter for general objects into XLObject handles.  Note the lower priority,
 * which means all the other converters get a crack at doing something nicer first.
 */
public class EnumXLStringTypeConverter extends AbstractTypeConverter {

  private static final int ENUM_CONVERTER_PRIORITY = 7;

  /**
   * Default constructor.
   */
  public EnumXLStringTypeConverter() {
    super(Enum.class, XLString.class, ENUM_CONVERTER_PRIORITY);
  }

  @Override
  public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
    Enum<?> enumeration = (Enum<?>) from;
    return XLString.of(enumeration.name());
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
    XLString string = (XLString) from;
    Class<? extends Enum> enumClass = (Class<? extends Enum>) expectedClass;
    return Enum.valueOf(enumClass, string.getValue());
  }

}
