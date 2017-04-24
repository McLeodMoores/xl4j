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
 * Unit tests for {@link UpperRangeFilter}.
 */
public class UpperRangeFilterTest {
  private static final TimeSeriesBiFunction<Double, TimeSeries> CALCULATOR = new UpperRangeFilter();

  /**
   * Tests the function.
   */
  @Test
  public void test() {
    final TimeSeries ts = TimeSeries.newTimeSeries();
    final double range = 0.5;
    IntStream.range(1, 100).forEach(i -> {
      ts.put(LocalDate.now().plusDays(i), i % 5 == 0 ? i / 10. : i);
    });
    final TimeSeries result = CALCULATOR.apply(ts, range);
    IntStream.range(1, 100).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      switch (i % 5) {
        case 0:
          if (i / 10. > range) { // note that this test checks that the value must be less than or equal to the upper range
            assertFalse(result.containsKey(date));
          } else {
            assertEquals(result.get(date), i / 10., 1e-15);
          }
          break;
        default:
          assertFalse(result.containsKey(date)); // all values are filtered
      }
    });
  }

}
