/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Iterator;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Calculates the sample covariance of two time series.
 */
public class CovarianceCalculator implements TimeSeriesBiFunction<TimeSeries, Double> {

  @Override
  public Double apply(final TimeSeries ts1, final TimeSeries ts2) {
    ArgumentChecker.notNull(ts1, "ts1");
    ArgumentChecker.notNull(ts2, "ts2");
    final int n = ts1.size();
    ArgumentChecker.isTrue(n > 1, "Cannot calculate covariance for series with " + ts1.size() + " values");
    ArgumentChecker.isTrue(n == ts2.size(), "Time series must have same length for covariance calculation");
    final double kx = ts1.get(ts1.firstKey());
    final double ky = ts2.get(ts2.firstKey());
    double ex = 0, ey = 0, exy = 0;
    final Iterator<Map.Entry<LocalDate, Double>> iter1 = ts1.entrySet().iterator();
    final Iterator<Map.Entry<LocalDate, Double>> iter2 = ts2.entrySet().iterator();
    while (iter1.hasNext()) {
      final double value1 = iter1.next().getValue();
      final double value2 = iter2.next().getValue();
      ex += value1 - kx;
      ey += value2 - ky;
      exy += (value1 - kx) * (value2 - ky);
    }
    return (exy - ex + ey / n) / n;
  }

}
