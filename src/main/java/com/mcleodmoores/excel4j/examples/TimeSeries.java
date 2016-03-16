/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.examples;

import java.util.Arrays;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.TypeConversionMode;
import com.mcleodmoores.excel4j.XLArgument;
import com.mcleodmoores.excel4j.XLFunction;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * A simple implementation of a time series, defined as a list of LocalDate, double pairs that
 * is increasing in time. This class is immutable.
 */
public final class TimeSeries {

  /**
   * An empty time series.
   */
  public static final TimeSeries EMPTY = new TimeSeries(new LocalDate[0], new Double[0]);

  /**
   * Excel function that creates a time series from date, value pairs. The array can be either two rows
   * or two columns.
   * @param datesAndValuesArray  the dates and values, not null
   * @return  a time series
   */
  @XLFunction(name = "TimeSeries",
              description = "Create a time series",
              category = "Time series",
              typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static TimeSeries of(@XLArgument(name = "datesAndValues", description = "The dates and values")final XLArray datesAndValuesArray) {
    ArgumentChecker.notNull(datesAndValuesArray, "datesAndValuesArray");
    final TypeConverter dateConverter =
        ExcelFactory.getInstance().getTypeConverterRegistry().findConverter(ExcelToJavaTypeMapping.of(XLNumber.class, LocalDate.class));
    final TypeConverter doubleConverter =
        ExcelFactory.getInstance().getTypeConverterRegistry().findConverter(ExcelToJavaTypeMapping.of(XLNumber.class, Double.class));
    final XLValue[][] xlDatesAndValues = datesAndValuesArray.getArray();
    final LocalDate[] dates;
    final Double[] values;
    if (xlDatesAndValues.length == 2) { // have a horizontal range
      final int nDates = xlDatesAndValues[0].length;
      ArgumentChecker.isTrue(xlDatesAndValues[1].length == nDates, "Must have one value per date");
      dates = new LocalDate[nDates];
      values = new Double[nDates];
      for (int i = 0; i < nDates; i++) {
        dates[i] = (LocalDate) dateConverter.toJavaObject(LocalDate.class, xlDatesAndValues[0][i]);
        final XLValue value = xlDatesAndValues[1][i];
        values[i] = value == null ? null : (Double) doubleConverter.toJavaObject(Double.class, value);
      }
    } else if (xlDatesAndValues[0].length == 2) { // have a vertical range
      final int n = xlDatesAndValues.length;
      dates = new LocalDate[n];
      values = new Double[n];
      for (int i = 0; i < n; i++) {
        dates[i] = (LocalDate) dateConverter.toJavaObject(LocalDate.class, xlDatesAndValues[i][0]);
        final XLValue value = xlDatesAndValues[i][1];
        values[i] = value == null ? null : (Double) doubleConverter.toJavaObject(Double.class, value);
      }
    } else {
      throw new Excel4JRuntimeException("Could not create time series");
    }
    return new TimeSeries(dates, values);
  }

  /**
   * Excel function that creates a time series from a row or column of dates and a row or column of values.
   * There must be an equal number of dates and values.
   * @param datesArray  the dates, must be either a row or column, not null
   * @param valuesArray  the values, must be either a row or column, not null
   * @return  a time series
   */
  @XLFunction(name = "TimeSeries",
              description = "Create a time series",
              category = "Time series",
              typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static TimeSeries of(@XLArgument(name = "dates", description = "The dates")final XLArray datesArray,
                              @XLArgument(name = "values", description = "The values")final XLArray valuesArray) {
    ArgumentChecker.notNull(datesArray, "dates");
    ArgumentChecker.notNull(valuesArray, "values");
    ArgumentChecker.isFalse(datesArray.isArea(), "The date array must be either a column or row");
    ArgumentChecker.isFalse(valuesArray.isArea(), "The values array must be either a column or row");
    final TypeConverter dateConverter =
        ExcelFactory.getInstance().getTypeConverterRegistry().findConverter(ExcelToJavaTypeMapping.of(XLNumber.class, LocalDate.class));
    final TypeConverter doubleConverter =
        ExcelFactory.getInstance().getTypeConverterRegistry().findConverter(ExcelToJavaTypeMapping.of(XLNumber.class, Double.class));
    final XLValue[][] xlDates = datesArray.getArray();
    final XLValue[][] xlValues = valuesArray.getArray();
    final LocalDate[] dates;
    final Double[] values;
    if (datesArray.isRow()) {
      final int n = xlDates[0].length;
      dates = new LocalDate[n];
      for (int i = 0; i < n; i++) {
        dates[i] = (LocalDate) dateConverter.toJavaObject(LocalDate.class, xlDates[0][i]);
      }
    } else {
      final int n = xlDates.length;
      dates = new LocalDate[n];
      for (int i = 0; i < n; i++) {
        dates[i] = (LocalDate) dateConverter.toJavaObject(LocalDate.class, xlDates[i][0]);
      }
    }
    if (valuesArray.isRow()) {
      final int n = xlValues[0].length;
      values = new Double[n];
      for (int i = 0; i < n; i++) {
        // nulls are fine but can't be converted
        final XLValue xlValue = xlValues[0][i];
        values[i] = xlValue == null ? null : (Double) doubleConverter.toJavaObject(Double.class, xlValue);
      }
    } else {
      final int n = xlValues.length;
      values = new Double[n];
      for (int i = 0; i < n; i++) {
        final XLValue xlValue = xlValues[i][0];
        values[i] = xlValue == null ? null : (Double) doubleConverter.toJavaObject(Double.class, xlValue);
      }
    }
    return new TimeSeries(dates, values);
  }

  /**
   * Creates a time series. The inputs are copied and then sorted. The values can contain nulls.
   * @param dates  the dates, not null, cannot contain any null values, can be empty
   * @param values  the values, not null, can be empty
   * @return  the time series
   */
  public static TimeSeries of(final LocalDate[] dates, final Double[] values) {
    return new TimeSeries(dates, values);
  }

  /** The dates */
  private final LocalDate[] _dates;
  /** The values */
  private final Double[] _values;

  /**
   * Creates a time series.
   * @param dates  the dates
   * @param values  the values
   */
  private TimeSeries(final LocalDate[] dates, final Double[] values) {
    ArgumentChecker.notNullArray(dates, "dates");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(dates.length == values.length, "Must have one value per date");
    final int n = dates.length;
    _dates = new LocalDate[n];
    System.arraycopy(dates, 0, _dates, 0, n);
    _values = new Double[n];
    System.arraycopy(values, 0, _values, 0, n);
    sort(_dates, _values, 0, n - 1);
    // could probably check for duplicates in sorting code but this is just for examples
    if (_dates.length > 1) {
      LocalDate previous = _dates[0];
      for (int i = 1; i < _dates.length; i++) {
        final LocalDate next = dates[i];
        if (previous.equals(next)) {
          throw new Excel4JRuntimeException("Duplicate dates in time series");
        }
        previous = next;
      }
    }
  }

  /**
   * Gets all dates in the series.
   * @return  the dates
   */
  public LocalDate[] getDates() {
    return _dates;
  }

  /**
   * Gets all values in the series.
   * @return  the values
   */
  public Double[] getValues() {
    return _values;
  }

  /**
   * Gets the ith date of the series (zero-indexed).
   * @param i  the index
   * @return  the date
   */
  public LocalDate getDate(final int i) {
    return _dates[i];
  }

  /**
   * Gets the ith value of the series (zero-indexed).
   * @param i  the index
   * @return  the value
   */
  public Double getValue(final int i) {
    return _values[i];
  }

  /**
   * Gets the value for the date, or throws an exception if this value is not available.
   * @param date  the date, not null
   * @return  the value if present
   */
  public double getValue(final LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    final int index = Arrays.binarySearch(_dates, date);
    if (index < 0) {
      throw new Excel4JRuntimeException("No value for " + date);
    }
    return _values[index];
  }

  /**
   * Gets the index of a date. The result will be negative if this date is not present.
   * @param date  the date, not null
   * @return  the index
   */
  public int indexOf(final LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    return Arrays.binarySearch(_dates, date);
  }

  /**
   * Gets the size of the time series.
   * @return  the size
   */
  public int size() {
    return _dates.length;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_dates);
    result = prime * result + Arrays.hashCode(_values);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TimeSeries other = (TimeSeries) obj;
    if (!Arrays.equals(_dates, other._dates)) {
      return false;
    }
    if (!Arrays.equals(_values, other._values)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("TimeSeries[");
    for (int i = 0; i < _dates.length; i++) {
      sb.append("(");
      sb.append(_dates[i]);
      sb.append(", ");
      sb.append(_values[i]);
      sb.append(")");
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Simple quicksort implementation.
   * @param dates  the dates
   * @param values  the values
   * @param left  the left index of the partition
   * @param right  the right index of the partition
   */
  private static void sort(final LocalDate[] dates, final Double[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = (left + right) >> 1;
      final int newPivot = partition(dates, values, left, right, pivot);
      sort(dates, values, left, newPivot - 1);
      sort(dates, values, newPivot + 1, right);
    }
  }

  /**
   * Partitions the arrays.
   * @param dates  the dates
   * @param values  the values
   * @param left  the left index
   * @param right  the right index
   * @param pivot  the pivot
   * @return  the next pivot value
   */
  private static int partition(final LocalDate[] dates, final Double[] values, final int left, final int right, final int pivot) {
    final LocalDate pivotDate = dates[pivot];
    swap(dates, values, pivot, right);
    int index = left;
    for (int i = left; i < right; i++) {
      if (!dates[i].isAfter(pivotDate)) {
        swap(dates, values, i, index);
        index++;
      }
    }
    swap(dates, values, index, right);
    return index;
  }

  /**
   * Swaps elements in an array.
   * @param dates  the dates
   * @param values  the values
   * @param i  the first index to be swapped
   * @param j  the second index to be swapped
   */
  private static void swap(final LocalDate[] dates, final Double[] values, final int i, final int j) {
    final LocalDate temp1 = dates[i];
    final Double temp2 = values[i];
    dates[i] = dates[j];
    values[i] = values[j];
    dates[j] = temp1;
    values[j] = temp2;
  }
}
