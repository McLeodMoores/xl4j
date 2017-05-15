/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.List;
import java.util.function.BiFunction;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Constructs a covariance matrix.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Creates a labelled covariance matrix",
    category = "Time series")
public class CovarianceMatrixCalculator implements BiFunction<List<String>, List<TimeSeries>, LabelledMatrix> {
  private static final TimeSeriesBiFunction<TimeSeries, Double> CALCULATOR = new CovarianceCalculator();

  @Override
  public LabelledMatrix apply(final List<String> namesList, final List<TimeSeries> tsList) {
    ArgumentChecker.notNull(namesList, "names");
    ArgumentChecker.notNull(tsList, "ts");
    final int n = namesList.size();
    ArgumentChecker.isTrue(n == tsList.size(), "Must have the same number of time series as names, have {} and {}", n, tsList.size());
    final String[] names = new String[n];
    final double[][] covariances = new double[n][n];
    for (int i = 0; i < n; i++) {
      names[i] = namesList.get(i);
      final TimeSeries ts1 = tsList.get(i);
      for (int j = 0; j < i; j++) {
        final TimeSeries ts2 = tsList.get(j);
        covariances[i][j] = CALCULATOR.apply(ts1, ts2);
        covariances[j][i] = covariances[i][j];
      }
    }
    return LabelledMatrix.of(names, covariances);
  }

}
