/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.TestUtils.convertToXlType;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.heap.Heap;
import com.mcleodmoores.xl4j.simulator.MockFunctionProcessor;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLValue;
import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurveBuild;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDAInstrumentTypes;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Unit tests for {@link IsdaYieldCurveBuilder}.
 */
public class IsdaYieldCurveBuilderTest {
  /** The object heap */
  private static final Heap HEAP = ExcelFactory.getInstance().getHeap();
  /** The function processor */
  private static final MockFunctionProcessor PROCESSOR = MockFunctionProcessor.getInstance();
  /** The trade date */
  private static final LocalDate TRADE_DATE = LocalDate.of(2016, 10, 3);
  /** The instrument types */
  private static final String[] INSTRUMENT_TYPE_STRINGS = new String[] {"M", "M", "M", "S", "S",
        "S", "S", "S", "S", "S"};
  /** The tenors */
  private static final String[] TENOR_STRINGS = new String[] {"3M", "6M", "9M", "1Y", "2Y",
        "3Y", "4Y", "5Y", "7Y", "10Y"};
  /** The quotes */
  private static final double[] QUOTES = new double[] {0.001, 0.0011, 0.0012, 0.002, 0.0035,
        0.006, 0.01, 0.015, 0.025, 0.04};
  /** The money market day count */
  private static final String MONEY_MARKET_DAY_COUNT = "ACT/360";
  /** The swap day count */
  private static final String SWAP_DAY_COUNT = "ACT/365";
  /** The swap interval */
  private static final String SWAP_INTERVAL = "6M";
  /** The curve day count */
  private static final String CURVE_DAY_COUNT = "ACT/365";
  /** The business day convention */
  private static final String BDC = "Modified Following";
  /** The spot date */
  private static final LocalDate SPOT_DATE = LocalDate.of(2016, 10, 5);
  /** The holidays */
  private static final LocalDate[] HOLIDAYS = new LocalDate[] {LocalDate.of(2016, 8, 1)};
  /** The expected curve */
  private static final ISDACompliantCurve EXPECTED_CURVE;
  /** The object constructed by the function processor */
  private static final Object XL_RESULT = PROCESSOR.invoke("ISDAYieldCurve.BuildCurve", convertToXlType(TRADE_DATE), convertToXlType(INSTRUMENT_TYPE_STRINGS),
      convertToXlType(TENOR_STRINGS), convertToXlType(QUOTES), convertToXlType(MONEY_MARKET_DAY_COUNT), convertToXlType(SWAP_DAY_COUNT),
      convertToXlType(SWAP_INTERVAL), convertToXlType(CURVE_DAY_COUNT), convertToXlType(BDC), convertToXlType(SPOT_DATE),
      convertToXlType(HOLIDAYS));


