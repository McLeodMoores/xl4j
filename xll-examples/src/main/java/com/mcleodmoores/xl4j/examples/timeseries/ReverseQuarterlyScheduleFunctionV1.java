/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Collections;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Generates a quarterly schedule from the end date to start date inclusive. The series is returned
 * with increasing dates.
 */
public class ReverseQuarterlyScheduleFunctionV1 implements ScheduleFunctionV1 {

  @Override
  public Schedule apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    LocalDate date = endInclusive;
    final Schedule result = new Schedule();
    int i = 0;
    while (!date.isBefore(start)) {
      result.add(date);
      i += 3;
      // offset from start to avoid end-of-month effects e.g. don't want 30 Nov <- 28 Feb <- 31 May
      date = endInclusive.minusMonths(i);
    }
    Collections.reverse(result);
    return result;
  }

}
