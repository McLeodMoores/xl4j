/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.function.BiFunction;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.XLFunctions;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Generates a monthly schedule from the start date to end date inclusive.
 */
@XLNamespace("Schedule.")
@XLFunctions(
    prefix = "ForwardMonthly",
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Generates a monthly schedule from the start to end date",
    category = "Schedule")
public class ForwardMonthlyScheduleFunction implements BiFunction<LocalDate, LocalDate, Schedule> {

  @Override
  public Schedule apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    LocalDate date = start;
    final Schedule result = new Schedule();
    int i = 0;
    while (!date.isAfter(endInclusive)) {
      result.add(date);
      // offset from start to avoid end-of-month effects e.g. don't want 31 Jan -> 28 Feb -> 28 Mar
      date = start.plusMonths(++i);
    }
    return result;
  }
}
