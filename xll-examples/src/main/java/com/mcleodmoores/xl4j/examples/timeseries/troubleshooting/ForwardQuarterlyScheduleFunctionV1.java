/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries.troubleshooting;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.examples.timeseries.Schedule;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Generates a quarterly schedule from the start date to end date inclusive.
 */
public class ForwardQuarterlyScheduleFunctionV1 implements ScheduleFunctionV1 {

  /**
   * Restricted constructor for registration example.
   */
  ForwardQuarterlyScheduleFunctionV1() {
  }

  @Override
  public Schedule apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    LocalDate date = start;
    final Schedule result = new Schedule();
    int i = 0;
    while (!date.isAfter(endInclusive)) {
      result.add(date);
      // offset from start to avoid end-of-month effects e.g. don't want 31 Nov -> 28 Feb -> 28 May
      i += 3;
      date = start.plusMonths(i);
    }
    return result;
  }
}
