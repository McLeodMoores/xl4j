/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 *
 */
public class TimeSeriesOperationTest {

  /**
   * Tests the abs and reciprocal functions.
   */
  @Test
  public void testAbsReciprocal() {
    final int n = 100;
    final LocalDate[] dates = new LocalDate[n];
    final LocalDate[] expectedDates = new LocalDate[n];
    final Double[] values = new Double[n];
    final Double[] expectedAbs = new Double[n];
    final Double[] expectedReciprocal = new Double[n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      dates[i] = date;
      expectedDates[i] = date;
      expectedAbs[i] = Double.valueOf(i);
      if (i < n / 2) {
        values[i] = Double.valueOf(i);
        expectedReciprocal[i] = 1. / Double.valueOf(i);
      } else {
        values[i] = Double.valueOf(-i);
        expectedReciprocal[i] = 1. / Double.valueOf(-i);
      }
    }
    final TimeSeries ts = TimeSeries.of(dates, values);
    assertEquals(ts.abs(), TimeSeries.of(dates, expectedAbs));
    assertEquals(ts.reciprocal(), TimeSeries.of(dates, expectedReciprocal));
  }

  /**
   * Tests performing operations on two series when both series contain the same dates.
   */
  @Test
  public void testAllOverlappingDates() {
    final int n = 100;
    final LocalDate[] dates = new LocalDate[n];
    final Double[] values1 = new Double[n];
    final Double[] values2 = new Double[n];
    final Double[] expectedAdd = new Double[n];
    final Double[] expectedSubtract = new Double[n];
    final Double[] expectedMultiply = new Double[n];
    final Double[] expectedDivide = new Double[n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      dates[i] = date;
      final double value = Double.valueOf(i + 1);
      values1[i] = value;
      values2[i] = -value;
      expectedAdd[i] = Double.valueOf(0);
      expectedSubtract[i] = 2 * value;
      expectedMultiply[i] = -value * value;
      expectedDivide[i] = Double.valueOf(-1);
    }
    final TimeSeries ts = TimeSeries.of(dates, values1);
    final TimeSeries other = TimeSeries.of(dates, values2);
    assertEquals(ts.add(other), TimeSeries.of(dates, expectedAdd));
    assertEquals(other.add(ts), TimeSeries.of(dates, expectedAdd));
    assertEquals(ts.subtract(other), TimeSeries.of(dates, expectedSubtract));
    assertEquals(other.subtract(ts).scale(-1), TimeSeries.of(dates, expectedSubtract));
    assertEquals(ts.multiply(other), TimeSeries.of(dates, expectedMultiply));
    assertEquals(other.multiply(ts), TimeSeries.of(dates, expectedMultiply));
    assertEquals(ts.divide(other), TimeSeries.of(dates, expectedDivide));
    assertEquals(other.divide(ts), TimeSeries.of(dates, expectedDivide));
  }

  /**
   * Tests performing operations on two series where the series end on the same date
   * but one starts before the other.
   */
  @Test
  public void testAddSeriesOverlapsAtEnd() {
    final int n = 100;
    final LocalDate[] dates1 = new LocalDate[n];
    final LocalDate[] dates2 = new LocalDate[n / 2];
    final LocalDate[] expectedDates = new LocalDate[n];
    final Double[] values1 = new Double[n];
    final Double[] values2 = new Double[n / 2];
    final Double[] expectedValues = new Double[n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      dates1[i] = date;
      expectedDates[i] = date;
      values1[i] = Double.valueOf(i);
      if (i < n / 2) {
        expectedValues[i] = Double.valueOf(i);
      } else {
        dates2[i - n / 2] = date;
        values2[i - n / 2] = Double.valueOf(-i);
        expectedValues[i] = Double.valueOf(0);
      }
    }
    final TimeSeries ts = TimeSeries.of(dates1, values1);
    final TimeSeries other = TimeSeries.of(dates2, values2);
    final TimeSeries expected = TimeSeries.of(expectedDates, expectedValues);
    assertEquals(ts.add(other), expected);
    assertEquals(other.add(ts), expected);
  }

  /**
   * Tests performing operations on two series where the series start on the same date
   * but one ends after the other.
   */
  @Test
  public void testAddSeriesOverlapsAtStart() {
    final int n = 10;
    final LocalDate[] dates1 = new LocalDate[n];
    final LocalDate[] dates2 = new LocalDate[n / 2];
    final LocalDate[] expectedDates = new LocalDate[n];
    final Double[] values1 = new Double[n];
    final Double[] values2 = new Double[n / 2];
    final Double[] expectedValues = new Double[n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      dates1[i] = date;
      expectedDates[i] = date;
      values1[i] = Double.valueOf(i);
      if (i < n / 2) {
        dates2[i] = date;
        values2[i] = Double.valueOf(-i);
        expectedValues[i] = Double.valueOf(0);
      } else {
        expectedValues[i] = Double.valueOf(i);
      }
    }
    final TimeSeries ts = TimeSeries.of(dates1, values1);
    final TimeSeries other = TimeSeries.of(dates2, values2);
    final TimeSeries expected = TimeSeries.of(expectedDates, expectedValues);
    assertEquals(ts.add(other), expected);
    assertEquals(other.add(ts), expected);
  }

