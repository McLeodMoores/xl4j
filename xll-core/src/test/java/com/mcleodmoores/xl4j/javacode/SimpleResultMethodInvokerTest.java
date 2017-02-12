/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.InvokerTestHelper;
import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.PrimitiveBooleanXLBooleanTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.PrimitiveIntegerArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.PrimitiveIntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.XLValueXLValueTypeConverter;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLMissing;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link SimpleResultMethodInvoker}.
 */
public class SimpleResultMethodInvokerTest {
  private static final TypeConverter INT_CONVERTER = new PrimitiveIntegerXLNumberTypeConverter();
  private static final TypeConverter INT_ARRAY_CONVERTER = new PrimitiveIntegerArrayXLArrayTypeConverter();
  private static final TypeConverter BOOLEAN_CONVERTER = new PrimitiveBooleanXLBooleanTypeConverter();
  private static final Method NO_ARGS_METHOD;
  private static final Method SINGLE_ARG_METHOD;
  private static final Method MULTI_ARGS_METHOD;
  private static final Method ARRAY_ARGS_METHOD;
  private static final Method VAR_ARGS_METHOD_1;
  private static final Method VAR_ARGS_METHOD_2;

  static {
    try {
      NO_ARGS_METHOD = InvokerTestHelper.class.getMethod("noArgsMethod", new Class<?>[0]);
      SINGLE_ARG_METHOD = InvokerTestHelper.class.getMethod("singleArgMethod", new Class<?>[] {Integer.TYPE});
      MULTI_ARGS_METHOD = InvokerTestHelper.class.getMethod("multiArgsMethod", new Class<?>[] {Integer.TYPE, Integer.TYPE});
      ARRAY_ARGS_METHOD = InvokerTestHelper.class.getMethod("arrayArgsMethod", new Class<?>[] {int[].class, int[].class});
      VAR_ARGS_METHOD_1 = InvokerTestHelper.class.getMethod("varArgsMethod1", new Class<?>[] {int[].class});
      VAR_ARGS_METHOD_2 = InvokerTestHelper.class.getMethod("varArgsMethod2", new Class<?>[] {Integer.TYPE, int[].class});
    } catch (NoSuchMethodException | SecurityException e) {
      throw new Excel4JRuntimeException("", e);
    }
  }

  /**
   * Tests the exception when the method is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullMethod() {
    new SimpleResultMethodInvoker(null, new TypeConverter[] {INT_CONVERTER}, BOOLEAN_CONVERTER);
  }

  /**
   * Tests the exception when the argument converter array is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullArgumentConverters() {
    new SimpleResultMethodInvoker(NO_ARGS_METHOD, null, BOOLEAN_CONVERTER);
  }

  /**
   * Tests the exception when the return converter is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullReturnConverter() {
    new SimpleResultMethodInvoker(NO_ARGS_METHOD, new TypeConverter[] {INT_ARRAY_CONVERTER}, null);
  }

  /**
   * Checks the metadata stored in the invoker.
   */
  @Test
  public void testMetadata() {
    MethodInvoker invoker = new SimpleResultMethodInvoker(MULTI_ARGS_METHOD, new TypeConverter[] {INT_CONVERTER, INT_CONVERTER}, BOOLEAN_CONVERTER);
    assertTrue(invoker.isStatic());
    assertFalse(invoker.isVarArgs());
    assertEquals(invoker.getExcelParameterTypes(), new Class<?>[] {XLNumber.class, XLNumber.class});
    assertEquals(invoker.getExcelReturnType(), XLBoolean.class);
    assertEquals(invoker.getMethodDeclaringClass(), InvokerTestHelper.class);
    assertEquals(invoker.getMethodName(), "multiArgsMethod");
    assertEquals(invoker.getMethodReturnType(), Boolean.TYPE);
    invoker = new SimpleResultMethodInvoker(VAR_ARGS_METHOD_1, new TypeConverter[] {INT_ARRAY_CONVERTER}, BOOLEAN_CONVERTER);
    assertTrue(invoker.isStatic());
    assertTrue(invoker.isVarArgs());
    assertEquals(invoker.getExcelParameterTypes(), new Class<?>[] {XLArray.class});
    assertEquals(invoker.getExcelReturnType(), XLBoolean.class);
    assertEquals(invoker.getMethodDeclaringClass(), InvokerTestHelper.class);
    assertEquals(invoker.getMethodName(), "varArgsMethod1");
    assertEquals(invoker.getMethodReturnType(), Boolean.TYPE);
  }

