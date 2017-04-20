/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.simulator;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.annotations.FunctionType;
import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.simulator.FunctionAttributes;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link FunctionAttributes}.
 */
public class FunctionAttributesTest {
  private static final FunctionType TYPE = FunctionType.FUNCTION;
  private static final boolean ASYNCHRONOUS = false;
  private static final boolean VOLATILE = true;
  private static final boolean MACRO_EQUIVALENT = false;
  private static final boolean MULTI_THREAD_SAFE = false;
  private static final TypeConversionMode MODE = TypeConversionMode.OBJECT_RESULT;

  /**
   * Tests that the function type cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullFunctionType() {
    FunctionAttributes.of(null, ASYNCHRONOUS, VOLATILE, MACRO_EQUIVALENT, MULTI_THREAD_SAFE, MODE);
  }

  /**
   * Tests that the mode cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullConversionMode() {
    FunctionAttributes.of(TYPE, ASYNCHRONOUS, VOLATILE, MACRO_EQUIVALENT, MULTI_THREAD_SAFE, null);
  }

  /**
   * Tests the getters.
   */
  @Test
  public void testGetters() {
    final FunctionAttributes attributes = FunctionAttributes.of(TYPE, ASYNCHRONOUS, VOLATILE, MACRO_EQUIVALENT, MULTI_THREAD_SAFE, MODE);
    assertEquals(attributes.getFunctionType(), TYPE);
    assertEquals(attributes.getResultType(), MODE);
    assertEquals(attributes.isAsynchronous(), ASYNCHRONOUS);
    assertEquals(attributes.isMacroEquivalent(), MACRO_EQUIVALENT);
    assertEquals(attributes.isMultiThreadSafe(), MULTI_THREAD_SAFE);
    assertEquals(attributes.isVolatile(), VOLATILE);
  }
}
