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
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.invoke.ObjectConstructorInvoker;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveIntegerArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveIntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.XLValueXLValueTypeConverter;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link ObjectConstructorInvoker}.
 */
public class ObjectConstructorInvokerTest {
  private static final Excel EXCEL = ExcelFactory.getInstance();
  private static final TypeConverter INT_CONVERTER = new PrimitiveIntegerXLNumberTypeConverter();
  private static final TypeConverter INT_ARRAY_CONVERTER = new PrimitiveIntegerArrayXLArrayTypeConverter();
  private static final TypeConverter OBJECT_CONVERTER = new ObjectXLObjectTypeConverter(EXCEL);
  private static final Constructor<?> NO_ARGS_CONSTRUCTOR;
  private static final Constructor<?> SINGLE_ARG_CONSTRUCTOR;
  private static final Constructor<?> MULTI_ARGS_CONSTRUCTOR;
  private static final Constructor<?> ARRAY_ARGS_CONSTRUCTOR;
  private static final Constructor<?> VAR_ARGS_CONSTRUCTOR_1;
  private static final Constructor<?> VAR_ARGS_CONSTRUCTOR_2;

  static {
    try {
      NO_ARGS_CONSTRUCTOR = InvokerTestHelper.class.getConstructor(new Class<?>[0]);
      SINGLE_ARG_CONSTRUCTOR = InvokerTestHelper.class.getConstructor(new Class<?>[] {Integer.TYPE});
      MULTI_ARGS_CONSTRUCTOR = InvokerTestHelper.class.getConstructor(new Class<?>[] {Integer.TYPE, Integer.TYPE});
      ARRAY_ARGS_CONSTRUCTOR = InvokerTestHelper.class.getConstructor(new Class<?>[] {int[].class, int[].class});
      VAR_ARGS_CONSTRUCTOR_1 = InvokerTestHelper.class.getConstructor(new Class<?>[] {int[].class});
      VAR_ARGS_CONSTRUCTOR_2 = InvokerTestHelper.class.getConstructor(new Class<?>[] {Integer.TYPE, int[].class});
    } catch (NoSuchMethodException | SecurityException e) {
      throw new XL4JRuntimeException("", e);
    }
  }

  /**
   * Tests the exception when the constructor is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullConstructor() {
    new ObjectConstructorInvoker(null, new TypeConverter[] {INT_ARRAY_CONVERTER}, OBJECT_CONVERTER, OBJECT_CONVERTER);
  }

  /**
   * Tests the exception when the argument converters are null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullArgumentConverters() {
    new ObjectConstructorInvoker(SINGLE_ARG_CONSTRUCTOR, null, OBJECT_CONVERTER, OBJECT_CONVERTER);
  }

  /**
   * Tests the exception when the return converter is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullReturnConverter() {
    new ObjectConstructorInvoker(SINGLE_ARG_CONSTRUCTOR, new TypeConverter[] {INT_CONVERTER}, null, OBJECT_CONVERTER);
  }

  /**
   * Tests the exception when the return converter is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullObjectConverter() {
    new ObjectConstructorInvoker(SINGLE_ARG_CONSTRUCTOR, new TypeConverter[] {INT_CONVERTER}, OBJECT_CONVERTER, null);
  }

  /**
   * Tests the exception when the arguments are null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullArguments() {
    final ConstructorInvoker invoker = new ObjectConstructorInvoker(SINGLE_ARG_CONSTRUCTOR,
        new TypeConverter[] {INT_CONVERTER}, OBJECT_CONVERTER, OBJECT_CONVERTER);
    invoker.newInstance(null);
  }

  /**
   * Tests the exception when the wrong number of arguments is passed for a varargs constructor.
   * @throws SecurityException  if the constructor could not be instantiated
   * @throws NoSuchMethodException  if the constructor does not exist
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testWrongArguments() throws NoSuchMethodException, SecurityException {
    final Constructor<?> constructor = InvokerTestHelper.class.getConstructor(new Class<?>[] {Integer.TYPE, Integer.TYPE, int[].class});
    final ConstructorInvoker invoker = new ObjectConstructorInvoker(constructor, new TypeConverter[] {INT_CONVERTER, INT_ARRAY_CONVERTER},
        OBJECT_CONVERTER, OBJECT_CONVERTER);
    invoker.newInstance(new XLValue[] {XLNumber.of(10)});
  }

  /**
   * Checks the metadata stored in the invoker.
   */
  @Test
  public void testMetadata() {
    ConstructorInvoker invoker = new ObjectConstructorInvoker(MULTI_ARGS_CONSTRUCTOR, new TypeConverter[] {INT_CONVERTER, INT_CONVERTER},
        OBJECT_CONVERTER, OBJECT_CONVERTER);
    assertFalse(invoker.isVarArgs());
    assertEquals(invoker.getDeclaringClass(), InvokerTestHelper.class);
    assertEquals(invoker.getExcelParameterTypes(), new Class<?>[] {XLNumber.class, XLNumber.class});
    assertEquals(invoker.getExcelReturnType(), XLObject.class);
    invoker = new ObjectConstructorInvoker(VAR_ARGS_CONSTRUCTOR_1, new TypeConverter[] {INT_ARRAY_CONVERTER}, OBJECT_CONVERTER, OBJECT_CONVERTER);
    assertTrue(invoker.isVarArgs());
    assertEquals(invoker.getDeclaringClass(), InvokerTestHelper.class);
    assertEquals(invoker.getExcelParameterTypes(), new Class<?>[] {XLArray.class});
    assertEquals(invoker.getExcelReturnType(), XLObject.class);
  }

