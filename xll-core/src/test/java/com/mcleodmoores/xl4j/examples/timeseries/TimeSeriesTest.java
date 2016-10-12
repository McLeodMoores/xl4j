/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.examples.timeseries.TimeSeries;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;

/**
 * Unit tests for {@link TimeSeries}.
 */
public class TimeSeriesTest {

  /**
   * Tests that unsorted data is sorted when creating the time series.
   */
  @Test
  public void testSortingUnsortedData() {
    final int n = 100;
    final List<LocalDate> dateList = new ArrayList<>();
    final Double[] values = new Double[n];
    for (int i = 0; i < n; i++) {
      dateList.add(LocalDate.now().plusDays(i));
      values[i] = Math.random();
    }
    Collections.shuffle(dateList);
    final LocalDate[] dates = dateList.toArray(new LocalDate[n]);
    // copies of original
    final LocalDate[] datesCopy = new LocalDate[n];
    final Double[] valuesCopy = new Double[n];
    System.arraycopy(dates, 0, datesCopy, 0, n);
    System.arraycopy(values, 0, valuesCopy, 0, n);
    final TimeSeries ts = TimeSeries.of(dates, values);
    // test that copy is made in constructor
    assertEquals(dates, datesCopy);
    assertEquals(values, valuesCopy);
    // test that something has happened - could fail if the shuffle produces an identical output
    assertNotEquals(ts.getDates(), dates);
    assertNotEquals(ts.getValues(), values);
    // test sorting of dates
    for (int i = 0; i < n; i++) {
      assertEquals(ts.getDate(i), LocalDate.now().plusDays(i));
    }
  }

  /**
   * Tests that unsorted data is sorted when creating the time series.
   */
  @Test
  public void testSortingSortedData() {
    final int n = 100;
    final LocalDate[] dates = new LocalDate[n];
    final Double[] values = new Double[n];
    for (int i = 0; i < n; i++) {
      dates[i] = LocalDate.now().plusDays(i);
      values[i] = Math.random();
    }
    final TimeSeries ts = TimeSeries.of(dates, values);
    // dates were sorted so nothing should have changed
    assertEquals(ts.getDates(), dates);
    assertEquals(ts.getValues(), values);
  }

  /**
   * Tests that unsorted data is sorted when creating the time series.
   */
  @Test
  public void testSortingReversedData() {
    final int n = 100;
    final LocalDate[] dates = new LocalDate[n];
    final Double[] values = new Double[n];
    for (int i = n - 1; i >= 0; i--) {
      dates[n - 1 - i] = LocalDate.now().plusDays(i);
      values[n - 1 - i] = Math.random();
    }
    final TimeSeries ts = TimeSeries.of(dates, values);
    assertNotEquals(ts.getDates(), dates);
    assertNotEquals(ts.getValues(), values);
    for (int i = 0; i < n; i++) {
      assertEquals(ts.getDate(n - 1 - i), dates[i]);
      assertEquals(ts.getValue(n - 1 - i), values[i]);
    }
  }

  /**
   * Tests that an exception is thrown if there are duplicate dates in the series.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testDuplicates() {
    final int n = 101;
    final LocalDate[] dates = new LocalDate[n];
    final Double[] values = new Double[n];
    for (int i = 0; i < n - 1; i++) {
      dates[i] = LocalDate.now().plusDays(i);
      values[i] = Math.random();
    }
    dates[100] = dates[n - 2];
    values[100] = values[n - 2];
    TimeSeries.of(dates, values);
  }

  /**
   * Tests the various getters.
   */
  @Test
  public void testMethods() {
    final int n = 100;
    final LocalDate[] dates = new LocalDate[n];
    final Double[] values = new Double[n];
    for (int i = 0; i < n; i++) {
      dates[i] = LocalDate.now().plusDays(i);
      values[i] = Math.random();
    }
    final TimeSeries ts = TimeSeries.of(dates, values);
    assertEquals(ts.getDates(), dates);
    assertEquals(ts.getValues(), values);
    assertEquals(ts.getValue(dates[n / 2]), values[n / 2]);
    assertEquals(ts.getDate(10), LocalDate.now().plusDays(10));
    assertEquals(ts.getValue(10), values[10]);
    assertEquals(ts.indexOf(LocalDate.now().plusDays(50)), 50);
    assertEquals(ts.indexOf(LocalDate.now().minusYears(1)), -1);
    assertEquals(ts.indexOf(LocalDate.now().plusYears(1)), -101);
    assertEquals(ts.size(), n);
  }

  /**
   * Tests that an exception is thrown if a value is requested for a date that is not present in the series.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testDateWithMissingValue() {
    final int n = 100;
    final LocalDate[] dates = new LocalDate[n];
    final Double[] values = new Double[n];
    for (int i = 0; i < n; i++) {
      dates[i] = LocalDate.now().plusDays(i);
      values[i] = Math.random();
    }
    final TimeSeries ts = TimeSeries.of(dates, values);
    ts.getValue(LocalDate.now().minusDays(10));
  }
}