  static {
    final ISDAInstrumentTypes[] instrumentTypes = new ISDAInstrumentTypes[] {
        ISDAInstrumentTypes.MoneyMarket, ISDAInstrumentTypes.MoneyMarket, ISDAInstrumentTypes.MoneyMarket, ISDAInstrumentTypes.Swap,
        ISDAInstrumentTypes.Swap, ISDAInstrumentTypes.Swap, ISDAInstrumentTypes.Swap, ISDAInstrumentTypes.Swap, ISDAInstrumentTypes.Swap,
        ISDAInstrumentTypes.Swap};
    final Period[] tenors = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
        Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7),
        Period.ofYears(10)};
    final ISDACompliantYieldCurveBuild builder = new ISDACompliantYieldCurveBuild(TRADE_DATE, SPOT_DATE, instrumentTypes, tenors,
      DayCountFactory.INSTANCE.instance(MONEY_MARKET_DAY_COUNT), DayCountFactory.INSTANCE.instance(SWAP_DAY_COUNT), Period.ofMonths(6),
      DayCountFactory.INSTANCE.instance(CURVE_DAY_COUNT), BusinessDayConventionFactory.INSTANCE.instance(BDC),
      new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY));
    EXPECTED_CURVE = builder.build(QUOTES);
  }

  @Test
  public void testConstructionOfCurve() {
    assertTrue(XL_RESULT instanceof XLObject);
    final Object result = HEAP.getObject(((XLObject) XL_RESULT).getHandle());
    assertTrue(result instanceof ISDACompliantCurve);
    final ISDACompliantCurve curve = (ISDACompliantCurve) result;
    // curves won't be equals() because the names will be different
    assertArrayEquals(curve.getKnotTimes(), EXPECTED_CURVE.getKnotTimes(), 1e-15);
    assertArrayEquals(curve.getKnotZeroRates(), EXPECTED_CURVE.getKnotZeroRates(), 1e-15);
  }

  @Test
  public void testExpandCurve() {
    final Object xlResult = PROCESSOR.invoke("ISDAYieldCurve.Expand", (XLObject) XL_RESULT);
    assertTrue(xlResult instanceof XLArray);
    final XLValue[][] xlValues = ((XLArray) xlResult).getArray();
    assertEquals(xlValues.length, INSTRUMENT_TYPE_STRINGS.length);
    assertEquals(xlValues[0].length, 2);
    for (int i = 0; i < xlValues.length; i++) {
      assertTrue(xlValues[i][0] instanceof XLNumber);
      assertTrue(xlValues[i][1] instanceof XLNumber);
      assertEquals(((XLNumber) xlValues[i][0]).getAsDouble(), EXPECTED_CURVE.getTimeAtIndex(i), 1e-15);
      assertEquals(((XLNumber) xlValues[i][1]).getAsDouble(), EXPECTED_CURVE.getZeroRateAtIndex(i), 1e-15);
    }
  }

  @Test
  public void testExpandDiscountFactors() {
    final Object xlResult = PROCESSOR.invoke("ISDAYieldCurve.ExpandDiscountFactors", (XLObject) XL_RESULT);
    assertTrue(xlResult instanceof XLArray);
    final XLValue[][] xlValues = ((XLArray) xlResult).getArray();
    assertEquals(xlValues.length, INSTRUMENT_TYPE_STRINGS.length);
    assertEquals(xlValues[0].length, 2);
    for (int i = 0; i < xlValues.length; i++) {
      assertTrue(xlValues[i][0] instanceof XLNumber);
      assertTrue(xlValues[i][1] instanceof XLNumber);
      assertEquals(((XLNumber) xlValues[i][0]).getAsDouble(), EXPECTED_CURVE.getTimeAtIndex(i), 1e-15);
      assertEquals(((XLNumber) xlValues[i][1]).getAsDouble(), EXPECTED_CURVE.getDiscountFactor(EXPECTED_CURVE.getTimeAtIndex(i)), 1e-15);
    }
  }

  @Test
  public void testExpandCurveForDates() {
    final LocalDate[] dates = new LocalDate[] {LocalDate.of(2017, 3, 19), LocalDate.of(2018, 3, 19), LocalDate.of(2019, 3, 19)};
    final double[] t = new double[dates.length];
    for (int i = 0; i < dates.length; i++) {
      t[i] = DayCountFactory.INSTANCE.instance(CURVE_DAY_COUNT).getDayCountFraction(SPOT_DATE, dates[i]);
    }
    final Object xlResult = PROCESSOR.invoke("ISDAYieldCurve.ExpandForDates", (XLObject) XL_RESULT, convertToXlType(SPOT_DATE),
        convertToXlType(CURVE_DAY_COUNT), convertToXlType(dates));
    assertTrue(xlResult instanceof XLArray);
    final XLValue[][] xlValues = ((XLArray) xlResult).getArray();
    assertEquals(xlValues.length, dates.length);
    assertEquals(xlValues[0].length, 2);
    for (int i = 0; i < xlValues.length; i++) {
      assertTrue(xlValues[i][0] instanceof XLNumber);
      assertTrue(xlValues[i][1] instanceof XLNumber);
      assertEquals(((XLNumber) xlValues[i][0]).getAsDouble(), t[i], 1e-15);
      assertEquals(((XLNumber) xlValues[i][1]).getAsDouble(), EXPECTED_CURVE.getZeroRate(t[i]), 1e-15);
    }
  }

  @Test
  public void testDiscountFactorsForDates() {
    final LocalDate[] dates = new LocalDate[] {LocalDate.of(2017, 3, 19), LocalDate.of(2018, 3, 19), LocalDate.of(2019, 3, 19)};
    final double[] t = new double[dates.length];
    for (int i = 0; i < dates.length; i++) {
      t[i] = DayCountFactory.INSTANCE.instance(CURVE_DAY_COUNT).getDayCountFraction(SPOT_DATE, dates[i]);
    }
    final Object xlResult = PROCESSOR.invoke("ISDAYieldCurve.ExpandDiscountFactorsForDates", (XLObject) XL_RESULT, convertToXlType(SPOT_DATE),
        convertToXlType(CURVE_DAY_COUNT), convertToXlType(dates));
    assertTrue(xlResult instanceof XLArray);
    final XLValue[][] xlValues = ((XLArray) xlResult).getArray();
    assertEquals(xlValues.length, dates.length);
    assertEquals(xlValues[0].length, 2);
    for (int i = 0; i < xlValues.length; i++) {
      assertTrue(xlValues[i][0] instanceof XLNumber);
      assertTrue(xlValues[i][1] instanceof XLNumber);
      assertEquals(((XLNumber) xlValues[i][0]).getAsDouble(), t[i], 1e-15);
      assertEquals(((XLNumber) xlValues[i][1]).getAsDouble(), EXPECTED_CURVE.getDiscountFactor(t[i]), 1e-15);
    }
  }

  @Test
  public void testZeroRateForDate() {
    final LocalDate date = LocalDate.of(2016, 12, 20);
    final double t = DayCountFactory.INSTANCE.instance(CURVE_DAY_COUNT).getDayCountFraction(SPOT_DATE, date);
    final Object xlResult = PROCESSOR.invoke("ISDAYieldCurve.ZeroRateForDate", (XLObject) XL_RESULT, convertToXlType(SPOT_DATE),
        convertToXlType(CURVE_DAY_COUNT), convertToXlType(date));
    assertTrue(xlResult instanceof XLNumber);
    assertEquals(((XLNumber) xlResult).getAsDouble(), EXPECTED_CURVE.getZeroRate(t), 1e-15);
  }

  @Test
  public void testDiscountFactorForDate() {
    final LocalDate date = LocalDate.of(2016, 12, 20);
    final double t = DayCountFactory.INSTANCE.instance(CURVE_DAY_COUNT).getDayCountFraction(SPOT_DATE, date);
    final Object xlResult = PROCESSOR.invoke("ISDAYieldCurve.DiscountFactorForDate", (XLObject) XL_RESULT, convertToXlType(SPOT_DATE),
        convertToXlType(CURVE_DAY_COUNT), convertToXlType(date));
    assertTrue(xlResult instanceof XLNumber);
    assertEquals(((XLNumber) xlResult).getAsDouble(), EXPECTED_CURVE.getDiscountFactor(t), 1e-15);
  }

  @Test
  public void testZeroRate() {
    final double t = 6.4;
    final Object xlResult = PROCESSOR.invoke("ISDAYieldCurve.ZeroRate", (XLObject) XL_RESULT, convertToXlType(t));
    assertTrue(xlResult instanceof XLNumber);
    assertEquals(((XLNumber) xlResult).getAsDouble(), EXPECTED_CURVE.getZeroRate(t), 1e-15);
  }

  @Test
  public void testDiscountFactor() {
    final double t = 2.76;
    final Object xlResult = PROCESSOR.invoke("ISDAYieldCurve.DiscountFactor", (XLObject) XL_RESULT, convertToXlType(t));
    assertTrue(xlResult instanceof XLNumber);
    assertEquals(((XLNumber) xlResult).getAsDouble(), EXPECTED_CURVE.getDiscountFactor(t), 1e-15);
  }
}
