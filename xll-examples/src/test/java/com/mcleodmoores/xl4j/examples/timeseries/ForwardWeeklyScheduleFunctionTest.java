/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * Unit tests for {@link ForwardWeeklyScheduleFunction}.
 */
public class ForwardWeeklyScheduleFunctionTest {
  private static final BiFunction<LocalDate, LocalDate, Schedule> FUNCTION = new ForwardWeeklyScheduleFunction();

  /**
   * Tests the function.
   */
  @Test
  public void testSampling() {
    final LocalDate startDate = LocalDate.of(2016, 1, 1);
    LocalDate endDate = LocalDate.of(2017, 1, 1);
    final Schedule schedule1 = FUNCTION.apply(startDate, endDate);
    assertEquals(schedule1.size(), 53);
    assertEquals(schedule1.get(0), startDate);
    assertEquals(schedule1.get(schedule1.size() - 1), LocalDate.of(2016, 12, 30));
    IntStream.range(0, schedule1.size()).forEach(i -> assertEquals(schedule1.get(i).getDayOfWeek(), startDate.getDayOfWeek()));
    endDate = LocalDate.of(2017, 1, 27);
    final Schedule schedule2 = FUNCTION.apply(startDate, endDate);
    assertEquals(schedule2.size(), 57);
    assertEquals(schedule2.get(0), startDate);
    assertEquals(schedule2.get(schedule2.size() - 1), endDate);
    IntStream.range(0, schedule2.size()).forEach(i -> assertEquals(schedule2.get(i).getDayOfWeek(), startDate.getDayOfWeek()));
  }
}
