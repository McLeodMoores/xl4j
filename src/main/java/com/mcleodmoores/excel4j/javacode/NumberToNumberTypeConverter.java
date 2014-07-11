package com.mcleodmoores.excel4j.javacode;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Excel Numbers and back again.
 * TODO: refactor out into lots of more specialized converters for better performance.
 */
public final class NumberToNumberTypeConverter implements TypeConverter {

  @Override
  public boolean canConvert(final ExcelToJavaTypeMapping excelToJava) {
    return excelToJava.getExcelType().isInstance(XLNumber.class)
        && excelToJava.getJavaType().getClass().isInstance(Number.class);
  }

  @Override
  public boolean canConvert(final JavaToExcelTypeMapping javaToExcel) {
    return javaToExcel.getExcelType().isInstance(XLNumber.class)
        && javaToExcel.getJavaType().getClass().isInstance(Number.class);
  }

  @Override
  public XLValue toXLValue(final JavaToExcelTypeMapping requiredConversion, final Object from) {
    if (from instanceof Number) {
      return XLNumber.of(((Number) from).doubleValue());
    } else {
      throw new Excel4JRuntimeException("Unsupported type " + from);
    }
  }

  @Override
  public Object toJavaObject(final ExcelToJavaTypeMapping requiredConversion, final XLValue from) {
    if (from instanceof XLNumber) {
      XLNumber xlNumber = (XLNumber) from;
      @SuppressWarnings("unchecked")
      Class<? extends Number> javaClass = (Class<? extends Number>) requiredConversion.getJavaType().getClass();
      if (javaClass == Integer.class) {
        return (int) xlNumber.getValue();
      } else if (javaClass == Double.class) {
        return xlNumber.getValue();
      } else if (javaClass == Long.class) {
        return (long) xlNumber.getValue();
      } else if (javaClass == Byte.class) {
        return (byte) xlNumber.getValue();
      } else if (javaClass == Short.class) {
        return (short) xlNumber.getValue();
      } else if (javaClass == Float.class) {
        return (float) xlNumber.getValue();
      } else if (javaClass == BigInteger.class) {
        return BigInteger.valueOf((long) xlNumber.getValue());
      } else if (javaClass == BigDecimal.class) {
        return BigDecimal.valueOf(xlNumber.getValue());
      } else {
        throw new Excel4JRuntimeException("Impossible Number subclass: " + javaClass);
      }
    } else {
      throw new Excel4JRuntimeException("Unsupported type " + from);
    }
  }

  @Override
  public int getPriority() {
    return 0;
  }

}
