/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.simulator.MockFunctionProcessor;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Tests for the Excel functions in @link TimeSeries}.
 */
public class TimeSeriesFunctionsTest {
  /** The number of days from the Excel epoch */
  private static final long DAYS_FROM_EXCEL_EPOCH = ChronoUnit.DAYS.between(LocalDate.of(1900, 1, 1),
      LocalDate.ofEpochDay(0)) + 1;

  /** The function processor */
  private MockFunctionProcessor _processor;

  /**
   * Initializes the function processor before the tests run.
   */
  @BeforeTest
  public void init() {
    _processor = MockFunctionProcessor.getInstance();
  }

  /**
   * Tests the result when the input from Excel is a nx2 range with missing values in the second column.
   * This condition should never be triggered when using Excel, as ranges are rectangular.
   */
  @Test
  public void testMissingValueInColumn() {
    final int n = 100;
    final XLValue[][] xlValues = new XLValue[n][2];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final XLNumber xlDate = XLNumber.of(now.plusDays(i).toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      if (i == n - 1) {
        xlValues[i] = new XLValue[] {xlDate};
      } else {
        xlValues[i] = new XLValue[] {xlDate, XLNumber.of(i)};
      }
    }
    final XLValue result = _processor.invoke("TimeSeries", XLArray.of(xlValues));
    assertEquals(result, XLError.Null);
  }

