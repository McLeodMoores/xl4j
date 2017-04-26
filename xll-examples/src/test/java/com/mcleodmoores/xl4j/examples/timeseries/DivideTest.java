/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * Unit tests for {@link Divide}.
 */
public class DivideTest {
  private static final BiFunction<TimeSeries, TimeSeries, TimeSeries> CALCULATOR = new Divide();
  private static final double EPS = 1e-15;

  /**
   * Tests the function when there are no null values in the time series.
   */
  @Test
  public void noNullValues() {
    final int n = 100;
    final TimeSeries ts1 = TimeSeries.newTimeSeries();
    final TimeSeries ts2 = TimeSeries.newTimeSeries();
    IntStream.range(1, n).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      ts1.put(date, Double.valueOf(i));
      ts2.put(date, i * 10.);
    });
    final TimeSeries result = CALCULATOR.apply(ts1, ts2);
    assertEquals(result.size(), ts1.size());
    assertEquals(result.keySet(), ts1.keySet());
    IntStream.range(1, n).forEach(i -> assertEquals(result.get(LocalDate.now().plusDays(i)), 1. / 10, EPS));
  }

  /**
   * Tests the function when there are nulls in the numerator.
   */
  @Test
  public void testNullsInFirstSeries() {
    final int n = 100;
    final TimeSeries ts1 = TimeSeries.newTimeSeries();
    final TimeSeries ts2 = TimeSeries.newTimeSeries();
    IntStream.range(1, n).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      ts1.put(date, i % 2 == 0 ? null : Double.valueOf(i));
      ts2.put(date, i * 10.);
    });
    final TimeSeries result = CALCULATOR.apply(ts1, ts2);
    assertEquals(result.size(), ts1.size());
    assertEquals(result.keySet(), ts1.keySet());
    IntStream.range(1, n).forEach(i -> assertEquals(result.get(LocalDate.now().plusDays(i)), i % 2 == 0 ? 0. : 1. / 10, EPS));
  }

  /**
   * Tests the function when there are nulls in the denominator.
   */
  @Test
  public void testNullsInSecondSeries() {
    final int n = 100;
    final TimeSeries ts1 = TimeSeries.newTimeSeries();
    final TimeSeries ts2 = TimeSeries.newTimeSeries();
    IntStream.range(1, n).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      ts1.put(date, Double.valueOf(i));
      ts2.put(date, i % 2 == 0 ? null : i * 10.);
    });
    final TimeSeries result = CALCULATOR.apply(ts1, ts2);
    assertEquals(result.size(), ts1.size());
    assertEquals(result.keySet(), ts1.keySet());
    IntStream.range(1, n).forEach(i -> assertEquals(result.get(LocalDate.now().plusDays(i)), i % 2 == 0 ? Double.POSITIVE_INFINITY : 1. / 10, EPS));
  }

  /**
   * Tests the function when there are nulls in the numerator and denominator.
   */
  @Test
  public void testNullsInBothSeries() {
    final int n = 100;
    final TimeSeries ts1 = TimeSeries.newTimeSeries();
    final TimeSeries ts2 = TimeSeries.newTimeSeries();
    IntStream.range(1, n).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      ts1.put(date, i % 2 == 0 ? null : Double.valueOf(i));
      ts2.put(date, i % 2 == 0 ? null : i * 10.);
    });
    final TimeSeries result = CALCULATOR.apply(ts1, ts2);
    assertEquals(result.size(), ts1.size());
    assertEquals(result.keySet(), ts1.keySet());
    IntStream.range(1, n).forEach(i -> {
      final Double value = result.get(LocalDate.now().plusDays(i));
      if (i % 2 == 0) {
        assertEquals(value, 0., EPS);
      } else {
        assertEquals(value, 1. / 10, EPS);
      }
    });
  }
}
