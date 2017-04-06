/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Iterator;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.XLFunctions;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Calculates the sample covariance of two time series.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(prefix = "Covariance", description = "Sample covariance of two time series", category = "Time series")
public class CovarianceCalculator implements TimeSeriesBiFunction<TimeSeries, Double> {

  @Override
  public Double apply(final TimeSeries ts1, final TimeSeries ts2) {
    ArgumentChecker.notNull(ts1, "ts1");
    ArgumentChecker.notNull(ts2, "ts2");
    final TimeSeries m1 = TimeSeries.of(ts1);
    m1.entrySet().removeIf(e -> e.getValue() == null);
    final int n = m1.size();
    ArgumentChecker.isTrue(n > 1, "Cannot calculate covariance for series with " + ts1.size() + " values");
    final TimeSeries m2 = TimeSeries.of(ts2);
    m2.entrySet().removeIf(e -> e.getValue() == null);
    ArgumentChecker.isTrue(m1.keySet().equals(m2.keySet()), "Time series must have contains the same dates for covariance calculation");
    double ex = 0, ey = 0, exy = 0;
    final Iterator<Map.Entry<LocalDate, Double>> iter1 = m1.entrySet().iterator();
    final Iterator<Map.Entry<LocalDate, Double>> iter2 = m2.entrySet().iterator();
    while (iter1.hasNext()) {
      final double value1 = iter1.next().getValue();
      final double value2 = iter2.next().getValue();
      ex += value1;
      ey += value2;
      exy += value1 * value2;
    }
    exy /= n - 1;
    ex /= n;
    ey /= n;
    return exy - ex * ey;
  }

}
