/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

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
    if (ts.get(ts.firstKey()) == null) {
      throw new XL4JRuntimeException("First value of time series was null: no value with which to pad");
    }
    final TimeSeries result = TimeSeries.newTimeSeries();
    result.putAll(ts);
    result.forEach((date, value) -> result.computeIfAbsent(date, value1 -> result.get(result.headMap(date).lastKey())));
    return result;
  }

}
