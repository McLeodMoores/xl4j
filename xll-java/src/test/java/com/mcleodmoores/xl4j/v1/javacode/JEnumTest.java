/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;

import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.core.Heap;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;

/**
 * Unit tests for {@link JEnum}.
 */
public class JEnumTest {

  /**
   * Tests the result when the enum doesn't exist.
   */
  @Test
  public void testNoEnumOfName1() {
    final Object result = JEnum.jEnum(XLString.of("org.threeten.bp.DayOfYear"));
    assertSame(result, XLError.Null);
  }

  /**
   * Tests the result when the enum doesn't exist.
   */
  @Test
  public void testNoEnumOfName2() {
    final Object result = JEnum.jEnumX(XLString.of("org.threeten.bp.DayOfYear"));
    assertSame(result, XLError.Null);
  }

  /**
   * Tests JEnum.
   */
  @Test
  public void testJEnum() {
    final Object result = JEnum.jEnum(XLString.of("org.threeten.bp.DayOfWeek"));
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 7);
    assertEquals(values[0].length, 1);
    for (int i = 0; i < 7; i++) {
      assertEquals(((XLString) values[i][0]).getValue(), DayOfWeek.of(i + 1).toString());
    }
  }

  /**
   * Tests JEnumX.
   */
  @Test
  public void testJEnumX() {
    final Heap heap = ExcelFactory.getInstance().getHeap();
    final Object result = JEnum.jEnumX(XLString.of("org.threeten.bp.DayOfWeek"));
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 7);
    assertEquals(values[0].length, 1);
    for (int i = 0; i < 7; i++) {
      assertEquals(heap.getObject(((XLObject) values[i][0]).getHandle()), DayOfWeek.of(i + 1));
    }
  }
}
