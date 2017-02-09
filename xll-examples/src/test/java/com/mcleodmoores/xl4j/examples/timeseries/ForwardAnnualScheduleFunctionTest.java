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

/**
 *
 */
public class ForwardAnnualScheduleFunctionTest {
  private static final BiFunction<LocalDate, LocalDate, List<LocalDate>> FUNCTION = new ForwardAnnualScheduleFunction();

  @Test
  public void testSampling() {
    final LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2017, 1, 31);
    final List<LocalDate> schedule1 = FUNCTION.apply(startDate, endDate);
    assertEquals(schedule1.size(), 18);
    assertEquals(schedule1.get(0), startDate);
    assertEquals(schedule1.get(schedule1.size() - 1), LocalDate.of(2017, 1, 1));
    IntStream.range(0, schedule1.size()).forEach(i -> assertEquals(schedule1.get(i), startDate.plusYears(i)));
    endDate = LocalDate.of(2017, 1, 1);
    final List<LocalDate> schedule2 = FUNCTION.apply(startDate, endDate);
    assertEquals(schedule2.size(), 18);
    assertEquals(schedule2.get(0), startDate);
    assertEquals(schedule2.get(schedule2.size() - 1), LocalDate.of(2017, 1, 1));
    IntStream.range(0, schedule2.size()).forEach(i -> assertEquals(schedule2.get(i), startDate.plusYears(i)));
  }
}
