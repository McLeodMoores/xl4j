/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.createHolidayCalendar;
import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.parsePeriod;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.XLArgument;
import com.mcleodmoores.xl4j.XLFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Methods that create CDS trades and produce price and risk values.
 */
public final class CdsPricer {
  /** The calculator */
  private static final AnalyticCDSPricer CALCULATOR = new AnalyticCDSPricer();

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
    return notional * CALCULATOR.pv(cds, yieldCurve, creditCurve, coupon, PriceType.CLEAN);
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
    return notional * CALCULATOR.pv(cds, yieldCurve, creditCurve, coupon, PriceType.DIRTY);
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
    return CALCULATOR.parSpread(cds, yieldCurve, creditCurve);
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
    return notional * CALCULATOR.protectionLeg(cds, yieldCurve, creditCurve);
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
    return notional * CALCULATOR.annuity(cds, yieldCurve, creditCurve, PriceType.CLEAN) * coupon;
  }

  public static double rr01() {
    return 0;
  }

  public static double ir01() {
    return 0;
  }

  public static double cs01() {
    return 0;
  }

  public static double[] bucketedIr01() {
    return null;
  }

  public static double[] bucketedCs01() {
    return null;
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
