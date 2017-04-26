/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.util;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

/**
 * Utility classes for dates.
 */
public final class XlDateUtils {
  /** The epoch year used in Excel */
  private static final int XL_EPOCH_YEAR = 1900;
  /**
   * Note this is does not work in 1904 mode (which is an odd Mac compatibility mode), but with 0-Jan-1900. + 1 below is to correct for the
   * extra leap day added by Lotus-123/Excel.
   */
  private static final long DAYS_FROM_XL_EPOCH = ChronoUnit.DAYS.between(LocalDate.of(XL_EPOCH_YEAR, 1, 1), LocalDate.ofEpochDay(0)) + 1;

  /**
   * Calculates the number of days from the Excel epoch.
   *
   * @param date
   *          the date
   * @return the number of days
   */
  public static long getDaysFromXlEpoch(final LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    return date.toEpochDay() + DAYS_FROM_XL_EPOCH;
  }

  private XlDateUtils() {
  }
}
