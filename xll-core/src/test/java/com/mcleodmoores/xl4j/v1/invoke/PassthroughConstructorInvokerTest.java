/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.InvokerTestHelper;
import com.mcleodmoores.xl4j.v1.api.core.ConstructorInvoker;
import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link PassthroughConstructorInvoker}.
 */
public class PassthroughConstructorInvokerTest {
  private static final Excel EXCEL = ExcelFactory.getInstance();
  private static final ObjectXLObjectTypeConverter OBJECT_CONVERTER = new ObjectXLObjectTypeConverter(EXCEL);
  private static final Constructor<?> NO_ARGS_CONSTRUCTOR;
  private static final Constructor<?> SINGLE_ARG_CONSTRUCTOR;
  private static final Constructor<?> MULTI_ARGS_CONSTRUCTOR;
  private static final Constructor<?> VAR_ARGS_CONSTRUCTOR_1;
  private static final Constructor<?> VAR_ARGS_CONSTRUCTOR_2;

  static {
    try {
      NO_ARGS_CONSTRUCTOR = InvokerTestHelper.class.getConstructor(new Class<?>[0]);
      SINGLE_ARG_CONSTRUCTOR = InvokerTestHelper.class.getConstructor(new Class<?>[] {XLValue.class});
      MULTI_ARGS_CONSTRUCTOR = InvokerTestHelper.class.getConstructor(new Class<?>[] {XLValue.class, XLValue.class});
      VAR_ARGS_CONSTRUCTOR_1 = InvokerTestHelper.class.getConstructor(new Class<?>[] {XLValue[].class});
      VAR_ARGS_CONSTRUCTOR_2 = InvokerTestHelper.class.getConstructor(new Class<?>[] {XLValue.class, XLValue.class, XLValue[].class});
    } catch (NoSuchMethodException | SecurityException e) {
      throw new XL4JRuntimeException("", e);
    }
  }

  /**
   * Tests the exception when the constructor is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullConstructor() {
    new PassthroughConstructorInvoker(null, OBJECT_CONVERTER);
  }

  /**
   * Tests the exception when the object converter is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullConverter() {
    new PassthroughConstructorInvoker(NO_ARGS_CONSTRUCTOR, null);
  }

  /**
   * Tests the exception when the arguments are null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullArguments() {
    final ConstructorInvoker invoker = new PassthroughConstructorInvoker(SINGLE_ARG_CONSTRUCTOR, OBJECT_CONVERTER);
    invoker.newInstance(null);
  }

  /**
   * Tests the exception when the wrong number of arguments is passed for a varargs constructor.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testWrongArguments() {
    final ConstructorInvoker invoker = new PassthroughConstructorInvoker(VAR_ARGS_CONSTRUCTOR_2, OBJECT_CONVERTER);
    invoker.newInstance(new XLValue[] {XLNumber.of(10)});
  }

  /**
   * Checks the metadata stored in the invoker.
   */
  @Test
  public void testMetadata() {
    ConstructorInvoker invoker = new PassthroughConstructorInvoker(MULTI_ARGS_CONSTRUCTOR, OBJECT_CONVERTER);
    assertFalse(invoker.isVarArgs());
    assertEquals(invoker.getDeclaringClass(), InvokerTestHelper.class);
    assertEquals(invoker.getExcelParameterTypes(), new Class<?>[] {XLValue.class, XLValue.class});
    assertEquals(invoker.getExcelReturnType(), XLObject.class);
    invoker = new PassthroughConstructorInvoker(VAR_ARGS_CONSTRUCTOR_1, OBJECT_CONVERTER);
    assertTrue(invoker.isVarArgs());
    assertEquals(invoker.getDeclaringClass(), InvokerTestHelper.class);
    assertEquals(invoker.getExcelParameterTypes(), new Class<?>[] {XLValue[].class});
    assertEquals(invoker.getExcelReturnType(), XLObject.class);
  }

