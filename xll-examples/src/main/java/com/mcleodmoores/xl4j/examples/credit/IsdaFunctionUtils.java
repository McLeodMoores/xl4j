/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.SimpleWorkingDayCalendar;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Utilities for the functions that construct ISDA curves.
 */
public final class IsdaFunctionUtils {

  /**
   * Parses a string as a Period. The initial "P" may or may not be included.
   * @param string  the string
   * @return  the period
   */
  public static Period parsePeriod(final String string) {
    ArgumentChecker.notNull(string, "string");
    if (string.toUpperCase().charAt(0) == 'P') {
      return Period.parse(string);
    }
    return Period.parse("P" + string);
  }

  /**
   * Creates a holiday calendar from dates. If the dates are null, a weekend-only calendar is returned.
   * @param holidayDates  the holidays dates, can be null
   * @return  a calendar
   */
  @SuppressWarnings("deprecation")
  public static Calendar createHolidayCalendar(final LocalDate[] holidayDates) {
    if (holidayDates == null) {
      return new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    }
    final List<LocalDate> holidays = new ArrayList<>();
    for (final LocalDate holidayDate : holidayDates) {
      holidays.add(holidayDate);
    }
    return new CalendarAdapter(new SimpleWorkingDayCalendar("Holidays", holidays, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
  }
  /**
   * Restricted constructor.
   */
  private IsdaFunctionUtils() {
  }
}
