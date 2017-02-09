/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import com.mcleodmoores.xl4j.XLFunctions;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Returns an element-by-element sum of the two time series. If a date is missing from either series, the value for
 * that date in the other series is used (if it is missing in both, the value for that date is
 * set to 0).
 */
@XLNamespace("TimeSeries.")
@XLFunctions(prefix = "Add", description = "Element-by-element addition of two time series", category = "Time series")
public class Add implements TimeSeriesBiFunction<TimeSeries, TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts1, final TimeSeries ts2) {
    ArgumentChecker.notNull(ts1, "ts1");
    ArgumentChecker.notNull(ts2, "ts2");
    final TimeSeries result = TimeSeries.of(ts1);
    // put missing values from second series or add values from ts1 and ts2
    ts2.forEach((date2, value2) -> {
      result.computeIfPresent(date2, (date1, value1) -> value2 == null ? value1 == null ? 0 : value1 : value1 + value2);
      result.putIfAbsent(date2, value2 == null ? 0 : value2);
    });
    return result;
  }
}
