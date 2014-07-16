package com.mcleodmoores.excel4j.typeconvert.converters;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Excel Numbers and back again.
 * TODO: refactor out into lots of more specialized converters for better performance.
 */
public final class LocalDateXLNumberTypeConverter extends AbstractTypeConverter {

  /**
   * Default constructor.
   */
  public LocalDateXLNumberTypeConverter() {
    super(LocalDate.class, XLNumber.class);
  }

  /**
   * Note this is does not work in 1904 mode (which is an odd Mac compatibility mode), but with 0-Jan-1900.
   * + 1 below is to correct for the extra leap day added by Lotus-123/Excel.
   */
  private static final long DAYS_FROM_EXCEL_EPOCH = ChronoUnit.DAYS.between(LocalDate.of(1900, 1, 1), LocalDate.ofEpochDay(0)) + 1;

  @Override
  public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
    return XLNumber.of(((LocalDate) from).toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
    final long epochDays = ((long) ((XLNumber) from).getValue()) - DAYS_FROM_EXCEL_EPOCH;
    return LocalDate.ofEpochDay(epochDays);
  }
}
