/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.examples;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;
import org.threeten.bp.temporal.TemporalAmount;

import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.TypeConversionMode;
import com.mcleodmoores.excel4j.XLFunction;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;

/**
 * Functions that perform data operations on time series.
 */
public final class TimeSeriesDataUtils {

  /**
   * Restricted constructor.
   */
  private TimeSeriesDataUtils() {
  }

  /**
   * Creates a new time series any null values filled with values from the previous date. If the first value in the time series
   * is null, throws an exception, as there is no value that can be inserted. This function pads multiple missing values, so
   * the result contains no null values.
   * @param xlTimeSeries  the Excel time series object, not null
   * @return  a time series with missing values filled with the previous value
   */
  @XLFunction(name = "FillTimeSeriesWithPreviousValue",
              description = "Fill missing values in a time series with the previous value",
              category = "Time series",
              typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static TimeSeries fillWithPreviousValue(final XLObject xlTimeSeries) {
    ArgumentChecker.notNull(xlTimeSeries, "xlTimeSeries");
    final TimeSeries ts = (TimeSeries) ExcelFactory.getInstance().getHeap().getObject(xlTimeSeries.getHandle());
    final int size = ts.size();
    if (size == 0) {
      return TimeSeries.EMPTY;
    }
    final Double[] padded = new Double[size];
    if (ts.getValue(0) == null) {
      throw new IllegalStateException("First value of time series was null: no value with which to pad");
    }
    padded[0] = ts.getValue(0);
    for (int i = 1; i < size; i++) {
      final Double value = ts.getValue(i);
      padded[i] = value == null ? padded[i - 1] : value;
    }
    return TimeSeries.of(ts.getDates(), padded);
  }

  /**
   * Creates a new time series with any negative values replaced with nulls or removed from the time series, depending
   * on the option.
   * @param xlTimeSeries  the Excel time series object, not null
   * @param removeNegativeValues  if true, removes the negative date and value, otherwise replaces the value for that date with null
   * @return  a time series with negative values removed and replaced with null
   */
  @XLFunction(name = "FilterNegativeValues",
              description = "Remove negative values in a time series",
              category = "Time series",
              typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static TimeSeries removeNegativeValues(final XLObject xlTimeSeries, final XLBoolean removeNegativeValues) {
    ArgumentChecker.notNull(xlTimeSeries, "xlTimeSeries");
    final TimeSeries ts = (TimeSeries) ExcelFactory.getInstance().getHeap().getObject(xlTimeSeries.getHandle());
    final int size = ts.size();
    if (size == 0) {
      return TimeSeries.EMPTY;
    }
    final List<LocalDate> dateList = new ArrayList<>(size);
    final List<Double> valueList = new ArrayList<>(size);
    final boolean remove = removeNegativeValues.getValue();
    for (int i = 0; i < size; i++) {
      final LocalDate date = ts.getDate(i);
      final Double value = ts.getValue(i);
      if (value < 0) {
        if (!remove) {
          dateList.add(date);
          valueList.add(null);
        }
      } else {
        dateList.add(date);
        valueList.add(value);
      }
    }
    return TimeSeries.of(dateList.toArray(new LocalDate[dateList.size()]), valueList.toArray(new Double[dateList.size()]));
  }

  /**
   * Creates a new time series with any values that lie in the range +/- 1E-9 replaced with nulls.
   * @param xlTimeSeries  the Excel time series object, not null
   * @param removeZeroes  if true, removes the zero date and value, otherwise replaces the value for that date with null
   * @return  a time series with zero values removed and replaced with null
   */
  @XLFunction(name = "FilterZeroes",
              description = "Remove values that lie in the range +/-1E-9",
              category = "Time series",
              typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static TimeSeries removeZeroes(final XLObject xlTimeSeries, final XLBoolean removeZeroes) {
    return removeZeroes(xlTimeSeries, XLNumber.of(1e-9), removeZeroes);
  }

  /**
   * Creates a new time series with any values that equal zero to within a tolerance replaced with nulls.
   * @param xlTimeSeries  the Excel time series object, not null
   * @param tolerance  the tolerance, must be positive
   * @param removeZeroes  if true, removes the zero date and value, otherwise replaces the value for that date with null
   * @return  a time series with zero values removed and replaced with null
   */
  @XLFunction(name = "FilterZeroes",
              description = "Remove values that lie in the range +/-tolerance",
              category = "Time series",
              typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static TimeSeries removeZeroes(final XLObject xlTimeSeries, final XLNumber tolerance, final XLBoolean removeZeroes) {
    ArgumentChecker.notNull(xlTimeSeries, "xlTimeSeries");
    ArgumentChecker.notNull(tolerance, "tolerance");
    ArgumentChecker.notNegative(tolerance.getAsDouble(), "tolerance");
    final TimeSeries ts = (TimeSeries) ExcelFactory.getInstance().getHeap().getObject(xlTimeSeries.getHandle());
    final int size = ts.size();
    if (size == 0) {
      return TimeSeries.EMPTY;
    }
    final double eps = tolerance.getValue();
    final List<LocalDate> dateList = new ArrayList<>();
    final List<Double> valueList = new ArrayList<>();
    final boolean remove = removeZeroes.getValue();
    for (int i = 0; i < size; i++) {
      final LocalDate date = ts.getDate(i);
      final Double value = ts.getValue(i);
      if (Math.abs(value) < eps) {
        if (!remove) {
          dateList.add(date);
          valueList.add(null);
        }
      } else {
        dateList.add(date);
        valueList.add(value);
      }
    }
    return TimeSeries.of(dateList.toArray(new LocalDate[dateList.size()]), valueList.toArray(new Double[dateList.size()]));
  }


  /**
   * Creates a new time series containing sampled values of the original at a given frequency. If there is no value
   * for a sampling date, the value can either be null or the point not added to the sampled series, depending on the option.
   * If fromStart is true, the sampling starts at the beginning of the series, otherwise it is performed back through the
   * dates.
   * <p>
   * The sampled series is the same as the original if daily sampling is selected, as the granularity of time series is daily.
   * @param xlTimeSeries  the Excel time series object, not null
   * @param samplingType  the sampling type, not null
   * @param fromStart  if true, sampling is performed forward in time
   * @param removeMissing  if true, missing values are removed, otherwise a null value is added for the date
   * @return  a sampled series
   */
  @XLFunction(name = "Sample",
              description = "Sample time series",
              category = "Time series",
              typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static TimeSeries sample(final XLObject xlTimeSeries, final XLString samplingType, final XLBoolean fromStart, final XLBoolean removeMissing) {
    ArgumentChecker.notNull(xlTimeSeries, "xlTimeSeries");
    ArgumentChecker.notNull(samplingType, "samplingType");
    ArgumentChecker.notNull(fromStart, "fromStart");
    ArgumentChecker.notNull(removeMissing, "removeMissing");
    final SamplingType type = SamplingType.valueOf(samplingType.getValue());
    final boolean forward = fromStart.getValue();
    final boolean remove = removeMissing.getValue();
    final TimeSeries ts = (TimeSeries) ExcelFactory.getInstance().getHeap().getObject(xlTimeSeries.getHandle());
    final LocalDate firstDate = ts.getDate(0);
    final LocalDate lastDate = ts.getDate(ts.size() - 1);
    switch (type) {
      case DAILY:
        return TimeSeries.of(ts.getDates(), ts.getValues());
      case WEEKLY: {
        final LocalDate startDate = forward ? firstDate : lastDate;
        return sampleSeries(ts, startDate, forward, remove, Period.ofDays(7), null);
      }
      case MONTHLY: {
        final LocalDate startDate = forward ? firstDate : lastDate;
        return sampleSeries(ts, startDate, forward, remove, Period.ofMonths(1), null);
      }
      case START_OF_MONTH: {
        LocalDate startDate;
        if (forward) {
          startDate = firstDate.getDayOfMonth() == 1 ? firstDate : firstDate.with(TemporalAdjusters.firstDayOfNextMonth());
        } else {
          startDate = lastDate.with(TemporalAdjusters.firstDayOfMonth());
        }
        return sampleSeries(ts, startDate, forward, remove, Period.ofMonths(1), TemporalAdjusters.firstDayOfMonth());
      }
      case END_OF_MONTH: {
        LocalDate startDate;
        if (forward) {
          startDate = firstDate.with(TemporalAdjusters.lastDayOfMonth());
        } else {
          startDate = lastDate.getDayOfMonth() == lastDate.lengthOfMonth() ? lastDate : lastDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        }
        return sampleSeries(ts, startDate, forward, remove, Period.ofMonths(1), TemporalAdjusters.lastDayOfMonth());
      }
      case ANNUALLY: {
        final LocalDate startDate = forward ? firstDate : lastDate;
        return sampleSeries(ts, startDate, forward, remove, Period.ofYears(1), null);
      }
      case START_OF_YEAR: {
        LocalDate startDate;
        if (forward) {
          startDate = firstDate.getMonthValue() == 1 && firstDate.getDayOfMonth() == 1 ? firstDate : firstDate.with(TemporalAdjusters.firstDayOfNextYear());
        } else {
          startDate = lastDate.with(TemporalAdjusters.firstDayOfYear());
        }
        return sampleSeries(ts, startDate, forward, remove, Period.ofYears(1), TemporalAdjusters.firstDayOfYear());
      }
      case END_OF_YEAR: {
        LocalDate startDate;
        if (forward) {
          startDate = firstDate.with(TemporalAdjusters.lastDayOfYear());
        } else {
          // shouldn't really hard-code 12 in here, but it's for examples
          startDate = lastDate.getMonthValue() == 12 && lastDate.getDayOfMonth() == lastDate.lengthOfMonth()
              ? lastDate : lastDate.minusYears(1).with(TemporalAdjusters.lastDayOfYear());
        }
        return sampleSeries(ts, startDate, forward, remove, Period.ofYears(1), TemporalAdjusters.lastDayOfYear());
      }
      default:
        throw new Excel4JRuntimeException("Sampling type " + type + " not recognised");
    }
  }

  /**
   * Samples the time series.
   * @param ts  the time series
   * @param startDate  the date from which to start counting forwards or backwards
   * @param start  true to sample forward through time
   * @param removeMissing  true to excluding points with missing values from the result
   * @param amount  the amount by which to adjust the time
   * @param adjuster  the date adjuster
   * @return  a sampled series
   */
  private static TimeSeries sampleSeries(final TimeSeries ts, final LocalDate startDate, final boolean start, final boolean removeMissing,
      final TemporalAmount amount, final TemporalAdjuster adjuster) {
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values = new ArrayList<>();
    final int size = ts.size();
    if (start) {
      LocalDate date = startDate; //adjuster == null ? ts.getDate(0) : ts.getDate(0).with(adjuster);
      final LocalDate lastDate = ts.getDate(size - 1);
      while (!date.isAfter(lastDate)) {
        final int index = ts.indexOf(date);
        if (index >= 0) {
          final Double value = ts.getValue(index);
          if (removeMissing && value != null) { // don't add entry for null value
            dates.add(date);
            values.add(value);
          } else {
            dates.add(date);
            values.add(value);
          }
        } else if (!removeMissing) { // no value, but doesn't matter as requested a series that fills missing values with null
          dates.add(date);
          values.add(null);
        }
        date = adjuster == null ? date.plus(amount) : date.plus(amount).with(adjuster);
      }
      return TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()]));
    }
    LocalDate date = startDate; //adjuster == null ? ts.getDate(size - 1) : ts.getDate(size - 1).with(adjuster);
    final LocalDate firstDate = ts.getDate(0);
    while (!date.isBefore(firstDate)) {
      final int index = ts.indexOf(date);
      if (index >= 0) {
        final Double value = ts.getValue(index);
        if (removeMissing && value != null) { // don't add entry for null value
          dates.add(date);
          values.add(value);
        } else {
          dates.add(date);
          values.add(value);
        }
      } else if (!removeMissing) { // no value, but doesn't matter as requested a series that fills missing values with null
        dates.add(date);
        values.add(null);
      }
      date = adjuster == null ? date.minus(amount) : date.minus(amount).with(adjuster);
    }
    return TimeSeries.of(dates.toArray(new LocalDate[dates.size()]), values.toArray(new Double[dates.size()]));
  }

}
