/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link ExpectedPortfolioReturnCalculator}.
 */
public class ExpectedPortfolioReturnCalculatorTest {
  private static final ExpectedPortfolioReturnCalculator CALC = new ExpectedPortfolioReturnCalculator();

  /**
   * Tests that the weights cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullWeights() {
    CALC.apply(null, Arrays.asList(2., 3., 4., 5.));
  }

  /**
   * Tests that the expected returns cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullExpectedReturns() {
    CALC.apply(Arrays.asList(0.25, 0.25, 0.25, 0.25), null);
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
    CALC.apply(Arrays.asList(0.25, 0.25, 0.25, 0.25), Arrays.asList(3., 5., 6.));
  }

  /**
   * Tests the expected return of a two-asset portfolio.
   */
  @Test
  public void testTwoAssets() {
    final List<Double> weights = Arrays.asList(0.5, 0.5);
    final List<Double> returns = Arrays.asList(0.12, 0.2);
    assertEquals(new ExpectedPortfolioReturnCalculator().apply(weights, returns), 0.16, 1e-15);
  }

  /**
   * Tests the expected return of a three-asset portfolio.
   */
  @Test
  public void testThreeAssets() {
    final List<Double> weights = Arrays.asList(1 / 3., 1 / 3., 1 / 3.);
    final List<Double> returns = Arrays.asList(0.2, 0.12, 0.15);
    assertEquals(new ExpectedPortfolioReturnCalculator().apply(weights, returns), 47 / 300., 1e-15);
  }
}
