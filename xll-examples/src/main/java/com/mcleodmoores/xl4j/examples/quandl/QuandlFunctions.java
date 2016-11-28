/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.quandl;

import java.util.Iterator;

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
      @XLArgument(optional = true, description = "Start Date", name = "startDate") final LocalDate startDate,
      @XLArgument(optional = true, description = "End Date", name = "endDate") final LocalDate endDate,
      @XLArgument(optional = true, description = "Column Index", name = "columnIndex") final Integer columnIndex,
      @XLArgument(optional = true, description = "Frequency", name = "frequency") final Frequency frequency,
      @XLArgument(optional = true, description = "Max Rows", name = "maxRows") final Integer maxRows,
      @XLArgument(optional = true, description = "Sort Order", name = "sortOrder") final SortOrder sortOrder,
      @XLArgument(optional = true, description = "Transform", name = "transform") final Transform transform) {
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

  @XLFunction(name = "ExpandTabularResult", category = "Quandl", description = "Array function to expand a TabularResult object")
  public static Object[][] expandTabularResult(
      @XLArgument(description = "The TabularResult object handle", name = "tabularResult") final TabularResult result,
      @XLArgument(optional = true, description = "Include Header Row", name = "includeHeader") final Boolean includeHeader) {
    final boolean isIncludeHeader;
    if (includeHeader == null) {
      isIncludeHeader = true;
    } else {
      isIncludeHeader = includeHeader;
    }
    final HeaderDefinition headerDefinition = result.getHeaderDefinition();
    final int cols = headerDefinition.size();
    final int rows = result.size();
    final Object[][] values = new Object[rows + (includeHeader ? 1 : 0)][cols];
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
