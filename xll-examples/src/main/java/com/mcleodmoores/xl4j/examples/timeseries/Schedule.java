/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.ArrayList;
import java.util.Collection;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.XLParameter;
import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Class representing a schedule.
 */
public class Schedule extends ArrayList<LocalDate> {
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an empty schedule.
   */
  public Schedule() {
    super();
  }

  /**
   * Constructs a schedule containing the elements of the collection.
   *
   * @param collection
   *          the collection, not null
   */
  public Schedule(final Collection<? extends LocalDate> collection) {
    super(collection);
  }

  /**
   * Constructs an empty schedule with the given capacity.
   *
   * @param initialCapacity
   *          the initial capacity
   */
  public Schedule(final int initialCapacity) {
    super(initialCapacity);
  }

  @Override
  public Schedule subList(final int fromIndex, final int toIndex) {
    return new Schedule(super.subList(fromIndex, toIndex));
  }

  /**
   * Converts a schedule to a 2D array.
   * @param schedule
   *          the schedule, not null
   * @return
   *          the XLArray
   */
  @XLFunction(name = "Schedule.Expand", description = "Converts a schedule to an Excel array")
  public static Object[][] expand(@XLParameter(name = "Schedule", description = "The schedule") final Schedule schedule) {
    ArgumentChecker.notNull(schedule, "schedule");
    final Object[][] result = new Object[schedule.size()][1];
    int i = 0;
    for (final LocalDate date : schedule) {
      result[i++][0] = date;
    }
    return result;
  }

  /**
   * Gets the ith date in a schedule.
   * @param schedule
   *          the schedule, not null
   * @param i
   *          the index, not negative
   * @return
   *          the date
   */
  @XLFunction(name = "Schedule.DateByIndex", description = "Gets the ith value from a time series")
  public static LocalDate dateByIndex(
      @XLParameter(name = "Schedule", description = "The schedule") final Schedule schedule,
      @XLParameter(name = "Index", description = "The index") final int i) {
    ArgumentChecker.notNegative(i, "i");
    return schedule.get(i);
  }
}