  /**
   * Tests the result when the input from Excel is two columns with different lengths.
   */
  @Test
  public void testDifferentLengthColumns() {
    final int n = 100;
    final XLValue[][] xlDates = new XLValue[n][1];
    final XLValue[][] xlValues = new XLValue[n + 1][1];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      xlDates[i] = new XLValue[] {XLNumber.of(now.plusDays(i).toEpochDay() + DAYS_FROM_EXCEL_EPOCH)};
      xlValues[i] = new XLValue[] {XLNumber.of(i)};
    }
    final XLValue result = _processor.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    assertEquals(result, XLError.Null);
  }

  /**
   * Tests the result when the input from Excel is two rows with different lengths.
   */
  @Test
  public void testDifferentLengthRows() {
    final int n = 100;
    final XLValue[][] xlDates = new XLValue[1][n];
    final XLValue[][] xlValues = new XLValue[1][n + 1];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      xlDates[0][i] = XLNumber.of(now.plusDays(i).toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[0][i] = XLNumber.of(i);
    }
    final XLValue result = _processor.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    assertEquals(result, XLError.Null);
  }

  /**
   * Tests the result when the input from Excel is two rows with different lengths in range.
   * This condition should never be triggered when using Excel, as ranges are rectangular.
   */
  @Test
  public void testDifferentLengthRowsInRange() {
    final int n = 100;
    final XLValue[] xlDates = new XLValue[n];
    final XLValue[] xlValues = new XLValue[n + 1];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      xlDates[i] = XLNumber.of(now.plusDays(i).toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i] = XLNumber.of(i);
    }
    final XLValue result = _processor.invoke("TimeSeries", XLArray.of(new XLValue[][] {xlDates, xlValues}));
    assertEquals(result, XLError.Null);
  }

  /**
   * Tests the result when the input from Excel is a nx3 range.
   */
  @Test
  public void testThreeColumnsInRange() {
    final int n = 100;
    final XLValue[][] xlValues = new XLValue[n][3];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      xlValues[i] = new XLValue[] {XLNumber.of(now.plusDays(i).toEpochDay() + DAYS_FROM_EXCEL_EPOCH), XLNumber.of(i), XLNumber.of(-i)};
    }
    final XLValue result = _processor.invoke("TimeSeries", XLArray.of(xlValues));
    assertEquals(result, XLError.Null);
  }

  /**
   * Tests the result when the input from Excel is a 3xn range.
   */
  @Test
  public void testThreeRowsInRange() {
    final int n = 100;
    final XLValue[][] xlValues = new XLValue[3][n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      xlValues[0][i] = XLNumber.of(now.plusDays(i).toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[1][i] = XLNumber.of(i);
      xlValues[2][i] = XLNumber.of(-i);
    }
    final XLValue result = _processor.invoke("TimeSeries", XLArray.of(xlValues));
    assertEquals(result, XLError.Null);
  }

  /**
   * Tests the result when the input from Excel contains only one column.
   */
  @Test
  public void testMissingColumnInRange() {
    final int n = 100;
    final XLValue[][] xlValues = new XLValue[n][1];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      xlValues[i] = new XLValue[] {XLNumber.of(now.plusDays(i).toEpochDay() + DAYS_FROM_EXCEL_EPOCH)};
    }
    final XLValue result = _processor.invoke("TimeSeries", XLArray.of(xlValues));
    assertEquals(result, XLError.Null);
  }

  /**
   * Tests the result when the input from Excel contains only one row.
   */
  @Test
  public void testMissingRowInRange() {
    final int n = 100;
    final XLValue[][] xlValues = new XLValue[1][n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      xlValues[0][i] = XLNumber.of(now.plusDays(i).toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
    }
    final XLValue result = _processor.invoke("TimeSeries", XLArray.of(xlValues));
    assertEquals(result, XLError.Null);
  }

  /**
   * Tests time series creation from two rows of input.
   */
  @Test
  public void testCreateFromRows() {
    final int n = 100;
    final LocalDate[] expectedDates = new LocalDate[n];
    final Double[] expectedValues = new Double[n];
    final XLValue[][] xlDates = new XLValue[1][n];
    final XLValue[][] xlValues = new XLValue[1][n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      expectedDates[i] = date;
      expectedValues[i] = i % 2 == 0 ? null : Double.valueOf(i); // nulls are allowed
      xlDates[0][i] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[0][i] = i % 2 == 0 ? null : XLNumber.of(i); // nulls are allowed
    }
    final TimeSeries expectedTs = TimeSeries.of(expectedDates, expectedValues);
    final XLValue result = _processor.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    assertTrue(result instanceof XLObject);
    final XLObject xlTsObject = (XLObject) result;
    final Object tsObject = ExcelFactory.getInstance().getHeap().getObject(xlTsObject.getHandle());
    assertTrue(tsObject instanceof TimeSeries);
    assertEquals(tsObject, expectedTs);
  }

  /**
   * Tests time series creation from two columns of input.
   */
  @Test
  public void testCreateFromColumns() {
    final int n = 100;
    final LocalDate[] expectedDates = new LocalDate[n];
    final Double[] expectedValues = new Double[n];
    final XLValue[][] xlDates = new XLValue[n][1];
    final XLValue[][] xlValues = new XLValue[n][1];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      expectedDates[i] = date;
      expectedValues[i] = i % 2 == 0 ? null : Double.valueOf(i); // nulls are allowed
      xlDates[i] = new XLValue[] {XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH)};
      xlValues[i] = new XLValue[] {i % 2 == 0 ? null : XLNumber.of(i)}; // nulls are allowed
    }
    final TimeSeries expectedTs = TimeSeries.of(expectedDates, expectedValues);
    final XLValue result = _processor.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    assertTrue(result instanceof XLObject);
    final XLObject xlTsObject = (XLObject) result;
    final Object tsObject = ExcelFactory.getInstance().getHeap().getObject(xlTsObject.getHandle());
    assertTrue(tsObject instanceof TimeSeries);
    assertEquals(tsObject, expectedTs);
  }

  /**
   * Tests time series creation from a column of dates and a row of values.
   */
  @Test
  public void testCreateFromRowAndColumn() {
    final int n = 100;
    final LocalDate[] expectedDates = new LocalDate[n];
    final Double[] expectedValues = new Double[n];
    final XLValue[][] xlDates = new XLValue[n][1];
    final XLValue[][] xlValues = new XLValue[1][n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      expectedDates[i] = date;
      expectedValues[i] = i % 2 == 0 ? null : Double.valueOf(i); // nulls are allowed
      xlDates[i] = new XLValue[] {XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH)};
      xlValues[0][i] = i % 2 == 0 ? null : XLNumber.of(i); // nulls are allowed
    }
    final TimeSeries expectedTs = TimeSeries.of(expectedDates, expectedValues);
    final XLValue result = _processor.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    assertTrue(result instanceof XLObject);
    final XLObject xlTsObject = (XLObject) result;
    final Object tsObject = ExcelFactory.getInstance().getHeap().getObject(xlTsObject.getHandle());
    assertTrue(tsObject instanceof TimeSeries);
    assertEquals(tsObject, expectedTs);
  }

  /**
   * Tests creation of a time series from a horizontal range with row1 = dates and row2 = values.
   */
  @Test
  public void testCreateFromHorizontalRange() {
    final LocalDate now = LocalDate.now();
    int n = 100;
    LocalDate[] expectedDates = new LocalDate[n];
    Double[] expectedValues = new Double[n];
    XLValue[][] xlValues = new XLValue[2][n];
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      expectedDates[i] = date;
      expectedValues[i] = i % 2 == 0 ? null : Double.valueOf(i); // nulls are allowed
      xlValues[0][i] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[1][i] = i % 2 == 0 ? null : XLNumber.of(i); // nulls are allowed
    }
    TimeSeries expectedTs = TimeSeries.of(expectedDates, expectedValues);
    XLValue result = _processor.invoke("TimeSeries", XLArray.of(xlValues));
    assertTrue(result instanceof XLObject);
    XLObject xlTsObject = (XLObject) result;
    Object tsObject = ExcelFactory.getInstance().getHeap().getObject(xlTsObject.getHandle());
    assertTrue(tsObject instanceof TimeSeries);
    assertEquals(tsObject, expectedTs);
    // check that it accepts a column as well
    n = 1;
    expectedDates = new LocalDate[] {now};
    expectedValues = new Double[] {20.};
    xlValues = new XLValue[][] {new XLValue[] {XLNumber.of(now.toEpochDay() + DAYS_FROM_EXCEL_EPOCH)}, new XLValue[]{XLNumber.of(20)}};
    expectedTs = TimeSeries.of(expectedDates, expectedValues);
    result = _processor.invoke("TimeSeries", XLArray.of(xlValues));
    assertTrue(result instanceof XLObject);
    xlTsObject = (XLObject) result;
    tsObject = ExcelFactory.getInstance().getHeap().getObject(xlTsObject.getHandle());
    assertTrue(tsObject instanceof TimeSeries);
    assertEquals(tsObject, expectedTs);
  }

  /**
   * Tests creation of a time series from a vertical range with column1 = dates and column2 = values.
   */
  @Test
  public void testCreateFromVerticalRange() {
    final LocalDate now = LocalDate.now();
    int n = 100;
    LocalDate[] expectedDates = new LocalDate[n];
    Double[] expectedValues = new Double[n];
    XLValue[][] xlValues = new XLValue[n][2];
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      expectedDates[i] = date;
      expectedValues[i] = i % 2 == 0 ? null : Double.valueOf(i); // nulls are allowed
      xlValues[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i][1] = i % 2 == 0 ? null : XLNumber.of(i); // nulls are allowed
    }
    TimeSeries expectedTs = TimeSeries.of(expectedDates, expectedValues);
    XLValue result = _processor.invoke("TimeSeries", XLArray.of(xlValues));
    assertTrue(result instanceof XLObject);
    XLObject xlTsObject = (XLObject) result;
    Object tsObject = ExcelFactory.getInstance().getHeap().getObject(xlTsObject.getHandle());
    assertTrue(tsObject instanceof TimeSeries);
    assertEquals(tsObject, expectedTs);
    // check that it accepts a row as well
    n = 1;
    expectedDates = new LocalDate[] {now};
    expectedValues = new Double[] {20.};
    xlValues = new XLValue[][] {new XLValue[] {XLNumber.of(now.toEpochDay() + DAYS_FROM_EXCEL_EPOCH)}, new XLValue[]{XLNumber.of(20)}};
    expectedTs = TimeSeries.of(expectedDates, expectedValues);
    result = _processor.invoke("TimeSeries", XLArray.of(xlValues));
    assertTrue(result instanceof XLObject);
    xlTsObject = (XLObject) result;
    tsObject = ExcelFactory.getInstance().getHeap().getObject(xlTsObject.getHandle());
    assertTrue(tsObject instanceof TimeSeries);
    assertEquals(tsObject, expectedTs);
  }
}
