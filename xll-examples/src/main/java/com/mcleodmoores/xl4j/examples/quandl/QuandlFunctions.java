/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.quandl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
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
import com.jimmoores.quandl.util.QuandlRuntimeException;
import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.XLParameter;
import com.mcleodmoores.xl4j.examples.timeseries.TimeSeries;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * User defined functions for interacting with Quandl via the Quandl4J library.
 */
public final class QuandlFunctions {
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlFunctions.class);

  /**
   * Simple cache value containing value and last update time.
   */
  static class CacheValue {
    private LocalDateTime _lastUpdate;
    private TabularResult _value;
    private CacheValue(LocalDateTime lastUpdate, TabularResult value) {
      _lastUpdate = lastUpdate;
      _value = value;
    }
    public static CacheValue of(TabularResult value) {
      return new CacheValue(LocalDateTime.now(), value);
    }
    public TabularResult getValue() {
      return _value;
    }
    public LocalDateTime getLastUpdate() {
      return _lastUpdate;
    }
    public int hashCode() {
      return _value.hashCode();
    }
    public boolean equals(Object other) {
      if (!(other instanceof CacheValue)) {
        return false;
      }
      CacheValue o = (CacheValue) other;
      return _value.equals(o._value);
    }
  }
  
  private static final QuandlSession SESSION;
  /** Super simple cache implementation, grows unbounded but replaces old values at next request after CACHE_LIFETIME_HOURS */
  private static final ConcurrentHashMap<DataSetRequest, CacheValue> CACHE;
  private static final boolean API_KEY_PRESENT = System.getProperty("quandl.auth.token") != null;
  private static final String API_KEY_MESSAGE = "No Quandl API key: set JVM property -Dquandl.auth.token=YOUR_KEY via settings on Add-in toolbar";
  private static final long CACHE_LIFETIME_HOURS = 6;

  static {
    //SessionOptions.Builder builder = null;
    //if (API_KEY_PRESENT) {
    //  LOGGER.info("Using Quandl API key from properties");
    //  builder = SessionOptions.Builder.withAuthToken(System.getProperty("quandl.auth.token"));
    //} else {
    //  LOGGER.warn("No Quandl API key detected");
    //  builder = SessionOptions.Builder.withoutAuthToken();
    //}
    //SessionOptions sessionOptions = builder.withRetryPolicy(RetryPolicy.createNoRetryPolicy()).build();
    SESSION = QuandlSession.create(); //sessionOptions);
    CACHE = new ConcurrentHashMap<>();
  }

  /**
   * Restricted constructor.
   */
  private QuandlFunctions() {
  }

  /**
   * Gets a time series of data from Quandl.
   *
   * @param quandlCode
   *          the ticker
   * @param startDate
   *          the start date of the time series. If not set, the entire history is obtained.
   * @param endDate
   *          the end date of the time series. If not set, the latest data are returned.
   * @param columnIndex
   *          the column index. If not set, all available columns are returned.
   * @param frequency
   *          the data frequency. If not set, a daily time series is returned.
   * @param maxRows
   *          the maximum number of rows to return. If not set, all available rows are returned.
   * @param sortOrder
   *          the sort order of the rows. If not set, the series will be descending in date.
   * @param transform
   *          the transformation of the data. If not set, no transformation is applied to the data.
   * @return the time series
   */
  @XLFunction(
      name = "QuandlDataSet",
      category = "Quandl",
      description = "Get a data set from Quandl",
      isAutoAsynchronous = true,
      isLongRunning = true)
  public static synchronized TabularResult dataSet(
      @XLParameter(description = "Quandl Code", name = "quandlCode") final String quandlCode,
      @XLParameter(optional = true, description = "Start Date", name = "StartDate") final LocalDate startDate,
      @XLParameter(optional = true, description = "End Date", name = "EndDate") final LocalDate endDate,
      @XLParameter(optional = true, description = "Column Index", name = "ColumnIndex") final Integer columnIndex,
      @XLParameter(optional = true, description = "Frequency", name = "Frequency") final Frequency frequency,
      @XLParameter(optional = true, description = "Max Rows", name = "MaxRows") final Integer maxRows,
      @XLParameter(optional = true, description = "Sort Order", name = "SortOrder") final SortOrder sortOrder,
      @XLParameter(optional = true, description = "Transform", name = "Transform") final Transform transform) {
    LOGGER.info("Fetching Quandl series for {}", quandlCode);
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
    try {
      DataSetRequest request = builder.build();
      if (CACHE.containsKey(request)) {
        CacheValue cacheValue = CACHE.get(request);
        if (cacheValue.getLastUpdate().isAfter(LocalDateTime.now().minusHours(CACHE_LIFETIME_HOURS))) {
          return cacheValue.getValue();
        }
      } 
      TabularResult result = SESSION.getDataSet(builder.build());
      CACHE.put(request, CacheValue.of(result)); // update cache or initial value.
      return result;
    } catch (final QuandlRuntimeException qre) {
      if (!API_KEY_PRESENT) {
        final HeaderDefinition headerDefinition = HeaderDefinition.of("Date", "Error");
        return TabularResult.of(headerDefinition, Collections.singletonList(Row.of(headerDefinition, new String[] { "1970-01-01", API_KEY_MESSAGE })));
      }
      return null;
    }
  }

  /**
   * Get the header names for a Quandl tabular result.
   *
   * @param result
   *          the tabular data
   * @return the header names or null if they could not be obtained
   */
  @XLFunction(name = "GetHeaders", category = "Quandl", description = "Get the headers")
  public static String[] getHeaders(
      @XLParameter(description = "The TabularResult object handle", name = "TabularResult") final TabularResult result) {
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
   *
   * @param result
   *          the tabular data
   * @param index
   *          the index number, must be greater than zero and less than the number of rows in the result
   * @return the row, or null if it could not be obtained
   */
  @XLFunction(name = "GetRow", category = "Quandl", description = "Get the ith row")
  public static Object[] getRow(@XLParameter(description = "The tabular data", name="result") final TabularResult result,
                                @XLParameter(description = "The index", name = "index") final int index) {
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
   * Get the ith column from a Quandl tabular result. Note that this is 0-indexed.
   *
   * @param result
   *          the tabular data
   * @param index
   *          the index number, must be greater than zero and less than the number of rows in the result
   * @return the row, or null if it could not be obtained
   */
  @XLFunction(name = "GetColumn", category = "Quandl", description = "Get the ith column")
  public static Object[] getColumn(@XLParameter(description = "The tabular data", name="result") final TabularResult result,
                                   @XLParameter(description = "The index", name = "index") final int index) {
    ArgumentChecker.isTrue(index >= 0 && index < result.size(), "Index {} out of range 1 to {}", index, result.size());
    final int n = result.size();
    final Object[] columnValues = new Object[n];
    for (int i = 0; i < n; i++) {
      Row row = result.get(i);
      try {
        columnValues[i] = row.getDouble(index);
      } catch (final NumberFormatException nfe) {
        try {
          columnValues[i] = row.getLocalDate(index);
        } catch (final DateTimeParseException dtpe) {
          columnValues[i] = row.getString(index);
        }
      }
    }
    return columnValues;
  }
  /**
   * Gets the named column from a Quandl tabular result.
   *
   * @param result
   *          the tabular data
   * @param header
   *          the row name
   * @return the row, or null if it could not be obtained
   */
  @XLFunction(name = "GetNamedColumn", category = "Quandl", description = "Get the named column")
  public static Object[] getColumn(
      @XLParameter(description = "The TabularResult object handle", name = "TabularResult") final TabularResult result,
      @XLParameter(description = "The row name", name = "rowName") final String header) {
    try {
      final int index = result.getHeaderDefinition().columnIndex(header);
      LOGGER.info("index for column " + header + " is " + index);
      return getColumn(result, index);
    } catch (IllegalArgumentException iae) {
      return null;
    }
  }

  /**
   * Transforms one row of a table of data into a time series if there are data points available for the header.
   *
   * @param result
   *          the tabular data
   * @param header
   *          the header
   * @return the data as a time series
   */
  @XLFunction(name = "TabularResultAsTimeSeries", category = "Quandl", typeConversionMode = TypeConversionMode.OBJECT_RESULT,
      description = "Convert a data from a specific field to a time series")
  public static TimeSeries getTabularResultAsTimeSeries(
      @XLParameter(description = "The TabularResult object handle", name = "TabularResult") final TabularResult result,
      @XLParameter(description = "The row name", name = "rowName") final String header) {
    ArgumentChecker.notNull(result, "result");
    final Object[] dateArray = getColumn(result, 0);
    if (dateArray == null) {
      throw new Excel4JRuntimeException("No dates available for TabularResult");
    }
    final Object[] valueArray = getColumn(result, header);
    if (valueArray == null) {
      throw new Excel4JRuntimeException("No data available for " + header);
    }
    final int n = dateArray.length;
    final TimeSeries ts = TimeSeries.emptyTimeSeries();
    for (int i = 0; i < n; i++) {
      LocalDate date = (LocalDate) dateArray[i];
      Double value = (Double) valueArray[i];
      ts.put(date, value);
    }
    return ts;
  }

  /**
   * Expands a tabular result into an array.
   * @param result
   *          the tabular data
   * @param includeHeader
   *          true if headers are to be included
   * @return the data as an array
   */
  @XLFunction(name = "ExpandTabularResult", category = "Quandl",
      description = "Array function to expand a TabularResult object",
      isAutoAsynchronous = true)
  public static Object[][] expandTabularResult(
      @XLParameter(description = "The TabularResult object handle", name = "tabularResult") final TabularResult result,
      @XLParameter(optional = true, description = "Include Header Row", name = "includeHeader") final Boolean includeHeader) {
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
