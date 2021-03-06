/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.TestUtils.assert2dXlArray;
import static com.mcleodmoores.xl4j.examples.TestUtils.convertToXlType;
import static com.mcleodmoores.xl4j.examples.credit.CdsQuoteConverter.createQuote;
import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.createHolidayCalendar;
import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.parsePeriod;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FastCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Unit tests for {@link IsdaCreditCurveBuilder}.
 */
public class IsdaCreditCurveBuilderTest extends IsdaTests {
  /** The trade date */
  private static final LocalDate TRADE_DATE = LocalDate.of(2016, 10, 3);
  /** The quote types */
  private static final String[] QUOTE_TYPES = new String[] {"PAR SPREAD", "PAR SPREAD", "PUF", "QUOTED SPREAD"};
  /** The tenors */
  private static final String[] TENORS = new String[] {"P1Y", "P2Y", "P5Y", "P10Y"};
  /** The quotes */
  private static final double[] QUOTES = new double[] {0.008, 0.01, -0.003, 0.025};
  /** The recovery rates */
  private static final double[] RECOVERY_RATES = new double[] {0.4, 0.4, 0.5, 0.4};
  /** The coupons */
  private static final double[] COUPONS = new double[] {0.01, 0.01, 0.05, 0.01};
  /** The yield curve */
  private static final ISDACompliantYieldCurve YIELD_CURVE = new ISDACompliantYieldCurve(1, 0.01);
  /** The accrual day count */
  private static final String ACCRUAL_DAY_COUNT_NAME = "ACT/360";
  /** The curve day count */
  private static final String CURVE_DAY_COUNT_NAME = "ACT/365";
  /** The business day convention */
  private static final String BDC_NAME = "F";
  /** The coupon interval */
  private static final String COUPON_INTERVAL = "6M";
  /** The stub type */
  private static final String STUB_TYPE = "BACKLONG";
  /** The number of cash settlement days */
  private static final int CASH_SETTLEMENT_DAYS = 2;
  /** The number of step-in days */
  private static final int STEP_IN_DAYS = 2;
  /** Pay accrual on default flag */
  private static final boolean PAY_ACCRUAL_ON_DEFAULT = false;
  /** Holidays */
  private static final LocalDate[] HOLIDAYS = new LocalDate[] {LocalDate.of(2016, 11, 1)};
  /** The expected curve */
  private static final ISDACompliantCreditCurve EXPECTED_CURVE;
  /** The object constructed by the function processor */
  private static final Object XL_RESULT = PROCESSOR.invoke("ISDACreditCurve.BuildIMMCurve", convertToXlType(TRADE_DATE), convertToXlType(TENORS),
      convertToXlType(QUOTE_TYPES), convertToXlType(QUOTES), convertToXlType(RECOVERY_RATES), convertToXlType(COUPONS), convertToXlType(YIELD_CURVE, HEAP),
      convertToXlType(ACCRUAL_DAY_COUNT_NAME), convertToXlType(CURVE_DAY_COUNT_NAME), convertToXlType(BDC_NAME), convertToXlType(COUPON_INTERVAL),
      convertToXlType(STUB_TYPE), convertToXlType(CASH_SETTLEMENT_DAYS), convertToXlType(STEP_IN_DAYS), convertToXlType(PAY_ACCRUAL_ON_DEFAULT),
      convertToXlType(HOLIDAYS));
  static {
    CDSAnalyticFactory cdsFactory = new CDSAnalyticFactory()
        .withAccrualDCC(DayCountFactory.INSTANCE.instance(ACCRUAL_DAY_COUNT_NAME))
        .withCurveDCC(DayCountFactory.INSTANCE.instance(CURVE_DAY_COUNT_NAME))
        .with(BusinessDayConventionFactory.INSTANCE.instance(BDC_NAME))
        .with(createHolidayCalendar(HOLIDAYS))
        .with(parsePeriod(COUPON_INTERVAL))
        .with(StubType.valueOf(STUB_TYPE))
        .withCashSettle(CASH_SETTLEMENT_DAYS)
        .withPayAccOnDefault(PAY_ACCRUAL_ON_DEFAULT)
        .withStepIn(STEP_IN_DAYS);
    final int n = TENORS.length;
    final CDSAnalytic[] calibrationCds = new CDSAnalytic[n];
    final CDSQuoteConvention[] marketQuotes = new CDSQuoteConvention[n];
    final ISDACompliantCreditCurveBuilder builder = new FastCreditCurveBuilder();
    for (int i = 0; i < n; i++) {
      cdsFactory = cdsFactory.withRecoveryRate(RECOVERY_RATES[i]);
      calibrationCds[i] = cdsFactory.makeIMMCDS(TRADE_DATE, parsePeriod(TENORS[i]));
      marketQuotes[i] = createQuote(COUPONS[i], QUOTES[i], QUOTE_TYPES[i]);
    }
    EXPECTED_CURVE = builder.calibrateCreditCurve(calibrationCds, marketQuotes, YIELD_CURVE);
  }

