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
 * Unit tests for {@link ReverseMonthlyScheduleFunction}.
 */
public class ReverseMonthlyScheduleFunctionTest {
  private static final BiFunction<LocalDate, LocalDate, Schedule> FUNCTION = new ReverseMonthlyScheduleFunction();

  /**
   * Tests the function.
   */
  @Test
  public void testSampling() {
    LocalDate startDate = LocalDate.of(2015, 1, 1);
    final LocalDate endDate = LocalDate.of(2017, 1, 31);
    final Schedule schedule1 = FUNCTION.apply(startDate, endDate);
    assertEquals(schedule1.size(), 25);
    assertEquals(schedule1.get(0), LocalDate.of(2015, 1, 31));
    assertEquals(schedule1.get(schedule1.size() - 1), endDate);
    IntStream.range(0, schedule1.size()).forEach(i -> assertEquals(schedule1.get(i), endDate.minusMonths(schedule1.size() - 1 - i)));
    startDate = LocalDate.of(2014, 12, 31);
    final Schedule schedule2 = FUNCTION.apply(startDate, endDate);
    assertEquals(schedule2.size(), 26);
    assertEquals(schedule2.get(0), startDate);
    assertEquals(schedule2.get(schedule2.size() - 1), endDate);
    IntStream.range(0, schedule2.size()).forEach(i -> assertEquals(schedule2.get(i), endDate.minusMonths(schedule2.size() - 1 - i)));
  }
}
