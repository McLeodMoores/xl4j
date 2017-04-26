/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.stream.IntStream;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * Unit tests for {@link ZeroValuesFilter}.
 */
public class ZeroValuesFilterTest {
  private static final TimeSeriesFunction<TimeSeries> CALCULATOR = new ZeroValuesFilter();

  /**
   * Tests this function.
   */
  @Test
  public void test() {
    final TimeSeries ts = TimeSeries.newTimeSeries();
    final double smallNotZero = 1e-11;
    IntStream.range(1, 100).forEach(i -> {
      ts.put(LocalDate.now().plusDays(i), i % 5 == 0 ? i % 15 == 0 ? 0 : i : i % 2 == 0 ? smallNotZero : -smallNotZero);
    });
    final TimeSeries result = CALCULATOR.apply(ts);
    IntStream.range(1, 100).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      switch (i % 5) {
        case 0:
          switch (i % 15) {
            case 0:
              assertFalse(result.containsKey(date));
              break;
            default:
              assertEquals(result.get(date), i, 1e-15);
          }
          break;
        default:
          assertEquals(Math.abs(result.get(date)), smallNotZero, 1e-15);
      }
    });
  }

}
