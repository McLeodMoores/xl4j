/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.createHolidayCalendar;
import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.parsePeriod;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurveBuild;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDAInstrumentTypes;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Methods that construct an ISDA CDS model yield curve and extract information from the curve once constructed.
 */
public final class IsdaYieldCurveBuilder {

  /**
   * Constructs a yield curve using the ISDA CDS model. If the spot date field
   * is supplied, this spot date is used in preference to that calculated from
   * the business day convention and trade date.
   * @param tradeDate  the trade date
   * @param instrumentTypeNames  the names of the instrument types: M, MM or MONEY MARKET; S or SWAP, in any case
   * @param tenors  the instrument tenors, in the form "P3M" or "3M". Must have one per instrument type
   * @param quotes  the market data quotes. Must have one per instrument type
   * @param convention  the convention for the curve.
   * @param spotDate  the spot date, is optional. If not supplied, the trade date is used
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  a yield curve constructed using the ISDA model
   */
  @SuppressWarnings("deprecation")
  @XLFunction(name = "ISDAYieldCurve.BuildCurveFromConvention", category = "ISDA CDS model",
  description = "Build a yield curve using the ISDA methodology")
  public static ISDACompliantYieldCurve buildYieldCurve(
      @XLParameter(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLParameter(description = "Instrument Types", name = "Instrument Types") final String[] instrumentTypeNames,
      @XLParameter(description = "Tenors", name = "Tenors") final String[] tenors,
      @XLParameter(description = "Quotes", name = "Quotes") final double[] quotes,
      @XLParameter(description = "Convention", name = "Convention") final IsdaYieldCurveConvention convention,
      @XLParameter(description = "Spot Date", name = "Spot Date", optional = true) final LocalDate spotDate,
      @XLParameter(description = "Holidays", name = "Holidays", optional = true) final LocalDate[] holidayDates) {
    final int n = instrumentTypeNames.length;
    ArgumentChecker.isTrue(n == tenors.length, "Must have one tenor per instrument, have {} tenors and {} instrument types", tenors.length, n);
    ArgumentChecker.isTrue(n == quotes.length, "Must have one quote per instrument, have {} quotes and {} instrument types", quotes.length, n);
    final ISDAInstrumentTypes[] instrumentTypes = new ISDAInstrumentTypes[n];
    final Period[] periods = new Period[n];
    for (int i = 0; i < instrumentTypes.length; i++) {
      switch (instrumentTypeNames[i].toUpperCase()) {
        case "S":
        case "SWAP":
          instrumentTypes[i] = ISDAInstrumentTypes.Swap;
          break;
        case "M":
        case "MM":
        case "MONEY MARKET":
          instrumentTypes[i] = ISDAInstrumentTypes.MoneyMarket;
          break;
        default:
          throw new IllegalArgumentException("Unsupported instrument type " + instrumentTypeNames[i]);
      }
      periods[i] = parsePeriod(tenors[i]);
    }
    final Calendar calendar = createHolidayCalendar(holidayDates);
    final ISDACompliantYieldCurveBuild builder;
    if (spotDate == null) {
      final LocalDate spotFromTrade = ScheduleCalculator.getAdjustedDate(tradeDate, convention.getSpotDays(), calendar);
      builder = new ISDACompliantYieldCurveBuild(tradeDate, spotFromTrade, instrumentTypes, periods,
          convention.getMoneyMarketDayCount(), convention.getSwapDayCount(), convention.getSwapInterval(),
          convention.getCurveDayCount(), convention.getBusinessDayConvention(), calendar);
    } else {
      builder = new ISDACompliantYieldCurveBuild(tradeDate, spotDate, instrumentTypes, periods,
          convention.getMoneyMarketDayCount(), convention.getSwapDayCount(), convention.getSwapInterval(),
          convention.getCurveDayCount(), convention.getBusinessDayConvention(), calendar);
    }
    return builder.build(quotes);
  }
  /**
   * Constructs a yield curve using the ISDA CDS model. If both the spot days and spot date field
   * are supplied, the spot date is used in preference. One of either the spot date or spot days
   * must be provided.
   * @param tradeDate  the trade date
   * @param instrumentTypeNames  the names of the instrument types: M, MM or MONEY MARKET; S or SWAP, in any case
   * @param tenors  the instrument tenors, in the form "P3M" or "3M". Must have one per instrument type
   * @param quotes  the market data quotes. Must have one per instrument type
   * @param moneyMarketDayCountName  the money market day count name
   * @param swapDayCountName  the swap day count name
   * @param swapIntervalName  the swap interval name, in the for "P1Y" or "1Y"
   * @param curveDayCountName  the curve day count name
   * @param businessDayConventionName  the business day convention name
   * @param spotDays  the number of spot days, is optional
   * @param spotDate  the spot date, is optional. If not supplied, the trade date is used
   * @param holidayDates  the holiday dates, is optional. If not supplied, weekend-only holidays are used
   * @return  a yield curve constructed using the ISDA model
   */
  @SuppressWarnings("deprecation")
  @XLFunction(name = "ISDAYieldCurve.BuildCurve", category = "ISDA CDS model",
  description = "Build a yield curve using the ISDA methodology")
  public static ISDACompliantYieldCurve buildYieldCurve(
      @XLParameter(description = "Trade Date", name = "Trade Date") final LocalDate tradeDate,
      @XLParameter(description = "Instrument Types", name = "Instrument Types") final String[] instrumentTypeNames,
      @XLParameter(description = "Tenors", name = "Tenors") final String[] tenors,
      @XLParameter(description = "Quotes", name = "Quotes") final double[] quotes,
      @XLParameter(description = "Money Market Day Count", name = "Money Market Day Count") final String moneyMarketDayCountName,
      @XLParameter(description = "Swap Day Count", name = "Swap Day Count") final String swapDayCountName,
      @XLParameter(description = "Swap Interval", name = "Swap Interval") final String swapIntervalName,
      @XLParameter(description = "Curve Day Count", name = "Curve Day Count") final String curveDayCountName,
      @XLParameter(description = "Business Day Convention", name = "Business Day Convention") final String businessDayConventionName,
      @XLParameter(description = "Spot Date", name = "spotDate", optional = true) final LocalDate spotDate,
      @XLParameter(description = "Spot Days", name = "spotDays", optional = true) final Integer spotDays,
      @XLParameter(description = "Holidays", name = "holidays", optional = true) final LocalDate[] holidayDates) {
    final int n = instrumentTypeNames.length;
    ArgumentChecker.isTrue(n == tenors.length, "Must have one tenor per instrument, have {} tenors and {} instrument types", tenors.length, n);
    ArgumentChecker.isTrue(n == quotes.length, "Must have one quote per instrument, have {} quotes and {} instrument types", quotes.length, n);
    final ISDAInstrumentTypes[] instrumentTypes = new ISDAInstrumentTypes[n];
    final Period[] periods = new Period[n];
    for (int i = 0; i < instrumentTypes.length; i++) {
      switch (instrumentTypeNames[i].toUpperCase()) {
        case "S":
        case "SWAP":
          instrumentTypes[i] = ISDAInstrumentTypes.Swap;
          break;
        case "M":
        case "MM":
        case "MONEY MARKET":
          instrumentTypes[i] = ISDAInstrumentTypes.MoneyMarket;
          break;
        default:
          throw new IllegalArgumentException("Unsupported instrument type " + instrumentTypeNames[i]);
      }
      periods[i] = parsePeriod(tenors[i]);
    }
    final DayCount moneyMarketDayCount = DayCountFactory.INSTANCE.instance(moneyMarketDayCountName);
    final DayCount swapDayCount = DayCountFactory.INSTANCE.instance(swapDayCountName);
    final DayCount curveDayCount = DayCountFactory.INSTANCE.instance(curveDayCountName);
    final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.instance(businessDayConventionName);
    final Period swapInterval = parsePeriod(swapIntervalName);
    final Calendar calendar = createHolidayCalendar(holidayDates);
    final ISDACompliantYieldCurveBuild builder;
    if (spotDate == null) {
      if (spotDays == null) {
        throw new XL4JRuntimeException("Did not supply either spot date or the number of spot days");
      }
      final LocalDate spotFromTrade = ScheduleCalculator.getAdjustedDate(tradeDate, spotDays, calendar);
      builder = new ISDACompliantYieldCurveBuild(tradeDate, spotFromTrade, instrumentTypes, periods,
          moneyMarketDayCount, swapDayCount, swapInterval, curveDayCount, businessDayConvention, calendar);
    } else {
      builder = new ISDACompliantYieldCurveBuild(tradeDate, spotDate, instrumentTypes, periods,
          moneyMarketDayCount, swapDayCount, swapInterval, curveDayCount, businessDayConvention, calendar);
    }
    return builder.build(quotes);
  }

  /**
   * Extracts the node times and zero rates from an ISDA yield curve.
   * @param yieldCurve  the yield curve
   * @return  an array containing a column of times and a column of zero rates
   */
  @XLFunction(name = "ISDAYieldCurve.Expand", category = "ISDA CDS model",
      description = "Show the nodal times and zero rates of the yield curve")
  public static Object[][] expandCurve(
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve) {
    final Object[][] result = new Object[yieldCurve.getNumberOfKnots()][2];
    for (int i = 0; i < yieldCurve.getNumberOfKnots(); i++) {
      final double t = yieldCurve.getTimeAtIndex(i);
      result[i][0] = t;
      result[i][1] = yieldCurve.getZeroRate(t);
    }
    return result;
  }

  /**
   * Extracts the node times and discount factors from an ISDA yield curve.
   * @param yieldCurve  the yield curve
   * @return  an array containing a column of times and a column of discount factors
   */
  @XLFunction(name = "ISDAYieldCurve.ExpandDiscountFactors", category = "ISDA CDS model",
      description = "Show the nodal times and discount factors of the yield curve")
  public static Object[][] expandDiscountFactors(
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve) {
    final Object[][] result = new Object[yieldCurve.getNumberOfKnots()][2];
    for (int i = 0; i < yieldCurve.getNumberOfKnots(); i++) {
      final double t = yieldCurve.getTimeAtIndex(i);
      result[i][0] = t;
      result[i][1] = yieldCurve.getDiscountFactor(t);
    }
    return result;
  }

  /**
   * Extracts the times and zero rates for a list of dates from an ISDA yield curve.
   * @param yieldCurve  the yield curve
   * @param currentDate  the current date, should be the curve construction date
   * @param curveDayCountConventionName  the day count convention name, should be the same as the curve day count used when constructing the curve
   * @param dates  the dates
   * @return  an array containing a column of times and a column of zero rates
   */
  @XLFunction(name = "ISDAYieldCurve.ExpandForDates", category = "ISDA CDS model", description = "Get times and zero rates for dates")
  public static Object[][] expandCurve(
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Current Date", name = "Current Date") final LocalDate currentDate,
      @XLParameter(description = "Day Count Convention", name = "Day Count Convention") final String curveDayCountConventionName,
      @XLParameter(description = "Dates", name = "Dates") final LocalDate[] dates) {
    final Object[][] result = new Object[dates.length][2];
    final DayCount curveDayCount = DayCountFactory.INSTANCE.instance(curveDayCountConventionName);
    for (int i = 0; i < dates.length; i++) {
      final double t = curveDayCount.getDayCountFraction(currentDate, dates[i]);
      result[i][0] = t;
      result[i][1] = yieldCurve.getZeroRate(t);
    }
    return result;
  }

  /**
   * Extracts the times and discount factors for a list of dates from an ISDA yield curve.
   * @param yieldCurve  the yield curve
   * @param currentDate  the current date, should be the curve construction date
   * @param curveDayCountConventionName  the day count convention name, should be the same as the curve day count used when constructing the curve
   * @param dates  the dates
   * @return  an array containing a column of times and a column of discount factors
   */
  @XLFunction(name = "ISDAYieldCurve.ExpandDiscountFactorsForDates", category = "ISDA CDS model", description = "Get times and discount factors for dates")
  public static Object[][] expandDiscountFactors(
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Current Date", name = "Current Date") final LocalDate currentDate,
      @XLParameter(description = "Day Count Convention", name = "Day Count Convention") final String curveDayCountConventionName,
      @XLParameter(description = "Dates", name = "Dates") final LocalDate[] dates) {
    final Object[][] result = new Object[dates.length][2];
    final DayCount curveDayCount = DayCountFactory.INSTANCE.instance(curveDayCountConventionName);
    for (int i = 0; i < dates.length; i++) {
      final double t = curveDayCount.getDayCountFraction(currentDate, dates[i]);
      result[i][0] = t;
      result[i][1] = yieldCurve.getDiscountFactor(t);
    }
    return result;
  }

  /**
   * Gets the zero rate for a date.
   * @param yieldCurve  the yield curve
   * @param currentDate  the current date, should be the curve construction date
   * @param curveDayCountConventionName  the day count convention name, should be the same as the curve day count used when constructing the curve
   * @param date  the date
   * @return  the zero rate
   */
  @XLFunction(name = "ISDAYieldCurve.ZeroRateForDate", category = "ISDA CDS model", description = "Get zero rate on a date")
  public static Double getZeroRate(
      @XLParameter(description = "Yield Curve", name = "YieldcCurve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Current Date", name = "Current Date") final LocalDate currentDate,
      @XLParameter(description = "Day Count Convention", name = "Curve Day Count Convention") final String curveDayCountConventionName,
      @XLParameter(description = "Date", name = "Date") final LocalDate date) {
    final DayCount curveDayCount = DayCountFactory.INSTANCE.instance(curveDayCountConventionName);
    final double t = curveDayCount.getDayCountFraction(currentDate, date);
    return yieldCurve.getZeroRate(t);
  }

  /**
   * Gets the discount factor for a date.
   * @param yieldCurve  the yield curve
   * @param currentDate  the current date, should be the curve construction date
   * @param curveDayCountConventionName  the day count convention name, should be the same as the curve day count used when constructing the curve
   * @param date  the date
   * @return  the discount factor
   */
  @XLFunction(name = "ISDAYieldCurve.DiscountFactorForDate", category = "ISDA CDS model", description = "Get discount factor on a date")
  public static Double getDiscountFactor(
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Current Date", name = "Current Date") final LocalDate currentDate,
      @XLParameter(description = "Day Count Convention", name = "Curve Day Count Convention") final String curveDayCountConventionName,
      @XLParameter(description = "Date", name = "date") final LocalDate date) {
    final DayCount curveDayCount = DayCountFactory.INSTANCE.instance(curveDayCountConventionName);
    final double t = curveDayCount.getDayCountFraction(currentDate, date);
    return yieldCurve.getDiscountFactor(t);
  }

  /**
   * Gets the zero rate for a time.
   * @param yieldCurve  the yield curve
   * @param t  the time
   * @return  the zero rate
   */
  @XLFunction(name = "ISDAYieldCurve.ZeroRate", category = "ISDA CDS model", description = "Get zero rate for a time")
  public static Double getZeroRate(
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Time", name = "Time") final Double t) {
    return yieldCurve.getZeroRate(t);
  }

  /**
   * Gets the discount factor for a time.
   * @param yieldCurve  the yield curve
   * @param t  the time
   * @return  the discount factor
   */
  @XLFunction(name = "ISDAYieldCurve.DiscountFactor", category = "ISDA CDS model", description = "Get discount factor for a time")
  public static Double getDiscountFactor(
      @XLParameter(description = "Yield Curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Time", name = "Time") final Double t) {
    return yieldCurve.getDiscountFactor(t);
  }

  /**
   * Restricted constructor.
   */
  private IsdaYieldCurveBuilder() {
  }
}
