/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.simulator.MockFunctionProcessor;
import com.mcleodmoores.xl4j.v1.util.XlDateUtils;

/**
 * Unit tests for {@link Schedule}.
 */
public class ScheduleTest {
  /** The function processor */
  private static final MockFunctionProcessor PROCESSOR = MockFunctionProcessor.getInstance();

  /**
   * Tests the expand function.
   */
  @Test
  public void testExpand() {
    final Schedule expectedSchedule = new Schedule();
    final int n = 100;
    final XLValue[][] xlArray = new XLValue[n][1];
    for (int i = 0; i < n; i++) {
      final LocalDate date = LocalDate.now().plusDays(i * 7);
      expectedSchedule.add(date);
      xlArray[i][0] = XLNumber.of(XlDateUtils.getDaysFromXlEpoch(date));
    }
    final XLValue[][] result = ((XLArray) PROCESSOR.invoke("Schedule.Expand", XLArray.of(xlArray))).getArray();
    for (int i = 0; i < n; i++) {
      assertEquals(((XLNumber) result[i][0]).getAsDouble(), XlDateUtils.getDaysFromXlEpoch(expectedSchedule.get(i)), 1e-15);
    }
  }

  /**
   * Tests the get by index function.
   */
  @Test
  public void testGetByIndex() {
    final Schedule expectedSchedule = new Schedule();
    final int n = 100;
    final XLValue[][] xlArray = new XLValue[n][1];
    for (int i = 0; i < n; i++) {
      final LocalDate date = LocalDate.now().plusDays(i * 7);
      expectedSchedule.add(date);
      xlArray[i][0] = XLNumber.of(XlDateUtils.getDaysFromXlEpoch(date));
    }
    for (int i = 0; i < n; i++) {
      final XLNumber result = (XLNumber) PROCESSOR.invoke("Schedule.DateByIndex", XLArray.of(xlArray), XLNumber.of(i));
      assertEquals(result.getAsDouble(), XlDateUtils.getDaysFromXlEpoch(expectedSchedule.get(i)), 1e-15);
    }
  }
}
