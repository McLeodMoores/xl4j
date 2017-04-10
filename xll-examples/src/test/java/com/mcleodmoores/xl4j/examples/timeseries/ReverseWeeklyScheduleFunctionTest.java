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
 *
 */
public class ReverseWeeklyScheduleFunctionTest {
  private static final BiFunction<LocalDate, LocalDate, Schedule> FUNCTION = new ReverseWeeklyScheduleFunction();

  @Test
  public void testSampling() {
    LocalDate startDate = LocalDate.of(2016, 1, 1);
    final LocalDate endDate = LocalDate.of(2017, 1, 1);
    final Schedule schedule1 = FUNCTION.apply(startDate, endDate);
    assertEquals(schedule1.size(), 53);
    assertEquals(schedule1.get(0), LocalDate.of(2016, 1, 3));
    assertEquals(schedule1.get(schedule1.size() - 1), endDate);
    IntStream.range(0, schedule1.size()).forEach(i -> assertEquals(schedule1.get(i).getDayOfWeek(), endDate.getDayOfWeek()));
    startDate = LocalDate.of(2016, 1, 3);
    final Schedule schedule2 = FUNCTION.apply(startDate, endDate);
    assertEquals(schedule2.size(), 53);
    assertEquals(schedule2.get(0), startDate);
    assertEquals(schedule2.get(schedule2.size() - 1), endDate);
    IntStream.range(0, schedule2.size()).forEach(i -> assertEquals(schedule2.get(i).getDayOfWeek(), endDate.getDayOfWeek()));
  }
}
