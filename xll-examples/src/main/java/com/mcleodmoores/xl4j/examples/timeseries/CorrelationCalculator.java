/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;

/**
 * Calculates the sample correlation of a time series.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(
    prefix = "Correlation",
    typeConversionMode = TypeConversionMode.SIMPLEST_RESULT,
    description = "Sample correlation of two time series",
    category = "Time series")
public class CorrelationCalculator implements TimeSeriesBiFunction<TimeSeries, Double> {
  private static final TimeSeriesFunction<Double> VARIANCE_CALCULATOR = new VarianceCalculator();
  private static final TimeSeriesBiFunction<TimeSeries, Double> COVARIANCE_CALCULATOR = new CovarianceCalculator();

  @Override
  public Double apply(final TimeSeries ts1, final TimeSeries ts2) {
    final double cov = COVARIANCE_CALCULATOR.apply(ts1, ts2);
    final double var1 = VARIANCE_CALCULATOR.apply(ts1);
    final double var2 = VARIANCE_CALCULATOR.apply(ts2);
    return cov / Math.sqrt(var1 * var2);
  }
}
