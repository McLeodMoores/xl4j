/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Calculates the expected return of a portfolio from the instrument weights and returns.
 */
@XLNamespace("Portfolio.")
@XLFunctions(prefix = "ExpectedStandardDeviation", description = "Calculates the expected standard devation of a portfolio")
public class ExpectedPortfolioStandardDeviationCalculator implements BiFunction<List<Double>, List<TimeSeries>, Double> {
  private static final CovarianceMatrixCalculator COV = new CovarianceMatrixCalculator();

  @Override
  public Double apply(final List<Double> weights, final List<TimeSeries> returns) {
    ArgumentChecker.notNull(weights, "weights");
    ArgumentChecker.notNull(returns, "returns");
    final int n = weights.size();
    ArgumentChecker.isTrue(n > 0, "Cannot calculate the expected standard deviation of an empty portfolio");
    ArgumentChecker.isTrue(returns.size() == n, "Must have a return series for each weight");
    final LabelledMatrix expectedCovariances = COV.apply(new ArrayList<>(Collections.nCopies(n, "")), returns);
    double variance = 0;
    for (int i = 0; i < n; i++) {
      final double wi = weights.get(i);
      final double var = expectedCovariances.getValueAt(i, i);
      variance += wi * wi * var;
      for (int j = i + 1; j < n; j++) {
        variance += 2 * wi * weights.get(j) * expectedCovariances.getValueAt(i, j);
      }
    }
    return Math.sqrt(variance);
  }
}
