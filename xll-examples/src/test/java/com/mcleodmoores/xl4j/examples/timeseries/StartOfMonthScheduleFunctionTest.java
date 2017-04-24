/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjusters;

/**
 * Unit tests for {@link StartOfMonthScheduleFunction}.
 */
public class StartOfMonthScheduleFunctionTest {
  private static final BiFunction<LocalDate, LocalDate, Schedule> FUNCTION = new StartOfMonthScheduleFunction();

  /**
   * Tests the function.
   */
  @Test
  public void testSampling() {
    final LocalDate startDate1 = LocalDate.of(2015, 1, 1);
    final LocalDate endDate = LocalDate.of(2017, 1, 31);
    final Schedule schedule1 = FUNCTION.apply(startDate1, endDate);
    assertEquals(schedule1.size(), 25);
    assertEquals(schedule1.get(0), startDate1);
    assertEquals(schedule1.get(schedule1.size() - 1), LocalDate.of(2017, 1, 1));
    IntStream.range(0, schedule1.size()).forEach(i -> assertEquals(schedule1.get(i), startDate1.plusMonths(i).with(TemporalAdjusters.firstDayOfMonth())));
    final LocalDate startDate2 = LocalDate.of(2015, 1, 3);
    final Schedule schedule2 = FUNCTION.apply(startDate2, endDate);
    assertEquals(schedule2.size(), 24);
    assertEquals(schedule2.get(0), LocalDate.of(2015, 2, 1));
    assertEquals(schedule2.get(schedule2.size() - 1), LocalDate.of(2017, 1, 1));
    IntStream.range(1, schedule1.size()).forEach(i -> assertEquals(schedule1.get(i), startDate2.plusMonths(i).with(TemporalAdjusters.firstDayOfMonth())));
  }
}
