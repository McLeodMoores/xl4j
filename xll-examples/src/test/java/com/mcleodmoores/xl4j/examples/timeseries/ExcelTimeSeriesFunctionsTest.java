/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import java.util.Random;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.simulator.MockFunctionProcessor;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.DoubleXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.ObjectXLObjectTypeConverter;

/**
 * Tests the example time series functions.
 */
public class ExcelTimeSeriesFunctionsTest {
  /** Test Excel instance */
  private static final Excel EXCEL = ExcelFactory.getInstance();
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
  /** The converter */
  private static final AbstractTypeConverter CONVERTER = new ObjectXLObjectTypeConverter(EXCEL);
  /** The double converter */
  private static final AbstractTypeConverter DOUBLE_CONVERTER = new DoubleXLNumberTypeConverter();
  static {
    final long offset = ChronoUnit.DAYS.between(LocalDate.of(1900, 1, 1),
        LocalDate.ofEpochDay(0)) + 1;
    final int n = 100;
    final XLValue[][] dates = new XLValue[1][n];
    final XLValue[][] values1 = new XLValue[1][n];
    final XLValue[][] values2 = new XLValue[1][n];
    final LocalDate now = LocalDate.now();
    final Random rng = new Random(56743);
    for (int i = 0; i < n; i++) {
      final LocalDate date = now.plusDays(i);
      dates[0][i] = XLNumber.of(date.toEpochDay() + offset);
      values1[0][i] = XLNumber.of(rng.nextDouble());
      values2[0][i] = XLNumber.of(rng.nextDouble());
    }
    XL_TS_1 = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(dates), XLArray.of(values1));
    XL_TS_2 = (XLObject) PROCESSOR.invoke("TimeSeries", XLArray.of(dates), XLArray.of(values2));
    TS_1 = (TimeSeries) CONVERTER.toJavaObject(TimeSeries.class, XL_TS_1);
    TS_2 = (TimeSeries) CONVERTER.toJavaObject(TimeSeries.class, XL_TS_2);
  }

  /**
   * Tests the addition of two time series.
   */
  @Test
  public void testAdd() {
    final XLValue calculator = PROCESSOR.invoke("TimeSeries.Add", new XLValue[0]);
    final Object xlValue = PROCESSOR.invoke("TimeSeries.Add.apply", calculator, XL_TS_1, XL_TS_2);
    assertEquals((TimeSeries) CONVERTER.toJavaObject(TimeSeries.class, xlValue), new Add().apply(TS_1, TS_2));
  }

  /**
   * Tests the subtraction of two time series.
   */
  @Test
  public void testSubtract() {
    final XLValue calculator = PROCESSOR.invoke("TimeSeries.Subtract", new XLValue[0]);
    final XLValue xlValue = PROCESSOR.invoke("TimeSeries.Subtract.apply", calculator, XL_TS_1, XL_TS_2);
    assertEquals((TimeSeries) CONVERTER.toJavaObject(TimeSeries.class, xlValue), new Subtract().apply(TS_1, TS_2));
  }

  /**
   * Tests the multiplication of two time series.
   */
  @Test
  public void testMultiply() {
    final XLValue calculator = PROCESSOR.invoke("TimeSeries.Multiply", new XLValue[0]);
    final XLValue xlValue = PROCESSOR.invoke("TimeSeries.Multiply.apply", calculator, XL_TS_1, XL_TS_2);
    assertEquals((TimeSeries) CONVERTER.toJavaObject(TimeSeries.class, xlValue), new Multiply().apply(TS_1, TS_2));
  }

  /**
   * Tests the division of two time series.
   */
  @Test
  public void testDivide() {
    final XLValue calculator = PROCESSOR.invoke("TimeSeries.Divide", new XLValue[0]);
    final XLValue xlValue = PROCESSOR.invoke("TimeSeries.Divide.apply", calculator, XL_TS_1, XL_TS_2);
    assertEquals((TimeSeries) CONVERTER.toJavaObject(TimeSeries.class, xlValue), new Divide().apply(TS_1, TS_2));
  }

  /**
   * Tests the scaling of a time series.
   */
  @Test
  public void testScale() {
    final XLValue calculator = PROCESSOR.invoke("TimeSeries.Scale", new XLValue[0]);
    final XLValue xlValue = PROCESSOR.invoke("TimeSeries.Scale.apply", calculator, XL_TS_1, XLNumber.of(5));
    assertEquals((TimeSeries) CONVERTER.toJavaObject(TimeSeries.class, xlValue), new Scale().apply(TS_1, 5.));
  }

  /**
   * Tests the absolute operation.
   */
  @Test
  public void testAbs() {
    final XLValue calculator = PROCESSOR.invoke("TimeSeries.Abs", new XLValue[0]);
    final XLValue xlValue = PROCESSOR.invoke("TimeSeries.Abs.apply", calculator, XL_TS_1);
    assertEquals((TimeSeries) CONVERTER.toJavaObject(TimeSeries.class, xlValue), new Abs().apply(TS_1));
  }

  /**
   * Tests the reciprocal operation.
   */
  @Test
  public void testReciprocal() {
    final XLValue calculator = PROCESSOR.invoke("TimeSeries.Reciprocal", new XLValue[0]);
    final XLValue xlValue = PROCESSOR.invoke("TimeSeries.Reciprocal.apply", calculator, XL_TS_1);
    assertEquals((TimeSeries) CONVERTER.toJavaObject(TimeSeries.class, xlValue), new Reciprocal().apply(TS_1));
  }

  /**
   * Tests the mean calculator.
   */
  @Test
  public void testMean() {
    final XLValue calculator = PROCESSOR.invoke("TimeSeries.Mean", new XLValue[0]);
    final XLValue xlValue = PROCESSOR.invoke("TimeSeries.Mean.apply", calculator, XL_TS_1);
    assertEquals(DOUBLE_CONVERTER.toJavaObject(Double.class, xlValue), new MeanCalculator().apply(TS_1));
  }

  /**
   * Tests the covariance calculator.
   */
  @Test
  public void testCovariance() {
    final XLValue calculator = PROCESSOR.invoke("TimeSeries.Covariance", new XLValue[0]);
    final XLValue xlValue = PROCESSOR.invoke("TimeSeries.Covariance.apply", calculator, XL_TS_1, XL_TS_1);
    assertEquals(DOUBLE_CONVERTER.toJavaObject(Double.class, xlValue), new CovarianceCalculator().apply(TS_1, TS_1));
  }

  /**
   * Tests the variance calculator.
   */
  @Test
  public void testVariance() {
    final XLValue calculator = PROCESSOR.invoke("TimeSeries.Variance", new XLValue[0]);
    final XLValue xlValue = PROCESSOR.invoke("TimeSeries.Variance.apply", calculator, XL_TS_1);
    assertEquals(DOUBLE_CONVERTER.toJavaObject(Double.class, xlValue), new VarianceCalculator().apply(TS_1));
  }

  /**
   * Tests the correlation calculator.
   */
  @Test
  public void testCorrelation() {
    final XLValue calculator = PROCESSOR.invoke("TimeSeries.Correlation", new XLValue[0]);
    XLValue xlValue = PROCESSOR.invoke("TimeSeries.Correlation.apply", calculator, XL_TS_1, XL_TS_1);
    assertEquals((Double) DOUBLE_CONVERTER.toJavaObject(Double.class, xlValue), 1, 1e-15);
    xlValue = PROCESSOR.invoke("TimeSeries.Correlation.apply", calculator, XL_TS_1, XL_TS_2);
    assertEquals((Double) DOUBLE_CONVERTER.toJavaObject(Double.class, xlValue), new CorrelationCalculator().apply(TS_1, TS_2), 1e-15);
  }
}
