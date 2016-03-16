/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.examples;

import java.util.Arrays;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;

/**
 * A simple implementation of a time series, defined as a list of LocalDate, double pairs that
 * is increasing in time. This class is immutable.
 */
/**
 *
 */
public final class TimeSeries {

  /**
   * An empty time series.
   */
  public static final TimeSeries EMPTY = TimeSeries.of(new LocalDate[0], new double[0]);

  /**
   * Creates a time series. The inputs are copied and then sorted.
   * @param dates  the dates, not null, cannot contain any null values, can be empty
   * @param values  the values, not null, can be empty
   * @return  the time series
   */
  public static TimeSeries of(final LocalDate[] dates, final double[] values) {
    ArgumentChecker.notNullArray(dates, "dates");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(dates.length == values.length, "Must have one value per date");
    return new TimeSeries(dates, values);
  }

  /** The dates */
  private final LocalDate[] _dates;
  /** The values */
  private final double[] _values;

  /**
   * Creates a time series.
   * @param dates  the dates
   * @param values  the values
   */
  private TimeSeries(final LocalDate[] dates, final double[] values) {
    final int n = dates.length;
    _dates = new LocalDate[n];
    System.arraycopy(dates, 0, _dates, 0, n);
    _values = new double[n];
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
  public double[] getValues() {
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
  public double getValue(final int i) {
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
  private static void sort(final LocalDate[] dates, final double[] values, final int left, final int right) {
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
  private static int partition(final LocalDate[] dates, final double[] values, final int left, final int right, final int pivot) {
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
  private static void swap(final LocalDate[] dates, final double[] values, final int i, final int j) {
    final LocalDate temp1 = dates[i];
    final double temp2 = values[i];
    dates[i] = dates[j];
    values[i] = values[j];
    dates[j] = temp1;
    values[j] = temp2;
  }
}