  /**
   * Tests a no-arg constructor.
   */
  @Test
  public void testNoArgs() {
    final ConstructorInvoker invoker = new PassthroughConstructorInvoker(NO_ARGS_CONSTRUCTOR, OBJECT_CONVERTER);
    final XLValue xlResult = invoker.newInstance(new XLValue[0]);
    final InvokerTestHelper result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    assertEquals(result.getInputs(), new ArrayList<>());
  }

  /**
   * Tests a constructor taking a single primitive argument.
   */
  @Test
  public void testSingleArg() {
    final ConstructorInvoker invoker = new PassthroughConstructorInvoker(SINGLE_ARG_CONSTRUCTOR, OBJECT_CONVERTER);
    final XLValue xlResult = invoker.newInstance(new XLValue[] {XLNumber.of(10)});
    final InvokerTestHelper result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    final List<Object> expectedResult = new ArrayList<>();
    expectedResult.add(XLNumber.of(10));
    assertEquals(result.getInputs(), expectedResult);
  }

  /**
   * Tests a constructor taking multiple primitive arguments.
   */
  @Test
  public void testMultiArgs() {
    final ConstructorInvoker invoker = new PassthroughConstructorInvoker(MULTI_ARGS_CONSTRUCTOR, OBJECT_CONVERTER);
    final XLValue xlResult = invoker.newInstance(new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
    final InvokerTestHelper result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    final List<Object> expectedResult = new ArrayList<>();
    expectedResult.add(XLNumber.of(10));
    expectedResult.add(XLNumber.of(20));
    assertEquals(result.getInputs(), expectedResult);
  }

  /**
   * Tests a constructor taking a vararg argument.
   */
  @Test
  public void testVarArgs() {
    final List<Object> expectedResult = new ArrayList<>();
    ConstructorInvoker invoker = new PassthroughConstructorInvoker(VAR_ARGS_CONSTRUCTOR_1, OBJECT_CONVERTER);
    // empty array
    XLValue xlResult = invoker.newInstance(new XLValue[0]);
    InvokerTestHelper result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    expectedResult.add(new int[0]);
    assertEquals(result.getInputs(), expectedResult);
    expectedResult.clear();
    xlResult = invoker.newInstance(new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
    result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    expectedResult.add(new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
    assertEquals(result.getInputs(), expectedResult);
    expectedResult.clear();
    invoker = new PassthroughConstructorInvoker(VAR_ARGS_CONSTRUCTOR_2, OBJECT_CONVERTER);
    // empty array
    xlResult = invoker.newInstance(new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
    result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    expectedResult.add(XLNumber.of(10));
    expectedResult.add(XLNumber.of(20));
    expectedResult.add(new XLValue[0]);
    assertEquals(result.getInputs(), expectedResult);
    expectedResult.clear();
    xlResult = invoker.newInstance(new XLValue[] {XLNumber.of(10), XLNumber.of(20), XLNumber.of(30)});
    result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    expectedResult.add(XLNumber.of(10));
    expectedResult.add(XLNumber.of(20));
    expectedResult.add(new XLValue[] {XLNumber.of(30)});
    assertEquals(result.getInputs(), expectedResult);
  }

  /**
   * Tests passing a XLMissing (translated to null) into a constructor.
   * @throws NoSuchMethodException  if the constructor can't be found
   * @throws SecurityException  if the constructor can't be called
   */
  @Test
  public void testPassthroughNull() throws NoSuchMethodException, SecurityException {
    final List<Object> expectedResult = new ArrayList<>();
    final Constructor<?> constructor = InvokerTestHelper.class.getConstructor(new Class<?>[] {XLValue.class});
    final ConstructorInvoker invoker = new PassthroughConstructorInvoker(constructor, OBJECT_CONVERTER);
    XLValue xlResult = invoker.newInstance(new XLValue[] {XLMissing.INSTANCE});
    InvokerTestHelper result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    expectedResult.add(XLMissing.INSTANCE);
    assertEquals(result.getInputs(), expectedResult);
    expectedResult.clear();
    xlResult = invoker.newInstance(new XLValue[] {XLNumber.of(10)});
    result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    expectedResult.add(XLNumber.of(10));
    assertEquals(result.getInputs(), expectedResult);
  }
}
