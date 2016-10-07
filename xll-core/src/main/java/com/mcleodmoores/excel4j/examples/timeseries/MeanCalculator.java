/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.examples.timeseries;

import java.util.function.Function;

import com.mcleodmoores.excel4j.XLClass;
import com.mcleodmoores.excel4j.util.ArgumentChecker;

/**
 * Calculates the arithmetic mean of a time series of values.
 */
@XLClass(description = "Calculates the mean of a time series",
         category = "Time series",
         name = "TimeSeriesMean",
         excludedMethods = {"andThen", "compose"})
public class MeanCalculator implements Function<TimeSeries, Double> {

  @Override
  public Double apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    ArgumentChecker.notNullArray(ts.getValues(), "ts.values");
    double sum = 0;
    for (final Double value : ts.getValues()) {
      sum += value;
    }
    return sum / ts.size();
  }

}
