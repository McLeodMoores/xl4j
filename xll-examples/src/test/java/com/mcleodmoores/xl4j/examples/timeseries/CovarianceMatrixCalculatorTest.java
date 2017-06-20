/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link CovarianceMatrixCalculator}.
 */
public class CovarianceMatrixCalculatorTest {
  private static final int N = 20;
  private static final List<String> NAMES = new ArrayList<>();
  private static final List<TimeSeries> TS = new ArrayList<>();
  private static final CovarianceMatrixCalculator CALC = new CovarianceMatrixCalculator();

  static {
    final Random rng = new Random(6739857);
    for (int i = 0; i < N; i++) {
      NAMES.add(String.valueOf('A' + i));
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
   * Tests that the names cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullNameList() {
    CALC.apply(null, TS);
  }

  /**
   * Tests that the time series cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullTsList() {
    CALC.apply(NAMES, null);
  }

  /**
   * Tests that the number of names and time series must be the same.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testSameSize() {
    CALC.apply(NAMES, TS.subList(0, N / 2));
  }

  /**
   * Tests that none of the time series can be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullTs() {
    final List<TimeSeries> ts = new ArrayList<>(TS);
    ts.set(0, null);
    CALC.apply(NAMES, ts);
  }

  /**
   * Tests the result.
   */
  @Test
  public void test() {
    final LabelledMatrix cov = CALC.apply(NAMES, TS);
    assertEquals(cov.getSize(), N);
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < i; j++) {
        assertEquals(cov.getValueAt(i, j), cov.getValueAt(j, i), 1e-15);
      }
    }
  }
}
