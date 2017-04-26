/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.simulator.MockFunctionProcessor;

/**
 * Unit tests for {@link JUtils}.
 */
public class JUtilsTest {
  private static final MockFunctionProcessor PROCESSOR = MockFunctionProcessor.getInstance();

  /**
   * Tests the "After" function.
   */
  @Test
  public void testAfter() {
    final XLValue before = XLNumber.of(2);
    final XLValue after = XLNumber.of(3);
    final XLValue result = PROCESSOR.invoke("After", before, after);
    assertEquals(result, after);
  }
}
