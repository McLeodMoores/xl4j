/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.XLFunctions;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Generates an annual schedule from the start date to end date inclusive.
 */
@XLNamespace("Schedule.")
@XLFunctions(prefix = "ForwardAnnual", description = "Generates an annual schedule from the start to end date", category = "Schedule")
public class ForwardAnnualScheduleFunction implements BiFunction<LocalDate, LocalDate, List<LocalDate>> {

  @Override
  public List<LocalDate> apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    LocalDate date = start;
    final List<LocalDate> result = new ArrayList<>();
    int i = 0;
    while (!date.isAfter(endInclusive)) {
      result.add(date);
      // offset from start of avoid leap day issues e.g. don't want 29 Feb 2012 -> 28 Feb 2013 -> 28 Feb 2014 -> 28 Feb 2015 -> 28 Feb 2016
      date = start.plusYears(++i);
    }
    return result;
  }
}