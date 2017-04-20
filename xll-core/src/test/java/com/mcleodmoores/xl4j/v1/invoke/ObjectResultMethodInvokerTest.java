/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.InvokerTestHelper;
import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.core.MethodInvoker;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.invoke.ObjectResultMethodInvoker;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveBooleanXLBooleanTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveIntegerArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveIntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.XLValueXLValueTypeConverter;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link ObjectResultMethodInvoker}.
 */
public class ObjectResultMethodInvokerTest {
  private static final Excel EXCEL = ExcelFactory.getInstance();
  private static final TypeConverter INT_CONVERTER = new PrimitiveIntegerXLNumberTypeConverter();
  private static final TypeConverter INT_ARRAY_CONVERTER = new PrimitiveIntegerArrayXLArrayTypeConverter();
  private static final TypeConverter BOOLEAN_CONVERTER = new PrimitiveBooleanXLBooleanTypeConverter();
  private static final TypeConverter OBJECT_CONVERTER = new ObjectXLObjectTypeConverter(EXCEL);
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
      VAR_ARGS_METHOD_2 = InvokerTestHelper.class.getMethod("varArgsMethod2", new Class<?>[] {Integer.TYPE, Integer.TYPE, int[].class});
    } catch (NoSuchMethodException | SecurityException e) {
      throw new XL4JRuntimeException("", e);
    }
  }

  /**
   * Tests the exception when the method is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullMethod() {
    new ObjectResultMethodInvoker(null, new TypeConverter[] {INT_CONVERTER}, BOOLEAN_CONVERTER, OBJECT_CONVERTER);
  }

  /**
   * Tests the exception when the argument converter array is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullArgumentConverters() {
    new ObjectResultMethodInvoker(NO_ARGS_METHOD, null, BOOLEAN_CONVERTER, OBJECT_CONVERTER);
  }

  /**
   * Tests the exception when the return converter is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullReturnConverter() {
    new ObjectResultMethodInvoker(NO_ARGS_METHOD, new TypeConverter[] {INT_ARRAY_CONVERTER}, null, OBJECT_CONVERTER);
  }

  /**
   * Tests the exception when the object converter is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullObjectConverter() {
    new ObjectResultMethodInvoker(NO_ARGS_METHOD, new TypeConverter[] {INT_ARRAY_CONVERTER}, BOOLEAN_CONVERTER, null);
  }

  /**
   * Tests the exception when the arguments are null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullArguments() {
    final MethodInvoker invoker =
        new ObjectResultMethodInvoker(SINGLE_ARG_METHOD, new TypeConverter[] {INT_ARRAY_CONVERTER}, BOOLEAN_CONVERTER, OBJECT_CONVERTER);
    invoker.invoke(null, null);
  }

  /**
   * Checks the metadata stored in the invoker.
   */
  @Test
  public void testMetadata() {
    MethodInvoker invoker = new ObjectResultMethodInvoker(MULTI_ARGS_METHOD, new TypeConverter[] {INT_CONVERTER, INT_CONVERTER},
        BOOLEAN_CONVERTER, OBJECT_CONVERTER);
    assertTrue(invoker.isStatic());
    assertFalse(invoker.isVarArgs());
    assertEquals(invoker.getExcelParameterTypes(), new Class<?>[] {XLNumber.class, XLNumber.class});
    assertEquals(invoker.getExcelReturnType(), XLBoolean.class);
    assertEquals(invoker.getMethodDeclaringClass(), InvokerTestHelper.class);
    assertEquals(invoker.getMethodName(), "multiArgsMethod");
    assertEquals(invoker.getMethodReturnType(), Boolean.TYPE);
    invoker = new ObjectResultMethodInvoker(VAR_ARGS_METHOD_1, new TypeConverter[] {INT_ARRAY_CONVERTER}, BOOLEAN_CONVERTER, OBJECT_CONVERTER);
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
    final Method method = InvokerTestHelper.class.getMethod("voidStaticMethod", new Class<?>[0]);
    final MethodInvoker invoker = new ObjectResultMethodInvoker(method, new TypeConverter[0], BOOLEAN_CONVERTER, OBJECT_CONVERTER);
    final XLValue result = invoker.invoke(null, new XLValue[0]);
    assertEquals(result, XLMissing.INSTANCE);
  }

  /**
   * Tests a no-arg method.
   */
  @Test
  public void testNoArgs() {
    final MethodInvoker invoker = new ObjectResultMethodInvoker(NO_ARGS_METHOD, new TypeConverter[0], BOOLEAN_CONVERTER, OBJECT_CONVERTER);
    final XLValue result = invoker.invoke(null, new XLValue[0]);
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), false);
  }

  /**
   * Tests a method with a single primitive argument.
   */
  @Test
  public void testSingleArg() {
    final MethodInvoker invoker = new ObjectResultMethodInvoker(SINGLE_ARG_METHOD, new TypeConverter[] {INT_CONVERTER},
        BOOLEAN_CONVERTER, OBJECT_CONVERTER);
    XLValue result = invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), true);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(-10)});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), false);
  }

  /**
   * Tests a method with multiple primitive arguments.
   */
  @Test
  public void testMultiArg() {
    final MethodInvoker invoker = new ObjectResultMethodInvoker(MULTI_ARGS_METHOD, new TypeConverter[] {INT_CONVERTER, INT_CONVERTER},
        BOOLEAN_CONVERTER, OBJECT_CONVERTER);
    XLValue result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), true);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(-10), XLNumber.of(20)});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), false);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(-10), XLNumber.of(-20)});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), true);
  }

  /**
   * Tests a method with multiple array arguments.
   */
  @Test
  public void testArrayArgs() {
    final MethodInvoker invoker = new ObjectResultMethodInvoker(ARRAY_ARGS_METHOD, new TypeConverter[] {INT_ARRAY_CONVERTER, INT_ARRAY_CONVERTER},
        BOOLEAN_CONVERTER, OBJECT_CONVERTER);
    XLValue result = invoker.invoke(null, new XLValue[] {
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10), XLNumber.of(20)}}),
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(30), XLNumber.of(40)}})});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), true);
    result = invoker.invoke(null, new XLValue[] {
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10), XLNumber.of(20)}}),
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(30), XLNumber.of(-40)}})});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), false);
    result = invoker.invoke(null, new XLValue[] {XLMissing.INSTANCE, XLMissing.INSTANCE});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), false);
  }

  /**
   * Tests varargs methods.
   */
  @Test
  public void testVarArgs() {
    MethodInvoker invoker = new ObjectResultMethodInvoker(VAR_ARGS_METHOD_1, new TypeConverter[] {INT_ARRAY_CONVERTER},
        BOOLEAN_CONVERTER, OBJECT_CONVERTER);
    // empty array for varargs
    XLValue result = invoker.invoke(null, new XLValue[0]);
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), true);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(10)});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), true);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(-10)});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), false);
    invoker = new ObjectResultMethodInvoker(VAR_ARGS_METHOD_2, new TypeConverter[] {INT_CONVERTER, INT_CONVERTER, INT_ARRAY_CONVERTER},
        BOOLEAN_CONVERTER, OBJECT_CONVERTER);
    // empty array for varargs
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), true);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(-20), XLNumber.of(20), XLNumber.of(30)});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), false);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20), XLNumber.of(-20), XLNumber.of(30)});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), false);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(-10), XLNumber.of(20), XLNumber.of(-20), XLNumber.of(-30)});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), false);
  }

  /**
   * Tests passing a XLMissing (translated to null) into a method.
   * @throws NoSuchMethodException  if the method can't be found
   * @throws SecurityException  if the method can't be called
   */
  @Test
  public void testPassthroughNull() throws NoSuchMethodException, SecurityException {
    final Method method = InvokerTestHelper.class.getMethod("passthroughMethod1", new Class<?>[] {XLValue.class});
    final MethodInvoker invoker = new ObjectResultMethodInvoker(method, new TypeConverter[] {new XLValueXLValueTypeConverter()},
        BOOLEAN_CONVERTER, OBJECT_CONVERTER);
    XLValue result = invoker.invoke(null, new XLValue[] {XLMissing.INSTANCE});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), false);
    result = invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
    assertEquals(EXCEL.getHeap().getObject(((XLObject) result).getHandle()), true);
  }

  /**
   * Tests passing an XLObject that is in the heap.
   * @throws NoSuchMethodException  if the method can't be found
   * @throws SecurityException  if the method can't be called
   */
  @Test
  public void testPassthroughXlObject() throws NoSuchMethodException, SecurityException {
    final Method method = InvokerTestHelper.class.getMethod("passthroughMethod2", new Class<?>[] {XLValue.class});
    final MethodInvoker invoker = new ObjectResultMethodInvoker(method, new TypeConverter[] {new XLValueXLValueTypeConverter()},
        BOOLEAN_CONVERTER, OBJECT_CONVERTER);
    final XLObject heapObject = XLObject.of(Boolean.class, ExcelFactory.getInstance().getHeap().getHandle(Boolean.FALSE));
    XLValue result = invoker.invoke(null, new XLValue[] {heapObject});
    // passthrough - object was already on heap so nothing is done
    assertEquals(result, heapObject);
    // invoke with object not on the heap
    final XLNumber nonHeapObject = XLNumber.of(10);
    result = invoker.invoke(null, new XLValue[] {nonHeapObject});
    assertTrue(result instanceof XLObject);
    assertNotEquals(result, nonHeapObject);
  }
}