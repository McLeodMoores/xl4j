/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Map;
import java.util.stream.Collectors;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.XLFunctions;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Removes negative values in a time series.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(description = "Removes negative values from a time series", category = "Time series")
public class NegativeValuesFilter implements TimeSeriesFunction<TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    final TimeSeries result = TimeSeries.of(ts);
    return TimeSeries.of(result.entrySet().stream()
        .filter(entry -> entry.getValue() >= 0)
        .collect(Collectors.<Map.Entry<LocalDate, Double>, LocalDate, Double>toConcurrentMap(e -> e.getKey(), e -> e.getValue())));
  }

}
