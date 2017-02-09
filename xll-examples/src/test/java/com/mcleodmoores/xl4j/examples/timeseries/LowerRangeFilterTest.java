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
 *
 */
public class LowerRangeFilterTest {
  private static final TimeSeriesBiFunction<Double, TimeSeries> CALCULATOR = new LowerRangeFilter();

  @Test
  public void test() {
    final TimeSeries ts = TimeSeries.emptyTimeSeries();
    final double range = 0.5;
    IntStream.range(1, 100).forEach(i -> {
      ts.put(LocalDate.now().plusDays(i), i % 5 == 0 ? i / 10. : i);
    });
    final TimeSeries result = CALCULATOR.apply(ts, range);
    IntStream.range(1, 100).forEach(i -> {
      final LocalDate date = LocalDate.now().plusDays(i);
      switch (i % 5) {
        case 0:
          if (i / 10. < range) { // note that this test checks that the value must be greater than or equal to the lower range
            assertFalse(result.containsKey(date));
          } else {
            assertEquals(result.get(date), i / 10., 1e-15);
          }
          break;
        default:
          assertEquals(result.get(date), i, 1e-15); // all values are above the lower bound
      }
    });
  }

}
