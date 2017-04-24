/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.credit.CdsQuoteConverter.createQuote;
import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.createHolidayCalendar;
import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.parsePeriod;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticSpreadSensitivityCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSRiskFactors;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FastCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.InterestRateSensitivityCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Methods that calculate price and risk values for CDS using the ISDA model.
 */
public final class CdsPricer {
  /** The price calculator */
  private static final AnalyticCDSPricer PRICE_CALCULATOR = new AnalyticCDSPricer();
  /** The risk factor calculator */
  private static final CDSRiskFactors RISK_CALCULATOR = new CDSRiskFactors();
  /** The interest rate sensitivity calculator */
  private static final InterestRateSensitivityCalculator IR_CALCULATOR = new InterestRateSensitivityCalculator();
  /** The spread sensitivity calculator */
  private static final AnalyticSpreadSensitivityCalculator SPREAD_CALCULATOR = new AnalyticSpreadSensitivityCalculator();
  /** One basis point */
  private static final double BPS = 1e-4;

  /**
   * Calculate the clean price of a CDS given a yield and credit curve.
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @return  the clean price
   */
  @XLFunction(name = "CDS.CleanPrice", category = "ISDA CDS model",
      description = "Calculate the clean price of a CDS")
  public static double cleanPrice(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    return cds.getNotional() * PRICE_CALCULATOR.pv(cds.getUnderlyingCds(), yieldCurve, creditCurve, cds.getInitialQuote().getCoupon(), PriceType.CLEAN);
  }

  /**
   * Calculate the dirty price of a CDS given a yield and credit curve.
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @return  the dirty price
   */
  @XLFunction(name = "CDS.DirtyPrice", category = "ISDA CDS model",
      description = "Calculate the dirty price of a CDS for the protection buyer")
  public static double dirtyPrice(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    return cds.getNotional() * PRICE_CALCULATOR.pv(cds.getUnderlyingCds(), yieldCurve, creditCurve, cds.getInitialQuote().getCoupon(), PriceType.DIRTY);
  }

  /**
   * Calculate the accrued interest of a CDS given a yield and credit curve.
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @return  the accrued interest
   */
  @XLFunction(name = "CDS.Accrued", category = "ISDA CDS model",
      description = "Calculate the accrued of a CDS")
  public static double accruedInterest(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    return dirtyPrice(cds, yieldCurve, creditCurve) - cleanPrice(cds, yieldCurve, creditCurve);
  }

  /**
   * Calculate the par spread of a CDS.
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @return  the par spread
   */
  @XLFunction(name = "CDS.ParSpread", category = "ISDA CDS model",
      description = "The par spread of a CDS")
  public static double parSpread(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    return PRICE_CALCULATOR.parSpread(cds.getUnderlyingCds(), yieldCurve, creditCurve);
  }

  /**
   * Calculates the price of the protection leg given a yield and credit curve.
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @return  the protection leg price
   */
  @XLFunction(name = "CDS.ProtectionLegPrice", category = "ISDA CDS model",
      description = "The present value of the protection leg of a CDS")
  public static double protectionLegPv(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    return cds.getNotional() * PRICE_CALCULATOR.protectionLeg(cds.getUnderlyingCds(), yieldCurve, creditCurve);
  }

  /**
   * Calculates the clean price of the premium leg.
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @return  the premium leg clean price
   */
  @XLFunction(name = "CDS.PremiumLegCleanPrice", category = "ISDA CDS model",
      description = "The clean price of the premium leg of a CDS")
  public static double premiumLegPv(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    return -cds.getNotional() * PRICE_CALCULATOR.annuity(cds.getUnderlyingCds(), yieldCurve, creditCurve, PriceType.CLEAN) * cds.getInitialQuote().getCoupon();
  }

  /**
   * Calculates the change in value of a CDS when the recovery rate is changed by 1 basis point.
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @return  the RR01
   */
  @XLFunction(name = "CDS.RR01", category = "ISDA CDS model",
      description = "The sensitivity of the price to a 1 basis point change in the recovery rate")
  public static double rr01(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    return cds.getNotional() * RISK_CALCULATOR.recoveryRateSensitivity(cds.getUnderlyingCds(), yieldCurve, creditCurve) * BPS;
  }

  /**
   * Calculates the value on default of a CDS.
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @return  the value on default
   */
  @XLFunction(name = "CDS.ValueOnDefault", category = "ISDA CDS model",
      description = "The value on default of a CDS")
  public static double valueOnDefault(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    return cds.getNotional() * RISK_CALCULATOR.valueOnDefault(cds.getUnderlyingCds(), yieldCurve, creditCurve, cds.getInitialQuote().getCoupon());
  }

  /**
   * Calculates the change in value of a CDS when the yield curve is parallel-shifted by 1 basis point.
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @return  the IR01
   */
  @XLFunction(name = "CDS.IR01", category = "ISDA CDS model",
      description = "The sensitivity of the price to a 1 basis point change in the yield curve")
  public static double ir01(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    return cds.getNotional() * IR_CALCULATOR.parallelIR01(cds.getUnderlyingCds(), cds.getInitialQuote().getCoupon(), creditCurve, yieldCurve);
  }

  /**
   * Calculates the change in value of a CDS when the credit spreads are parallel-shifted by 1 basis point.
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @return  the CS01
   */
  @XLFunction(name = "CDS.CS01", category = "ISDA CDS model",
      description = "The sensitivity of the price to a 1 basis point change in the market spread")
  public static double cs01(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve) {
    return cds.getNotional() * SPREAD_CALCULATOR.parallelCS01(cds.getUnderlyingCds(), cds.getInitialQuote(), yieldCurve) / 10000;
  }

