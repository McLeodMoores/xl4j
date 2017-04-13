/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.ArrayList;
import java.util.Collection;

import org.threeten.bp.LocalDate;

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
}
