/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.simulator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.FunctionType;
import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link FunctionEntry}.
 */
public class FunctionEntryTest {
  private static final String FUNCTION_NAME = "FUNCTION_NAME";
  private static final String[] ARGUMENT_NAMES = new String[] {"ARGUMENT_NAME1", "ARGUMENT_NAME2", "ARGUMENT_NAME3"};
  private static final Class<?>[] ARGUMENT_TYPES = new Class<?>[] {Double.class, Integer.class, String.class};
  private static final Class<?> RETURN_TYPE = String[].class;
  private static final String[] ARGUMENTS_HELP = new String[] {"ARG_HELP_1", "ARG_HELP_2", "ARG_HELP_3"};
  private static final String DESCRIPTION = "DESCRIPTION";
  private static final FunctionAttributes FUNCTION_ATTRIBUTES =
      FunctionAttributes.of(FunctionType.FUNCTION, false, false, false, true, TypeConversionMode.OBJECT_RESULT);
  private static final Method METHOD;

  static {
    try {
      METHOD = MockDLLExports.class.getMethod("UDF_0", XLValue[].class);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new Excel4JRuntimeException("", e);
    }
  }

  /**
   * Tests that the function name cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullFunctionName() {
    FunctionEntry.of(null, ARGUMENT_NAMES, ARGUMENT_TYPES, RETURN_TYPE, ARGUMENTS_HELP, DESCRIPTION, FUNCTION_ATTRIBUTES, METHOD);
  }

  /**
   * Tests that the argument names cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullArgumentNames() {
    FunctionEntry.of(FUNCTION_NAME, null, ARGUMENT_TYPES, RETURN_TYPE, ARGUMENTS_HELP, DESCRIPTION, FUNCTION_ATTRIBUTES, METHOD);
  }

  /**
   * Tests that the argument types cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullArgumentTypes() {
    FunctionEntry.of(FUNCTION_NAME, ARGUMENT_NAMES, null, RETURN_TYPE, ARGUMENTS_HELP, DESCRIPTION, FUNCTION_ATTRIBUTES, METHOD);
  }

  /**
   * Tests that the return type cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullReturnType() {
    FunctionEntry.of(FUNCTION_NAME, ARGUMENT_NAMES, ARGUMENT_TYPES, null, ARGUMENTS_HELP, DESCRIPTION, FUNCTION_ATTRIBUTES, METHOD);
  }

  /**
   * Tests that the arguments help cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullArgumentsHelp() {
    FunctionEntry.of(FUNCTION_NAME, ARGUMENT_NAMES, ARGUMENT_TYPES, RETURN_TYPE, null, DESCRIPTION, FUNCTION_ATTRIBUTES, METHOD);
  }

  /**
   * Tests that the description cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullDescription() {
    FunctionEntry.of(FUNCTION_NAME, ARGUMENT_NAMES, ARGUMENT_TYPES, RETURN_TYPE, ARGUMENTS_HELP, null, FUNCTION_ATTRIBUTES, METHOD);
  }

  /**
   * Tests that the attributes cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullAttributes() {
    FunctionEntry.of(FUNCTION_NAME, ARGUMENT_NAMES, ARGUMENT_TYPES, RETURN_TYPE, ARGUMENTS_HELP, DESCRIPTION, null, METHOD);
  }

  /**
   * Tests that the method cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullMethod() {
    FunctionEntry.of(FUNCTION_NAME, ARGUMENT_NAMES, ARGUMENT_TYPES, RETURN_TYPE, ARGUMENTS_HELP, DESCRIPTION, FUNCTION_ATTRIBUTES, null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testGetters() {
    final FunctionEntry entry =
        FunctionEntry.of(FUNCTION_NAME, ARGUMENT_NAMES, ARGUMENT_TYPES, RETURN_TYPE, ARGUMENTS_HELP, DESCRIPTION, FUNCTION_ATTRIBUTES, METHOD);
    assertEquals(entry.getArgumentNames(), ARGUMENT_NAMES);
    assertEquals(entry.getArgumentsHelp(), ARGUMENTS_HELP);
    assertEquals(entry.getDescription(), DESCRIPTION);
    assertEquals(entry.getEntryPointMethod(), METHOD);
    assertEquals(entry.getFunctionName(), FUNCTION_NAME);
    assertEquals(entry.getReturnType(), RETURN_TYPE);
    // FunctionAttributes doesn't override equals()
    final FunctionAttributes functionAttributes = entry.getFunctionAttributes();
    assertEquals(functionAttributes.getFunctionType(), FunctionType.FUNCTION);
    assertEquals(functionAttributes.getResultType(), TypeConversionMode.OBJECT_RESULT);
    assertFalse(functionAttributes.isAsynchronous());
    assertFalse(functionAttributes.isMacroEquivalent());
    assertTrue(functionAttributes.isMultiThreadSafe());
    assertFalse(functionAttributes.isVolatile());
  }
}