  /**
   * Calculates the change in value of a CDS at each yield curve node when the yield is shifted by 1 basis point.
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @return  the bucketed IR01
   */
  @XLFunction(name = "CDS.BucketedIR01", category = "ISDA CDS model",
      description = "The sensitivity of the price to a 1 basis point change in the yield curve")
  public static double[] bucketedIr01(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    final double[] bucketedIr01 = IR_CALCULATOR.bucketedIR01(cds.getUnderlyingCds(), cds.getInitialQuote().getCoupon(), creditCurve, yieldCurve);
    final double[] result = new double[bucketedIr01.length];
    for (int i = 0; i < bucketedIr01.length; i++) {
      result[i] = cds.getNotional() * bucketedIr01[i];
    }
    return result;
  }

  /**
   * Calculates the bucketed CS01.
   * @param tradeDate  the trade date
   * @param tenors  the tenors of the CDS used to construct the curve
   * @param quoteTypes  the quote types: PAR SPREAD; QUOTED SPREAD; and PUF or POINTS UPFRONT. Must have one per tenor
   * @param quotes  the market data quotes. Must have one per tenor
   * @param recoveryRates  the recovery rates as decimals. Must have one per tenor
   * @param coupons  the coupons as decimals. Must have one per tenor
   * @param yieldCurve  the yield curve to be used in discounting
   * @param convention  the convention for the CDS
   * @param cds  the CDS
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  the bucketed CS01
   */
  @XLFunction(name = "CDS.BucketedCS01", category = "ISDA CDS model",
      description = "The sensitivity of the price to a 1 basis point change in the market spread")
  public static double[] bucketedCs01(
      @XLParameter(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLParameter(description = "Tenors", name = "Tenors") final String[] tenors,
      @XLParameter(description = "Quote Type", name = "Quote Types") final String[] quoteTypes,
      @XLParameter(description = "Market Quotes", name = "Market Quotes") final double[] quotes,
      @XLParameter(description = "Recovery Rates", name = "Recovery Rates") final double[] recoveryRates,
      @XLParameter(description = "Coupons", name = "Coupons") final double[] coupons,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Convention", name = "Convention") final IsdaCdsConvention convention,
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(optional = true, description = "Holidays", name = "Holidays") final LocalDate[] holidayDates) {
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
    final ISDACompliantCreditCurve creditCurve = builder.calibrateCreditCurve(calibrationCds, marketQuotes, yieldCurve);
    final double[] bucketedCs01 = SPREAD_CALCULATOR.bucketedCS01FromCreditCurve(cds.getUnderlyingCds(), cds.getInitialQuote().getCoupon(),
        calibrationCds, yieldCurve, creditCurve);
    for (int i = 0; i < bucketedCs01.length; i++) {
      bucketedCs01[i] *= cds.getNotional();
    }
    return bucketedCs01;
  }

  /**
   * Calculates the bucketed CS01 for a CDS.
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
   * @param cds  the CDS
   * @param quoteType  the market quote type
   * @param marketQuote  the market quote
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  the bucketed CS01
   */
  @XLFunction(name = "ISDACreditCurve.BuildIMMCurve", category = "ISDA CDS model",
      description = "Build a hazard rate curve for IMM CDS using the ISDA methodology")
  public static double[] bucketedCs01(
      @XLParameter(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLParameter(description = "Tenors", name = "Tenors") final String[] tenors,
      @XLParameter(description = "Quote Type", name = "Quote Types") final String[] quoteTypes,
      @XLParameter(description = "Market Quotes", name = "Market Quotes") final double[] quotes,
      @XLParameter(description = "Recovery Rates", name = "Recovery Rates") final double[] recoveryRates,
      @XLParameter(description = "Coupons", name = "Coupons") final double[] coupons,
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Accrual Day Count", name = "Accrual Day Count") final String accrualDayCountName,
      @XLParameter(description = "Curve Day Count", name = "Curve Day Count") final String curveDayCountName,
      @XLParameter(description = "Business Day Convention", name = "Business Day Convention") final String businessDayConventionName,
      @XLParameter(optional = true, description = "Coupon Interval", name = "Coupon Interval") final String couponIntervalName,
      @XLParameter(optional = true, description = "Stub Type", name = "Stub Type") final String stubTypeName,
      @XLParameter(optional = true, description = "Cash Settlement Days", name = "Cash Settlement Days") final Integer cashSettlementDays,
      @XLParameter(optional = true, description = "Step In Days", name = "Step In Days") final Integer stepInDays,
      @XLParameter(optional = true, description = "Pay Accrual On Default", name = "Pay Accrual On Default") final Boolean payAccrualOnDefault,
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds,
      @XLParameter(description = "Quote Type", name = "Quote Type") final String quoteType,
      @XLParameter(description = "Market Quote", name = "Market Quote") final double marketQuote,
      @XLParameter(optional = true, description = "Holidays", name = "Holidays") final LocalDate[] holidayDates) {
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
    final ISDACompliantCreditCurve creditCurve = builder.calibrateCreditCurve(calibrationCds, marketQuotes, yieldCurve);
    final double[] bucketedCs01 = SPREAD_CALCULATOR.bucketedCS01FromCreditCurve(cds.getUnderlyingCds(), cds.getInitialQuote().getCoupon(),
        calibrationCds, yieldCurve, creditCurve);
    for (int i = 0; i < bucketedCs01.length; i++) {
      bucketedCs01[i] *= cds.getNotional();
    }
    return bucketedCs01;
  }

  /**
   * Restricted constructor.
   */
  private CdsPricer() {
  }
}
