/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link ExpectedPortfolioStandardDeviationCalculator}.
 */
public class ExpectedPortfolioStandardDeviationCalculatorTest {
  private static final ExpectedPortfolioStandardDeviationCalculator CALC = new ExpectedPortfolioStandardDeviationCalculator();
  private static final List<Double> WEIGHTS;
  private static final List<TimeSeries> TS = new ArrayList<>();

  static {
    final int n = 100;
    WEIGHTS = new ArrayList<>(Collections.nCopies(n, 1. / n));
    final Random rng = new Random(6739857);
    for (int i = 0; i < n; i++) {
      final LocalDate date = LocalDate.of(2015, 1, 1);
      final int m = 500;
      final List<LocalDate> dates = new ArrayList<>();
      final List<Double> values = new ArrayList<>();
      for (int j = 0; j < m; j++) {
        final double a = rng.nextDouble() / 100;
        dates.add(date.plusDays(j));
        values.add(a * rng.nextGaussian());
      }
      TS.add(TimeSeries.of(dates, values));
    }
  }

  /**
   * Tests that the weights cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullWeights() {
    CALC.apply(null, TS);
  }

  /**
   * Tests that the expected returns cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullExpectedReturns() {
    CALC.apply(WEIGHTS, null);
  }

  /**
   * Tests that the portfolio cannot be empty.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testEmptyPortfolio() {
    CALC.apply(Collections.emptyList(), Collections.emptyList());
  }

  /**
   * Tests that there must be one return for each weight.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testDifferentSizes() {
    CALC.apply(WEIGHTS, TS.subList(0, TS.size() - 1));
  }
}
