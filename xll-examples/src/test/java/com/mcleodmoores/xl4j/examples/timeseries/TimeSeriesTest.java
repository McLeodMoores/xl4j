/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;

/**
 * Unit tests for {@link TimeSeries}.
 */
public class TimeSeriesTest {

  /**
   * Tests that an exception is thrown if there are duplicate dates in the series.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testDuplicates() {
    final int n = 101;
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values = new ArrayList<>();
    IntStream.range(0, n).forEach(i -> {
      dates.add(LocalDate.now().plusDays(i));
      values.add(i / 100.);
    });
    dates.add(dates.get(dates.size() - 1));
    values.add(null);
    TimeSeries.of(dates, values);
  }

  /**
   * Tests that a null date cannot be added.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testAddNullDate() {
    TimeSeries.emptyTimeSeries().put(null, 3.);
  }

}
