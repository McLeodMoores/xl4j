/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import com.mcleodmoores.xl4j.XLFunctions;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Calculates the arithmetic mean of a time series of values.
 */
@XLNamespace("TimeSeries")
@XLFunctions(prefix = "Mean", category = "Time Series", description = "Calculates the arithmetic mean of a time series")
public class MeanCalculator implements TimeSeriesFunction<Double> {

  @Override
  public Double apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    return ts.values().stream().mapToDouble(i -> i).sum() / ts.size();
  }

}
