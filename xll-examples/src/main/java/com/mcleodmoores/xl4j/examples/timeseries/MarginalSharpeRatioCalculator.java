/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 *
 */
public class MarginalSharpeRatioCalculator implements BiFunction<List<TimeSeries>, Double, List<Double>> {
  private static final Add ADD = new Add();
  private static final MeanCalculator MEAN = new MeanCalculator();
  private static final CovarianceCalculator COV = new CovarianceCalculator();

  @Override
  public List<Double> apply(final List<TimeSeries> returns, final Double riskFreeRate) {
    final TimeSeries portfolioReturn = TimeSeries.newTimeSeries();
    returns.parallelStream().forEach(ts -> ADD.apply(ts, portfolioReturn));
    final List<Double> result = new ArrayList<>();
    returns.parallelStream().forEach(ts -> result.add((MEAN.apply(ts) - riskFreeRate) / COV.apply(ts, portfolioReturn)));
    return result;
  }
}
