/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.stream.Collectors;

import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.XLFunctions;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Returns a time series containing the reciprocals of the original values. A null value is treated as a 0.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(prefix = "Reciprocal", 
  typeConversionMode=TypeConversionMode.OBJECT_RESULT,
  description = "Return the reciprocal values of a time series", 
  category = "Time series")
public class Reciprocal implements TimeSeriesFunction<TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    final TimeSeries result = TimeSeries.of(ts);
    return TimeSeries.of(result.entrySet().parallelStream()
        .collect(Collectors.toConcurrentMap(e -> e.getKey(), e -> e.getValue() == null ? Double.POSITIVE_INFINITY : 1. / e.getValue())));
  }

}
