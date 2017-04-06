/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.createHolidayCalendar;
import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.parsePeriod;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIMMDate;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getPrevIMMDate;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.XLParameter;
import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSCoupon;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDAPremiumLegSchedule;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

/**
 * Methods that create CDS trades and expose information about those trades.
 */
public final class CdsTradeDetails {

  /**
   * Constructs an IMM  CDS trade.
   * @param tradeDate  the trade date
   * @param currency  the currency
   * @param notional  the notional
   * @param buyProtection  true if protection is bought
   * @param tenor  the tenor of the CDS
   * @param coupon  the CDS coupon
   * @param recoveryRate the recovery rates as decimal
   * @param initialMarketQuote  the initial market quote as decimal
   * @param initialQuoteType  the initial market quote type
   * @param convention  the convention to be used
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  a credit curve constructed using the ISDA model
   */
  @XLFunction(name = "CDS.BuildCDSFromConvention", category = "ISDA CDS model",
      description = "Build a CDS")
  public static CdsTrade createImmCds(
      @XLParameter(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLParameter(description = "Currency", name = "Currency") final String currency,
      @XLParameter(description = "Notional", name = "Notional") final double notional,
      @XLParameter(description = "Buy protection", name = "Buy Protection") final boolean buyProtection,
      @XLParameter(description = "Tenor", name = "Tenor") final String tenor,
      @XLParameter(description = "Coupon", name = "Coupon") final double coupon,
      @XLParameter(description = "Recovery Rate", name = "Recovery Rate") final double recoveryRate,
      @XLParameter(description = "Initial Market Quote", name = "Initial Market Quote") final double initialMarketQuote,
      @XLParameter(description = "Initial Quote Type", name = "Initial Quote Type") final String initialQuoteType,
      @XLParameter(description = "Convention", name = "Convention") final IsdaCdsConvention convention,
      @XLParameter(optional = true, description = "Holidays", name = "Holidays") final LocalDate[] holidayDates) {
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
    return CdsTrade.of(cdsFactory.makeIMMCDS(tradeDate, parsePeriod(tenor)), Currency.of(currency), notional, coupon, buyProtection,
        initialMarketQuote, initialQuoteType);
  }

  /**
   * Constructs a CDS.
   * @param tradeDate  the trade date
   * @param currency  the currency
   * @param notional  the notional
   * @param buyProtection  true if protection is bought
   * @param tenor  the tenor of the CDS
   * @param coupon  the CDS coupon
   * @param recoveryRate the recovery rates as decimal
   * @param initialMarketQuote  the initial market quote as decimal
   * @param initialQuoteType  the initial market quote type
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
  public static CdsTrade createImmCds(
      @XLParameter(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLParameter(description = "Currency", name = "Currency") final String currency,
      @XLParameter(description = "Notional", name = "Notional") final double notional,
      @XLParameter(description = "Buy protection", name = "Buy Protection") final boolean buyProtection,
      @XLParameter(description = "Tenor", name = "Tenor") final String tenor,
      @XLParameter(description = "Coupon", name = "Coupon") final double coupon,
      @XLParameter(description = "Recovery Rate", name = "Recovery Rate") final double recoveryRate,
      @XLParameter(description = "Initial Market Quote", name = "Initial Market Quote") final double initialMarketQuote,
      @XLParameter(description = "Initial Quote Type", name = "Initial Quote Type") final String initialQuoteType,
      @XLParameter(description = "Accrual Day Count", name = "Accrual Day Count") final String accrualDayCountName,
      @XLParameter(description = "Curve Day Count", name = "Curve Day Count") final String curveDayCountName,
      @XLParameter(description = "Business Day Convention", name = "Business Day Convention") final String businessDayConventionName,
      @XLParameter(optional = true, description = "Coupon Interval", name = "Coupon Interval") final String couponIntervalName,
      @XLParameter(optional = true, description = "Stub Type", name = "Stub Type") final String stubTypeName,
      @XLParameter(optional = true, description = "Cash Settlement Days", name = "Cash Settlement Days") final Integer cashSettlementDays,
      @XLParameter(optional = true, description = "Step In Days", name = "Step In Days") final Integer stepInDays,
      @XLParameter(optional = true, description = "Pay Accrual On Default", name = "Pay Accrual On Default") final Boolean payAccrualOnDefault,
      @XLParameter(optional = true, description = "Holidays", name = "holidays") final LocalDate[] holidayDates) {
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
    return CdsTrade.of(cdsFactory.makeIMMCDS(tradeDate, parsePeriod(tenor)), Currency.of(currency), notional, coupon, buyProtection,
        initialMarketQuote, initialQuoteType);
  }

  /**
   * Gets all accrual start dates for the lifetime of the CDS.
   * @param tradeDate  the trade date
   * @param tenor  the tenor of the CDS
   * @param businessDayConventionName  the business day convention name
   * @param couponIntervalName  the coupon interval name, is optional. If not supplied, 3 months is used
   * @param stubTypeName  the stub type name, is optional: FRONTSHORT; FRONTLONG; BACKSHORT; or BACKLONG. If not supplied, FRONTSHORT is used
   * @param cashSettlementDays  the number of cash settlement days, is optional. If not supplied, 3 is used
   * @param stepInDays  the number of step-in days, is optional. If not supplied, 1 is used
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  the accrual start dates
   */
  @XLFunction(name = "CDS.AccrualStartDates", category = "CDS Trade")
  public static Object[] accrualStartDates(
      @XLParameter(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLParameter(description = "Tenor", name = "Tenor") final String tenor,
      @XLParameter(description = "Business Day Convention", name = "Business Day Convention") final String businessDayConventionName,
      @XLParameter(optional = true, description = "Coupon Interval", name = "Coupon Interval") final String couponIntervalName,
      @XLParameter(optional = true, description = "Stub Type", name = "Stub Type") final String stubTypeName,
      @XLParameter(optional = true, description = "Cash Settlement Days", name = "Cash Settlement Days") final Integer cashSettlementDays,
      @XLParameter(optional = true, description = "Step In Days", name = "Step In Days") final Integer stepInDays,
      @XLParameter(optional = true, description = "Holidays", name = "holidays") final LocalDate[] holidayDates) {
    final ISDAPremiumLegSchedule paymentSchedule = getPremiumSchedule(tradeDate, tenor, businessDayConventionName, couponIntervalName,
        stubTypeName, holidayDates);
    final int n = paymentSchedule.getNumPayments();
    final Object[] result = new Object[n];
    for (int i = 0; i < n; i++) {
      result[i] = paymentSchedule.getAccStartDate(i);
    }
    return result;
  }

  /**
   * Gets all accrual end dates for the lifetime of the CDS.
   * @param tradeDate  the trade date
   * @param tenor  the tenor of the CDS
   * @param businessDayConventionName  the business day convention name
   * @param couponIntervalName  the coupon interval name, is optional. If not supplied, 3 months is used
   * @param stubTypeName  the stub type name, is optional: FRONTSHORT; FRONTLONG; BACKSHORT; or BACKLONG. If not supplied, FRONTSHORT is used
   * @param cashSettlementDays  the number of cash settlement days, is optional. If not supplied, 3 is used
   * @param stepInDays  the number of step-in days, is optional. If not supplied, 1 is used
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  the accrual end dates
   */
  @XLFunction(name = "CDS.AccrualEndDates", category = "CDS Trade")
  public static Object[] accrualEndDates(
      @XLParameter(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLParameter(description = "Tenor", name = "Tenor") final String tenor,
      @XLParameter(description = "Business Day Convention", name = "Business Day Convention") final String businessDayConventionName,
      @XLParameter(optional = true, description = "Coupon Interval", name = "Coupon Interval") final String couponIntervalName,
      @XLParameter(optional = true, description = "Stub Type", name = "Stub Type") final String stubTypeName,
      @XLParameter(optional = true, description = "Cash Settlement Days", name = "Cash Settlement Days") final Integer cashSettlementDays,
      @XLParameter(optional = true, description = "Step In Days", name = "Step In Days") final Integer stepInDays,
      @XLParameter(optional = true, description = "Holidays", name = "holidays") final LocalDate[] holidayDates) {
    final ISDAPremiumLegSchedule paymentSchedule = getPremiumSchedule(tradeDate, tenor, businessDayConventionName, couponIntervalName,
        stubTypeName, holidayDates);
    final int n = paymentSchedule.getNumPayments();
    final Object[] result = new Object[n];
    for (int i = 0; i < n; i++) {
      result[i] = paymentSchedule.getAccEndDate(i);
    }
    return result;
  }

  /**
   * Gets all payment dates for the lifetime of the CDS.
   * @param tradeDate  the trade date
   * @param tenor  the tenor of the CDS
   * @param businessDayConventionName  the business day convention name
   * @param couponIntervalName  the coupon interval name, is optional. If not supplied, 3 months is used
   * @param stubTypeName  the stub type name, is optional: FRONTSHORT; FRONTLONG; BACKSHORT; or BACKLONG. If not supplied, FRONTSHORT is used
   * @param cashSettlementDays  the number of cash settlement days, is optional. If not supplied, 3 is used
   * @param stepInDays  the number of step-in days, is optional. If not supplied, 1 is used
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  the payments dates
   */
  @XLFunction(name = "CDS.PaymentDates", category = "CDS Trade")
  public static Object[] paymentDates(
      @XLParameter(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLParameter(description = "Tenor", name = "Tenor") final String tenor,
      @XLParameter(description = "Business Day Convention", name = "Business Day Convention") final String businessDayConventionName,
      @XLParameter(optional = true, description = "Coupon Interval", name = "Coupon Interval") final String couponIntervalName,
      @XLParameter(optional = true, description = "Stub Type", name = "Stub Type") final String stubTypeName,
      @XLParameter(optional = true, description = "Cash Settlement Days", name = "Cash Settlement Days") final Integer cashSettlementDays,
      @XLParameter(optional = true, description = "Step In Days", name = "Step In Days") final Integer stepInDays,
      @XLParameter(optional = true, description = "Holidays", name = "holidays") final LocalDate[] holidayDates) {
    final ISDAPremiumLegSchedule paymentSchedule = getPremiumSchedule(tradeDate, tenor, businessDayConventionName, couponIntervalName,
        stubTypeName, holidayDates);
    final int n = paymentSchedule.getNumPayments();
    final Object[] result = new Object[n];
    for (int i = 0; i < n; i++) {
      result[i] = paymentSchedule.getPaymentDate(i);
    }
    return result;
  }

  /**
   * Creates the premium leg schedule for a CDS.
   * @param tradeDate  the trade date
   * @param tenor  the tenor
   * @param businessDayConventionName  the business day convention
   * @param couponIntervalName  the coupon interval
   * @param stubTypeName  the stub type
   * @param holidayDates  the holiday dates
   * @return  the schedule
   */
  @SuppressWarnings("deprecation")
  private static ISDAPremiumLegSchedule getPremiumSchedule(final LocalDate tradeDate, final String tenor, final String businessDayConventionName,
      final String couponIntervalName, final String stubTypeName, final LocalDate[] holidayDates) {
    final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.instance(businessDayConventionName);
    final Calendar calendar = holidayDates == null ? new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY) : createHolidayCalendar(holidayDates);
    final Period paymentInterval = couponIntervalName == null ? Period.ofMonths(3) : parsePeriod(couponIntervalName);
    final StubType stubType = stubTypeName == null ? StubType.FRONTSHORT : StubType.valueOf(stubTypeName);
    final LocalDate effectiveDate = businessDayConvention.adjustDate(calendar, getPrevIMMDate(tradeDate));
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final LocalDate maturity = nextIMM.plus(parsePeriod(tenor));
    final ISDAPremiumLegSchedule paymentSchedule = new ISDAPremiumLegSchedule(effectiveDate, maturity, paymentInterval, stubType,
        businessDayConvention, calendar, true);
    return paymentSchedule;
  }

  /**
   * Gets the accrual start times for the remaining life of the CDS.
   * @param cds  the CDS
   * @return  the accrual start times
   */
  @XLFunction(name = "CDS.AccrualStartTimes", category = "CDS Trade")
  public static Object[] accrualStartTimes(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds) {
    final CDSCoupon[] coupons = cds.getUnderlyingCds().getCoupons();
    final Object[] result = new Object[coupons.length];
    for (int i = 0; i < coupons.length; i++) {
      result[i] = coupons[i].getEffStart();
    }
    return result;
  }

  /**
   * Gets the accrual end times for the remaining life of the CDS.
   * @param cds  the CDS
   * @return  the accrual end times
   */
  @XLFunction(name = "CDS.AccrualEndTimes", category = "CDS Trade")
  public static Object[] accrualEndTimes(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds) {
    final CDSCoupon[] coupons = cds.getUnderlyingCds().getCoupons();
    final Object[] result = new Object[coupons.length];
    for (int i = 0; i < coupons.length; i++) {
      result[i] = coupons[i].getEffEnd();
    }
    return result;
  }

  /**
   * Gets the payment times for the remaining life of the CDS.
   * @param cds  the CDS
   * @return  the payment times
   */
  @XLFunction(name = "CDS.PaymentTimes", category = "CDS Trade")
  public static Object[] paymentTimes(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds) {
    final CDSCoupon[] coupons = cds.getUnderlyingCds().getCoupons();
    final Object[] result = new Object[coupons.length];
    for (int i = 0; i < coupons.length; i++) {
      result[i] = coupons[i].getPaymentTime();
    }
    return result;
  }

  /**
   * Gets the annuity payments for the remaining life of the CDS.
   * @param cds  the CDS
   * @return  the annuity payments
   */
  @XLFunction(name = "CDS.AnnuityPayments", category = "CDS Trade")
  public static Object[] annuityPayments(
      @XLParameter(description = "CDS", name = "CDS") final CdsTrade cds) {
    final CDSCoupon[] coupons = cds.getUnderlyingCds().getCoupons();
    final Object[] result = new Object[coupons.length];
    for (int i = 0; i < coupons.length; i++) {
      result[i] = coupons[i].getYearFrac() * cds.getNotional() * cds.getCoupon();
    }
    return result;
  }

  /**
   * Restricted constructor.
   */
  private CdsTradeDetails() {
  }
}
