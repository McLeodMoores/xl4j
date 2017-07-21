/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Calculates the returns of a time series at the frequency of the time series data.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(
    prefix = "PercentageReturn",
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Calculates the percentage return of a time series",
    category = "Time Series")
public class PercentageReturnCalculator implements TimeSeriesFunction<TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    final int n = ts.size();
    ArgumentChecker.isTrue(n > 1, "Need more than one data point to calculate the returns");
    final TimeSeries result = TimeSeries.newTimeSeries();
    final Double[] previous = new Double[] { ts.get(ts.firstKey()) };
    ts.entrySet().stream().skip(1L).forEach(e -> {
      result.put(e.getKey(), e.getValue() / previous[0] - 1);
      previous[0] = e.getValue();
    });
    return result;
  }

}