/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Collections;
import java.util.function.BiFunction;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Generates a weekly schedule from the end date to start date inclusive. The series is returned
 * with increasing dates.
 */
@XLNamespace("Schedule.")
@XLFunctions(
    prefix = "ReverseWeekly",
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Generates a weekly schedule from the end to start date",
    category = "Schedule")
public class ReverseWeeklyScheduleFunction implements BiFunction<LocalDate, LocalDate, Schedule> {

  @Override
  public Schedule apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    LocalDate date = endInclusive;
    final Schedule result = new Schedule();
    while (!date.isBefore(start)) {
      result.add(date);
      date = date.minusDays(7);
    }
    Collections.reverse(result);
    return result;
  }

}