  /**
   * Tests a no-arg constructor.
   */
  @Test
  public void testNoArgs() {
    final ConstructorInvoker invoker = new ObjectConstructorInvoker(NO_ARGS_CONSTRUCTOR, new TypeConverter[0], OBJECT_CONVERTER, OBJECT_CONVERTER);
    final XLValue xlResult = invoker.newInstance(new XLValue[0]);
    final InvokerTestHelper result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    assertEquals(result.getInputs(), new ArrayList<>());
  }

  /**
   * Tests a constructor taking a single primitive argument.
   */
  @Test
  public void testSingleArg() {
    final ConstructorInvoker invoker = new ObjectConstructorInvoker(SINGLE_ARG_CONSTRUCTOR,
        new TypeConverter[] {INT_CONVERTER}, OBJECT_CONVERTER, OBJECT_CONVERTER);
    final XLValue xlResult = invoker.newInstance(new XLValue[] {XLNumber.of(10)});
    final InvokerTestHelper result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    final List<Object> expectedResult = new ArrayList<>();
    expectedResult.add(10);
    assertEquals(result.getInputs(), expectedResult);
  }

  /**
   * Tests a constructor taking multiple primitive arguments.
   */
  @Test
  public void testMultiArgs() {
    final ConstructorInvoker invoker = new ObjectConstructorInvoker(MULTI_ARGS_CONSTRUCTOR, new TypeConverter[] {INT_CONVERTER, INT_CONVERTER},
        OBJECT_CONVERTER, OBJECT_CONVERTER);
    final XLValue xlResult = invoker.newInstance(new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
    final InvokerTestHelper result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    final List<Object> expectedResult = new ArrayList<>();
    expectedResult.add(10);
    expectedResult.add(20);
    assertEquals(result.getInputs(), expectedResult);
  }

  /**
   * Tests a constructor taking multiple array arguments.
   */
  @Test
  public void testArrayArgs() {
    final ConstructorInvoker invoker = new ObjectConstructorInvoker(ARRAY_ARGS_CONSTRUCTOR,
        new TypeConverter[] {INT_ARRAY_CONVERTER, INT_ARRAY_CONVERTER}, OBJECT_CONVERTER, OBJECT_CONVERTER);
    final XLValue xlResult = invoker.newInstance(new XLValue[] {
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10), XLNumber.of(20)}}),
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(30), XLNumber.of(40)}})});
    final InvokerTestHelper result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    final List<Object> expectedResult = new ArrayList<>();
    expectedResult.add(new int[] {10, 20});
    expectedResult.add(new int[] {30, 40});
    assertEquals(result.getInputs(), expectedResult);
  }

  /**
   * Tests a constructor taking a vararg argument.
   */
  @Test
  public void testVarArgs() {
    final List<Object> expectedResult = new ArrayList<>();
    ConstructorInvoker invoker = new ObjectConstructorInvoker(VAR_ARGS_CONSTRUCTOR_1, new TypeConverter[] {INT_ARRAY_CONVERTER},
        OBJECT_CONVERTER, OBJECT_CONVERTER);
    // empty array
    XLValue xlResult = invoker.newInstance(new XLValue[0]);
    InvokerTestHelper result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    expectedResult.add(new int[0]);
    assertEquals(result.getInputs(), expectedResult);
    expectedResult.clear();
    xlResult = invoker.newInstance(new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
    result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    expectedResult.add(new int[] {10, 20});
    assertEquals(result.getInputs(), expectedResult);
    expectedResult.clear();
    invoker = new ObjectConstructorInvoker(VAR_ARGS_CONSTRUCTOR_2, new TypeConverter[] {INT_CONVERTER, INT_ARRAY_CONVERTER},
        OBJECT_CONVERTER, OBJECT_CONVERTER);
    // empty array
    xlResult = invoker.newInstance(new XLValue[] {XLNumber.of(10)});
    result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    expectedResult.add(10);
    expectedResult.add(new int[0]);
    assertEquals(result.getInputs(), expectedResult);
    expectedResult.clear();
    xlResult = invoker.newInstance(new XLValue[] {XLNumber.of(10), XLNumber.of(20), XLNumber.of(30)});
    result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    expectedResult.add(10);
    expectedResult.add(new int[] {20, 30});
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
    final ConstructorInvoker invoker =
        new ObjectConstructorInvoker(constructor, new TypeConverter[] {new XLValueXLValueTypeConverter()}, OBJECT_CONVERTER, OBJECT_CONVERTER);
    XLValue xlResult = invoker.newInstance(new XLValue[] {XLMissing.INSTANCE});
    InvokerTestHelper result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    expectedResult.add(null);
    assertEquals(result.getInputs(), expectedResult);
    expectedResult.clear();
    xlResult = invoker.newInstance(new XLValue[] {XLNumber.of(10)});
    result = (InvokerTestHelper) EXCEL.getHeap().getObject(((XLObject) xlResult).getHandle());
    expectedResult.add(XLNumber.of(10));
    assertEquals(result.getInputs(), expectedResult);
  }
}
