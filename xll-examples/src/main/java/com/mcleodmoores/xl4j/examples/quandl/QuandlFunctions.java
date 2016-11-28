/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.quandl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import com.jimmoores.quandl.DataSetRequest;
import com.jimmoores.quandl.DataSetRequest.Builder;
import com.jimmoores.quandl.Frequency;
import com.jimmoores.quandl.HeaderDefinition;
import com.jimmoores.quandl.QuandlSession;
import com.jimmoores.quandl.Row;
import com.jimmoores.quandl.SortOrder;
import com.jimmoores.quandl.TabularResult;
import com.jimmoores.quandl.Transform;
import com.mcleodmoores.xl4j.XLArgument;
import com.mcleodmoores.xl4j.XLFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public final class QuandlFunctions {
  private static final QuandlSession SESSION = QuandlSession.create();

  /**
   * Restricted constructor.
   */
  private QuandlFunctions() {
  }

  @XLFunction(name = "QuandlDataSet", category = "Quandl", description = "Get a data set from Quandl")
  public static TabularResult dataSet(
      @XLArgument(description = "Quandl Code", name = "quandlCode") final String quandlCode,
      @XLArgument(optional = true, description = "Start Date", name = "StartDate") final LocalDate startDate,
      @XLArgument(optional = true, description = "End Date", name = "EndDate") final LocalDate endDate,
      @XLArgument(optional = true, description = "Column Index", name = "ColumnIndex") final Integer columnIndex,
      @XLArgument(optional = true, description = "Frequency", name = "Frequency") final Frequency frequency,
      @XLArgument(optional = true, description = "Max Rows", name = "MaxRows") final Integer maxRows,
      @XLArgument(optional = true, description = "Sort Order", name = "SortOrder") final SortOrder sortOrder,
      @XLArgument(optional = true, description = "Transform", name = "Transform") final Transform transform) {
    Builder builder = DataSetRequest.Builder.of(quandlCode);
    if (startDate != null) {
      builder = builder.withStartDate(startDate);
    }
    if (endDate != null) {
      builder = builder.withEndDate(endDate);
    }
    if (columnIndex != null) {
      builder = builder.withColumn(columnIndex);
    }
    if (frequency != null) {
      builder = builder.withFrequency(frequency);
    }
    if (maxRows != null) {
      builder = builder.withMaxRows(maxRows);
    }
    if (sortOrder != null) {
      builder = builder.withSortOrder(sortOrder);
    }
    if (transform != null) {
      builder = builder.withTransform(transform);
    }
    return SESSION.getDataSet(builder.build());
  }

  /**
   * Get the header names for a Quandl tabular result.
   *
   * @param result
   *          the tabular data
   * @return
   *          the header names or null if they could not be obtained
   */
  @XLFunction(name = "GetHeaders", category = "Quandl", description = "Get the headers")
  public static String[] getHeaders(
      @XLArgument(description = "The TabularResult object handle", name = "TabularResult") final TabularResult result) {
    final HeaderDefinition headerDefinition = result.getHeaderDefinition();
    if (headerDefinition == null) {
      return null;
    }
    final List<String> columnNames = headerDefinition.getColumnNames();
    if (columnNames == null) {
      return null;
    }
    return columnNames.toArray(new String[columnNames.size()]);
  }

  /**
   * Get the ith row from a Quandl tabular result. Note that this is 1-indexed.
   * @param result
   *          the tabular data
   * @param index
   *          the index number, must be greater than zero and less than the number of rows in the result
   * @return
   *          the row, or null if it could not be obtained
   */
  @XLFunction(name = "GetRow", category = "Quandl", description = "Get the ith row")
  public static Object[] getRow(
      @XLArgument(description = "The TabularResult object handle", name = "TabularResult") final TabularResult result,
      @XLArgument(description = "The index", name = "index") final int index) {
    ArgumentChecker.isTrue(index > 0 && index <= result.size(), "Index {} out of range 1 to {}", index, result.size());
    final Row row = result.get(index - 1);
    if (row == null) {
      return null;
    }
    final int n = row.size();
    final Object[] rowValues = new Object[n];
    for (int i = 0; i < n; i++) {
      try {
        rowValues[i] = row.getDouble(i);
      } catch (final NumberFormatException nfe) {
        try {
          rowValues[i] = row.getLocalDate(i);
        } catch (final DateTimeParseException dtpe) {
          rowValues[i] = row.getString(i);
        }
      }
    }
    return rowValues;
  }

  /**
   * Gets the named row from a Quandl tabular result.
   * @param result
   *          the tabular data
   * @param header
   *          the row name
   * @return
   *          the row, or null if it could not be obtained
   */
  @XLFunction(name = "GetNamedRow", category = "Quandl", description = "Get the named row")
  public static Object[] getRow(
      @XLArgument(description = "The TabularResult object handle", name = "TabularResult") final TabularResult result,
      @XLArgument(description = "The row name", name = "rowName") final String header) {
    final int index = Arrays.binarySearch(getHeaders(result), header);
    if (index < 0) {
      return null;
    }
    return getRow(result, index + 1);
  }

  @XLFunction(name = "ExpandTabularResult", category = "Quandl", description = "Array function to expand a TabularResult object")
  public static Object[][] expandTabularResult(
      @XLArgument(description = "The TabularResult object handle", name = "tabularResult") final TabularResult result,
      @XLArgument(optional = true, description = "Include Header Row", name = "includeHeader") final Boolean includeHeader) {
    final boolean isIncludeHeader = includeHeader == null ? true : includeHeader;
    final HeaderDefinition headerDefinition = result.getHeaderDefinition();
    final int cols = headerDefinition.size();
    final int rows = result.size();
    final Object[][] values = new Object[rows + (isIncludeHeader ? 1 : 0)][cols];
    int row = 0;
    if (isIncludeHeader) {
      final Object[] headerRow = values[0];
      int i = 0;
      for (final String columnName : headerDefinition.getColumnNames()) {
        headerRow[i++] = columnName;
      }
      row++;
    }
    final Iterator<Row> iterator = result.iterator();
    while (iterator.hasNext()) {
      final Row theRow = iterator.next();
      for (int col = 0; col < cols; col++) {
        try {
          values[row][col] = theRow.getDouble(col);
        } catch (final NumberFormatException nfe) {
          try {
            values[row][col] = theRow.getLocalDate(col);
          } catch (final DateTimeParseException dtpe) {
            values[row][col] = theRow.getString(col);
          }
        }
      }
      row++;
    }
    return values;
  }
}
