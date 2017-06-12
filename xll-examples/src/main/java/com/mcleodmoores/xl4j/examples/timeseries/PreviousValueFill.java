/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Fills any missing values in a time series with the previous value available in the time series.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Fill missing values in a time series with the previous value",
    category = "Time series")
public class PreviousValueFill implements TimeSeriesFunction<TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    if (ts.size() == 0) {
      return TimeSeries.newTimeSeries();
    }
    final LocalDate firstDate = ts.firstKey();
    final LocalDate lastDate = ts.lastKey();
    final TimeSeries result = TimeSeries.newTimeSeries();
    LocalDate date = firstDate;
    Double previousValue = ts.values().stream().filter(value -> value != null).findFirst().get();
    while (!date.isAfter(lastDate)) {
      final Double value = ts.get(date);
      if (value == null) {
        result.put(date, previousValue);
      } else {
        previousValue = value;
        result.put(date, value);
      }
      date = date.plusDays(1);
    }
    return result;
  }

}
