/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.examples.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.heap.Heap;
import com.mcleodmoores.excel4j.javacode.JMethod;
import com.mcleodmoores.excel4j.simulator.MockFunctionProcessor;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Tests the example time series functions.
 */
public class ExcelTimeSeriesFunctionsTest {
  /** The object heap */
  private static final Heap HEAP = ExcelFactory.getInstance().getHeap();
  /** The function processor */
  private static final MockFunctionProcessor PROCESSOR = MockFunctionProcessor.getInstance();
  /** An Excel object containing a time series */
  private static final XLObject XL_TS_1;
  /** An Excel object containing a time series */
  private static final XLObject XL_TS_2;
  /** A time series */
  private static final TimeSeries TS_1;
  /** A time series */
  private static final TimeSeries TS_2;

  static {
    final long offset = ChronoUnit.DAYS.between(LocalDate.of(1900, 1, 1),
        LocalDate.ofEpochDay(0)) + 1;
    final int n = 100;
    final XLValue[][] dates = new XLValue[1][n];
    final XLValue[][] values1 = new XLValue[1][n];
    final XLValue[][] values2 = new XLValue[1][n];
    final LocalDate now = LocalDate.now();
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      dates[0][i] = XLNumber.of(date.toEpochDay() + offset);
      values1[0][i] = i % 2 == 0 ? null : XLNumber.of(i);
      values2[0][i] = XLNumber.of(3.4 * i);
    }
    XL_TS_1 = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(dates), XLArray.of(values1));
    XL_TS_2 = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(dates), XLArray.of(values2));
    TS_1 = (TimeSeries) HEAP.getObject(XL_TS_1.getHandle());
    TS_2 = (TimeSeries) HEAP.getObject(XL_TS_2.getHandle());
  }

  /**
   * Tests the addition of two time series.
   */
  @Test
  public void testAdd() {
    final XLValue xlValue = PROCESSOR.invoke("add", XL_TS_1, XL_TS_2);
    assertTrue(xlValue instanceof XLObject);
    assertEquals(HEAP.getObject(((XLObject) xlValue).getHandle()), TS_1.add(TS_2));
  }

  /**
   * Tests the subtraction of two time series.
   */
  @Test
  public void testSubtract() {
    final XLValue xlValue = PROCESSOR.invoke("subtract", XL_TS_1, XL_TS_2);
    assertTrue(xlValue instanceof XLObject);
    assertEquals(HEAP.getObject(((XLObject) xlValue).getHandle()), TS_1.subtract(TS_2));
  }

  /**
   * Tests the multiplication of two time series.
   */
  @Test
  public void testMultiply() {
    final XLValue xlValue = PROCESSOR.invoke("multiply", XL_TS_1, XL_TS_2);
    assertTrue(xlValue instanceof XLObject);
    assertEquals(HEAP.getObject(((XLObject) xlValue).getHandle()), TS_1.multiply(TS_2));
  }

  /**
   * Tests the division of two time series.
   */
  @Test
  public void testDivide() {
    final XLValue xlValue = PROCESSOR.invoke("divide", XL_TS_1, XL_TS_2);
    assertTrue(xlValue instanceof XLObject);
    assertEquals(HEAP.getObject(((XLObject) xlValue).getHandle()), TS_1.divide(TS_2));
  }

  /**
   * Tests the scaling of a time series.
   */
  @Test
  public void testScale() {
    final XLValue xlValue = PROCESSOR.invoke("scale", XL_TS_1, XLNumber.of(5));
    assertTrue(xlValue instanceof XLObject);
    assertEquals(HEAP.getObject(((XLObject) xlValue).getHandle()), TS_1.scale(5));
  }

  /**
   * Tests the absolute operation.
   */
  @Test
  public void testAbs() {
    final XLValue xlValue = PROCESSOR.invoke("abs", XL_TS_1);
    assertTrue(xlValue instanceof XLObject);
    assertEquals(HEAP.getObject(((XLObject) xlValue).getHandle()), TS_1.abs());
  }

  /**
   * Tests the reciprocal operation.
   */
  @Test
  public void testReciprocal() {
    final XLValue xlValue = PROCESSOR.invoke("reciprocal", XL_TS_1);
    assertTrue(xlValue instanceof XLObject);
    assertEquals(HEAP.getObject(((XLObject) xlValue).getHandle()), TS_1.reciprocal());
  }

  /**
   * Tests the mean and covariance calculators.
   */
  @Test
  public void testMeanAndCovariance() {
    final XLValue calculator = PROCESSOR.newInstance("TimeSeriesMean");
    final Object xlValue = JMethod.jMethod((XLObject) calculator, XLString.of("apply"), XL_TS_2);
    assertTrue(xlValue instanceof XLNumber);
//    assertEquals(((XLNumber) xlValue).getAsDouble(), new MeanCalculator().apply(TS_2), 1e-15);
//    xlValue = PROCESSOR.invoke("CovarianceCalculator.apply", XL_TS_1, XL_TS_2);
//    assertTrue(xlValue instanceof XLObject);
//    assertEquals(HEAP.getObject(((XLObject) xlValue).getHandle()), new CovarianceCalculator().apply(TS_1, TS_2));
  }

  /**
   * Tests the return function calculators.
   */
  @Test
  public void testReturnFunction() {
    final XLValue xlValue = PROCESSOR.newInstance("TimeSeriesReturn", XLBoolean.FALSE);
    assertTrue(xlValue instanceof XLObject);
    final Object object = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(object instanceof ReturnCalculator);
    //xlValue = PROCESSOR.invoke(functionName, args);
  }
}