  /**
   * Tests that a void method returns XLMissing.
   * @throws NoSuchMethodException  if the method can't be found
   * @throws SecurityException  if the method can't be called
   */
  @Test
  public void testVoid() throws NoSuchMethodException, SecurityException {
    final Method method = InvokerTestHelper.class.getMethod("voidMethod", new Class<?>[0]);
    final MethodInvoker invoker = new SimpleResultMethodInvoker(method, new TypeConverter[0], BOOLEAN_CONVERTER);
    final XLValue result = invoker.invoke(null, new XLValue[0]);
    assertEquals(result, XLMissing.INSTANCE);
  }

  /**
   * Tests a no-arg method.
   */
  @Test
  public void testNoArgs() {
    final MethodInvoker invoker = new SimpleResultMethodInvoker(NO_ARGS_METHOD, new TypeConverter[0], BOOLEAN_CONVERTER);
    final XLValue result = invoker.invoke(null, new XLValue[0]);
    assertEquals(result, XLBoolean.FALSE);
  }

  /**
   * Tests a method with a single primitive argument.
   */
  @Test
  public void testSingleArg() {
    final MethodInvoker invoker = new SimpleResultMethodInvoker(SINGLE_ARG_METHOD, new TypeConverter[] {INT_CONVERTER}, BOOLEAN_CONVERTER);
    XLValue result = invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(-10)});
    assertEquals(result, XLBoolean.FALSE);
  }

  /**
   * Tests a method with multiple primitive arguments.
   */
  @Test
  public void testMultiArg() {
    final MethodInvoker invoker = new SimpleResultMethodInvoker(MULTI_ARGS_METHOD, new TypeConverter[] {INT_CONVERTER, INT_CONVERTER}, BOOLEAN_CONVERTER);
    XLValue result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(-10), XLNumber.of(20)});
    assertEquals(result, XLBoolean.FALSE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(-10), XLNumber.of(-20)});
    assertEquals(result, XLBoolean.TRUE);
  }

  /**
   * Tests a method with multiple array arguments.
   */
  @Test
  public void testArrayArgs() {
    final MethodInvoker invoker = new SimpleResultMethodInvoker(ARRAY_ARGS_METHOD, new TypeConverter[] {INT_ARRAY_CONVERTER, INT_ARRAY_CONVERTER},
        BOOLEAN_CONVERTER);
    XLValue result = invoker.invoke(null, new XLValue[] {
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10), XLNumber.of(20)}}),
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(30), XLNumber.of(40)}})});
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10), XLNumber.of(20)}}),
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(30), XLNumber.of(-40)}})});
    assertEquals(result, XLBoolean.FALSE);
    result = invoker.invoke(null, new XLValue[] {XLMissing.INSTANCE, XLMissing.INSTANCE});
    assertEquals(result, XLBoolean.FALSE);
  }

  /**
   * Tests varargs methods.
   */
  @Test
  public void testVarArgs() {
    MethodInvoker invoker = new SimpleResultMethodInvoker(VAR_ARGS_METHOD_1, new TypeConverter[] {INT_ARRAY_CONVERTER}, BOOLEAN_CONVERTER);
    // empty array for varargs
    XLValue result = invoker.invoke(null, new XLValue[0]);
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(10)});
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(-10)});
    assertEquals(result, XLBoolean.FALSE);
    invoker = new SimpleResultMethodInvoker(VAR_ARGS_METHOD_2, new TypeConverter[] {INT_CONVERTER, INT_ARRAY_CONVERTER}, BOOLEAN_CONVERTER);
    // empty array for varargs
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20), XLNumber.of(30)});
    assertEquals(result, XLBoolean.TRUE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(-20), XLNumber.of(30)});
    assertEquals(result, XLBoolean.FALSE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(-10), XLNumber.of(-20), XLNumber.of(-30)});
    assertEquals(result, XLBoolean.TRUE);
  }

  /**
   * Tests passing a XLMissing (translated to null) into a method.
   * @throws NoSuchMethodException  if the method can't be found
   * @throws SecurityException  if the method can't be called
   */
  @Test
  public void testPassthroughNull() throws NoSuchMethodException, SecurityException {
    final Method method = InvokerTestHelper.class.getMethod("passthroughMethod1", new Class<?>[] {XLValue.class});
    final MethodInvoker invoker = new SimpleResultMethodInvoker(method, new TypeConverter[] {new XLValueXLValueTypeConverter()}, BOOLEAN_CONVERTER);
    XLValue result = invoker.invoke(null, new XLValue[] {XLMissing.INSTANCE});
    assertEquals(result, XLBoolean.FALSE);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
    assertEquals(result, XLBoolean.TRUE);
  }
}