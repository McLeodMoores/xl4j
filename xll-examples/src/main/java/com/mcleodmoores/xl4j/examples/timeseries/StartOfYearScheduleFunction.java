/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.function.BiFunction;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.XLFunctions;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Generates a start-of-year schedule of dates from the start date to end date inclusive.
 */
@XLNamespace("Schedule.")
@XLFunctions(
    prefix = "StartOfYear",
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Generates a start-of-year schedule",
    category = "Schedule")
public class StartOfYearScheduleFunction implements BiFunction<LocalDate, LocalDate, Schedule> {

  @Override
  public Schedule apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    final Schedule result = new Schedule();
    LocalDate date = start.getDayOfYear() == 1 ? start : start.with(TemporalAdjusters.firstDayOfNextYear());
    while (!date.isAfter(endInclusive)) {
      result.add(date);
      date = date.plusYears(1);
    }
    return result;
  }

}
