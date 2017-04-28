/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Calculates the sample covariance of two time series.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(
    prefix = "Covariance",
    typeConversionMode = TypeConversionMode.SIMPLEST_RESULT,
    description = "Sample covariance of two time series",
    category = "Time series")
public class CovarianceCalculator implements TimeSeriesBiFunction<TimeSeries, Double> {

  @Override
  public Double apply(final TimeSeries ts1, final TimeSeries ts2) {
    ArgumentChecker.notNull(ts1, "ts1");
    ArgumentChecker.notNull(ts2, "ts2");
    final TimeSeries m1 = TimeSeries.of(ts1);
    m1.entrySet().removeIf(e -> e.getValue() == null);
    ArgumentChecker.isTrue(m1.size() > 1, "Cannot calculate covariance for series with {} values", m1.size());
    final TimeSeries m2 = TimeSeries.of(ts2);
    m2.entrySet().removeIf(e -> e.getValue() == null);
    double n = 0, mu1 = 0, mu2 = 0, cov = 0;
    final Iterator<Map.Entry<LocalDate, Double>> iter = m1.entrySet().iterator();
    while (iter.hasNext()) {
      final Entry<LocalDate, Double> entry = iter.next();
      final LocalDate date = entry.getKey();
      final double x1 = entry.getValue();
      final Double x2 = m2.get(date);
      if (x2 == null) {
        throw new XL4JRuntimeException("Could not get value for " + date + " from " + ts2);
      }
      n++;
      final double dx = x1 - mu1;
      mu1 += dx / n;
      mu2 += (x2 - mu2) / n;
      cov += dx * (x2 - mu2);
    }
    return cov / (n - 1);
  }

}
