/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.TestUtils.assert2dXlArray;
import static com.mcleodmoores.xl4j.examples.TestUtils.convertToXlType;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.SimpleWorkingDayCalendar;
import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurveBuild;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDAInstrumentTypes;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Unit tests for {@link IsdaYieldCurveBuilder}.
 */
public class IsdaYieldCurveBuilderTest extends IsdaTests {
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
  /** The number of spot days */
  private static final int SPOT_DAYS = 2;
  /** The holidays */
  private static final LocalDate[] HOLIDAYS = new LocalDate[] {LocalDate.of(2016, 8, 1)};
  /** The calendar */
  private static final Calendar CALENDAR =
      new CalendarAdapter(new SimpleWorkingDayCalendar("Calendar", Arrays.asList(LocalDate.of(2016, 8, 1)), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
  /** The expected curve */
  private static final ISDACompliantYieldCurve EXPECTED_CURVE;
  /** The object constructed by the function processor */
  private static final Object XL_RESULT = PROCESSOR.invoke("ISDAYieldCurve.BuildCurve", convertToXlType(TRADE_DATE), convertToXlType(INSTRUMENT_TYPE_STRINGS),
      convertToXlType(TENOR_STRINGS), convertToXlType(QUOTES), convertToXlType(MONEY_MARKET_DAY_COUNT), convertToXlType(SWAP_DAY_COUNT),
      convertToXlType(SWAP_INTERVAL), convertToXlType(CURVE_DAY_COUNT), convertToXlType(BDC), convertToXlType(SPOT_DATE), XLMissing.INSTANCE,
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
        CALENDAR);
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
  public void testConstructionOfCurveWithConvention() {
    final IsdaYieldCurveConvention convention = ConventionFunctions.buildYieldCurveConvention(XLString.of(MONEY_MARKET_DAY_COUNT), XLString.of(SWAP_DAY_COUNT),
        XLString.of(SWAP_INTERVAL), XLString.of(CURVE_DAY_COUNT), XLString.of(BDC), XLNumber.of(SPOT_DAYS));
    final Object xlResult = PROCESSOR.invoke("ISDAYieldCurve.BuildCurveFromConvention", convertToXlType(TRADE_DATE),
        convertToXlType(INSTRUMENT_TYPE_STRINGS), convertToXlType(TENOR_STRINGS), convertToXlType(QUOTES),
        convertToXlType(convention, HEAP), convertToXlType(SPOT_DATE), convertToXlType(HOLIDAYS));
    assertTrue(xlResult instanceof XLObject);
    final Object result = HEAP.getObject(((XLObject) xlResult).getHandle());
    assertTrue(result instanceof ISDACompliantCurve);
    final ISDACompliantCurve curve = (ISDACompliantCurve) result;
    // curves won't be equals() because the names will be different
    assertArrayEquals(curve.getKnotTimes(), EXPECTED_CURVE.getKnotTimes(), 1e-15);
    assertArrayEquals(curve.getKnotZeroRates(), EXPECTED_CURVE.getKnotZeroRates(), 1e-15);
  }

  @Test
  public void testConstructionOfCurveWithoutOptional() {
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
    final ISDACompliantYieldCurve expectedCurve = builder.build(QUOTES);
    final Object xlResult = PROCESSOR.invoke("ISDAYieldCurve.BuildCurve", convertToXlType(TRADE_DATE), convertToXlType(INSTRUMENT_TYPE_STRINGS),
        convertToXlType(TENOR_STRINGS), convertToXlType(QUOTES), convertToXlType(MONEY_MARKET_DAY_COUNT), convertToXlType(SWAP_DAY_COUNT),
        convertToXlType(SWAP_INTERVAL), convertToXlType(CURVE_DAY_COUNT), convertToXlType(BDC), convertToXlType(SPOT_DATE), XLMissing.INSTANCE,
        XLMissing.INSTANCE);
    assertTrue(xlResult instanceof XLObject);
    final Object result = HEAP.getObject(((XLObject) xlResult).getHandle());
    assertTrue(result instanceof ISDACompliantCurve);
    final ISDACompliantCurve curve = (ISDACompliantCurve) result;
    // curves won't be equals() because the names will be different
    assertArrayEquals(curve.getKnotTimes(), expectedCurve.getKnotTimes(), 1e-15);
    assertArrayEquals(curve.getKnotZeroRates(), expectedCurve.getKnotZeroRates(), 1e-15);
  }

  @Test
  public void testExpandCurve() {
    final Object xlResult = PROCESSOR.invoke("ISDAYieldCurve.Expand", (XLObject) XL_RESULT);
    assertTrue(xlResult instanceof XLArray);
    assert2dXlArray((XLArray) xlResult, EXPECTED_CURVE.getXData(), EXPECTED_CURVE.getYData());
  }

  @Test
  public void testExpandDiscountFactors() {
    final Object xlResult = PROCESSOR.invoke("ISDAYieldCurve.ExpandDiscountFactors", (XLObject) XL_RESULT);
    assertTrue(xlResult instanceof XLArray);
    final Double[] discountFactors = new Double[EXPECTED_CURVE.getNumberOfKnots()];
    for (int i = 0; i < EXPECTED_CURVE.getNumberOfKnots(); i++) {
      discountFactors[i] = EXPECTED_CURVE.getDiscountFactor(EXPECTED_CURVE.getTimeAtIndex(i));
    }
    assert2dXlArray((XLArray) xlResult, EXPECTED_CURVE.getXData(), discountFactors);
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
    final double[] zeroRates = new double[dates.length];
    for (int i = 0; i < dates.length; i++) {
      zeroRates[i] = EXPECTED_CURVE.getZeroRate(t[i]);
    }
    assert2dXlArray((XLArray) xlResult, t, zeroRates);
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
    final double[] discountFactors = new double[dates.length];
    for (int i = 0; i < dates.length; i++) {
      discountFactors[i] = EXPECTED_CURVE.getDiscountFactor(t[i]);
    }
    assert2dXlArray((XLArray) xlResult, t, discountFactors);
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
