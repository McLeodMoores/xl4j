/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.stream.Collectors;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Removes values below an amount from a time series.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Removes values below an amount from a time series",
    category = "Time series")
public class LowerRangeFilter implements TimeSeriesBiFunction<Double, TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts, final Double lower) {
    ArgumentChecker.notNull(ts, "ts");
    final TimeSeries result = TimeSeries.of(ts);
    return TimeSeries.of(result.entrySet().stream()
        .filter(entry -> Math.abs(entry.getValue()) >= lower)
        .collect(Collectors.toConcurrentMap(e -> e.getKey(), e -> e.getValue())));
  }

}
