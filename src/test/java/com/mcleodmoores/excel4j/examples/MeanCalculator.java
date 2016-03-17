/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.examples;

import java.util.function.Function;

import com.mcleodmoores.excel4j.util.ArgumentChecker;

/**
 *
 */
public class MeanCalculator implements Function<TimeSeries, Double> {

  @Override
  public Double apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    ArgumentChecker.notNullArray(ts.getValues(), "ts.values");
    double sum = 0;
    for (final double value : ts.getValues()) {
      sum += value;
    }
    return sum / ts.size();
  }

}
