/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Map;
import java.util.stream.Stream;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

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
    final LocalDate firstDate = ts.firstKey();
    final LocalDate lastDate = ts.lastKey();
    final TimeSeries result = TimeSeries.newTimeSeries();
    final Map.Entry<LocalDate, Double> firstEntryWithData = ts.entrySet().stream().filter(entry -> entry.getValue() != null).findFirst().get();
    final LocalDate firstDateWithData = firstEntryWithData.getKey();
    final Double firstData = firstEntryWithData.getValue();
    // fill start of series with data
    Stream.iterate(firstDate,
        date -> date.plusDays(1)).limit(ChronoUnit.DAYS.between(firstDate, firstDateWithData)).forEach(date -> result.put(date, firstData));
    Stream.iterate(firstDateWithData,
        date -> date.plusDays(1)).limit(ChronoUnit.DAYS.between(firstDateWithData, lastDate) + 1).forEach(date -> {
          final Double value = ts.get(date);
          result.put(date, value == null ? result.get(date.minusDays(1)) : value);
        });
    return result;
  }

}
