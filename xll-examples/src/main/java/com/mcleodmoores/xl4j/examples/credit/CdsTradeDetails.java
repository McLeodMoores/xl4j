/**
 *
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.createHolidayCalendar;
import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.parsePeriod;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.XLArgument;
import com.mcleodmoores.xl4j.XLFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSCoupon;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Methods that create CDS trades and expose information about those trades.
 */
public class CdsTradeDetails {

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

  @XLFunction(name = "CDS.AccrualStartTimes", category = "CDS Trade")
  public Object[] accrualStartTimes(
      @XLArgument(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLArgument(description = "notional", name = "notional") final double notional) {
    final CDSCoupon[] coupons = cds.getCoupons();
    final Object[] result = new Object[coupons.length];
    for (int i = 0; i < coupons.length; i++) {
      result[i] = coupons[i].getEffStart();
    }
    return result;
  }
}
