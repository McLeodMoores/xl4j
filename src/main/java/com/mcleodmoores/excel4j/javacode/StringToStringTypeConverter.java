package com.mcleodmoores.excel4j.javacode;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Excel Numbers and back again.
 * TODO: refactor out into lots of more specialized converters for better performance.
 */
public final class StringToStringTypeConverter implements TypeConverter {

  @Override
  public boolean canConvert(final ExcelToJavaTypeMapping excelToJava) {
    return excelToJava.getExcelType().isInstance(XLString.class)
        && excelToJava.getJavaType().getClass().isInstance(String.class);
  }

  @Override
  public boolean canConvert(final JavaToExcelTypeMapping javaToExcel) {
    return javaToExcel.getExcelType().isInstance(XLString.class)
        && javaToExcel.getJavaType().getClass().isInstance(String.class);
  }

  @Override
  public XLValue toXLValue(final JavaToExcelTypeMapping requiredConversion, final Object from) {
    if (from instanceof String) {
      return XLString.of((String) from);
    } else {
      throw new Excel4JRuntimeException("Unsupported type " + from);
    }
  }

  @Override
  public Object toJavaObject(final ExcelToJavaTypeMapping requiredConversion, final XLValue from) {
    if (from instanceof XLString) {
      XLString xlNumber = (XLString) from;
      return xlNumber.getValue();
    } else {
      throw new Excel4JRuntimeException("Unsupported type " + from);
    }
  }

  @Override
  public int getPriority() {
    return 0;
  }

}