  /**
   * Tests the credit curve construction function.
   */
  @Test
  public void testConstructionOfCurve() {
    assertTrue(XL_RESULT instanceof XLObject);
    final Object result = HEAP.getObject(((XLObject) XL_RESULT).getHandle());
    assertTrue(result instanceof ISDACompliantCreditCurve);
    final ISDACompliantCreditCurve curve = (ISDACompliantCreditCurve) result;
    // curves won't be equals() because the names will be different
    assertArrayEquals(curve.getKnotTimes(), EXPECTED_CURVE.getKnotTimes(), 1e-15);
    assertArrayEquals(curve.getKnotZeroRates(), EXPECTED_CURVE.getKnotZeroRates(), 1e-15);
  }

  /**
   * Tests the construction of the credit curve.
   */
  @Test
  public void testConstructionOfCurveWithConvention() {
    final IsdaCdsConvention convention = ConventionFunctions.buildCdsConvention(XLString.of(ACCRUAL_DAY_COUNT_NAME),
        XLString.of(CURVE_DAY_COUNT_NAME), XLString.of(BDC_NAME), XLString.of(COUPON_INTERVAL), XLString.of(STUB_TYPE),
        XLNumber.of(CASH_SETTLEMENT_DAYS), XLNumber.of(STEP_IN_DAYS), XLBoolean.from(PAY_ACCRUAL_ON_DEFAULT));
    final Object xlResult = PROCESSOR.invoke("ISDACreditCurve.BuildIMMCurveFromConvention", convertToXlType(TRADE_DATE), convertToXlType(TENORS),
        convertToXlType(QUOTE_TYPES), convertToXlType(QUOTES), convertToXlType(RECOVERY_RATES), convertToXlType(COUPONS), convertToXlType(YIELD_CURVE, HEAP),
        convertToXlType(convention, HEAP), convertToXlType(HOLIDAYS));
    assertTrue(xlResult instanceof XLObject);
    final Object result = HEAP.getObject(((XLObject) xlResult).getHandle());
    assertTrue(result instanceof ISDACompliantCreditCurve);
    final ISDACompliantCreditCurve curve = (ISDACompliantCreditCurve) result;
    // curves won't be equals() because the names will be different
    assertArrayEquals(curve.getKnotTimes(), EXPECTED_CURVE.getKnotTimes(), 1e-15);
    assertArrayEquals(curve.getKnotZeroRates(), EXPECTED_CURVE.getKnotZeroRates(), 1e-15);
  }

