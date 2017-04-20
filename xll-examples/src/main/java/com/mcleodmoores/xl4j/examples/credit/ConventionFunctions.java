/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.parsePeriod;

import org.threeten.bp.Period;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Methods that construct conventions for the ISDA model.
 */
public final class ConventionFunctions {

  /**
   * Constructs a yield curve convention. All fields are required.
   * @param xlMoneyMarketDayCountName  the money market day count name
   * @param xlSwapDayCountName  the swap day count name
   * @param xlSwapIntervalName  the swap payment interval name
   * @param xlCurveDayCountName  the curve day count name
   * @param xlBusinessDayConventionName  the business day convention name
   * @param xlSpotDays  the number of spot days
   * @return  a convention
   */
  @XLFunction(name = "ISDAYieldCurveConvention", category = "ISDA CDS model", description = "Create a yield curve convention")
  public static IsdaYieldCurveConvention buildYieldCurveConvention(
      @XLParameter(description = "Money Market Day Count", name = "Money Market Day Count") final XLString xlMoneyMarketDayCountName,
      @XLParameter(description = "Swap Day Count", name = "Swap Day Count") final XLString xlSwapDayCountName,
      @XLParameter(description = "Swap Interval", name = "Swap Interval") final XLString xlSwapIntervalName,
      @XLParameter(description = "Curve Day Count", name = "Curve Day Count") final XLString xlCurveDayCountName,
      @XLParameter(description = "Business Day Convention", name = "Business Day Convention") final XLString xlBusinessDayConventionName,
      @XLParameter(description = "Spot Days", name = "spotDays") final XLNumber xlSpotDays) {
    final DayCount moneyMarketDayCount = DayCountFactory.INSTANCE.instance(xlMoneyMarketDayCountName.getValue());
    final DayCount swapDayCount = DayCountFactory.INSTANCE.instance(xlSwapDayCountName.getValue());
    final DayCount curveDayCount = DayCountFactory.INSTANCE.instance(xlCurveDayCountName.getValue());
    final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.instance(xlBusinessDayConventionName.getValue());
    final Period swapInterval = parsePeriod(xlSwapIntervalName.getValue());
    return new IsdaYieldCurveConvention(moneyMarketDayCount, swapDayCount, swapInterval, curveDayCount, businessDayConvention, xlSpotDays.getAsInt());
  }

  /**
   * Constructs a CDS convention.
   * @param xlAccrualDayCountName  the accrual day count name
   * @param xlCurveDayCountName  the curve day count name
   * @param xlBusinessDayConventionName  the business day convention name
   * @param xlCouponInterval  the coupon interval name, is optional. If not supplied, 3 months is used
   * @param xlStubType  the stub type name, is optional: FRONTSHORT; FRONTLONG; BACKSHORT; or BACKLONG. If not supplied, FRONTSHORT is used
   * @param xlCashSettlementDays  the number of cash settlement days, is optional. If not supplied, 3 is used
   * @param xlStepInDays  the number of step-in days, is optional. If not supplied, 1 is used
   * @param xlPayAccrualOnDefault  true if the accrued is paid on default, is optional. If not supplied, true is used
   * @return  the convention
   */
  @XLFunction(name = "ISDACDSConvention", category = "ISDA CDS model", description = "Create a CDS convention")
  public static IsdaCdsConvention buildCdsConvention(
      @XLParameter(description = "Accrual Day Count", name = "Accrual Day Count") final XLString xlAccrualDayCountName,
      @XLParameter(description = "Curve Day Count", name = "Curve Day Count") final XLString xlCurveDayCountName,
      @XLParameter(description = "Business Day Convention", name = "Business Day Convention") final XLString xlBusinessDayConventionName,
      @XLParameter(description = "Coupon Interval", name = "Coupon Interval", optional = true) final XLString xlCouponInterval,
      @XLParameter(description = "Stub Type", name = "Stub Type", optional = true) final XLString xlStubType,
      @XLParameter(description = "Cash Settlement Days", name = "Cash Settlement Days", optional = true) final XLNumber xlCashSettlementDays,
      @XLParameter(description = "Step In Days", name = "Step In Days", optional = true) final XLNumber xlStepInDays,
      @XLParameter(description = "Pay Accrual On Default", name = "Pay Accrual On Default", optional = true) final XLBoolean xlPayAccrualOnDefault) {
    final String stubType = xlStubType == null ? null : xlStubType.getValue();
    final Integer cashSettlementDays = xlCashSettlementDays == null ? null : xlCashSettlementDays.getAsInt();
    final Integer stepInDays = xlStepInDays == null ? null : xlStepInDays.getAsInt();
    final Boolean payAccrualOnDefault = xlPayAccrualOnDefault == null ? null : xlPayAccrualOnDefault.getValue();
    return new IsdaCdsConvention(xlAccrualDayCountName.getValue(), xlCurveDayCountName.getValue(), xlBusinessDayConventionName.getValue(),
        xlCouponInterval.getValue(), stubType, cashSettlementDays, stepInDays, payAccrualOnDefault);
  }

  private ConventionFunctions() {
  }
}
