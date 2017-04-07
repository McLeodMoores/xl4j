/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.XLFunctions;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Generates an annual schedule from the end date to start date inclusive. The series is returned
 * with increasing dates.
 */
@XLNamespace("Schedule.")
@XLFunctions(prefix = "ReverseAnnual", 
  typeConversionMode=TypeConversionMode.OBJECT_RESULT,
  description = "Generates an annual schedule from the end to start date",
  category = "Schedule")
public class ReverseAnnualScheduleFunction implements BiFunction<LocalDate, LocalDate, List<LocalDate>> {

  @Override
  public List<LocalDate> apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    LocalDate date = endInclusive;
    final List<LocalDate> result = new ArrayList<>();
    int i = 0;
    while (!date.isBefore(start)) {
      result.add(date);
      // offset from end to avoid leap day issues e.g. don't want 29 Feb 2016 <- 28 Feb 2015 <- 28 Feb 2014 <- 28 Feb 2013 <- 28 Feb 2012
      date = endInclusive.minusYears(++i);
    }
    Collections.reverse(result);
    return result;
  }

}
