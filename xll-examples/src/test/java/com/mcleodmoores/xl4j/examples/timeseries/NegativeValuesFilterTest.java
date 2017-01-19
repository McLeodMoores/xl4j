/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.function.Function;
import java.util.stream.IntStream;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 *
 */
public class NegativeValuesFilterTest {
  private static final Function<TimeSeries, TimeSeries> CALCULATOR = new NegativeValuesFilter();

  @Test
  public void testNegativeValueFilter() {
    final TimeSeries ts = TimeSeries.emptyTimeSeries();
    IntStream.range(1, 100).forEach(i -> ts.put(LocalDate.now().plusDays(i), i % 5 == 0 ? -i * 2. : i * 3.));
    final TimeSeries result = CALCULATOR.apply(ts);
    IntStream.range(1, 100).forEach(i -> {
      final Double value = result.get(LocalDate.now().plusDays(i));
      switch (i % 5) {
        case 0:
          assertNull(value);
          break;
        default:
          assertEquals(value, i * 3.);
      }
    });
  }
}
