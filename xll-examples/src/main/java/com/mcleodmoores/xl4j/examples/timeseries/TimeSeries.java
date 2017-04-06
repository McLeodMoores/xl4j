/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.XLParameter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * A simple implementation of a time series, defined as a list of LocalDate, double pairs that is increasing in time. This class is
 * immutable.
 */
public final class TimeSeries implements SortedMap<LocalDate, Double> {

  /**
   * A function that creates a time series from date, value pairs or from a row or column of dates and a row or column of values. In the
   * first case, the array can be either two rows or two columns. Otherwise, the time series can be created from a row or column of dates
   * and a row or column of values with an equal number of dates and values.
   *
   * @param datesAndValues
   *          the dates and values, not null
   * @return a time series
   */
  @XLFunction(name = "TimeSeries",
      description = "Create a time series",
      category = "Time series",
      typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static TimeSeries of(@XLParameter(name = "datesAndValues", description = "The dates and values") final XLValue... datesAndValues) {
    ArgumentChecker.notNull(datesAndValues, "datesAndValues");
    if (datesAndValues.length == 1 && datesAndValues[0] instanceof XLArray) {
      return ofRange((XLArray) datesAndValues[0]);
    } else if (datesAndValues.length == 2 && datesAndValues[0] instanceof XLArray && datesAndValues[1] instanceof XLArray) {
      return of((XLArray) datesAndValues[0], (XLArray) datesAndValues[1]);
    }
    throw new Excel4JRuntimeException("Cannot create time series from input");
  }

  /**
   * A function that creates a time series from date, value pairs. The array can be either two rows or two columns.
   *
   * @param datesAndValuesArray
   *          the dates and values, not null
   * @return a time series
   */
  private static TimeSeries ofRange(final XLArray datesAndValuesArray) {
    final TypeConverter dateConverter = ExcelFactory.getInstance().getTypeConverterRegistry()
        .findConverter(ExcelToJavaTypeMapping.of(XLNumber.class, LocalDate.class));
    final TypeConverter doubleConverter = ExcelFactory.getInstance().getTypeConverterRegistry()
        .findConverter(ExcelToJavaTypeMapping.of(XLNumber.class, Double.class));
    final XLValue[][] xlDatesAndValues = datesAndValuesArray.getArray();
    final SortedMap<LocalDate, Double> data = new TreeMap<>();
    if (xlDatesAndValues.length == 2) { // have a horizontal range
      final int nDates = xlDatesAndValues[0].length;
      ArgumentChecker.isTrue(xlDatesAndValues[1].length == nDates, "Must have one value per date");
      for (int i = 0; i < nDates; i++) {
        final LocalDate date = (LocalDate) dateConverter.toJavaObject(LocalDate.class, xlDatesAndValues[0][i]);
        final XLValue xlValue = xlDatesAndValues[1][i];
        final Double value = xlValue == null ? null : (Double) doubleConverter.toJavaObject(Double.class, xlValue);
        if (data.put(date, value) != null) {
          throw new Excel4JRuntimeException("Value already set for " + date);
        }
      }
    } else if (xlDatesAndValues[0].length == 2) { // have a vertical range
      final int n = xlDatesAndValues.length;
      for (int i = 0; i < n; i++) {
        final LocalDate date = (LocalDate) dateConverter.toJavaObject(LocalDate.class, xlDatesAndValues[i][0]);
        final XLValue xlValue = xlDatesAndValues[i][1];
        final Double value = xlValue == null ? null : (Double) doubleConverter.toJavaObject(Double.class, xlValue);
        if (data.put(date, value) != null) {
          throw new Excel4JRuntimeException("Value already set for " + date);
        }
      }
    } else {
      throw new Excel4JRuntimeException("Could not create time series");
    }
    return new TimeSeries(data);
  }

  /**
   * A function that creates a time series from a row or column of dates and a row or column of values. There must be an equal number of
   * dates and values.
   *
   * @param datesArray
   *          the dates, must be either a row or column, not null
   * @param valuesArray
   *          the values, must be either a row or column, not null
   * @return a time series
   */
  private static TimeSeries of(final XLArray datesArray, final XLArray valuesArray) {
    ArgumentChecker.isFalse(datesArray.isArea(), "The date array must be either a column or row");
    ArgumentChecker.isFalse(valuesArray.isArea(), "The values array must be either a column or row");
    final TypeConverter dateConverter = ExcelFactory.getInstance().getTypeConverterRegistry()
        .findConverter(ExcelToJavaTypeMapping.of(XLNumber.class, LocalDate.class));
    final TypeConverter doubleConverter = ExcelFactory.getInstance().getTypeConverterRegistry()
        .findConverter(ExcelToJavaTypeMapping.of(XLNumber.class, Double.class));
    final XLValue[][] xlDates = datesArray.getArray();
    final XLValue[][] xlValues = valuesArray.getArray();
    final SortedMap<LocalDate, Double> data = new TreeMap<>();
    if (datesArray.isRow()) {
      final int n = xlDates[0].length;
      if (valuesArray.isRow() && valuesArray.getArray()[0].length != n || valuesArray.isColumn() && valuesArray.getArray().length != n) {
        throw new Excel4JRuntimeException("Values not the same length as dates");
      }
      for (int i = 0; i < n; i++) {
        final LocalDate date = (LocalDate) dateConverter.toJavaObject(LocalDate.class, xlDates[0][i]);
        final XLValue xlValue;
        if (valuesArray.isRow()) {
          xlValue = xlValues[0][i];
        } else {
          xlValue = xlValues[i][0];
        }
        final Double value = xlValue == null ? null : (Double) doubleConverter.toJavaObject(Double.class, xlValue);
        if (data.put(date, value) != null) {
          throw new Excel4JRuntimeException("Value already set for " + date);
        }
      }
    } else {
      final int n = xlDates.length;
      if (valuesArray.isRow() && valuesArray.getArray()[0].length != n || valuesArray.isColumn() && valuesArray.getArray().length != n) {
        throw new Excel4JRuntimeException("Values not the same length as dates");
      }
      for (int i = 0; i < n; i++) {
        final LocalDate date = (LocalDate) dateConverter.toJavaObject(LocalDate.class, xlDates[i][0]);
        final XLValue xlValue;
        if (valuesArray.isRow()) {
          xlValue = xlValues[0][i];
        } else {
          xlValue = xlValues[i][0];
        }
        final Double value = xlValue == null ? null : (Double) doubleConverter.toJavaObject(Double.class, xlValue);
        if (data.put(date, value) != null) {
          throw new Excel4JRuntimeException("Value already set for " + date);
        }
      }
    }
    return new TimeSeries(data);
  }

  /**
   * Creates a time series. The inputs are copied and then sorted.
   *
   * @param dates
   *          the dates, not null, cannot contain any null values, can be empty
   * @param values
   *          the values, not null, can be empty
   * @return the time series
   */
  public static TimeSeries of(final List<LocalDate> dates, final List<Double> values) {
    ArgumentChecker.notNull(dates, "dates");
    ArgumentChecker.notNull(values, "values");
    final SortedMap<LocalDate, Double> data = new TreeMap<>();
    for (int i = 0; i < dates.size(); i++) {
      if (data.put(ArgumentChecker.notNull(dates.get(i), "date"), values.get(i)) != null) {
        throw new Excel4JRuntimeException("Value already set for " + dates.get(i));
      }
    }
    return new TimeSeries(data);
  }

  /**
   * Creates a time series.
   *
   * @param data
   *          the data, not null
   * @return
   *          a time series
   */
  public static TimeSeries of(final Map<LocalDate, Double> data) {
    ArgumentChecker.notNull(data, "data");
    return new TimeSeries(data);
  }

  /**
   * Creates a copy of a time series.
   *
   * @param other
   *          the data, not null
   * @return
   *          a time series
   */
  public static TimeSeries of(final TimeSeries other) {
    return new TimeSeries(other._data);
  }

  /**
   * Creates a mutable empty time series.
   *
   * @return
   *          an empty time series
   */
  public static TimeSeries newTimeSeries() {
    return new TimeSeries(Collections.emptySortedMap());
  }

  private final SortedMap<LocalDate, Double> _data;

  /**
   * Creates a time series.
   *
   * @param dates
   *          the dates
   * @param values
   *          the values
   */
  private TimeSeries(final Map<LocalDate, Double> data) {
    _data = new TreeMap<>(ArgumentChecker.notNull(data, "data"));
  }

  @Override
  public void clear() {
    _data.clear();
  }

  @Override
  public boolean containsKey(final Object key) {
    return _data.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return _data.containsValue(value);
  }

  @Override
  public Double get(final Object key) {
    return _data.get(key);
  }

  @Override
  public boolean isEmpty() {
    return _data.isEmpty();
  }

  @Override
  public Double put(final LocalDate date, final Double value) {
    return _data.put(ArgumentChecker.notNull(date, "date"), value);
  }

  @Override
  public void putAll(final Map<? extends LocalDate, ? extends Double> m) {
    _data.putAll(ArgumentChecker.notNull(m, "m"));
  }

  @Override
  public Double remove(final Object key) {
    return _data.remove(key);
  }

  @Override
  public int size() {
    return _data.size();
  }

  @Override
  public Comparator<? super LocalDate> comparator() {
    return _data.comparator();
  }

  @Override
  public Set<Map.Entry<LocalDate, Double>> entrySet() {
    return _data.entrySet();
  }

  @Override
  public LocalDate firstKey() {
    return _data.firstKey();
  }

  @Override
  public SortedMap<LocalDate, Double> headMap(final LocalDate toKey) {
    return _data.headMap(toKey);
  }

  @Override
  public Set<LocalDate> keySet() {
    return _data.keySet();
  }

  @Override
  public LocalDate lastKey() {
    return _data.lastKey();
  }

  @Override
  public SortedMap<LocalDate, Double> subMap(final LocalDate fromKey, final LocalDate toKey) {
    return _data.subMap(fromKey, toKey);
  }

  @Override
  public SortedMap<LocalDate, Double> tailMap(final LocalDate fromKey) {
    return _data.tailMap(fromKey);
  }

  @Override
  public Collection<Double> values() {
    return _data.values();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _data.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TimeSeries)) {
      return false;
    }
    final TimeSeries other = (TimeSeries) obj;
    return Objects.deepEquals(_data, other._data);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("TimeSeries");
    sb.append(_data.toString());
    return sb.toString();
  }

}