  /**
   * Tests the construction of the credit curve.
   */
  @Test
  public void testConstructionOfCurveWithoutOptional() {
    CDSAnalyticFactory cdsFactory = new CDSAnalyticFactory()
        .withAccrualDCC(DayCountFactory.INSTANCE.instance(ACCRUAL_DAY_COUNT_NAME))
        .withCurveDCC(DayCountFactory.INSTANCE.instance(CURVE_DAY_COUNT_NAME))
        .with(BusinessDayConventionFactory.INSTANCE.instance(BDC_NAME));
    final int n = TENORS.length;
    final CDSAnalytic[] calibrationCds = new CDSAnalytic[n];
    final CDSQuoteConvention[] marketQuotes = new CDSQuoteConvention[n];
    final ISDACompliantCreditCurveBuilder builder = new FastCreditCurveBuilder();
    for (int i = 0; i < n; i++) {
      cdsFactory = cdsFactory.withRecoveryRate(RECOVERY_RATES[i]);
      calibrationCds[i] = cdsFactory.makeIMMCDS(TRADE_DATE, parsePeriod(TENORS[i]));
      marketQuotes[i] = createQuote(COUPONS[i], QUOTES[i], QUOTE_TYPES[i]);
    }
    final ISDACompliantCreditCurve expectedCurve = builder.calibrateCreditCurve(calibrationCds, marketQuotes, YIELD_CURVE);
    final Object xlResult = PROCESSOR.invoke("ISDACreditCurve.BuildIMMCurve", convertToXlType(TRADE_DATE), convertToXlType(TENORS),
        convertToXlType(QUOTE_TYPES), convertToXlType(QUOTES), convertToXlType(RECOVERY_RATES), convertToXlType(COUPONS), convertToXlType(YIELD_CURVE, HEAP),
        convertToXlType(ACCRUAL_DAY_COUNT_NAME), convertToXlType(CURVE_DAY_COUNT_NAME), convertToXlType(BDC_NAME),
        XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE);
    assertTrue(xlResult instanceof XLObject);
    final Object result = HEAP.getObject(((XLObject) xlResult).getHandle());
    assertTrue(result instanceof ISDACompliantCreditCurve);
    final ISDACompliantCreditCurve curve = (ISDACompliantCreditCurve) result;
    // curves won't be equals() because the names will be different
    assertArrayEquals(curve.getKnotTimes(), expectedCurve.getKnotTimes(), 1e-15);
    assertArrayEquals(curve.getKnotZeroRates(), expectedCurve.getKnotZeroRates(), 1e-15);
  }

  /**
   * Tests the function that expands the credit curve.
   */
  @Test
  public void testExpandCurve() {
    final Object xlResult = PROCESSOR.invoke("ISDACreditCurve.Expand", (XLObject) XL_RESULT);
    assertTrue(xlResult instanceof XLArray);
    assert2dXlArray((XLArray) xlResult, EXPECTED_CURVE.getXData(), EXPECTED_CURVE.getYData());
  }

  /**
   * Tests the function that expands the credit curve to survival probabilities.
   */
  @Test
  public void testExpandSurvivalProbabilities() {
    final Object xlResult = PROCESSOR.invoke("ISDACreditCurve.ExpandSurvivalProbabilities", (XLObject) XL_RESULT);
    assertTrue(xlResult instanceof XLArray);
    final Double[] survivalProbabilities = new Double[EXPECTED_CURVE.getNumberOfKnots()];
    for (int i = 0; i < EXPECTED_CURVE.getNumberOfKnots(); i++) {
      survivalProbabilities[i] = EXPECTED_CURVE.getSurvivalProbability(EXPECTED_CURVE.getTimeAtIndex(i));
    }
    assert2dXlArray((XLArray) xlResult, EXPECTED_CURVE.getXData(), survivalProbabilities);
  }

  /**
   * Tests the function that expands the credit curve for a list of dates.
   */
  @Test
  public void testExpandCurveForDates() {
    final LocalDate[] dates = new LocalDate[] {LocalDate.of(2017, 3, 19), LocalDate.of(2018, 3, 19), LocalDate.of(2019, 3, 19)};
    final double[] t = new double[dates.length];
    for (int i = 0; i < dates.length; i++) {
      t[i] = DayCountFactory.INSTANCE.instance(CURVE_DAY_COUNT_NAME).getDayCountFraction(TRADE_DATE, dates[i]);
    }
    final Object xlResult = PROCESSOR.invoke("ISDACreditCurve.ExpandForDates", (XLObject) XL_RESULT, convertToXlType(TRADE_DATE),
        convertToXlType(CURVE_DAY_COUNT_NAME), convertToXlType(dates));
    assertTrue(xlResult instanceof XLArray);
    final double[] hazardRates = new double[dates.length];
    for (int i = 0; i < dates.length; i++) {
      hazardRates[i] = EXPECTED_CURVE.getZeroRate(t[i]);
    }
    assert2dXlArray((XLArray) xlResult, t, hazardRates);
  }

