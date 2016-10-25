/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.function.Function;

import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 *
 */
// @XLClass(name = "Variance",
// description = "Calculates the sample variance of a time series",
// category = "Time series")
public class VarianceCalculator implements Function<TimeSeries, Double> {

  @Override
  public Double apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    ArgumentChecker.notNullArray(ts.getValues(), "ts.values");
    ArgumentChecker.isTrue(ts.size() > 1, "Cannot calculate variance for series with " + ts.size() + " values");
    int n = 1;
    double m = 0, m2 = 0;
    for (final Double value : ts.getValues()) {
      final double delta = value - m;
      m += delta / n;
      m2 += delta * (value - m);
      n++;
    }
    return m2 / (n - 1);
  }

}
