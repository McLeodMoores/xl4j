/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjusters;

/**
 *
 */
public class StartOfYearScheduleFunctionTest {
  private static final BiFunction<LocalDate, LocalDate, Schedule> FUNCTION = new StartOfYearScheduleFunction();

  @Test
  public void testSampling() {
    final LocalDate startDate1 = LocalDate.of(2000, 1, 1);
    final LocalDate endDate = LocalDate.of(2017, 1, 31);
    final List<LocalDate> schedule1 = FUNCTION.apply(startDate1, endDate);
    assertEquals(schedule1.size(), 18);
    assertEquals(schedule1.get(0), startDate1);
    assertEquals(schedule1.get(schedule1.size() - 1), LocalDate.of(2017, 1, 1));
    IntStream.range(0, schedule1.size()).forEach(i -> assertEquals(schedule1.get(i), startDate1.plusYears(i).with(TemporalAdjusters.firstDayOfYear())));
    final LocalDate startDate2 = LocalDate.of(2000, 1, 3);
    final List<LocalDate> schedule2 = FUNCTION.apply(startDate2, endDate);
    assertEquals(schedule2.size(), 17);
    assertEquals(schedule2.get(0), LocalDate.of(2001, 1, 1));
    assertEquals(schedule2.get(schedule2.size() - 1), LocalDate.of(2017, 1, 1));
    IntStream.range(1, schedule1.size()).forEach(i -> assertEquals(schedule1.get(i), startDate2.plusYears(i).with(TemporalAdjusters.firstDayOfYear())));
  }
}
