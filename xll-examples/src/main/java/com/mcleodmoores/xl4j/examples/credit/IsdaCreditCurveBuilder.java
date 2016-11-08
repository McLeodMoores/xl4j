/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.credit.CdsQuoteConverter.createQuote;
import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.createHolidayCalendar;
import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.parsePeriod;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.XLArgument;
import com.mcleodmoores.xl4j.XLFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FastCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Methods that construct an ISDA CDS model credit curve and extract information from the curve once constructed.
 */
public final class IsdaCreditCurveBuilder {

  /**
   * Constructs a credit curve using the ISDA CDS model.
   * @param tradeDate  the trade date
   * @param tenors  the tenors of the CDS used to construct the curve
   * @param quoteTypes  the quote types: PAR SPREAD; QUOTED SPREAD; and PUF or POINTS UPFRONT. Must have one per tenor
   * @param quotes  the market data quotes. Must have one per tenor
   * @param recoveryRates  the recovery rates as decimals. Must have one per tenor
   * @param coupons  the coupons as decimals. Must have one per tenor
   * @param yieldCurve  the yield curve to be used in discounting
   * @param convention  the convention for the CDS
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  a credit curve constructed using the ISDA model
   */
  @XLFunction(name = "ISDACreditCurve.BuildIMMCurveFromConvention", category = "ISDA CDS model",
      description = "Build a hazard rate curve for IMM CDS using the ISDA methodology")
  public static ISDACompliantCreditCurve buildCreditCurve(
      @XLArgument(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLArgument(description = "Tenors", name = "Tenors") final String[] tenors,
      @XLArgument(description = "Quote Type", name = "Quote Types") final String[] quoteTypes,
      @XLArgument(description = "Quotes", name = "Quotes") final double[] quotes,
      @XLArgument(description = "Recovery Rates", name = "Recovery Rates") final double[] recoveryRates,
      @XLArgument(description = "Coupons", name = "Coupons") final double[] coupons,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Convention", name = "Convention") final IsdaCdsConvention convention,
      @XLArgument(optional = true, description = "Holidays", name = "Holidays") final LocalDate[] holidayDates) {
    final int n = tenors.length;
    ArgumentChecker.isTrue(n == quoteTypes.length, "Must have one quote type per tenor, have {} tenors and {} quote types", n, quoteTypes.length);
    ArgumentChecker.isTrue(n == quotes.length, "Must have one quote per tenor, have {} tenors and {} quotes", n, quotes.length);
    ArgumentChecker.isTrue(n == recoveryRates.length, "Must have one recovery rate per tenor, have {} tenors and {} recovery rates", n, recoveryRates.length);
    ArgumentChecker.isTrue(n == coupons.length, "Must have one coupon per tenor, have {} tenors and {} quote types", n, coupons.length);
    CDSAnalyticFactory cdsFactory = new CDSAnalyticFactory();
    cdsFactory = cdsFactory.withAccrualDCC(convention.getAccrualDayCount());
    cdsFactory = cdsFactory.withCurveDCC(convention.getCurveDayCount());
    cdsFactory = cdsFactory.with(convention.getBusinessDayConvention());
    if (holidayDates != null) {
      cdsFactory = cdsFactory.with(createHolidayCalendar(holidayDates));
    }
    if (convention.getCouponInterval() != null) {
      cdsFactory = cdsFactory.with(convention.getCouponInterval());
    }
    if (convention.getStubType() != null) {
      cdsFactory = cdsFactory.with(convention.getStubType());
    }
    if (convention.getCashSettlementDays() != null) {
      cdsFactory = cdsFactory.withCashSettle(convention.getCashSettlementDays());
    }
    if (convention.getPayAccrualOnDefault() != null) {
      cdsFactory = cdsFactory.withPayAccOnDefault(convention.getPayAccrualOnDefault());
    }
    if (convention.getStepInDays() != null) {
      cdsFactory = cdsFactory.withStepIn(convention.getStepInDays());
    }
    final CDSAnalytic[] calibrationCds = new CDSAnalytic[n];
    final CDSQuoteConvention[] marketQuotes = new CDSQuoteConvention[n];
    final ISDACompliantCreditCurveBuilder builder = new FastCreditCurveBuilder();
    for (int i = 0; i < n; i++) {
      cdsFactory = cdsFactory.withRecoveryRate(recoveryRates[i]);
      calibrationCds[i] = cdsFactory.makeIMMCDS(tradeDate, parsePeriod(tenors[i]));
      marketQuotes[i] = createQuote(coupons[i], quotes[i], quoteTypes[i]);
    }
    return builder.calibrateCreditCurve(calibrationCds, marketQuotes, yieldCurve);
  }

  /**
   * Constructs a credit curve using the ISDA CDS model.
   * @param tradeDate  the trade date
   * @param tenors  the tenors of the CDS used to construct the curve
   * @param quoteTypes  the quote types: PAR SPREAD; QUOTED SPREAD; and PUF or POINTS UPFRONT. Must have one per tenor
   * @param quotes  the market data quotes. Must have one per tenor
   * @param recoveryRates  the recovery rates as decimals. Must have one per tenor
   * @param coupons  the coupons as decimals. Must have one per tenor
   * @param yieldCurve  the yield curve to be used in discounting
   * @param accrualDayCountName  the accrual day count convention name
   * @param curveDayCountName  the curve day count convention name
   * @param businessDayConventionName  the business day convention name
   * @param couponIntervalName  the coupon interval name, is optional. If not supplied, 3 months is used
   * @param stubTypeName  the stub type name, is optional: FRONTSHORT; FRONTLONG; BACKSHORT; or BACKLONG. If not supplied, FRONTSHORT is used
   * @param cashSettlementDays  the number of cash settlement days, is optional. If not supplied, 3 is used
   * @param stepInDays  the number of step-in days, is optional. If not supplied, 1 is used
   * @param payAccrualOnDefault  true if the accrued is paid on default, is optional. If not supplied, true is used
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  a credit curve constructed using the ISDA model
   */
  @XLFunction(name = "ISDACreditCurve.BuildIMMCurve", category = "ISDA CDS model",
      description = "Build a hazard rate curve for IMM CDS using the ISDA methodology")
  public static ISDACompliantCreditCurve buildCreditCurve(
      @XLArgument(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLArgument(description = "Tenors", name = "Tenors") final String[] tenors,
      @XLArgument(description = "Quote Type", name = "Quote Types") final String[] quoteTypes,
      @XLArgument(description = "Quotes", name = "Quotes") final double[] quotes,
      @XLArgument(description = "Recovery Rates", name = "Recovery Rates") final double[] recoveryRates,
      @XLArgument(description = "Coupons", name = "Coupons") final double[] coupons,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Accrual Day Count", name = "Accrual Day Count") final String accrualDayCountName,
      @XLArgument(description = "Curve Day Count", name = "Curve Day Count") final String curveDayCountName,
      @XLArgument(description = "Business Day Convention", name = "Business Day Convention") final String businessDayConventionName,
      @XLArgument(optional = true, description = "Coupon Interval", name = "Coupon Interval") final String couponIntervalName,
      @XLArgument(optional = true, description = "Stub Type", name = "Stub Type") final String stubTypeName,
      @XLArgument(optional = true, description = "Cash Settlement Days", name = "Cash Settlement Days") final Integer cashSettlementDays,
      @XLArgument(optional = true, description = "Step In Days", name = "Step In Days") final Integer stepInDays,
      @XLArgument(optional = true, description = "Pay Accrual On Default", name = "Pay Accrual On Default") final Boolean payAccrualOnDefault,
      @XLArgument(optional = true, description = "Holidays", name = "Holidays") final LocalDate[] holidayDates) {
    final int n = tenors.length;
    ArgumentChecker.isTrue(n == quoteTypes.length, "Must have one quote type per tenor, have {} tenors and {} quote types", n, quoteTypes.length);
    ArgumentChecker.isTrue(n == quotes.length, "Must have one quote per tenor, have {} tenors and {} quotes", n, quotes.length);
    ArgumentChecker.isTrue(n == recoveryRates.length, "Must have one recovery rate per tenor, have {} tenors and {} recovery rates", n, recoveryRates.length);
    ArgumentChecker.isTrue(n == coupons.length, "Must have one coupon per tenor, have {} tenors and {} quote types", n, coupons.length);
    CDSAnalyticFactory cdsFactory = new CDSAnalyticFactory();
    cdsFactory = cdsFactory.withAccrualDCC(DayCountFactory.INSTANCE.instance(accrualDayCountName));
    cdsFactory = cdsFactory.withCurveDCC(DayCountFactory.INSTANCE.instance(curveDayCountName));
    cdsFactory = cdsFactory.with(BusinessDayConventionFactory.INSTANCE.instance(businessDayConventionName));
    if (holidayDates != null) {
      cdsFactory = cdsFactory.with(createHolidayCalendar(holidayDates));
    }
    if (couponIntervalName != null) {
      cdsFactory = cdsFactory.with(parsePeriod(couponIntervalName));
    }
    if (stubTypeName != null) {
      cdsFactory = cdsFactory.with(StubType.valueOf(stubTypeName));
    }
    if (cashSettlementDays != null) {
      cdsFactory = cdsFactory.withCashSettle(cashSettlementDays);
    }
    if (payAccrualOnDefault != null) {
      cdsFactory = cdsFactory.withPayAccOnDefault(payAccrualOnDefault);
    }
    if (stepInDays != null) {
      cdsFactory = cdsFactory.withStepIn(stepInDays);
    }
    final CDSAnalytic[] calibrationCds = new CDSAnalytic[n];
    final CDSQuoteConvention[] marketQuotes = new CDSQuoteConvention[n];
    final ISDACompliantCreditCurveBuilder builder = new FastCreditCurveBuilder();
    for (int i = 0; i < n; i++) {
      cdsFactory = cdsFactory.withRecoveryRate(recoveryRates[i]);
      calibrationCds[i] = cdsFactory.makeIMMCDS(tradeDate, parsePeriod(tenors[i]));
      marketQuotes[i] = createQuote(coupons[i], quotes[i], quoteTypes[i]);
    }
    return builder.calibrateCreditCurve(calibrationCds, marketQuotes, yieldCurve);
  }

  /**
   * Extracts the node times and hazard rates from an ISDA credit curve.
   * @param creditCurve  the credit cure
   * @return  an array containing a column of times and a column of hazard rates
   */
  @XLFunction(name = "ISDACreditCurve.Expand", category = "ISDA CDS model",
      description = "Show the nodal times and hazard rates of the credit curve")
  public static Object[][] expandCurve(
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    final Object[][] result = new Object[creditCurve.getNumberOfKnots()][2];
    for (int i = 0; i < creditCurve.getNumberOfKnots(); i++) {
      final double t = creditCurve.getTimeAtIndex(i);
      result[i][0] = t;
      result[i][1] = creditCurve.getHazardRate(t);
    }
    return result;
  }

  /**
   * Extracts the times and survival probabilities from an ISDA credit curve.
   * @param creditCurve  the credit curve
   * @return  an array containing a column of times and a column of survival probabilities
   */
  @XLFunction(name = "ISDACreditCurve.ExpandSurvivalProbabilities", category = "ISDA CDS model",
      description = "Show the nodal times and survival probabilities of the credit curve")
  public static Object[][] expandSurvivalProbabilities(
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    final Object[][] result = new Object[creditCurve.getNumberOfKnots()][2];
    for (int i = 0; i < creditCurve.getNumberOfKnots(); i++) {
      final double t = creditCurve.getTimeAtIndex(i);
      result[i][0] = t;
      result[i][1] = creditCurve.getSurvivalProbability(t);
    }
    return result;
  }

  /**
   * Extracts the times and hazard rates for a list of dates from an ISDA creedit curve.
   * @param creditCurve  the credit curve
   * @param currentDate  the current date, should be the curve construction date
   * @param curveDayCountConventionName  the day count convention name, should be the same as the curve day count used when constructing the curve
   * @param dates  the dates
   * @return  an array containing a column of times and a column of hazard rates
   */
  @XLFunction(name = "ISDACreditCurve.ExpandForDates", category = "ISDA CDS model", description = "Get times and hazard rates for dates")
  public static Object[][] expandCurve(
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Current Date", name = "Current Date") final LocalDate currentDate,
      @XLArgument(description = "Day Count Convention", name = "Day Count Convention") final String curveDayCountConventionName,
      @XLArgument(description = "Dates", name = "Dates") final LocalDate[] dates) {
    final Object[][] result = new Object[dates.length][2];
    final DayCount curveDayCount = DayCountFactory.INSTANCE.instance(curveDayCountConventionName);
    for (int i = 0; i < dates.length; i++) {
      final double t = curveDayCount.getDayCountFraction(currentDate, dates[i]);
      result[i][0] = t;
      result[i][1] = creditCurve.getHazardRate(t);
    }
    return result;
  }

  /**
   * Extracts the times and survival probabilities for a list of dates from an ISDA credit curve.
   * @param creditCurve  the credit curve
   * @param currentDate  the current date, should be the curve construction date
   * @param curveDayCountConventionName  the day count convention name, should be the same as the curve day count used when constructing the curve
   * @param dates  the dates
   * @return  an array containing a column of times and a column of survival probabilities
   */
  @XLFunction(name = "ISDACreditCurve.ExpandSurvivalProbabilitiesForDates", category = "ISDA CDS model",
      description = "Get times and survival probabilities for dates")
  public static Object[][] expandSurvivalProbabilities(
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Current Date", name = "Current Date") final LocalDate currentDate,
      @XLArgument(description = "Day Count Convention", name = "Day Count Convention") final String curveDayCountConventionName,
      @XLArgument(description = "Dates", name = "Dates") final LocalDate[] dates) {
    final Object[][] result = new Object[dates.length][2];
    final DayCount curveDayCount = DayCountFactory.INSTANCE.instance(curveDayCountConventionName);
    for (int i = 0; i < dates.length; i++) {
      final double t = curveDayCount.getDayCountFraction(currentDate, dates[i]);
      result[i][0] = t;
      result[i][1] = creditCurve.getSurvivalProbability(t);
    }
    return result;
  }

  /**
   * Gets the hazard rate for a date.
   * @param creditCurve  the credit curve
   * @param currentDate  the current date, should be the curve construction date
   * @param curveDayCountConventionName  the day count convention name, should be the saem as the curve day count used when constructing the curve
   * @param date  the date
   * @return  the hazard rate
   */
  @XLFunction(name = "ISDACreditCurve.HazardRateForDate", category = "ISDA CDS model", description = "Get hazard rate on a date")
  public static Double getHazardRate(
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Current Date", name = "Current Date") final LocalDate currentDate,
      @XLArgument(description = "Day Count Convention", name = "Curve Day Count Convention") final String curveDayCountConventionName,
      @XLArgument(description = "Date", name = "Date") final LocalDate date) {
    final DayCount curveDayCount = DayCountFactory.INSTANCE.instance(curveDayCountConventionName);
    final double t = curveDayCount.getDayCountFraction(currentDate, date);
    return creditCurve.getHazardRate(t);
  }

  /**
   * Gets the survival probability for a date.
   * @param creditCurve  the credit curve
   * @param currentDate  the current date, should be the curve construction date
   * @param curveDayCountConventionName  the day count convention name, should be the same as the curve day count used when constructing the curve
   * @param date  the date
   * @return  the survival probability
   */
  @XLFunction(name = "ISDACreditCurve.SurvivalProbabilityForDate", category = "ISDA CDS model", description = "Get survival probability on a date")
  public static Double getSurvivalProbability(
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Current Date", name = "Current Date") final LocalDate currentDate,
      @XLArgument(description = "Day Count Convention", name = "Curve Day Count Convention") final String curveDayCountConventionName,
      @XLArgument(description = "Date", name = "Date") final LocalDate date) {
    final DayCount curveDayCount = DayCountFactory.INSTANCE.instance(curveDayCountConventionName);
    final double t = curveDayCount.getDayCountFraction(currentDate, date);
    return creditCurve.getSurvivalProbability(t);
  }

  /**
   * Gets the hazard rate for a time.
   * @param creditCurve  the credit curve
   * @param t  the time
   * @return  the hazard rate
   */
  @XLFunction(name = "ISDACreditCurve.HazardRate", category = "ISDA CDS model", description = "Get hazard rate for a time")
  public static Double getHazardRate(
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Time", name = "Time") final Double t) {
    return creditCurve.getHazardRate(t);
  }


  /**
   * Gets the survival probability for a time.
   * @param creditCurve  the credit curve
   * @param t  the time
   * @return  the survival probability
   */
  @XLFunction(name = "ISDACreditCurve.SurvivalProbability", category = "ISDA CDS model", description = "Get survival probability for a time")
  public static Double getSurvivalProbability(
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Time", name = "Time") final Double t) {
    return creditCurve.getSurvivalProbability(t);
  }


  /**
   * Restricted constructor.
   */
  private IsdaCreditCurveBuilder() {
  }
}
