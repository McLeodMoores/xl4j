/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Returns an element-by-element subtraction of the two time series. If a date is missing from either series, the value for
 * that date in the other series is used (if it is missing in both, the value for that date is set to 0).
 */
@XLNamespace("TimeSeries.")
@XLFunctions(typeConversionMode = TypeConversionMode.OBJECT_RESULT,
description = "Element-by-element subtraction of one time series from the other",
category = "Time series")
public class Subtract implements TimeSeriesBiFunction<TimeSeries, TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts1, final TimeSeries ts2) {
    ArgumentChecker.notNull(ts1, "ts1");
    ArgumentChecker.notNull(ts2, "ts2");
    final TimeSeries result = TimeSeries.of(ts1);
    // put missing values from second series or subtract ts2 values from ts1
    ts2.forEach((date2, value2) -> {
      result.computeIfPresent(date2, (date1, value1) -> value2 == null ? value1 == null ? 0. : value1 : value1 - value2);
      result.putIfAbsent(date2, value2 == null ? 0. : -value2);
    });
    return result;
  }
}
