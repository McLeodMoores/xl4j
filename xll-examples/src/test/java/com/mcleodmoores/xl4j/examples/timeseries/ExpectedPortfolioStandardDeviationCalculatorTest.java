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
 * Unit tests for {@link ExpectedPortfolioStandardDeviationCalculator}.
 */
public class ExpectedPortfolioStandardDeviationCalculatorTest {
  private static final ExpectedPortfolioStandardDeviationCalculator CALC = new ExpectedPortfolioStandardDeviationCalculator();

  /**
   * Tests that the weights cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullWeights() {
    CALC.apply(null, LabelledMatrix.of(new String[] {"A",  "B"}, new double[][] {{1, 2}, {3, 4}}));
  }

  /**
   * Tests that the covariance matrix cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullCovarianceMatrix() {
    CALC.apply(Arrays.asList(0.5, 0.5), null);
  }

  /**
   * Tests that the portfolio cannot be empty.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testEmptyPortfolio() {
    CALC.apply(Collections.emptyList(), LabelledMatrix.of(new String[] {"A",  "B"}, new double[][] {{1, 2}, {3, 4}}));
  }

  /**
   * Tests that the weights and covariance matrix must the the same size.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testSameSize() {
    CALC.apply(Arrays.asList(0.25, 0.25, 0.25, 0.25), LabelledMatrix.of(new String[] {"A",  "B"}, new double[][] {{1, 2}, {3, 4}}));
  }

  /**
   * Tests the expected standard deviation of a two-asset portfolio.
   */
  @Test
  public void testTwoAssetPortfolio() {
    final String[] names = {"A", "B"};
    final double[][] covariances = {{0.04, -0.016}, {-0.016, 0.16}};
    final List<Double> weights = Arrays.asList(0.5, 0.5);
    assertEquals(CALC.apply(weights, LabelledMatrix.of(names, covariances)), Math.sqrt(0.042), 1e-15);
  }

  /**
   * Tests the expected standard deviation of a three-asset portfolio.
   */
  @Test
  public void testThreeAssetPortfolio() {
    final String[] names = {"A", "B", "C"};
    final double[][] covariances = {{0.09, 0.045, 0.05}, {0.045, 0.07, 0.04}, {0.05, 0.04, 0.06}};
    final List<Double> weights = Arrays.asList(1. / 3, 1. / 3, 1. / 3);
    assertEquals(CALC.apply(weights, LabelledMatrix.of(names, covariances)), Math.sqrt(49. / 900), 1e-15);
  }
}
