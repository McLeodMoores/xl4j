/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.XLFunctions;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Returns an element-by-element division of the two time series. Any missing values in either
 * series are assumed to be zero, so the resulting series could contain infinite values.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(prefix = "Divide", 
  typeConversionMode=TypeConversionMode.OBJECT_RESULT, 
  description = "Element-by-element division of one time series by the other", 
  category = "Time series")
public class Divide implements TimeSeriesBiFunction<TimeSeries, TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts1, final TimeSeries ts2) {
    ArgumentChecker.notNull(ts1, "ts1");
    ArgumentChecker.notNull(ts2, "ts2");
    final TimeSeries result = TimeSeries.of(ts1);
    // put missing values from second series or divide values from ts1 by ts2
    ts2.forEach((date2, value2) -> {
      result.computeIfPresent(date2, (date1, value1) -> value2 == null ? Double.POSITIVE_INFINITY : value1 / value2);
      result.putIfAbsent(date2, 0.);
    });
    return result;
  }
}
