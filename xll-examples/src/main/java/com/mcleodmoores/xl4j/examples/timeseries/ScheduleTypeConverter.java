/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.lang.reflect.Type;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.LocalDateXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Converts a {@link Schedule} to an {@link XLArray} and vice versa.
 */
public class ScheduleTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = 16;
  /** The date converter */
  private static final TypeConverter DATE_CONVERTER = new LocalDateXLNumberTypeConverter();

  /**
   * Default constructor.
   */
  public ScheduleTypeConverter() {
    super(Schedule.class, XLArray.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (!(from instanceof Schedule)) {
      throw new XL4JRuntimeException("\"from\" parameter must be a schedule");
    }
    final Schedule schedule = (Schedule) from;
    if (schedule.size() == 0) {
      return XLArray.of(new XLValue[1][1]);
    }
    final XLValue[][] toArr = new XLValue[schedule.size()][1];
    int i = 0;
    for (final LocalDate date : schedule) {
      toArr[i++][0] = (XLValue) DATE_CONVERTER.toXLValue(date);
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final XLArray xlArr = (XLArray) from;
    final XLValue[][] arr = xlArr.getArray();
    final Schedule schedule = new Schedule();
    final boolean isRow = arr.length == 1;
    final int n = isRow ? arr[0].length : arr.length;
    for (int i = 0; i < n; i++) {
      final XLValue value = isRow ? arr[0][i] : arr[i][0];
      schedule.add((LocalDate) DATE_CONVERTER.toJavaObject(LocalDate.class, value));
    }
    return schedule;
  }
}
