/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import java.util.stream.IntStream;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.TestGroup;

/**
 *
 */
public class AbsTest {
  private static final TimeSeriesFunction<TimeSeries> CALCULATOR = new Abs();
  private static final double EPS = 1e-15;

  @Test
  public void noNullValues() {
    final int n = 100;
    final TimeSeries ts = TimeSeries.newTimeSeries();
    IntStream.range(0, n).forEach(i -> {
      ts.put(LocalDate.now().plusDays(i), Double.valueOf(-i));
    });
    final TimeSeries result = CALCULATOR.apply(ts);
    assertEquals(result.size(), ts.size());
    assertEquals(result.keySet(), ts.keySet());
    IntStream.range(0, n).forEach(i -> assertEquals(result.get(LocalDate.now().plusDays(i)), i, EPS));
  }

  @Test
  public void testNullsInSeries() {
    final int n = 100;
    final TimeSeries ts = TimeSeries.newTimeSeries();
    IntStream.range(0, n).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      ts.put(date, i % 2 == 0 ? null : Double.valueOf(-i));
    });
    final TimeSeries result = CALCULATOR.apply(ts);
    assertEquals(result.size(), ts.size());
    assertEquals(result.keySet(), ts.keySet());
    IntStream.range(0, n).forEach(i -> assertEquals(result.get(LocalDate.now().plusDays(i)), i % 2 == 0 ? 0. : i, EPS));
  }

}
