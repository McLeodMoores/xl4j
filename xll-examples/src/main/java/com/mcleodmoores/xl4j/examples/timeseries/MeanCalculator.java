/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Calculates the arithmetic mean of a time series of values.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(
    prefix = "Mean",
    typeConversionMode = TypeConversionMode.SIMPLEST_RESULT,
    description = "Calculates the arithmetic mean of a time series",
    category = "Time Series")
public class MeanCalculator implements TimeSeriesFunction<Double> {

  @Override
  public Double apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    final TimeSeries result = TimeSeries.of(ts);
    result.entrySet().removeIf(e -> e.getValue() == null);
    return result.values().stream().mapToDouble(i -> i).sum() / ts.size();
  }

}
