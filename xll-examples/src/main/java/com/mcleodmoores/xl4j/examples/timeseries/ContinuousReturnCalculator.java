/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Calculates the returns of a time series assuming continuous compounding.
 */
@XLNamespace("TimeSeries.")
@XLFunctions(
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Calculates the continuous return of a time series",
    category = "Time Series")
public class ContinuousReturnCalculator implements TimeSeriesFunction<TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    final int n = ts.size();
    ArgumentChecker.isTrue(n > 1, "Need more than one data point to calculate the returns");
    final Map<LocalDate, Double> result = new TreeMap<>();
    final Iterator<Map.Entry<LocalDate, Double>> iter1 = ts.entrySet().iterator();
    iter1.next();
    final Iterator<Map.Entry<LocalDate, Double>> iter2 = ts.entrySet().iterator();
    while (iter1.hasNext()) {
      final Map.Entry<LocalDate, Double> entry1 = iter1.next();
      final Map.Entry<LocalDate, Double> entry2 = iter2.next();
      result.put(entry1.getKey(), Math.log(entry1.getValue() / entry2.getValue()));
    }
    return TimeSeries.of(result);
  }

}