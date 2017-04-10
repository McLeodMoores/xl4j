/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.XLFunctions;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Performs element-by-element multiplication of the two series. Any missing values are treated
 * as zeros.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Element-by-element multiplication of two time series",
    category = "Time series")
public class Multiply implements TimeSeriesBiFunction<TimeSeries, TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts1, final TimeSeries ts2) {
    ArgumentChecker.notNull(ts1, "ts1");
    ArgumentChecker.notNull(ts2, "ts2");
    final TimeSeries result = TimeSeries.newTimeSeries();
    // put all values from first series
    result.putAll(ts1);
    // put missing values from second series or multiply values from ts1 and ts2
    ts2.forEach((date2, value2) -> {
      result.computeIfPresent(date2, (date1, value1) -> value2 == null ? 0. : value1 * value2);
      result.putIfAbsent(date2, 0.);
    });
    return result;
  }
}
