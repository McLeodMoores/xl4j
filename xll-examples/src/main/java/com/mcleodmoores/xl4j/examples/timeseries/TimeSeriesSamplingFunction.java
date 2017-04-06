/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.List;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 *
 */
public class TimeSeriesSamplingFunction implements TimeSeriesBiFunction<List<LocalDate>, TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts, final List<LocalDate> schedule) {
    ArgumentChecker.notNull(ts, "ts");
    ArgumentChecker.notNull(schedule, "schedule");
    final TimeSeries result = TimeSeries.newTimeSeries();
    // adds a null if there is no value in the series
    schedule.parallelStream().forEach((date) -> result.put(date, ts.get(date)));
    return result;
  }

}
