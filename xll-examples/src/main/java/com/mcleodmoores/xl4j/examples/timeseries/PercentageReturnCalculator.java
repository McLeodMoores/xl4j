/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Calculates the returns of a time series at the frequency of the time series data.
 */
@XLNamespace("TimeSeries")
@XLFunction(name = "PercentageReturn", description = "Calculates the percentage return of a time series", category = "Time Series")
public class PercentageReturnCalculator implements TimeSeriesFunction<TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    final int n = ts.size();
    ArgumentChecker.isTrue(n > 1, "Need more than one data point to calculate the returns");
    final Map<LocalDate, Double> result = new TreeMap<>();
    final Iterator<Map.Entry<LocalDate, Double>> iter1 = result.entrySet().iterator();
    iter1.next();
    final Iterator<Map.Entry<LocalDate, Double>> iter2 = result.entrySet().iterator();
    while (iter1.hasNext()) {
      final Map.Entry<LocalDate, Double> entry1 = iter1.next();
      final Map.Entry<LocalDate, Double> entry2 = iter2.next();
      result.put(entry1.getKey(), entry1.getValue() / entry2.getValue() - 1);
    }
    return TimeSeries.of(result);
  }

}