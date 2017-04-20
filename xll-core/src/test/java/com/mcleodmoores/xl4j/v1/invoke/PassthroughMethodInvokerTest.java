/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.InvokerTestHelper;
import com.mcleodmoores.xl4j.v1.api.core.MethodInvoker;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.invoke.PassthroughMethodInvoker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link PassthroughMethodInvoker}.
 */
public class PassthroughMethodInvokerTest {
  private static final Method NO_ARGS_METHOD;
  private static final Method SINGLE_ARG_METHOD;
  private static final Method MULTI_ARGS_METHOD;
  private static final Method VAR_ARGS_METHOD_1;
  private static final Method VAR_ARGS_METHOD_2;

  static {
    try {
      NO_ARGS_METHOD = InvokerTestHelper.class.getMethod("noArgsXlMethod", new Class<?>[0]);
      SINGLE_ARG_METHOD = InvokerTestHelper.class.getMethod("singleArgXlMethod", new Class<?>[] {XLValue.class});
      MULTI_ARGS_METHOD = InvokerTestHelper.class.getMethod("multiArgsXlMethod", new Class<?>[] {XLString.class, XLNumber.class});
      VAR_ARGS_METHOD_1 = InvokerTestHelper.class.getMethod("varArgsXlMethod1", new Class<?>[] {XLValue[].class});
      VAR_ARGS_METHOD_2 = InvokerTestHelper.class.getMethod("varArgsXlMethod2", new Class<?>[] {XLValue.class, XLValue.class, XLValue[].class});
    } catch (NoSuchMethodException | SecurityException e) {
      throw new XL4JRuntimeException("", e);
    }
  }

  /**
   * Tests the exception when the method is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullMethod() {
    new PassthroughMethodInvoker(null);
  }

  /**
   * Tests the exception when the arguments are null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullArguments() {
    final MethodInvoker invoker = new PassthroughMethodInvoker(SINGLE_ARG_METHOD);
    invoker.invoke(null, null);
  }

  /**
   * Tests the exception when the wrong number of arguments is passed for a varargs constructor.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testWrongArguments() {
    final PassthroughMethodInvoker invoker = new PassthroughMethodInvoker(VAR_ARGS_METHOD_2);
    invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
  }

  /**
   * Checks the metadata stored in the invoker.
   */
  @Test
  public void testMetadata() {
    MethodInvoker invoker = new PassthroughMethodInvoker(MULTI_ARGS_METHOD);
    assertTrue(invoker.isStatic());
    assertFalse(invoker.isVarArgs());
    assertEquals(invoker.getExcelParameterTypes(), new Class<?>[] {XLString.class, XLNumber.class});
    assertEquals(invoker.getExcelReturnType(), XLValue.class);
    assertEquals(invoker.getMethodDeclaringClass(), InvokerTestHelper.class);
    assertEquals(invoker.getMethodName(), "multiArgsXlMethod");
    assertEquals(invoker.getMethodReturnType(), XLValue.class);
    invoker = new PassthroughMethodInvoker(VAR_ARGS_METHOD_1);
    assertTrue(invoker.isStatic());
    assertTrue(invoker.isVarArgs());
    assertEquals(invoker.getExcelParameterTypes(), new Class<?>[] {XLValue[].class});
    assertEquals(invoker.getExcelReturnType(), XLValue.class);
    assertEquals(invoker.getMethodDeclaringClass(), InvokerTestHelper.class);
    assertEquals(invoker.getMethodName(), "varArgsXlMethod1");
    assertEquals(invoker.getMethodReturnType(), XLValue.class);
  }

  /**
   * Tests that a void method returns XLMissing.
   * @throws NoSuchMethodException  if the method can't be found
   */
  @Test
  public void testVoid() throws NoSuchMethodException {
    final Method method = InvokerTestHelper.class.getMethod("voidStaticMethod", new Class<?>[0]);
    final MethodInvoker invoker = new PassthroughMethodInvoker(method);
    final XLValue result = invoker.invoke(null, new XLValue[0]);
    assertEquals(result, XLMissing.INSTANCE);
  }

  /**
   * Tests a no-arg method.
   */
  @Test
  public void testNoArgs() {
    final MethodInvoker invoker = new PassthroughMethodInvoker(NO_ARGS_METHOD);
    final XLValue result = invoker.invoke(null, new XLValue[0]);
    assertEquals(result, XLBoolean.FALSE);
  }

  /**
   * Tests a method with a single primitive argument.
   */
  @Test
  public void testSingleArg() {
    final MethodInvoker invoker = new PassthroughMethodInvoker(SINGLE_ARG_METHOD);
    XLValue result = invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {XLString.of(Integer.toString(-10))});
    assertEquals(result, XLBoolean.FALSE);
  }

  /**
   * Tests a method with multiple arguments.
   */
  @Test
  public void testMultiArg() {
    final MethodInvoker invoker = new PassthroughMethodInvoker(MULTI_ARGS_METHOD);
    XLValue result = invoker.invoke(null, new XLValue[] {XLString.of("10"), XLNumber.of(10)});
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {XLString.of("-10"), XLNumber.of(10)});
    assertEquals(result, XLBoolean.FALSE);
  }


  /**
   * Tests varargs methods.
   */
  @Test
  public void testVarArgs() {
    MethodInvoker invoker = new PassthroughMethodInvoker(VAR_ARGS_METHOD_1);
    // empty array for varargs
    XLValue result = invoker.invoke(null, new XLValue[0]);
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20), XLNumber.of(30)});
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20), XLString.of("30")});
    assertEquals(result, XLBoolean.FALSE);
    invoker = new PassthroughMethodInvoker(VAR_ARGS_METHOD_2);
    // empty array for varargs
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20), XLNumber.of(30), XLNumber.of(40)});
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20), XLString.of("30")});
    assertEquals(result, XLBoolean.FALSE);
  }

}