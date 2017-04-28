/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Iterator;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Calculates the sample variance of a time series.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(
    prefix = "Variance",
    typeConversionMode = TypeConversionMode.SIMPLEST_RESULT,
    description = "Sample variance of a time series",
    category = "Time series")
public class VarianceCalculator implements TimeSeriesFunction<Double> {

  @Override
  public Double apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    final TimeSeries m = TimeSeries.of(ts);
    m.entrySet().removeIf(e -> e.getValue() == null);
    ArgumentChecker.isTrue(m.size() > 1, "Cannot calculate variance for series with " + m.size() + " values");
    double n = 0, mu = 0, var = 0, previousMu = 0;
    final Iterator<Map.Entry<LocalDate, Double>> iter = m.entrySet().iterator();
    while (iter.hasNext()) {
      final double x = iter.next().getValue();
      n++;
      previousMu = mu;
      mu += (x - mu) / n;
      var += (x - mu) * (x - previousMu);
    }
    return var / (n - 1);
  }

}
