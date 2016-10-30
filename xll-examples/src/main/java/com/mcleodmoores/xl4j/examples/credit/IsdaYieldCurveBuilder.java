/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.mcleodmoores.xl4j.XLArgument;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.SimpleWorkingDayCalendar;
import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurveBuild;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDAInstrumentTypes;
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
   * Constructs a yield curve using the ISDA CDS model.
   * @param tradeDate  the trade date
   * @param instrumentTypeNames  the names of the instrument types: M, MM or MONEY MARKET; S or SWAP, in any case
   * @param tenors  the instrument tenors, in the form "P3M" or "3M". Must have one per instrument type
   * @param quotes  the market data quotes. Must have one per instrument type
   * @param moneyMarketDayCountName  the money market day count name
   * @param swapDayCountName  the swap day count name
   * @param swapIntervalName  the swap interval name, in the for "P1Y" or "1Y"
   * @param curveDayCountName  the curve day count name
   * @param businessDayConventionName  the business day convention name
   * @param spotDate  the spot date, can be null. If null, the trade date is used
   * @param holidayDates  the holiday dates, can be null. If null, weekend-only holidays are used
   * @return  a yield curve constructed using the ISDA model
   */
  @XLFunction(name = "ISDAYieldCurve.BuildCurve", category = "ISDA CDS model",
      description = "Build a yield curve using the ISDA methodology")
  public static ISDACompliantCurve buildYieldCurve(
      @XLArgument(description = "Trade Date", name = "tradeDate") final LocalDate tradeDate,
      @XLArgument(description = "Instrument Types", name = "instrumentTypes") final String[] instrumentTypeNames,
      @XLArgument(description = "Tenors", name = "tenors") final String[] tenors,
      @XLArgument(description = "Quotes", name = "quotes") final double[] quotes,
      @XLArgument(description = "Money Market Day Count", name = "moneyMarketDayCount") final String moneyMarketDayCountName,
      @XLArgument(description = "Swap Day Count", name = "swapDayCount") final String swapDayCountName,
      @XLArgument(description = "Swap Interval", name = "swapInterval") final String swapIntervalName,
      @XLArgument(description = "Curve Day Count", name = "curveDayCount") final String curveDayCountName,
      @XLArgument(description = "Business Day Convention", name = "businessDayConvention") final String businessDayConventionName,
      @XLArgument(description = "Spot Date", name = "spotDate", optional = true) final LocalDate spotDate,
      @XLArgument(description = "Holidays", name = "holidays", optional = true) final LocalDate[] holidayDates) {
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
    @SuppressWarnings("deprecation")
    final Calendar calendar;
    if (holidayDates == null) {
      calendar = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    } else {
      final List<LocalDate> holidays = new ArrayList<>();
      for (final LocalDate holidayDate : holidayDates) {
        holidays.add(holidayDate);
      }
      calendar = new CalendarAdapter(new SimpleWorkingDayCalendar("Holidays", holidays, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    }
    final ISDACompliantYieldCurveBuild builder;
    if (spotDate == null) {
      builder = new ISDACompliantYieldCurveBuild(tradeDate, tradeDate, instrumentTypes, periods,
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
  public static Object[][] expandCurve(@XLArgument(description = "Yield Curve", name = "yieldCurve") final ISDACompliantCurve yieldCurve) {
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
  public static Object[][] expandDiscountFactors(@XLArgument(description = "Yield Curve", name = "yieldCurve") final ISDACompliantCurve yieldCurve) {
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
  public static Object[][] expandCurve(@XLArgument(description = "Yield Curve", name = "yieldCurve") final ISDACompliantCurve yieldCurve,
      @XLArgument(description = "Current Date", name = "currentDate") final LocalDate currentDate,
      @XLArgument(description = "Day Count Convention", name = "dayCountConventionName") final String curveDayCountConventionName,
      @XLArgument(description = "Dates", name = "dates") final LocalDate[] dates) {
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
  @XLFunction(name = "ISDAYieldCurve.ExpandDiscountFactorsForDates", category = "ISDA CDS model", description = "Get times and zero rates for dates")
  public static Object[][] expandDiscountFactors(@XLArgument(description = "Yield Curve", name = "yieldCurve") final ISDACompliantCurve yieldCurve,
      @XLArgument(description = "Current Date", name = "currentDate") final LocalDate currentDate,
      @XLArgument(description = "Day Count Convention", name = "dayCountConventionName") final String curveDayCountConventionName,
      @XLArgument(description = "Dates", name = "dates") final LocalDate[] dates) {
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
  public static Double getZeroRate(@XLArgument(description = "Yield Curve", name = "yieldCurve") final ISDACompliantCurve yieldCurve,
      @XLArgument(description = "Current Date", name = "currentDate") final LocalDate currentDate,
      @XLArgument(description = "Day Count Convention", name = "curveDayCountConventionName") final String curveDayCountConventionName,
      @XLArgument(description = "Date", name = "date") final LocalDate date) {
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
  public static Double getDiscountFactor(@XLArgument(description = "Yield Curve", name = "yieldCurve") final ISDACompliantCurve yieldCurve,
      @XLArgument(description = "Current Date", name = "currentDate") final LocalDate currentDate,
      @XLArgument(description = "Day Count Convention", name = "curveDayCountConventionName") final String curveDayCountConventionName,
      @XLArgument(description = "Date", name = "date") final LocalDate date) {
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
  public static Double getZeroRate(@XLArgument(description = "Yield Curve", name = "yieldCurve") final ISDACompliantCurve yieldCurve,
      @XLArgument(description = "Time", name = "time") final Double t) {
    return yieldCurve.getZeroRate(t);
  }

  /**
   * Gets the discount factor for a time.
   * @param yieldCurve  the yield curve
   * @param t  the time
   * @return  the discount factor
   */
  @XLFunction(name = "ISDAYieldCurve.DiscountFactor", category = "ISDA CDS model", description = "Get discount factor for a time")
  public static Double getDiscountFactor(@XLArgument(description = "Yield Curve", name = "yieldCurve") final ISDACompliantCurve yieldCurve,
      @XLArgument(description = "Time", name = "time") final Double t) {
    return yieldCurve.getDiscountFactor(t);
  }

  /**
   * Parses a string as a Period. The initial "P" may or may not be included.
   * @param string  the string
   * @return  the period
   */
  private static Period parsePeriod(final String string) {
    if (string.toUpperCase().startsWith("P")) {
      return Period.parse(string);
    }
    return Period.parse("P" + string);
  }

  /**
   * Restricted constructor.
   */
  private IsdaYieldCurveBuilder() {
  }
}
