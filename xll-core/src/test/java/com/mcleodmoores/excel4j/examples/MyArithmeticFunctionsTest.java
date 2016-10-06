/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.examples;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.simulator.MockFunctionProcessor;
import com.mcleodmoores.excel4j.values.XLNumber;

/**
 * Tests for the example functions in {@link com.mcleodmoores.excel4j.examples.MyArithmeticFunctions}.
 */
public class MyArithmeticFunctionsTest {
  /** The function processor */
  private MockFunctionProcessor _processor;

  /**
   * Initializes the function processor before the tests are run.
   */
  @BeforeTest
  public void init() {
    _processor = MockFunctionProcessor.getInstance();
  }

  /**
   * Tests addition.
   */
  @Test
  public void testMyAdd() {
    long result = 0;
    for (int i = 1; i <= 100; i++) {
      result = ((XLNumber) _processor.invoke("MyAdd", XLNumber.of(result), XLNumber.of(i))).getAsLong();
    }
    assertEquals(result, 5050L);
  }

  /**
   * Tests subtraction.
   */
  @Test
  public void testMySubtract() {
    long result = 0;
    for (int i = 1; i <= 100; i++) {
      result = ((XLNumber) _processor.invoke("MySubtract", XLNumber.of(result), XLNumber.of(i))).getAsLong();
    }
    assertEquals(result, -5050L);
  }

  /**
   * Tests multiplication.
   */
  @Test
  public void testMyMultiply() {
    long result = 1;
    for (int i = 1; i <= 10; i++) {
      result = ((XLNumber) _processor.invoke("MyMultiply", XLNumber.of(result), XLNumber.of(i))).getAsLong();
    }
    assertEquals(result, 3628800L);
  }

  /**
   * Tests division.
   */
  @Test
  public void testMyDivision() {
    double result = 1;
    for (int i = 1; i <= 10; i++) {
      result = ((XLNumber) _processor.invoke("MyDivide", XLNumber.of(result), XLNumber.of(i))).getValue();
    }
    assertEquals(result, 1 / 3628800., 1e-12);
  }

  /**
   * Tests factorial calculations.
   */
  @Test
  public void testMyFactorial() {
    final long result = ((XLNumber) _processor.invoke("MyFactorial", XLNumber.of(10))).getAsLong();
    assertEquals(result, 3628800L);
  }
}
