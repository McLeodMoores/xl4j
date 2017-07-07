/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import java.util.stream.IntStream;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link PercentageReturnCalculator}.
 */
public class PercentageReturnCalculatorTest {
  private static final TimeSeriesFunction<TimeSeries> CALC = new PercentageReturnCalculator();

  /**
   * Checks that the time series cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullTimeSeries() {
    CALC.apply(null);
  }

  /**
   * Checks that the time series needs at least two data points.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testTimeSeriesSize() {
    final TimeSeries ts = TimeSeries.newTimeSeries();
    ts.put(LocalDate.of(2017, 1, 1), 100.);
    CALC.apply(ts);
  }

  /**
   * Tests the return calculation.
   */
  @Test
  public void testCalculator() {
    final TimeSeries ts = TimeSeries.newTimeSeries();
    final double r = 0.001;
    IntStream.range(0, 100).forEach(i -> ts.put(LocalDate.now().plusDays(i), Math.pow(1 + r, i)));
    CALC.apply(ts).forEach((k, v) -> assertEquals(v, r, 1e-15));
  }
}
