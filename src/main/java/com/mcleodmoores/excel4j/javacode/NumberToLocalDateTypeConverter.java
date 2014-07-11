package com.mcleodmoores.excel4j.javacode;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Excel Numbers and back again.
 * TODO: refactor out into lots of more specialized converters for better performance.
 */
public final class NumberToLocalDateTypeConverter implements TypeConverter {

  /**
   * Note this is does not work in 1904 mode (which is an odd Mac compatibility mode), but with 0-Jan-1900.
   * + 1 below is to correct for the extra leap day added by Lotus-123/Excel.
   */
  private static final long DAYS_FROM_EXCEL_EPOCH = ChronoUnit.DAYS.between(LocalDate.of(1900, 1, 1), LocalDate.ofEpochDay(0)) + 1;

  @Override
  public boolean canConvert(final ExcelToJavaTypeMapping excelToJava) {
    return excelToJava.getExcelType().isInstance(XLNumber.class)
        && excelToJava.getJavaType().getClass().isInstance(LocalDate.class);
  }

  @Override
  public boolean canConvert(final JavaToExcelTypeMapping javaToExcel) {
    return javaToExcel.getExcelType().isInstance(XLNumber.class)
        && javaToExcel.getJavaType().getClass().isInstance(LocalDate.class);
  }

  @Override
  public XLValue toXLValue(final JavaToExcelTypeMapping requiredConversion, final Object from) {
    if (from instanceof LocalDate) {
      LocalDate date = (LocalDate) from;
      return XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
    } else {
      throw new Excel4JRuntimeException("Unsupported type " + from);
    }
  }

  @Override
  public Object toJavaObject(final ExcelToJavaTypeMapping requiredConversion, final XLValue from) {
    if (from instanceof XLNumber) {
      XLNumber xlNumber = (XLNumber) from;
      final long epochDays = ((long) xlNumber.getValue()) - DAYS_FROM_EXCEL_EPOCH;
      return LocalDate.ofEpochDay(epochDays);
    } else {
      throw new Excel4JRuntimeException("Unsupported type " + from);
    }
  }

  @Override
  public int getPriority() {
    return 0;
  }

}
