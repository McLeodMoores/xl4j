/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.function.BiFunction;

import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Calculates the sample covariance of two time series.
 */
@XLFunction()
public class CovarianceCalculator implements BiFunction<TimeSeries, TimeSeries, Double> {

  @Override
  public Double apply(final TimeSeries ts1, final TimeSeries ts2) {
    ArgumentChecker.notNull(ts1, "ts1");
    ArgumentChecker.notNullArray(ts1.getValues(), "ts1.values");
    ArgumentChecker.isTrue(ts1.size() > 1, "Cannot calculate covariance for series with " + ts1.size() + " values");
    ArgumentChecker.notNull(ts2, "ts2");
    ArgumentChecker.notNullArray(ts2.getValues(), "ts2.values");
    final int n = ts1.size();
    ArgumentChecker.isTrue(n == ts2.size(), "Time series must have same length for covariance calculation");
    final double kx = ts1.getValue(0);
    final double ky = ts2.getValue(0);
    double ex = 0, ey = 0, exy = 0;
    for (int i = 0; i < n; i++) {
      ex += ts1.getValue(i) - kx;
      ey += ts2.getValue(i) - ky;
      exy += (ts1.getValue(i) - kx) * (ts2.getValue(i) - ky);
    }
    return (exy - ex + ey / n) / n;
  }

}
