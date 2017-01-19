/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.mcleodmoores.xl4j.simulator.MockFunctionProcessor;
import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Tests the example time series functions.
 */
public class ExcelTimeSeriesFunctionsTest {
  /** The function processor */
  private static final MockFunctionProcessor PROCESSOR = MockFunctionProcessor.getInstance();
  /** An Excel object containing a time series */
  private static final XLArray XL_TS_1;
  /** An Excel object containing a time series */
  private static final XLArray XL_TS_2;
  /** A time series */
  private static final TimeSeries TS_1;
  /** A time series */
  private static final TimeSeries TS_2;
  /** The converter */
  private static final AbstractTypeConverter CONVERTER = new TimeSeriesTypeConverter();
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
    XL_TS_1 = (XLArray) PROCESSOR.invoke("TimeSeries", XLArray.of(dates), XLArray.of(values1));
    XL_TS_2 = (XLArray) PROCESSOR.invoke("TimeSeries", XLArray.of(dates), XLArray.of(values2));
    TS_1 = (TimeSeries) CONVERTER.toJavaObject(TimeSeries.class, XL_TS_1);
    TS_2 = (TimeSeries) CONVERTER.toJavaObject(TimeSeries.class, XL_TS_2);
  }


  /**
   * Tests the addition of two time series.
   */
  @Test
  public void testAdd() {
    final XLValue calculator = PROCESSOR.invoke("TimeSeries.Add", new XLValue[0]);
    final XLValue xlValue = PROCESSOR.invoke("TimeSeries.Add.apply", calculator, XL_TS_1, XL_TS_2);
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

  //  /**
  //   * Tests the mean and covariance calculators.
  //   */
  //  @Test
  //  public void testMeanAndCovariance() {
  //    final XLValue calculator = PROCESSOR.newInstance("TimeSeriesMean");
  //    final Object xlValue = JMethod.jMethod((XLObject) calculator, XLString.of("apply"), XL_TS_2);
  //    assertTrue(xlValue instanceof XLNumber);
  ////    assertEquals(((XLNumber) xlValue).getAsDouble(), new MeanCalculator().apply(TS_2), 1e-15);
  ////    xlValue = PROCESSOR.invoke("CovarianceCalculator.apply", XL_TS_1, XL_TS_2);
  ////    assertTrue(xlValue instanceof XLObject);
  ////    assertEquals(HEAP.getObject(((XLObject) xlValue).getHandle()), new CovarianceCalculator().apply(TS_1, TS_2));
  //  }
  //
  //  /**
  //   * Tests the return function calculators.
  //   */
  //  @Test
  //  public void testReturnFunction() {
  //    final XLValue xlValue = PROCESSOR.newInstance("TimeSeriesReturn", XLBoolean.FALSE);
  //    assertTrue(xlValue instanceof XLObject);
  //    final Object object = HEAP.getObject(((XLObject) xlValue).getHandle());
  //    assertTrue(object instanceof ReturnCalculator);
  //    //xlValue = PROCESSOR.invoke(functionName, args);
  //  }
}