  /**
   * Tests the function that expands a credit curve to survival probabilities for a list of dates.
   */
  @Test
  public void testSurvivalProbabilitiesForDates() {
    final LocalDate[] dates = new LocalDate[] {LocalDate.of(2017, 3, 19), LocalDate.of(2018, 3, 19), LocalDate.of(2019, 3, 19)};
    final double[] t = new double[dates.length];
    for (int i = 0; i < dates.length; i++) {
      t[i] = DayCountFactory.INSTANCE.instance(CURVE_DAY_COUNT_NAME).getDayCountFraction(TRADE_DATE, dates[i]);
    }
    final Object xlResult = PROCESSOR.invoke("ISDACreditCurve.ExpandSurvivalProbabilitiesForDates", (XLObject) XL_RESULT, convertToXlType(TRADE_DATE),
        convertToXlType(CURVE_DAY_COUNT_NAME), convertToXlType(dates));
    assertTrue(xlResult instanceof XLArray);
    final double[] survivalProbabilities = new double[dates.length];
    for (int i = 0; i < dates.length; i++) {
      survivalProbabilities[i] = EXPECTED_CURVE.getDiscountFactor(t[i]);
    }
    assert2dXlArray((XLArray) xlResult, t, survivalProbabilities);
  }

  /**
   * Test the function that gets a hazard rate for a date.
   */
  @Test
  public void testHazardRateForDate() {
    final LocalDate date = LocalDate.of(2016, 12, 20);
    final double t = DayCountFactory.INSTANCE.instance(CURVE_DAY_COUNT_NAME).getDayCountFraction(TRADE_DATE, date);
    final Object xlResult = PROCESSOR.invoke("ISDACreditCurve.HazardRateForDate", (XLObject) XL_RESULT, convertToXlType(TRADE_DATE),
        convertToXlType(CURVE_DAY_COUNT_NAME), convertToXlType(date));
    assertTrue(xlResult instanceof XLNumber);
    assertEquals(((XLNumber) xlResult).getAsDouble(), EXPECTED_CURVE.getHazardRate(t), 1e-15);
  }

  /**
   * Tests the function that gets a survival probability for a date.
   */
  @Test
  public void testSurvivalProbabilityForDate() {
    final LocalDate date = LocalDate.of(2016, 12, 20);
    final double t = DayCountFactory.INSTANCE.instance(CURVE_DAY_COUNT_NAME).getDayCountFraction(TRADE_DATE, date);
    final Object xlResult = PROCESSOR.invoke("ISDACreditCurve.SurvivalProbabilityForDate", (XLObject) XL_RESULT, convertToXlType(TRADE_DATE),
        convertToXlType(CURVE_DAY_COUNT_NAME), convertToXlType(date));
    assertTrue(xlResult instanceof XLNumber);
    assertEquals(((XLNumber) xlResult).getAsDouble(), EXPECTED_CURVE.getSurvivalProbability(t), 1e-15);
  }

  /**
   * Tests the function that gets the hazard rate for a time.
   */
  @Test
  public void testHazardRate() {
    final double t = 6.4;
    final Object xlResult = PROCESSOR.invoke("ISDACreditCurve.HazardRate", (XLObject) XL_RESULT, convertToXlType(t));
    assertTrue(xlResult instanceof XLNumber);
    assertEquals(((XLNumber) xlResult).getAsDouble(), EXPECTED_CURVE.getHazardRate(t), 1e-15);
  }

  /**
   * Tests the survival probability curve function.
   */
  @Test
  public void testSurvivalProbability() {
    final double t = 2.76;
    final Object xlResult = PROCESSOR.invoke("ISDACreditCurve.SurvivalProbability", (XLObject) XL_RESULT, convertToXlType(t));
    assertTrue(xlResult instanceof XLNumber);
    assertEquals(((XLNumber) xlResult).getAsDouble(), EXPECTED_CURVE.getSurvivalProbability(t), 1e-15);
  }

}
