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
 *
 */
public class EndOfYearScheduleFunctionTest {
  private static final BiFunction<LocalDate, LocalDate, Schedule> FUNCTION = new EndOfYearScheduleFunction();

  @Test
  public void testSampling() {
    final LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2017, 1, 31);
    final Schedule schedule1 = FUNCTION.apply(startDate, endDate);
    assertEquals(schedule1.size(), 17);
    assertEquals(schedule1.get(0), LocalDate.of(2000, 12, 31));
    assertEquals(schedule1.get(schedule1.size() - 1), LocalDate.of(2016, 12, 31));
    IntStream.range(0, schedule1.size()).forEach(i -> assertEquals(schedule1.get(i), startDate.plusYears(i).with(TemporalAdjusters.lastDayOfYear())));
    endDate = LocalDate.of(2016, 12, 31);
    final Schedule schedule2 = FUNCTION.apply(startDate, endDate);
    assertEquals(schedule2.size(), 17);
    assertEquals(schedule2.get(0), LocalDate.of(2000, 12, 31));
    assertEquals(schedule2.get(schedule2.size() - 1), LocalDate.of(2016, 12, 31));
    IntStream.range(0, schedule1.size()).forEach(i -> assertEquals(schedule1.get(i), startDate.plusYears(i).with(TemporalAdjusters.lastDayOfYear())));
  }
}