  /**
   * Tests performing operations on two series where one series starts and ends before the other.
   */
  @Test
  public void testAddSeriesOverlapsAtStartAndEnd() {
    final int n = 12;
    final LocalDate[] dates1 = new LocalDate[n];
    final LocalDate[] dates2 = new LocalDate[n / 2];
    final LocalDate[] expectedDates = new LocalDate[n];
    final Double[] values1 = new Double[n];
    final Double[] values2 = new Double[n / 2];
    final Double[] expectedValues = new Double[n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      dates1[i] = date;
      expectedDates[i] = date;
      values1[i] = Double.valueOf(i);
      if (i >= n / 4 && i < 3 * n / 4) {
        dates2[i - n / 4] = date;
        values2[i - n / 4] = Double.valueOf(-i);
        expectedValues[i] = Double.valueOf(0);
      } else {
        expectedValues[i] = Double.valueOf(i);
      }
    }
    final TimeSeries ts = TimeSeries.of(dates1, values1);
    final TimeSeries other = TimeSeries.of(dates2, values2);
    final TimeSeries expected = TimeSeries.of(expectedDates, expectedValues);
    assertEquals(ts.add(other), expected);
    assertEquals(other.add(ts), expected);
  }

  /**
   * Tests performing operations on two series where there is no overlap in dates.
   */
  @Test
  public void testAddSeriesNoOverlaps() {
    final int n = 10;
    final LocalDate[] dates1 = new LocalDate[n / 2];
    final LocalDate[] dates2 = new LocalDate[n / 2];
    final LocalDate[] expectedDates = new LocalDate[n];
    final Double[] values1 = new Double[n / 2];
    final Double[] values2 = new Double[n / 2];
    final Double[] expectedValues = new Double[n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      expectedDates[i] = date;
      expectedValues[i] = Double.valueOf(i);
      if (i < n / 2) {
        dates1[i] = date;
        values1[i] = Double.valueOf(i);
      } else {
        dates2[i - n / 2] = date;
        values2[i - n / 2] = Double.valueOf(i);
      }
    }
    final TimeSeries ts = TimeSeries.of(dates1, values1);
    final TimeSeries other = TimeSeries.of(dates2, values2);
    final TimeSeries expected = TimeSeries.of(expectedDates, expectedValues);
    assertEquals(ts.add(other), expected);
    assertEquals(other.add(ts), expected);
  }

  /**
   * Checks that a null value on one of the dates of the series means that the other value is inserted.
   */
  @Test
  public void testWithNulls() {
    final int n = 100;
    final LocalDate[] dates = new LocalDate[n];
    final LocalDate[] expectedDates = new LocalDate[n];
    final Double[] values1 = new Double[n];
    final Double[] values2 = new Double[n];
    final Double[] expectedValues = new Double[n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      expectedDates[i] = date;
      dates[i] = date;
      if (i == n / 2) {
        expectedValues[i] = null; // nulls in both inputs
      } else {
        values1[i] = Double.valueOf(i);
        if (i % 2 == 0) {
          values2[i] = Double.valueOf(i);
          expectedValues[i] = Double.valueOf(2 * i);
        } else {
          expectedValues[i] = Double.valueOf(i);
        }
      }
    }
    final TimeSeries ts = TimeSeries.of(dates, values1);
    final TimeSeries other = TimeSeries.of(dates, values2);
    final TimeSeries expected = TimeSeries.of(expectedDates, expectedValues);
    assertEquals(ts.add(other), expected);
    assertEquals(other.add(ts), expected);
  }

  /**
   * Checks that a NaN in either of the series means that NaN is inserted.
   */
  @Test
  public void testWithNaNs() {
    final int n = 10;
    final LocalDate[] dates = new LocalDate[n];
    final LocalDate[] expectedDates = new LocalDate[n];
    final Double[] values1 = new Double[n];
    final Double[] values2 = new Double[n];
    final Double[] expectedValues = new Double[n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      expectedDates[i] = date;
      dates[i] = date;
      if (i == n / 2) {
        values1[i] = Double.NaN;
        values2[i] = Double.NaN;
        expectedValues[i] = Double.NaN;
      } else {
        values1[i] = Double.valueOf(i);
        if (i % 2 == 0) {
          values2[i] = Double.NaN;
          expectedValues[i] = Double.NaN;
        } else {
          values2[i] = Double.valueOf(i);
          expectedValues[i] = Double.valueOf(2 * i);
        }
      }
    }
    final TimeSeries ts = TimeSeries.of(dates, values1);
    final TimeSeries other = TimeSeries.of(dates, values2);
    final TimeSeries expected = TimeSeries.of(expectedDates, expectedValues);
    assertEquals(ts.add(other), expected);
    assertEquals(other.add(ts), expected);
  }

  /**
   * Checks that infinity in either of the series means that infinity is inserted or NaN if both are infinite.
   */
  @Test
  public void testWithInfinites() {
    final int n = 10;
    final LocalDate[] dates = new LocalDate[n];
    final LocalDate[] expectedDates = new LocalDate[n];
    final Double[] values1 = new Double[n];
    final Double[] values2 = new Double[n];
    final Double[] expectedValues = new Double[n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      expectedDates[i] = date;
      dates[i] = date;
      if (i == n / 2) {
        values1[i] = Double.POSITIVE_INFINITY;
        values2[i] = Double.NEGATIVE_INFINITY;
        expectedValues[i] = Double.NaN;
      } else {
        values1[i] = Double.valueOf(i);
        if (i % 2 == 0) {
          if (i % 4 == 0) {
            values2[i] = Double.POSITIVE_INFINITY;
            expectedValues[i] = Double.POSITIVE_INFINITY;
          } else {
            values2[i] = Double.NEGATIVE_INFINITY;
            expectedValues[i] = Double.NEGATIVE_INFINITY;
          }
        } else {
          values2[i] = Double.valueOf(i);
          expectedValues[i] = Double.valueOf(2 * i);
        }
      }
    }
    final TimeSeries ts = TimeSeries.of(dates, values1);
    final TimeSeries other = TimeSeries.of(dates, values2);
    final TimeSeries expected = TimeSeries.of(expectedDates, expectedValues);
    assertEquals(ts.add(other), expected);
    assertEquals(other.add(ts), expected);
  }
}
