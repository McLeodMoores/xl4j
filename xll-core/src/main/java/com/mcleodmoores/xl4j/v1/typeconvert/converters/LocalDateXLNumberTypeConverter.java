/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Type converter to convert from Excel Numbers and back again. TODO: refactor out into lots of more specialized converters for better
 * performance.
 */
public final class LocalDateXLNumberTypeConverter extends AbstractTypeConverter {
  /** The epoch year used in Excel */
  private static final int EXCEL_EPOCH_YEAR = 1900;
  /**
   * Note this is does not work in 1904 mode (which is an odd Mac compatibility mode), but with 0-Jan-1900. + 1 below is to correct for the
   * extra leap day added by Lotus-123/Excel.
   */
  private static final long DAYS_FROM_EXCEL_EPOCH = ChronoUnit.DAYS.between(LocalDate.of(EXCEL_EPOCH_YEAR, 1, 1), LocalDate.ofEpochDay(0))
      + 1;

  /**
   * Default constructor.
   */
  public LocalDateXLNumberTypeConverter() {
    super(LocalDate.class, XLNumber.class);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of(((LocalDate) from).toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final long epochDays = (long) ((XLNumber) from).getValue() - DAYS_FROM_EXCEL_EPOCH;
    return LocalDate.ofEpochDay(epochDays);
  }
}
