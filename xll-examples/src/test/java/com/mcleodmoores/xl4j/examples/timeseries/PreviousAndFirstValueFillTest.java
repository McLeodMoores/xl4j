/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.function.Function;
import java.util.stream.IntStream;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * Unit tests for {@link PreviousAndFirstValueFill}.
 */
public class PreviousAndFirstValueFillTest {
  private static final Function<TimeSeries, TimeSeries> CALCULATOR = new PreviousAndFirstValueFill();
  private static final double EPS = 1e-15;

  /**
   * Pad the first values.
   */
  @Test
  public void testPreviousValueFillFirstValuesNull() {
    final TimeSeries ts = TimeSeries.newTimeSeries();
    IntStream.range(0, 10).forEach(i -> ts.put(LocalDate.now().plusDays(i), null));
    IntStream.range(10, 100).forEach(i -> ts.put(LocalDate.now().plusDays(i), i % 2 == 0 ? null : i * 2.));
    final TimeSeries padded = CALCULATOR.apply(ts);
    IntStream.range(0, 100).forEach(i -> {
      final Double value = padded.get(LocalDate.now().plusDays(i));
      if (i < 11) {
        assertEquals(value, 22, EPS);
      } else {
        switch (i % 2) {
          case 0:
            assertEquals(value, (i - 1) * 2, EPS);
            break;
          case 1:
            assertEquals(value, i * 2, EPS);
            break;
          default:
            fail();
        }
      }
    });
  }

  /**
   * Tests the function.
   */
  @Test
  public void testPreviousValueFill() {
    final TimeSeries ts = TimeSeries.newTimeSeries();
    IntStream.range(0, 100).forEach(i -> ts.put(LocalDate.now().plusDays(i), (i + 3) % 3 == 0 ? i * 3. : null));
    final TimeSeries padded = CALCULATOR.apply(ts);
    IntStream.range(0, 100).forEach(i -> {
      final Double value = padded.get(LocalDate.now().plusDays(i));
      switch ((i + 3) % 3) {
        case 0:
          assertEquals(value, i * 3, EPS);
          break;
        case 1:
          assertEquals(value, (i - 1) * 3, EPS);
          break;
        case 2:
          assertEquals(value, (i - 2) * 3, EPS);
          break;
        default:
          fail();
      }
    });
  }
}
