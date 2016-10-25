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

public class QuandlFunctions {
	private static QuandlSession _session = QuandlSession.create();
	
	@XLFunction(name = "QuandlDataSet", category = "Quandl", description = "Get a data set from Quandl")
	public static TabularResult dataSet(@XLArgument(description = "Quandl Code", name = "quandlCode") 
                                        String quandlCode, 
	                                    @XLArgument(optional=true, description = "Start Date", name = "startDate")
                                        LocalDate startDate, 
	                                    @XLArgument(optional=true, description = "End Date", name = "endDate")
                                        LocalDate endDate, 
	                                    @XLArgument(optional=true, description = "Column Index", name = "columnIndex")
                                        Integer columnIndex,
                                        @XLArgument(optional=true, description = "Frequency", name = "frequency")
                                        Frequency frequency,
                                        @XLArgument(optional=true, description = "Max Rows", name = "maxRows")
                                        Integer maxRows,
                                        @XLArgument(optional=true, description = "Sort Order", name = "sortOrder")
                                     	SortOrder sortOrder,
                                        @XLArgument(optional=true, description = "Transform", name = "transform")
                                        Transform transform) {
		Builder builder = DataSetRequest.Builder.of(quandlCode);
		if (startDate != null) builder = builder.withStartDate(startDate);
		if (endDate != null) builder = builder.withEndDate(endDate);
		if (columnIndex != null) builder = builder.withColumn(columnIndex);
		if (frequency != null) builder = builder.withFrequency(frequency);
		if (maxRows != null) builder = builder.withMaxRows(maxRows);
		if (sortOrder != null) builder = builder.withSortOrder(sortOrder);
		if (transform != null) builder = builder.withTransform(transform);
		return _session.getDataSet(builder.build());
	}
	
	@XLFunction(name = "ExpandTabularResult", category = "Quandl", description = "Array function to expand a TabularResult object")
	public static Object[][] expandTabularResult(
			@XLArgument(description="The TabularResult object handle", name="tabularResult") TabularResult result, 
			@XLArgument(optional=true, description = "Include Header Row", name="includeHeader") Boolean includeHeader) {
		
	  if (includeHeader == null) includeHeader=true;
	  HeaderDefinition headerDefinition = result.getHeaderDefinition();
		int cols = headerDefinition.size();
		int rows = result.size();
		Object[][] values = new Object[rows + (includeHeader ? 1 : 0)][cols];
		int row = 0;
		if (includeHeader) {
			Object[] headerRow = values[0];
			int i = 0;
			for (String columnName : headerDefinition.getColumnNames()) {
				headerRow[i++] = columnName;
			}
			row++;
		}
		Iterator<Row> iterator = result.iterator();
		while (iterator.hasNext()) {
			Row theRow = iterator.next();
			for (int col = 0; col < cols; col++) {
				try {
					values[row][col] = theRow.getDouble(col);
				} catch (NumberFormatException nfe) {
					try {
						values[row][col] = theRow.getLocalDate(col);
					} catch (DateTimeParseException dtpe) {
						values[row][col] = theRow.getString(col);
					}
				}
			}
		    row++;
		}
		return values;
	}
}
