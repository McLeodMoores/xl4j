/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries.troubleshooting;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.examples.timeseries.Schedule;
import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Generates a schedule with intervals of n months from the start date to end date inclusive.
 */
@XLNamespace("Schedule.")
public class ForwardNMonthsScheduleFunctionV2 implements ScheduleFunctionV2 {
  private final int _n;

  /**
   * @param n
   *          the number of months in the interval
   */
  @XLFunction(
      name = "ForwardNMonths",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT,
      description = "Generates a schedule from the start to end date",
      category = "Schedule")
  public ForwardNMonthsScheduleFunctionV2(
      @XLParameter(name = "Number of months") final int n) {
    _n = n;
  }

  @XLFunction(
      name = "GenerateForwardNMonthsSchedule",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT,
      description = "Generates a schedule from the start to end date",
      category = "Schedule")
  @Override
  public Schedule apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    ArgumentChecker.isTrue(_n > 0, "The interval must be greater than zero: have {}", _n);
    LocalDate date = start;
    final Schedule result = new Schedule();
    int i = 0;
    while (!date.isAfter(endInclusive)) {
      result.add(date);
      // offset from start to avoid end-of-month effects e.g. don't want 31 Nov -> 28 Feb -> 28 May
      i += _n;
      date = start.plusMonths(i);
    }
    return result;
  }
}
