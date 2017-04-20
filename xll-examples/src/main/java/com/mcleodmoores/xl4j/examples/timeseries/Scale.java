/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.stream.Collectors;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Scales each element of a time series.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Scales each element of a time series",
    category = "Time series")
public class Scale implements TimeSeriesBiFunction<Double, TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts, final Double scale) {
    ArgumentChecker.notNull(ts, "ts");
    ArgumentChecker.notNull(scale, "scale");
    final TimeSeries result = TimeSeries.of(ts);
    return TimeSeries.of(result.entrySet().parallelStream()
        .collect(Collectors.toConcurrentMap(e -> e.getKey(), e -> (e.getValue() == null ? 0. : e.getValue() * scale))));
  }
}
