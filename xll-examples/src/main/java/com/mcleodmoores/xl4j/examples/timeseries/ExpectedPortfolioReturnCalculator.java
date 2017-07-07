/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Calculates the expected return of a portfolio from the instrument weights and returns.
 */
@XLNamespace("Portfolio.")
@XLFunctions(
    prefix = "ExpectedReturn",
    description = "Calculates the expected return of a portfolio",
    category = "Portfolio")
public class ExpectedPortfolioReturnCalculator implements BiFunction<List<Double>, List<TimeSeries>, Double> {
  private static final MeanCalculator MEAN = new MeanCalculator();

  @Override
  public Double apply(final List<Double> weights, final List<TimeSeries> returns) {
    ArgumentChecker.notNull(weights, "weights");
    ArgumentChecker.notNull(returns, "returns");
    final int n = weights.size();
    ArgumentChecker.isTrue(n > 0, "Cannot calculate the expected returns of an empty portfolio");
    ArgumentChecker.isTrue(returns.size() == n, "Must have a return series for each weight");
    final List<Double> expectedReturns = returns.parallelStream().mapToDouble(MEAN::apply).boxed().collect(Collectors.toList());
    return IntStream.range(0, n).mapToDouble(i -> weights.get(i) * expectedReturns.get(i)).sum();
  }
}
