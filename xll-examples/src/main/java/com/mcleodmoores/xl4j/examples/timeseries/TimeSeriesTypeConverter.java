/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.lang.reflect.Type;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.DoubleXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.InfNaNXLErrorTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.LocalDateXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Type converter for {@link TimeSeries}.
 */
public class TimeSeriesTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = 7;
  /** The key converter */
  private static final AbstractTypeConverter KEY_CONVERTER = new LocalDateXLNumberTypeConverter();
  /** The value converter */
  private static final AbstractTypeConverter VALUE_CONVERTER = new DoubleXLNumberTypeConverter();
  /** The value converter for cases where the value is NaN or Inf */
  private static final AbstractTypeConverter NAN_INF_CONVERTER = new InfNaNXLErrorTypeConverter();

  /**
   * Constructor.
   */
  public TimeSeriesTypeConverter() {
    super(TimeSeries.class, XLArray.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    final TimeSeries ts = (TimeSeries) from;
    final XLValue[][] toArr = new XLValue[ts.size()][2];
    int i = 0;
    // convert each element of the map with the converters
    for (final Map.Entry<LocalDate, Double> entry : ts.entrySet()) {
      final XLValue key = (XLValue) KEY_CONVERTER.toXLValue(entry.getKey());
      final XLValue value = entry.getValue() == null ? null : (XLValue) VALUE_CONVERTER.toXLValue(entry.getValue());
      toArr[i][0] = key;
      toArr[i][1] = value;
      i++;
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final XLArray xlArr = (XLArray) from;
    final XLValue[][] arr = xlArr.getArray();
    final TimeSeries ts = TimeSeries.newTimeSeries();
    for (final XLValue[] element : arr) {
      final XLValue keyValue = element[0];
      final XLValue valueValue = element[1];
      final LocalDate key = (LocalDate) KEY_CONVERTER.toJavaObject(LocalDate.class, keyValue);
      final Double value;
      if (valueValue == null) {
        value = null;
      } else if (valueValue instanceof XLError) {
        value = (Double) NAN_INF_CONVERTER.toJavaObject(Double.class, valueValue);
      } else {
        value = (Double) VALUE_CONVERTER.toJavaObject(Double.class, valueValue);
      }
      ts.put(key, value);
    }
    return ts;
  }
}
