/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries.troubleshooting;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.examples.timeseries.Schedule;
import com.mcleodmoores.xl4j.examples.timeseries.TimeSeries;
import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Returns a schedule with a given number of days offset.
 */
//@XLNamespace(value = "Schedule.")
public final class ScheduleAdjusterV1 {

  /**
   * Factory method.
   * @param schedule
   *          the schedule, not null
   * @return
   *          an adjuster
   */
  @XLFunction(
      name = "ScheduleAdjuster",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public static ScheduleAdjusterV1 of(
      @XLParameter(name = "schedule") final Schedule schedule) {
    return new ScheduleAdjusterV1(schedule);
  }

  private final Schedule _schedule;

  private ScheduleAdjusterV1(final Schedule schedule) {
    _schedule = ArgumentChecker.notNull(schedule, "schedule");
  }

  /**
   * Adds the number of days to each date in the schedule and returns a new schedule. The number of days
   * can be positive or negative.
   * @param days
   *          the number of days
   * @return
   *          the adjusted schedule
   */
  @XLFunction(
      name = "WithDayOffset",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public Schedule withDayOffset(
      @XLParameter(name = "days") final int days) {
    final Schedule adjusted = new Schedule();
    for (final LocalDate date : _schedule) {
      adjusted.add(date.plusDays(days));
    }
    return adjusted;
  }

  /**
   * Adds the number of weeks to each date in the schedule and returns a new schedule. The number of days
   * can be positive or negative.
   * @param weeks
   *          the number of weeks
   * @return
   *          the adjusted schedule
   */
  @XLFunction(
      name = "WithWeekOffset")
  public Schedule withWeekOffset(
      @XLParameter(name = "weeks") final int weeks) {
    final Schedule adjusted = new Schedule();
    for (final LocalDate date : _schedule) {
      adjusted.add(date.plusDays(weeks * 7));
    }
    return adjusted;
  }

  /**
   * Adds the number of months to each date in the schedule. The number of days per month can optionally
   * be set.
   * @param months
   *          the number of months
   * @param daysPerMonth
   *          the number of days per month, can be null
   * @return
   *          the adjusted schedule
   */
  @XLFunction(
      name = "WithMonthOffset",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public Schedule withMonthOffset(
      @XLParameter(name = "months") final int months,
      @XLParameter(name = "daysPerMonth", optional = true) final Integer daysPerMonth) {
    final Schedule adjusted = new Schedule();
    if (daysPerMonth != null) {
      for (final LocalDate date : _schedule) {
        adjusted.add(date.plusDays(daysPerMonth * months));
      }
    } else {
      for (final LocalDate date : _schedule) {
        adjusted.add(date.plusMonths(months));
      }
    }
    return adjusted;
  }

  /**
   * Returns a time series whose values are an intersection of the dates of this schedule and the time series.
   * @param ts
   *          a time series, not null
   * @return
   *          the time series whose dates are an intersection with this schedule
   */
  @XLFunction(
      name = "IntersectTimeSeries",
      typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public TimeSeries intersectTimeSeries(
      @XLParameter(name = "ts") final TimeSeries ts) {
    final TimeSeries result = TimeSeries.newTimeSeries();
    for (final LocalDate date : _schedule) {
      final double value = ts.get(date);
      result.put(date, value);
    }
    return result;
  }
}
