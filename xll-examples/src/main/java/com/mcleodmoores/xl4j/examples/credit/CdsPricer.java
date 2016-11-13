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
import com.opengamma.util.ArgumentChecker;

/**
 * Methods that create CDS trades and produce price and risk values.
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
   * Constructs a CDS.
   * @param tradeDate  the trade date
   * @param tenor  the tenor of the CDS
   * @param recoveryRate the recovery rates as decimal
   * @param convention  the convention to be used
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  a credit curve constructed using the ISDA model
   */
  @XLFunction(name = "CDS.BuildCDSFromConvention", category = "ISDA CDS model",
      description = "Build a CDS")
  public static CDSAnalytic createImmCds(
      @XLArgument(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLArgument(description = "Tenor", name = "Tenor") final String tenor,
      @XLArgument(description = "Recovery Rate", name = "Recovery Rate") final double recoveryRate,
      @XLArgument(description = "Convention", name = "Convention") final IsdaCdsConvention convention,
      @XLArgument(optional = true, description = "Holidays", name = "Holidays") final LocalDate[] holidayDates) {
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
    cdsFactory = cdsFactory.withRecoveryRate(recoveryRate);
    return cdsFactory.makeIMMCDS(tradeDate, parsePeriod(tenor));
  }

  /**
   * Constructs a CDS.
   * @param tradeDate  the trade date
   * @param tenor  the tenor of the CDS
   * @param recoveryRate the recovery rates as decimal
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
  @XLFunction(name = "CDS.BuildCDS", category = "ISDA CDS model",
      description = "Build a CDS")
  public static CDSAnalytic createImmCds(
      @XLArgument(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLArgument(description = "Tenor", name = "Tenor") final String tenor,
      @XLArgument(description = "Recovery Rate", name = "Recovery Rate") final double recoveryRate,
      @XLArgument(description = "Accrual Day Count", name = "Accrual Day Count") final String accrualDayCountName,
      @XLArgument(description = "Curve Day Count", name = "Curve Day Count") final String curveDayCountName,
      @XLArgument(description = "Business Day Convention", name = "Business Day Convention") final String businessDayConventionName,
      @XLArgument(optional = true, description = "Coupon Interval", name = "Coupon Interval") final String couponIntervalName,
      @XLArgument(optional = true, description = "Stub Type", name = "Stub Type") final String stubTypeName,
      @XLArgument(optional = true, description = "Cash Settlement Days", name = "Cash Settlement Days") final Integer cashSettlementDays,
      @XLArgument(optional = true, description = "Step In Days", name = "Step In Days") final Integer stepInDays,
      @XLArgument(optional = true, description = "Pay Accrual On Default", name = "Pay Accrual On Default") final Boolean payAccrualOnDefault,
      @XLArgument(optional = true, description = "Holidays", name = "holidays") final LocalDate[] holidayDates) {
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
    cdsFactory = cdsFactory.withRecoveryRate(recoveryRate);
    return cdsFactory.makeIMMCDS(tradeDate, parsePeriod(tenor));
  }

  /**
   * Calculate the clean price for the protection buyer given a yield and credit curve.
   * @param notional  the CDS notional
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @param coupon  the CDS coupon as a decimal (e.g. 0.01 for a 100bps coupon)
   * @return  the clean price
   */
  @XLFunction(name = "CDS.CleanPrice", category = "ISDA CDS model",
      description = "Calculate the clean price of a CDS for the protection buyer")
  public static double cleanPrice(
      @XLArgument(description = "Notional, positive for the protection buyer", name = "Notional") final double notional,
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Coupon", name = "Coupon") final double coupon) {
    return notional * PRICE_CALCULATOR.pv(cds, yieldCurve, creditCurve, coupon, PriceType.CLEAN);
  }

  /**
   * Calculate the dirty price for the protection buyer given a yield and credit curve.
   * @param notional  the CDS notional
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @param coupon  the CDS coupon as a decimal (e.g. 0.01 for a 100bps coupon)
   * @return  the dirty price
   */
  @XLFunction(name = "CDS.DirtyPrice", category = "ISDA CDS model",
      description = "Calculate the dirty price of a CDS for the protection buyer")
  public static double dirtyPrice(
      @XLArgument(description = "Notional, positive for the protection buyer", name = "Notional") final double notional,
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Coupon", name = "Coupon") final double coupon) {
    return notional * PRICE_CALCULATOR.pv(cds, yieldCurve, creditCurve, coupon, PriceType.DIRTY);
  }

  /**
   * Calculate the accrued interest for the protection buyer given a yield and credit curve.
   * @param notional  the CDS notional
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @param coupon  the CDS coupon as a decimal (e.g. 0.01 for a 100bps coupon)
   * @return  the accrued interest
   */
  @XLFunction(name = "CDS.Accrued", category = "ISDA CDS model",
      description = "Calculate the accrued of a CDS for the protection buyer")
  public static double accruedInterest(
      @XLArgument(description = "Notional, positive for the protection buyer", name = "Notional") final double notional,
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Coupon", name = "Coupon") final double coupon) {
    return dirtyPrice(notional, cds, yieldCurve, creditCurve, coupon) - cleanPrice(notional, cds, yieldCurve, creditCurve, coupon);
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
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    return PRICE_CALCULATOR.parSpread(cds, yieldCurve, creditCurve);
  }

  /**
   * Calculates the price for the protection buyer of the protection leg given a yield and credit curve.
   * @param notional  the notional
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @return  the protection leg price
   */
  @XLFunction(name = "CDS.ProtectionLegPrice", category = "ISDA CDS model",
      description = "The present value of the protection leg of a CDS for the protection buyer")
  public static double protectionLegPv(
      @XLArgument(description = "Notional, positive for the protection buyer", name = "Notional") final double notional,
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    return notional * PRICE_CALCULATOR.protectionLeg(cds, yieldCurve, creditCurve);
  }

  /**
   * Calculates the clean price of the premium leg for the protection buyer.
   * @param notional  the notional
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @param coupon  the coupon
   * @return  the premium leg clean price
   */
  @XLFunction(name = "CDS.PremiumLegCleanPrice", category = "ISDA CDS model",
      description = "The clean price of the premium leg of a CDS for the protection buyer")
  public static double premiumLegPv(
      @XLArgument(description = "Notional, positive for the protection buyer", name = "Notional") final double notional,
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Coupon", name = "Coupon") final double coupon) {
    return -notional * PRICE_CALCULATOR.annuity(cds, yieldCurve, creditCurve, PriceType.CLEAN) * coupon;
  }

  /**
   * Calculates the change in value of a CDS when the recovery rate is changed by 1 basis point.
   * @param notional  the notional
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @return  the RR01
   */
  @XLFunction(name = "CDS.RR01", category = "ISDA CDS model",
      description = "The sensitivity of the price to a 1 basis point change in the recovery rate")
  public static double rr01(
      @XLArgument(description = "Notional, positive for the protection buyer", name = "Notional") final double notional,
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve) {
    return notional * RISK_CALCULATOR.recoveryRateSensitivity(cds, yieldCurve, creditCurve) * BPS;
  }

  /**
   * Calculates the value on default of a CDS.
   * @param notional  the notional
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @param coupon  the coupon
   * @return  the value on default
   */
  @XLFunction(name = "CDS.ValueOnDefault", category = "ISDA CDS model",
      description = "The value on default of a CDS")
  public static double valueOnDefault(
      @XLArgument(description = "Notional, positive for the protection buyer", name = "Notional") final double notional,
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Coupon", name = "Coupon") final double coupon) {
    return notional * RISK_CALCULATOR.valueOnDefault(cds, yieldCurve, creditCurve, coupon);
  }

  /**
   * Calculates the change in value of a CDS when the yield curve is parallel-shifted by 1 basis point.
   * @param notional  the notional
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @param coupon  the CDS coupon
   * @return  the IR01
   */
  @XLFunction(name = "CDS.IR01", category = "ISDA CDS model",
      description = "The sensitivity of the price to a 1 basis point change in the yield curve")
  public static double ir01(
      @XLArgument(description = "Notional, positive for the protection buyer", name = "Notional") final double notional,
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Coupon", name = "Coupon") final double coupon) {
    return notional * IR_CALCULATOR.parallelIR01(cds, coupon, creditCurve, yieldCurve);
  }

  /**
   * Calculates the change in value of a CDS when the credit spreads are parallel-shifted by 1 basis point.
   * @param notional  the notional
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param coupon  the CDS coupon
   * @param quoteType  the market quote type
   * @param marketQuote  the market quote
   * @return  the CS01
   */
  @XLFunction(name = "CDS.CS01", category = "ISDA CDS model",
      description = "The sensitivity of the price to a 1 basis point change in the market spread")
  public static double cs01(
      @XLArgument(description = "Notional, positive for the protection buyer", name = "Notional") final double notional,
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Coupon", name = "Coupon") final double coupon,
      @XLArgument(description = "Quote Type", name = "Quote Type") final String quoteType,
      @XLArgument(description = "Market Quote", name = "Market Quote") final double marketQuote) {
    final CDSQuoteConvention quote = createQuote(coupon, marketQuote, quoteType);
    return notional * SPREAD_CALCULATOR.parallelCS01(cds, quote, yieldCurve);
  }

  /**
   * Calculates the change in value of a CDS at each yield curve node when the yield is shifted by 1 basis point.
   * @param notional  the notional
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param creditCurve  the credit curve
   * @param coupon  the CDS coupon
   * @return  the bucketed IR01
   */
  @XLFunction(name = "CDS.BucketedIR01", category = "ISDA CDS model",
      description = "The sensitivity of the price to a 1 basis point change in the yield curve")
  public static double[] bucketedIr01(
      @XLArgument(description = "Notional, positive for the protection buyer", name = "Notional") final double notional,
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Credit Curve", name = "Credit Curve") final ISDACompliantCreditCurve creditCurve,
      @XLArgument(description = "Coupon", name = "Coupon") final double coupon) {
    final double[] bucketedIr01 = IR_CALCULATOR.bucketedIR01(cds, coupon, creditCurve, yieldCurve);
    final double[] result = new double[bucketedIr01.length];
    for (int i = 0; i < bucketedIr01.length; i++) {
      result[i] = notional * bucketedIr01[i];
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
   * @param notional  the notional
   * @param cds  the CDS
   * @param coupon  the CDS coupon
   * @param quoteType  the market quote type
   * @param marketQuote  the market quote
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  the bucketed CS01
   */
  @XLFunction(name = "CDS.BucketedCS01", category = "ISDA CDS model",
      description = "The sensitivity of the price to a 1 basis point change in the market spread")
  public static double[] bucketedCs01(
      @XLArgument(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLArgument(description = "Tenors", name = "Tenors") final String[] tenors,
      @XLArgument(description = "Quote Type", name = "Quote Types") final String[] quoteTypes,
      @XLArgument(description = "Market Quotes", name = "Market Quotes") final double[] quotes,
      @XLArgument(description = "Recovery Rates", name = "Recovery Rates") final double[] recoveryRates,
      @XLArgument(description = "Coupons", name = "Coupons") final double[] coupons,
      @XLArgument(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLArgument(description = "Convention", name = "Convention") final IsdaCdsConvention convention,
      @XLArgument(description = "Notional, positive for the protection buyer", name = "Notional") final double notional,
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Coupon", name = "Coupon") final double coupon,
      @XLArgument(description = "Quote Type", name = "Quote Type") final String quoteType,
      @XLArgument(description = "Market Quote", name = "Market Quote") final double marketQuote,
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
    final ISDACompliantCreditCurve creditCurve = builder.calibrateCreditCurve(calibrationCds, marketQuotes, yieldCurve);
    final double[] bucketedCs01 = SPREAD_CALCULATOR.bucketedCS01FromCreditCurve(cds, coupon, calibrationCds, yieldCurve, creditCurve);
    for (int i = 0; i < bucketedCs01.length; i++) {
      bucketedCs01[i] *= notional;
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
   * @param notional  the notional
   * @param cds  the CDS
   * @param coupon  the CDS coupon
   * @param quoteType  the market quote type
   * @param marketQuote  the market quote
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  the bucketed CS01
   */
  @XLFunction(name = "ISDACreditCurve.BuildIMMCurve", category = "ISDA CDS model",
      description = "Build a hazard rate curve for IMM CDS using the ISDA methodology")
  public static double[] bucketedCs01(
      @XLArgument(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLArgument(description = "Tenors", name = "Tenors") final String[] tenors,
      @XLArgument(description = "Quote Type", name = "Quote Types") final String[] quoteTypes,
      @XLArgument(description = "Market Quotes", name = "Market Quotes") final double[] quotes,
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
      @XLArgument(description = "Notional, positive for the protection buyer", name = "Notional") final double notional,
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "Coupon", name = "Coupon") final double coupon,
      @XLArgument(description = "Quote Type", name = "Quote Type") final String quoteType,
      @XLArgument(description = "Market Quote", name = "Market Quote") final double marketQuote,
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
    final ISDACompliantCreditCurve creditCurve = builder.calibrateCreditCurve(calibrationCds, marketQuotes, yieldCurve);
    final double[] bucketedCs01 = SPREAD_CALCULATOR.bucketedCS01FromCreditCurve(cds, coupon, calibrationCds, yieldCurve, creditCurve);
    for (int i = 0; i < bucketedCs01.length; i++) {
      bucketedCs01[i] *= notional;
    }
    return bucketedCs01;
  }

  public static Object[][] protectionLegDetails() {
    // payment dates, times and amounts
    return null;
  }

  public static Object[][] premiumLegDetails() {
    // payment dates, times and amounts
    return null;
  }

  /**
   * Restricted constructor.
   */
  private CdsPricer() {
  }
}
