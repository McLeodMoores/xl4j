/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import java.util.function.Function;
import java.util.stream.IntStream;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * Unit tests for {@link Reciprocal}.
 */
public class ReciprocalTest {
  private static final Function<TimeSeries, TimeSeries> CALCULATOR = new Reciprocal();
  private static final double EPS = 1e-15;

  /**
   * Tests the function when there are no values in the series.
   */
  @Test
  public void noNullValues() {
    final int n = 100;
    final TimeSeries ts = TimeSeries.newTimeSeries();
    IntStream.range(0, n).forEach(i -> {
      ts.put(LocalDate.now().plusDays(i), Double.valueOf(i));
    });
    final TimeSeries result = CALCULATOR.apply(ts);
    assertEquals(result.size(), ts.size());
    assertEquals(result.keySet(), ts.keySet());
    IntStream.range(0, n).forEach(i -> assertEquals(result.get(LocalDate.now().plusDays(i)), 1. / i, EPS));
  }

  /**
   * Tests the function when there are nulls in the series.
   */
  @Test
  public void testNullsInSeries() {
    final int n = 100;
    final TimeSeries ts = TimeSeries.newTimeSeries();
    IntStream.range(0, n).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      ts.put(date, i % 2 == 0 ? null : Double.valueOf(i));
    });
    final TimeSeries result = CALCULATOR.apply(ts);
    assertEquals(result.size(), ts.size());
    assertEquals(result.keySet(), ts.keySet());
    IntStream.range(0, n).forEach(i -> assertEquals(result.get(LocalDate.now().plusDays(i)), i % 2 == 0 ? Double.POSITIVE_INFINITY : 1. / i, EPS));
  }

}
