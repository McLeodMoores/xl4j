/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Samples a time series.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(
    prefix = "Sample",
    description = "Sampling a time series",
    category = "Time series",
    typeConversionMode = TypeConversionMode.OBJECT_RESULT)
public class TimeSeriesSamplingFunction implements TimeSeriesBiFunction<Schedule, TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts, final Schedule schedule) {
    ArgumentChecker.notNull(ts, "ts");
    ArgumentChecker.notNull(schedule, "schedule");
    final TimeSeries result = TimeSeries.newTimeSeries();
    // adds a null if there is no value in the series
    schedule.parallelStream().forEach((date) -> result.put(date, ts.get(date)));
    return result;
  }

}
