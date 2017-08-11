/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Iterator;
import java.util.Map;

import org.threeten.bp.LocalDate;

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
public class PreviousAndFirstValueFill implements TimeSeriesFunction<TimeSeries> {

  @Override
  public TimeSeries apply(final TimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    if (ts.size() == 0) {
      return TimeSeries.newTimeSeries();
    }
    LocalDate firstDateWithData = null;
    Double firstData = null;
    final Iterator<Map.Entry<LocalDate, Double>> iter = ts.entrySet().iterator();
    while (iter.hasNext()) {
      final Map.Entry<LocalDate, Double> entry = iter.next();
      if (entry.getValue() != null) {
        firstDateWithData = entry.getKey();
        firstData = entry.getValue();
        break;
      }
    }
    if (firstData == null) {
      throw new XL4JRuntimeException("No data found in time series");
    }
    final TimeSeries result = TimeSeries.newTimeSeries();
    result.putAll(ts);
    LocalDate start = ts.firstKey();
    while (start.isBefore(firstDateWithData)) {
      result.put(start, firstData);
      start = start.plusDays(1);
    }
    result.forEach((date, value) -> result.computeIfAbsent(date, value1 -> result.get(result.headMap(date).lastKey())));
    return result;
  }

}
