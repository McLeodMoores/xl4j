/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.stream.Collectors;

import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Returns a time series containing the absolute values of a series. If a value is null in the input
 * series, the returned series will contain 0 on that date.
 */
@XLNamespace("TimeSeries.")
@XLFunction(name = "Abs", description = "Return the absolute values of a time series", category = "Time series")
public class Abs implements TimeSeriesFunction<TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    final TimeSeries result = TimeSeries.of(ts);
    return TimeSeries.of(result.entrySet().parallelStream()
        .collect(Collectors.toConcurrentMap(e -> e.getKey(), e -> e.getValue() == null ? 0. : Math.abs(e.getValue()))));
  }

}
