/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.examples.timeseries.SamplingType;
import com.mcleodmoores.xl4j.examples.timeseries.TimeSeries;
import com.mcleodmoores.xl4j.examples.timeseries.TimeSeriesDataUtils;
import com.mcleodmoores.xl4j.heap.Heap;
import com.mcleodmoores.xl4j.simulator.MockFunctionProcessor;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link TimeSeriesDataUtils}.
 */
public class TimeSeriesDataUtilsTest {
  /** The function processor */
  private static final MockFunctionProcessor PROCESSOR = MockFunctionProcessor.getInstance();
  /** The heap containing XL objects */
  private static final Heap HEAP = ExcelFactory.getInstance().getHeap();
  /** The number of days from the Excel epoch */
  private static final long DAYS_FROM_EXCEL_EPOCH = ChronoUnit.DAYS.between(LocalDate.of(1900, 1, 1),
      LocalDate.ofEpochDay(0)) + 1;

  /**
   * Tests the exception thrown when the time series has a null first value.
   */
  @Test
  public void testPreviousValueFillFirstValueNull() {
    final int n = 100;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
    }
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    final XLValue xlFilledTimeSeries = PROCESSOR.invoke("FillTimeSeriesWithPreviousValue", xlTimeSeries);
    assertEquals(xlFilledTimeSeries, XLError.Null);
  }

  /**
   * Tests that any missing values in the time series are filled with the previous value.
   */
  @Test
  public void testPreviousValueFill() {
    final int n = 100;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    final LocalDate[] dates = new LocalDate[n];
    final Double[] values = new Double[n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      dates[i] = date;
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      if (i % 4 == 0) {
        xlValues[i][0] = XLNumber.of(i);
        values[i] = Double.valueOf(i);
      } else {
        values[i] = Double.valueOf(4 * (i >> 2));
      }
    }
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    final XLValue xlFilledTimeSeries = PROCESSOR.invoke("FillTimeSeriesWithPreviousValue", xlTimeSeries);
    assertTrue(xlFilledTimeSeries instanceof XLObject);
    final Object tsObject = HEAP.getObject(((XLObject) xlFilledTimeSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates, values));
  }

  /**
   * Tests the negative value filter.
   */
  @Test
  public void testNegativeValueFilter() {
    final int n = 100;
     final XLValue[][] xlDates = new XLNumber[n][1];
     final XLValue[][] xlValues = new XLNumber[n][1];
     final LocalDate[] dates1 = new LocalDate[n];
     final Double[] values1 = new Double[n];
     final List<LocalDate> dates2 = new ArrayList<>();
     final List<Double> values2 = new ArrayList<>();
     final LocalDate now = LocalDate.now();
     for (int i = 0; i < n; i++) {
       final LocalDate date = now.plusDays(i);
       dates1[i] = date;
       xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
       if ((i + 1) % 3 == 0) {
         xlValues[i][0] = XLNumber.of(-i);
       } else {
         xlValues[i][0] = XLNumber.of(i);
         final Double value = Double.valueOf(i);
         values1[i] = value;
         dates2.add(date);
         values2.add(value);
       }
     }
     // tests that negative values are replaced with nulls
     XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
     XLValue xlTimeSeriesNoNegatives = PROCESSOR.invoke("FilterNegativeValues", xlTimeSeries, XLBoolean.FALSE);
     assertTrue(xlTimeSeriesNoNegatives instanceof XLObject);
     Object tsObject = HEAP.getObject(((XLObject) xlTimeSeriesNoNegatives).getHandle());
     assertEquals(tsObject, TimeSeries.of(dates1, values1));
     // tests that points with negative values are removed from the series
     xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
     xlTimeSeriesNoNegatives = PROCESSOR.invoke("FilterNegativeValues", xlTimeSeries, XLBoolean.TRUE);
     assertTrue(xlTimeSeriesNoNegatives instanceof XLObject);
     tsObject = HEAP.getObject(((XLObject) xlTimeSeriesNoNegatives).getHandle());
     assertEquals(tsObject, TimeSeries.of(dates2.toArray(new LocalDate[dates2.size()]), values2.toArray(new Double[values2.size()])));
  }

  /**
   * Tests the zero value filter.
   */
  @Test
  public void testZeroValueFilter() {
    final int n = 100;
     final XLValue[][] xlDates = new XLNumber[n][1];
     final XLValue[][] xlValues = new XLNumber[n][1];
     final LocalDate[] dates1 = new LocalDate[n];
     final Double[] values1 = new Double[n];
     final List<LocalDate> dates2 = new ArrayList<>();
     final List<Double> values2 = new ArrayList<>();
     final LocalDate now = LocalDate.now();
     for (int i = 0; i < n; i++) {
       final LocalDate date = now.plusDays(i);
       dates1[i] = date;
       xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
       if ((i + 1) % 3 == 0) {
         xlValues[i][0] = XLNumber.of(0);
       } else {
         xlValues[i][0] = XLNumber.of(i + 1);
         final Double value = Double.valueOf(i + 1);
         values1[i] = value;
         dates2.add(date);
         values2.add(value);
       }
     }
     // tests that zeroes are replaced with nulls
     XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
     XLValue xlTimeSeriesNoNegatives = PROCESSOR.invoke("FilterZeroes", xlTimeSeries, XLBoolean.FALSE);
     assertTrue(xlTimeSeriesNoNegatives instanceof XLObject);
     Object tsObject = HEAP.getObject(((XLObject) xlTimeSeriesNoNegatives).getHandle());
     assertEquals(tsObject, TimeSeries.of(dates1, values1));
     // tests that points with zeroes are removed from the series
     xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
     xlTimeSeriesNoNegatives = PROCESSOR.invoke("FilterZeroes", xlTimeSeries, XLBoolean.TRUE);
     assertTrue(xlTimeSeriesNoNegatives instanceof XLObject);
     tsObject = HEAP.getObject(((XLObject) xlTimeSeriesNoNegatives).getHandle());
     assertEquals(tsObject, TimeSeries.of(dates2.toArray(new LocalDate[dates2.size()]), values2.toArray(new Double[values2.size()])));
  }

  /**
   * Tests daily sampling.
   */
  @Test
  public void testDailySampling() {
    final int n = 10000;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    // sampled series from start and end should be identical
    final LocalDate[] dates = new LocalDate[n];
    final Double[] values = new Double[n];
    final LocalDate start = LocalDate.of(2016, 3, 17);
    for (int i = 0; i < n; i++) {
      final LocalDate date = start.plusDays(i);
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i][0] = XLNumber.of(i);
      dates[i] = date;
      values[i] = Double.valueOf(i);
    }
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    // from the start
    XLValue xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.DAILY.name()), XLBoolean.TRUE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    Object tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates, values));
    // from the end
    xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.DAILY.name()), XLBoolean.FALSE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates, values));
  }

  /**
   * Tests weekly sampling from the start of the series.
   */
  @Test
  public void testWeeklySamplingFromStart() {
    final int n = 10000;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values = new ArrayList<>();
    final LocalDate start = LocalDate.of(2016, 3, 17);
    for (int i = 0; i < n; i++) {
      final LocalDate date = start.plusDays(i);
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i][0] = XLNumber.of(i);
      if (i % 7 == 0) {
        dates.add(date);
        values.add(Double.valueOf(i));
      }
    }
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    final XLValue xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.WEEKLY.name()), XLBoolean.TRUE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    final Object tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
  }

  /**
   * Tests weekly sampling from the end of the series.
   */
  @Test
  public void testWeeklySamplingFromEnd() {
    final int n = 10000;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values = new ArrayList<>();
    final LocalDate start = LocalDate.of(2016, 3, 17);
    for (int i = n - 1, j = 0; i >= 0; i--, j++) {
      final LocalDate date = start.plusDays(i);
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i][0] = XLNumber.of(i);
      if (j % 7 == 0) {
        dates.add(date);
        values.add(Double.valueOf(i));
      }
    }
    Collections.reverse(dates);
    Collections.reverse(values);
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    final XLValue xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.WEEKLY.name()), XLBoolean.FALSE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    final Object tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
  }

  /**
   * Tests monthly sampling from the start of the series.
   */
  @Test
  public void testMonthlySamplingFromStart() {
    final int n = 10000;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values = new ArrayList<>();
    final LocalDate start = LocalDate.of(2016, 3, 17);
    for (int i = 0; i < n; i++) {
      final LocalDate date = start.plusDays(i);
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i][0] = XLNumber.of(i);
      if (date.getDayOfMonth() == start.getDayOfMonth()) {
        dates.add(date);
        values.add(Double.valueOf(i));
      }
    }
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    final XLValue xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.MONTHLY.name()), XLBoolean.TRUE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    final Object tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
  }

  /**
   * Tests monthly sampling from the end of the series.
   */
  @Test
  public void testMonthlySamplingFromEnd() {
    final int n = 10000;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values = new ArrayList<>();
    final LocalDate start = LocalDate.of(2016, 3, 17);
    final LocalDate end = start.plusDays(n - 1);
    for (int i = n - 1; i >= 0; i--) {
      final LocalDate date = start.plusDays(i);
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i][0] = XLNumber.of(i);
      if (date.getDayOfMonth() == end.getDayOfMonth()) {
        dates.add(date);
        values.add(Double.valueOf(i));
      }
    }
    Collections.reverse(dates);
    Collections.reverse(values);
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    final XLValue xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.MONTHLY.name()), XLBoolean.FALSE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    final Object tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
  }

  /**
   * Tests annual sampling from the start of the series.
   */
  @Test
  public void testAnnualSamplingFromStart() {
    final int n = 10000;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values = new ArrayList<>();
    final LocalDate start = LocalDate.of(2016, 3, 17);
    for (int i = 0; i < n; i++) {
      final LocalDate date = start.plusDays(i);
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i][0] = XLNumber.of(i);
      if (date.getMonthValue() == start.getMonthValue() && date.getDayOfMonth() == start.getDayOfMonth()) {
        dates.add(date);
        values.add(Double.valueOf(i));
      }
    }
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    final XLValue xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.ANNUALLY.name()), XLBoolean.TRUE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    final Object tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
  }

  /**
   * Tests annual sampling from the end of the series.
   */
  @Test
  public void testAnnualSamplingFromEnd() {
    final int n = 10000;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values = new ArrayList<>();
    final LocalDate start = LocalDate.of(2016, 3, 17);
    final LocalDate end = start.plusDays(n - 1);
    for (int i = n - 1; i >= 0; i--) {
      final LocalDate date = start.plusDays(i);
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i][0] = XLNumber.of(i);
      if (date.getMonthValue() == end.getMonthValue() && date.getDayOfMonth() == end.getDayOfMonth()) {
        dates.add(date);
        values.add(Double.valueOf(i));
      }
    }
    Collections.reverse(dates);
    Collections.reverse(values);
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    final XLValue xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.ANNUALLY.name()), XLBoolean.FALSE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    final Object tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
  }

  /**
   * Tests first-of-month sampling.
   */
  @Test
  public void testFirstOfMonthSampling() {
    final int n = 10000;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values = new ArrayList<>();
    final LocalDate start = LocalDate.of(2016, 3, 17);
    for (int i = 0; i < n; i++) {
      final LocalDate date = start.plusDays(i);
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i][0] = XLNumber.of(i);
      if (date.getDayOfMonth() == 1) {
        dates.add(date);
        values.add(Double.valueOf(i));
      }
    }
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    // forward
    XLValue xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.START_OF_MONTH.name()), XLBoolean.TRUE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    Object tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
    // backward should give the same result as forwards
    xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.START_OF_MONTH.name()), XLBoolean.FALSE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
  }

  /**
   * Tests end-of-month sampling.
   */
  @Test
  public void testEndOfMonthSampling() {
    final int n = 10000;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values = new ArrayList<>();
    final LocalDate start = LocalDate.of(2016, 3, 17);
    for (int i = n - 1; i >= 0; i--) {
      final LocalDate date = start.plusDays(i);
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i][0] = XLNumber.of(i);
      if (date.getDayOfMonth() == date.lengthOfMonth()) {
        dates.add(date);
        values.add(Double.valueOf(i));
      }
    }
    Collections.reverse(dates);
    Collections.reverse(values);
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    // forwards
    XLValue xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.END_OF_MONTH.name()), XLBoolean.TRUE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    Object tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
    // backward should give the same result as forwards
    xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.END_OF_MONTH.name()), XLBoolean.FALSE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
  }

  /**
   * Tests first-of-year sampling.
   */
  @Test
  public void testFirstOfYearSampling() {
    final int n = 10000;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values = new ArrayList<>();
    final LocalDate start = LocalDate.of(2016, 3, 17);
    for (int i = 0; i < n; i++) {
      final LocalDate date = start.plusDays(i);
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i][0] = XLNumber.of(i);
      if (date.getMonthValue() == 1 && date.getDayOfMonth() == 1) {
        dates.add(date);
        values.add(Double.valueOf(i));
      }
    }
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    // backwards
    XLValue xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.START_OF_YEAR.name()), XLBoolean.TRUE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    Object tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
    // backward should give the same result as forwards
    xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.START_OF_YEAR.name()), XLBoolean.FALSE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
  }

  /**
   * Tests end-of-year sampling.
   */
  @Test
  public void testEndOfYearSampling() {
    final int n = 10000;
    final XLValue[][] xlDates = new XLNumber[n][1];
    final XLValue[][] xlValues = new XLNumber[n][1];
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values = new ArrayList<>();
    final LocalDate start = LocalDate.of(2016, 3, 17);
    for (int i = n - 1; i >= 0; i--) {
      final LocalDate date = start.plusDays(i);
      xlDates[i][0] = XLNumber.of(date.toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
      xlValues[i][0] = XLNumber.of(i);
      if (date.getMonthValue() == 12 && date.getDayOfMonth() == date.lengthOfMonth()) {
        dates.add(date);
        values.add(Double.valueOf(i));
      }
    }
    Collections.reverse(dates);
    Collections.reverse(values);
    final XLObject xlTimeSeries = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(xlDates), XLArray.of(xlValues));
    // forwards
    XLValue xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.END_OF_YEAR.name()), XLBoolean.TRUE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    Object tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
    // backward should give the same result as forwards
    xlSampledSeries = PROCESSOR.invoke("Sample", xlTimeSeries, XLString.of(SamplingType.END_OF_YEAR.name()), XLBoolean.FALSE, XLBoolean.FALSE);
    assertTrue(xlSampledSeries instanceof XLObject);
    tsObject = HEAP.getObject(((XLObject) xlSampledSeries).getHandle());
    assertEquals(tsObject, TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()])));
  }

}
