/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import java.util.function.BiFunction;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * Unit tests for {@link DailyScheduleFunction}.
 */
public class DailyScheduleFunctionTest {
  private static final BiFunction<LocalDate, LocalDate, Schedule> FUNCTION = new DailyScheduleFunction();

  /**
   * Tests the function.
   */
  @Test
  public void testSampling() {
    final LocalDate startDate = LocalDate.of(2015, 1, 1);
    final LocalDate endDate = LocalDate.of(2017, 1, 1);
    final Schedule schedule = FUNCTION.apply(startDate, endDate);
    assertEquals(schedule.size(), 365 + 366 + 1);
    assertEquals(schedule.get(0), startDate);
    assertEquals(schedule.get(schedule.size() - 1), endDate);
    for (int i = 0; i < schedule.size(); i++) {
      assertEquals(schedule.get(i), startDate.plusDays(i));
    }
  }
}
