/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import java.util.stream.IntStream;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 *
 */
public class AddTest {
  private static final TimeSeriesBiFunction<TimeSeries, TimeSeries> CALCULATOR = new Add();
  private static final double EPS = 1e-15;

  @Test
  public void noNullValues() {
    final int n = 100;
    final TimeSeries ts1 = TimeSeries.newTimeSeries();
    final TimeSeries ts2 = TimeSeries.newTimeSeries();
    IntStream.range(0, n).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      ts1.put(date, Double.valueOf(i));
      ts2.put(date, i * 10.);
    });
    final TimeSeries sum = CALCULATOR.apply(ts1, ts2);
    assertEquals(sum.size(), ts1.size());
    assertEquals(sum.keySet(), ts1.keySet());
    IntStream.range(0, n).forEach(i -> assertEquals(sum.get(LocalDate.now().plusDays(i)), i * 11, EPS));
  }

  @Test
  public void testNullsInFirstSeries() {
    final int n = 100;
    final TimeSeries ts1 = TimeSeries.newTimeSeries();
    final TimeSeries ts2 = TimeSeries.newTimeSeries();
    IntStream.range(0, n).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      ts1.put(date, i % 2 == 0 ? null : Double.valueOf(i));
      ts2.put(date, i * 10.);
    });
    final TimeSeries sum = CALCULATOR.apply(ts1, ts2);
    assertEquals(sum.size(), ts1.size());
    assertEquals(sum.keySet(), ts1.keySet());
    IntStream.range(0, n).forEach(i -> assertEquals(sum.get(LocalDate.now().plusDays(i)), i % 2 == 0 ? i * 10 : i * 11, EPS));
  }

  @Test
  public void testNullsInSecondSeries() {
    final int n = 100;
    final TimeSeries ts1 = TimeSeries.newTimeSeries();
    final TimeSeries ts2 = TimeSeries.newTimeSeries();
    IntStream.range(0, n).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      ts1.put(date, Double.valueOf(i));
      ts2.put(date, i % 2 == 0 ? null : i * 10.);
    });
    final TimeSeries sum = CALCULATOR.apply(ts1, ts2);
    assertEquals(sum.size(), ts1.size());
    assertEquals(sum.keySet(), ts1.keySet());
    IntStream.range(0, n).forEach(i -> assertEquals(sum.get(LocalDate.now().plusDays(i)), i % 2 == 0 ? i : i * 11, EPS));
  }

  @Test
  public void testNullsInBothSeries() {
    final int n = 100;
    final TimeSeries ts1 = TimeSeries.newTimeSeries();
    final TimeSeries ts2 = TimeSeries.newTimeSeries();
    IntStream.range(0, n).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      ts1.put(date, i % 2 == 0 ? null : Double.valueOf(i));
      ts2.put(date, i % 2 == 0 ? null : i * 10.);
    });
    final TimeSeries sum = CALCULATOR.apply(ts1, ts2);
    assertEquals(sum.size(), ts1.size());
    assertEquals(sum.keySet(), ts1.keySet());
    IntStream.range(0, n).forEach(i -> {
      final Double value = sum.get(LocalDate.now().plusDays(i));
      if (i % 2 == 0) {
        assertEquals(value, 0, EPS);
      } else {
        assertEquals(value, i * 11, EPS);
      }
    });
  }
}
