/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.reflections.Reflections;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.InvokerTestHelper;
import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.core.ConstructorInvoker;
import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.core.FieldGetter;
import com.mcleodmoores.xl4j.v1.api.core.InvokerFactory;
import com.mcleodmoores.xl4j.v1.api.core.MethodInvoker;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.typeconvert.CachingTypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.typeconvert.ScanningTypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.util.ReflectionsUtils;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link ReflectiveInvokerFactory}.
 */
@SuppressWarnings("unchecked")
public class ReflectiveInvokerFactoryTest {
  private static final Excel EXCEL = ExcelFactory.getInstance();
  private static final Reflections REFLECTIONS = ReflectionsUtils.getReflections();
  private static final TypeConverterRegistry REGISTRY = new CachingTypeConverterRegistry(new ScanningTypeConverterRegistry(EXCEL, REFLECTIONS));
  private static final InvokerFactory FACTORY = new ReflectiveInvokerFactory(EXCEL, REGISTRY);
  private static final Field INT_FIELD;
  private static final Field XL_VALUE_FIELD;
  private static final Method METHOD;
  

  static {
    try {
      INT_FIELD = InvokerTestHelper.class.getField("INT_FIELD");
      XL_VALUE_FIELD = InvokerTestHelper.class.getField("XL_VALUE_FIELD");
      METHOD = InvokerTestHelper.class.getMethod("singleArgMethod", Integer.TYPE);
    } catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
      throw new XL4JRuntimeException("", e);
    }
  }
  
  /**
   * Tests that the Excel factory cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullExcel() {
    new ReflectiveInvokerFactory(null, REGISTRY);
  }

  /**
   * Tests that the registry cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullRegistry() {
    new ReflectiveInvokerFactory(EXCEL, null);
  }

  /**
   * Tests the exception when the field is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullField() {
    FACTORY.getFieldTypeConverter(null, TypeConversionMode.SIMPLEST_RESULT);
  }

  /**
   * Tests the exception when the result type is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullResultTypeForField() {
    FACTORY.getFieldTypeConverter(INT_FIELD, null);
  }

  /**
   * Tests the exception when the class is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullClass1() {
    FACTORY.getConstructorTypeConverter(null, TypeConversionMode.OBJECT_RESULT, XLNumber.class);
  }

  /**
   * Tests the exception when the type conversion mode is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullTypeConversionMode1() {
    FACTORY.getConstructorTypeConverter(InvokerTestHelper.class, null, XLNumber.class);
  }

  /**
   * Tests the exception when the argument array is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullArgumentArray1() {
    FACTORY.getConstructorTypeConverter(InvokerTestHelper.class, TypeConversionMode.OBJECT_RESULT, (Class<? extends XLValue>[]) null);
  }

  /**
   * Tests the exception when the constructor is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullConstructor() {
    FACTORY.getConstructorTypeConverter(null);
  }

  /**
   * Tests the exception when the class is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullClass2() {
    FACTORY.getMethodTypeConverter(null, XLString.of("method"), TypeConversionMode.SIMPLEST_RESULT, XLNumber.class);
  }

  /**
   * Tests the exception when the method name is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullMethodName() {
    FACTORY.getMethodTypeConverter(InvokerTestHelper.class, null, TypeConversionMode.SIMPLEST_RESULT, XLNumber.class);
  }

  /**
   * Tests the exception when the type conversion mode is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullTypeConversionMode2() {
    FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("method"), null, XLNumber.class);
  }

  /**
   * Tests the exception when the argument array is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullArgumentArray2() {
    FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("method"), TypeConversionMode.OBJECT_RESULT, (Class<? extends XLValue>[]) null);
  }

  /**
   * Tests the exception when the method is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullMethod() {
    FACTORY.getMethodTypeConverter(null, TypeConversionMode.OBJECT_RESULT);
  }

  /**
   * Tests the exception when the type conversion mode is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullTypeConversionMode3() {
    FACTORY.getMethodTypeConverter(METHOD, null);
  }

  /**
   * Tests the exception when a suitable constructor cannot be found.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNoAvailableConstructor() {
    FACTORY.getConstructorTypeConverter(TestClass1.class, TypeConversionMode.OBJECT_RESULT, XLArray.class);
  }

  /**
   * Tests the behaviour when a simple result type is requested for a constructor.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testSimpleResultTypeForConstructor1() {
    FACTORY.getConstructorTypeConverter(InvokerTestHelper.class, TypeConversionMode.SIMPLEST_RESULT);
  }

  /**
   * Tests the behaviour when a simple result type is requested for a constructor.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testSimpleResultTypeForConstructor2() {
    FACTORY.getConstructorTypeConverter(InvokerTestHelper.class, TypeConversionMode.SIMPLEST_RESULT, XLNumber.class);
  }

  /**
   * Tests the behaviour when a suitable method cannot be found.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNoAvailableMethod() {
    FACTORY.getMethodTypeConverter(TestClass1.class, XLString.of("method"), TypeConversionMode.SIMPLEST_RESULT, XLNumber.class);
  }

  /**
   * Tests the type of field invoker returned.
   */
  @Test
  public void testFieldInvokerType() {
    FieldGetter invoker = FACTORY.getFieldTypeConverter(INT_FIELD, TypeConversionMode.OBJECT_RESULT);
    assertTrue(invoker instanceof ObjectFieldGetter);
    invoker.get(null); // will fail if there's an issue getting the field
    invoker = FACTORY.getFieldTypeConverter(XL_VALUE_FIELD, TypeConversionMode.PASSTHROUGH);
    assertTrue(invoker instanceof PassthroughFieldGetter);
    invoker.get(null); // will fail if there's an issue getting the field
    invoker = FACTORY.getFieldTypeConverter(INT_FIELD, TypeConversionMode.SIMPLEST_RESULT);
    assertTrue(invoker instanceof ObjectFieldGetter);
    invoker.get(null);
  }

  /**
   * Tests a no-arg constructor.
   */
  @Test
  public void testNoArgConstructor() {
    ConstructorInvoker[] invokers = FACTORY.getConstructorTypeConverter(InvokerTestHelper.class, TypeConversionMode.OBJECT_RESULT, new Class[0]);
    assertEquals(invokers.length, 1);
    assertTrue(invokers[0] instanceof ObjectConstructorInvoker);
    invokers[0].newInstance(new XLValue[0]);
    invokers = FACTORY.getConstructorTypeConverter(InvokerTestHelper.class, TypeConversionMode.PASSTHROUGH, new Class[0]);
    assertEquals(invokers.length, 1);
    assertTrue(invokers[0] instanceof PassthroughConstructorInvoker);
    invokers[0].newInstance(new XLValue[0]);
  }

  /**
   * Tests the types of constructor invoker returned and that they can be successfully invoked.
   */
  @Test
  public void testObjectConstructorInvokers() {
    ConstructorInvoker[] invokers = FACTORY.getConstructorTypeConverter(InvokerTestHelper.class, TypeConversionMode.OBJECT_RESULT, XLNumber.class);
    assertEquals(invokers.length, InvokerTestHelper.class.getConstructors().length);
    // expecting (int), (XLValue), (int...), (int, int...), (XLValue...) constructors
    for (int i = 0; i < invokers.length; i++) {
      final ConstructorInvoker invoker = invokers[i];
      if (i < 2) {
        assertTrue(invoker instanceof ObjectConstructorInvoker);
        assertFalse(invoker.isVarArgs());
        invoker.newInstance(new XLValue[] {XLNumber.of(10)}); // will fail if there was an issue with selecting the constructor
      } else if (i >= invokers.length - 3) {
        assertTrue(invoker instanceof ObjectConstructorInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.newInstance(new XLValue[] {XLNumber.of(10)}); // will fail if there was an issue with selecting the constructor
      } else {
        assertNull(invoker);
      }
    }
    invokers = FACTORY.getConstructorTypeConverter(InvokerTestHelper.class, TypeConversionMode.OBJECT_RESULT,
        XLNumber.class, XLNumber.class, XLNumber.class, XLString.class);
    assertEquals(invokers.length, InvokerTestHelper.class.getConstructors().length);
    // expecting (int...), (int, int, int...), (int, int...), (XLValue...), (XLValue, XLValue, XLValue...) constructors
    for (int i = 0; i < invokers.length; i++) {
      final ConstructorInvoker invoker = invokers[i];
      if (i >= invokers.length - 5) {
        assertTrue(invoker instanceof ObjectConstructorInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.newInstance(new XLValue[] {XLNumber.of(10), XLNumber.of(20), XLNumber.of(30), XLString.of("40")});
      } else {
        assertNull(invoker);
      }
    }
  }

  /**
   * Tests the types of constructor invoker returned and that they can be successfully invoked.
   */
  @Test
  public void testPassthroughConstructorInvokers() {
    ConstructorInvoker[] invokers = FACTORY.getConstructorTypeConverter(InvokerTestHelper.class, TypeConversionMode.PASSTHROUGH,
        XLNumber.class, XLNumber.class);
    assertEquals(invokers.length, InvokerTestHelper.class.getConstructors().length);
    // expecting (XLValue, XLValue), (XLValue...), (XLValue, XLValue, XLValue...) to match
    for (int i = 0; i < invokers.length; i++) {
      final ConstructorInvoker invoker = invokers[i];
      if (i == 0) {
        assertTrue(invoker instanceof PassthroughConstructorInvoker);
        assertFalse(invoker.isVarArgs());
        invoker.newInstance(new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
      } else if (i >= invokers.length - 2) {
        assertTrue(invoker instanceof PassthroughConstructorInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.newInstance(new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
      } else {
        assertNull(invoker);
      }
    }
    invokers = FACTORY.getConstructorTypeConverter(InvokerTestHelper.class, TypeConversionMode.PASSTHROUGH,
        XLNumber.class, XLNumber.class, XLNumber.class, XLString.class);
    assertEquals(invokers.length, InvokerTestHelper.class.getConstructors().length);
    // expecting (XLValue...), (XLValue, XLValue, XLValue...) to match
    for (int i = 0; i < invokers.length; i++) {
      final ConstructorInvoker invoker = invokers[i];
      if (i >= invokers.length - 2) {
        assertTrue(invoker instanceof PassthroughConstructorInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.newInstance(new XLValue[] {XLNumber.of(10), XLNumber.of(20), XLNumber.of(30), XLString.of("40")});
      } else {
        assertNull(invoker);
      }
    }
    invokers = FACTORY.getConstructorTypeConverter(InvokerTestHelper.class, TypeConversionMode.PASSTHROUGH, XLNumber.class);
    assertEquals(invokers.length, InvokerTestHelper.class.getConstructors().length);
    // expecting (XLValue), (XLValue...) to match
    for (int i = 0; i < invokers.length; i++) {
      final ConstructorInvoker invoker = invokers[i];
      if (i == 0) {
        assertTrue(invoker instanceof PassthroughConstructorInvoker);
        assertFalse(invoker.isVarArgs());
        invoker.newInstance(new XLValue[] {XLNumber.of(10)});
      } else if (i == invokers.length - 1) {
        assertTrue(invoker instanceof PassthroughConstructorInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.newInstance(new XLValue[] {XLNumber.of(10)});
      } else {
        assertNull(invoker);
      }
    }
  }

  /**
   * Tests that classes without specific converters can be used as constructor arguments and
   * returned from the invoker.
   * @throws NoSuchMethodException  if the constructor cannot be obtained
   * @throws SecurityException  if the constructor cannot be obtained
   */
  @Test
  public void testSpecificConvertersNotAvailable1() throws NoSuchMethodException, SecurityException {
    final Constructor<TestClass2> constructor = TestClass2.class.getConstructor(TestClass1.class);
    final ConstructorInvoker invoker = FACTORY.getConstructorTypeConverter(constructor);
    assertTrue(invoker instanceof ObjectConstructorInvoker);
    final XLObject xlObject = XLObject.of(TestClass1.class, EXCEL.getHeap().getHandle(new TestClass1()));
    final XLValue[] arguments = new XLValue[] {xlObject};
    final XLValue result = invoker.newInstance(arguments);
    assertTrue(result instanceof XLObject);
  }

  /**
   * Tests a no-arg method.
   */
  @Test
  public void testNoArgMethod() {
    MethodInvoker[] invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("noArgsMethod"),
        TypeConversionMode.OBJECT_RESULT, new Class[0]);
    assertEquals(invokers.length, 1);
    assertTrue(invokers[0] instanceof ObjectResultMethodInvoker);
    invokers[0].invoke(null, new XLValue[0]);
    invokers = FACTORY.getMethodTypeConverter(TestClass2.class, XLString.of("noArgsMethod"),
        TypeConversionMode.PASSTHROUGH, new Class[0]);
    assertEquals(invokers.length, 1);
    assertTrue(invokers[0] instanceof PassthroughMethodInvoker);
    invokers[0].invoke(null, new XLValue[0]);
    invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("noArgsMethod"),
        TypeConversionMode.SIMPLEST_RESULT, new Class[0]);
    assertEquals(invokers.length, 1);
    assertTrue(invokers[0] instanceof SimpleResultMethodInvoker);
    invokers[0].invoke(null, new XLValue[0]);
  }

  /**
   * Tests the types of method invoker returns and that they can be successfully invoked.
   */
  @Test
  public void testObjectResultMethodInvokers() {
    MethodInvoker[] invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("overloadedMethodName"),
        TypeConversionMode.OBJECT_RESULT, XLNumber.class);
    assertEquals(invokers.length, 35);
    // expecting (int), (int...), (int, int...) (XLValue), (XLValue, XLValue...), (XLValue...) methods
    for (int i = 0; i < invokers.length; i++) {
      final MethodInvoker invoker = invokers[i];
      if (i < 2) {
        assertTrue(invoker instanceof ObjectResultMethodInvoker);
        assertFalse(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
      } else if (i >= invokers.length - 4) {
        assertTrue(invoker instanceof ObjectResultMethodInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
      } else {
        assertNull(invoker);
      }
    }
    invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("overloadedMethodName"),
        TypeConversionMode.OBJECT_RESULT, XLNumber.class, XLNumber.class);
    assertEquals(invokers.length, 35);
    // expecting (int, int), (int, int...) (int, int, int...), (int...), (XLValue, XLValue), (XLValue, XLValue...)
    // (XLValue, XLValue, XLValue...), (XLValue...) methods
    for (int i = 0; i < invokers.length; i++) {
      final MethodInvoker invoker = invokers[i];
      if (i < 2) {
        assertTrue(invoker instanceof ObjectResultMethodInvoker);
        assertFalse(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
      } else if (i >= invokers.length - 6) {
        assertTrue(invoker instanceof ObjectResultMethodInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
      } else {
        assertNull(invoker);
      }
    }
    invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("overloadedMethodName"),
        TypeConversionMode.OBJECT_RESULT, XLNumber.class, XLString.class, XLString.class, XLString.class);
    assertEquals(invokers.length, 35);
    // expecting (int, int...) (int, int, int...), (int...), (XLValue, XLValue...)
    // (XLValue, XLValue, XLValue...), (XLValue...) methods
    for (int i = 0; i < invokers.length; i++) {
      final MethodInvoker invoker = invokers[i];
      if (i >= invokers.length - 6) {
        assertTrue(invoker instanceof ObjectResultMethodInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLString.of("20"), XLString.of("30"), XLString.of("40")});
      } else {
        assertNull(invoker);
      }
    }
    invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("overloadedMethodName"),
        TypeConversionMode.OBJECT_RESULT, XLString.class);
    assertEquals(invokers.length, 35);
    // expecting (int), (int...), (int, int...), (XLValue), (XLValue, XLValue...), (XLValue...) methods
    for (int i = 0; i < invokers.length; i++) {
      final MethodInvoker invoker = invokers[i];
      if (i < 2) {
        assertTrue(invoker instanceof ObjectResultMethodInvoker);
        assertFalse(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLString.of("10")});
      } else if (i >= invokers.length - 4) {
        assertTrue(invoker instanceof ObjectResultMethodInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLString.of("10")});
      } else {
        assertNull(invoker);
      }
    }
  }

  /**
   * Tests the types of method invoker returns and that they can be successfully invoked.
   */
  @Test
  public void testSimplestResultMethodInvokers() {
    MethodInvoker[] invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("overloadedMethodName"),
        TypeConversionMode.SIMPLEST_RESULT, XLNumber.class);
    assertEquals(invokers.length, 35);
    // expecting (int), (int...), (int, int...) (XLValue), (XLValue, XLValue...), (XLValue...) methods
    for (int i = 0; i < invokers.length; i++) {
      final MethodInvoker invoker = invokers[i];
      if (i < 2) {
        assertTrue(invoker instanceof SimpleResultMethodInvoker);
        assertFalse(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
      } else if (i >= invokers.length - 4) {
        assertTrue(invoker instanceof SimpleResultMethodInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
      } else {
        assertNull(invoker);
      }
    }
    invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("overloadedMethodName"),
        TypeConversionMode.SIMPLEST_RESULT, XLNumber.class, XLNumber.class);
    assertEquals(invokers.length, 35);
    // expecting (int, int), (int, int...) (int, int, int...), (int...), (XLValue, XLValue), (XLValue, XLValue...)
    // (XLValue, XLValue, XLValue...), (XLValue...) methods
    for (int i = 0; i < invokers.length; i++) {
      final MethodInvoker invoker = invokers[i];
      if (i < 2) {
        assertTrue(invoker instanceof SimpleResultMethodInvoker);
        assertFalse(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
      } else if (i >= invokers.length - 6) {
        assertTrue(invoker instanceof SimpleResultMethodInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
      } else {
        assertNull(invoker);
      }
    }
    invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("overloadedMethodName"),
        TypeConversionMode.SIMPLEST_RESULT, XLNumber.class, XLString.class, XLString.class, XLString.class);
    assertEquals(invokers.length, 35);
    // expecting (int, int...) (int, int, int...), (int...), (XLValue, XLValue...)
    // (XLValue, XLValue, XLValue...), (XLValue...) methods
    for (int i = 0; i < invokers.length; i++) {
      final MethodInvoker invoker = invokers[i];
      if (i >= invokers.length - 6) {
        assertTrue(invoker instanceof SimpleResultMethodInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLString.of("20"), XLString.of("30"), XLString.of("40")});
      } else {
        assertNull(invoker);
      }
    }
    invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("overloadedMethodName"),
        TypeConversionMode.SIMPLEST_RESULT, XLString.class);
    assertEquals(invokers.length, 35);
    // expecting (int), (int...), (int, int...), (XLValue), (XLValue, XLValue...), (XLValue...) methods
    for (int i = 0; i < invokers.length; i++) {
      final MethodInvoker invoker = invokers[i];
      if (i < 2) {
        assertTrue(invoker instanceof SimpleResultMethodInvoker);
        assertFalse(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLString.of("10")});
      } else if (i >= invokers.length - 4) {
        assertTrue(invoker instanceof SimpleResultMethodInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLString.of("10")});
      } else {
        assertNull(invoker);
      }
    }
  }

  /**
   * Tests the types of method invoker returns and that they can be successfully invoked.
   */
  @Test
  public void testPassthroughMethodInvokers() {
    MethodInvoker[] invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("overloadedMethodName"),
        TypeConversionMode.PASSTHROUGH, XLNumber.class);
    assertEquals(invokers.length, 35);
    // expecting (XLValue), (XLValue, XLValue...), (XLValue...) methods
    for (int i = 0; i < invokers.length; i++) {
      final MethodInvoker invoker = invokers[i];
      if (i == 0) {
        assertTrue(invoker instanceof PassthroughMethodInvoker);
        assertFalse(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
      } else if (i >= invokers.length - 2) {
        assertTrue(invoker instanceof PassthroughMethodInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10)});
      } else {
        assertNull(invoker);
      }
    }
    invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("overloadedMethodName"),
        TypeConversionMode.PASSTHROUGH, XLNumber.class, XLNumber.class);
    assertEquals(invokers.length, 35);
    // expecting (XLValue, XLValue), (XLValue, XLValue...), (XLValue, XLValue, XLValue...), (XLValue...) methods
    for (int i = 0; i < invokers.length; i++) {
      final MethodInvoker invoker = invokers[i];
      if (i == 0) {
        assertTrue(invoker instanceof PassthroughMethodInvoker);
        assertFalse(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
      } else if (i >= invokers.length - 3) {
        assertTrue(invoker instanceof PassthroughMethodInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLNumber.of(20)});
      } else {
        assertNull(invoker);
      }
    }
    invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("overloadedMethodName"),
        TypeConversionMode.PASSTHROUGH, XLNumber.class, XLString.class, XLString.class, XLString.class);
    assertEquals(invokers.length, 35);
    // expecting (XLValue, XLValue...) (XLValue, XLValue, XLValue...), (XLValue...) methods
    for (int i = 0; i < invokers.length; i++) {
      final MethodInvoker invoker = invokers[i];
      if (i >= invokers.length - 3) {
        assertTrue(invoker instanceof PassthroughMethodInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLNumber.of(10), XLString.of("20"), XLString.of("30"), XLString.of("40")});
      } else {
        assertNull(invoker);
      }
    }
    invokers = FACTORY.getMethodTypeConverter(InvokerTestHelper.class, XLString.of("overloadedMethodName"),
        TypeConversionMode.PASSTHROUGH, XLString.class);
    assertEquals(invokers.length, 35);
    // expecting (XLValue), (XLValue, XLValue...), (XLValue...) methods
    for (int i = 0; i < invokers.length; i++) {
      final MethodInvoker invoker = invokers[i];
      if (i == 0) {
        assertTrue(invoker instanceof PassthroughMethodInvoker);
        assertFalse(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLString.of("10")});
      } else if (i >= invokers.length - 2) {
        assertTrue(invoker instanceof PassthroughMethodInvoker);
        assertTrue(invoker.isVarArgs());
        invoker.invoke(null, new XLValue[] {XLString.of("10")});
      } else {
        assertNull(invoker);
      }
    }
  }

  /** Tests that methods without specific converters can be used as method arguments and return
   * values.
   * @throws NoSuchMethodException  if the method cannot be obtained
   * @throws SecurityException  if the method cannot be obtained
   */
  @Test
  public void testSpecificConvertersNotAvailable2() throws NoSuchMethodException, SecurityException {
    final XLObject xlObject = XLObject.of(TestClass1.class, EXCEL.getHeap().getHandle(new TestClass1()));
    final XLValue[] arguments = new XLValue[] {xlObject};
    final Method method = TestClass2.class.getMethod("of", TestClass1.class);
    MethodInvoker invoker = FACTORY.getMethodTypeConverter(method, TypeConversionMode.OBJECT_RESULT);
    assertTrue(invoker instanceof ObjectResultMethodInvoker);
    XLValue result = invoker.invoke(null, arguments);
    assertTrue(result instanceof XLObject);
    invoker = FACTORY.getMethodTypeConverter(method, TypeConversionMode.SIMPLEST_RESULT);
    assertTrue(invoker instanceof SimpleResultMethodInvoker);
    result = invoker.invoke(null, arguments);
    assertTrue(result instanceof XLObject);
    invoker = FACTORY.getMethodTypeConverter(method, TypeConversionMode.PASSTHROUGH);
    assertTrue(invoker instanceof PassthroughResultMethodInvoker);
    result = invoker.invoke(null, arguments);
    assertTrue(result instanceof XLObject);
  }

  /**
   * Test class.
   */
  public static class TestClass1 {

    /**
     * No-args constructor.
     */
    public TestClass1() {
    }

    /**
     * Test method.
     * @return  true
     */
    public static boolean method() {
      return true;
    }
  }

  /**
   * Test class.
   */
  public static class TestClass2 {

    /**
     * Constructor.
     * @param type  the type
     */
    public TestClass2(final TestClass1 type) {
    }

    /**
     * Static factory method.
     * @param type  the type
     * @return  the instance
     */
    public static TestClass2 of(final TestClass1 type) {
      return new TestClass2(type);
    }

    /**
     * Test method.
     * @return  false
     */
    public static XLValue noArgsMethod() {
      return XLBoolean.FALSE;
    }
  }

}
