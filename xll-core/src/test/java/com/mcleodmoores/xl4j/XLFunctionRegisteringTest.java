/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.javacode.InvokerFactory;
import com.mcleodmoores.xl4j.javacode.JavaTypeForFunction;
import com.mcleodmoores.xl4j.javacode.ObjectConstructorInvoker;
import com.mcleodmoores.xl4j.javacode.ReflectiveInvokerFactory;
import com.mcleodmoores.xl4j.javacode.SimpleResultMethodInvoker;
import com.mcleodmoores.xl4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.typeconvert.converters.ObjectArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.PrimitiveDoubleArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.PrimitiveIntegerArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.PrimitiveIntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.StringXLStringTypeConverter;

/**
 * Tests registration of methods or constructors annotated with {@link XLFunction}.
 */
public class XLFunctionRegisteringTest {
  private static final Excel EXCEL = ExcelFactory.getInstance();
  private static final TypeConverterRegistry TYPE_CONVERTERS = MockTypeConverterRegistry.builder()
      .with(new StringXLStringTypeConverter())
      .with(new PrimitiveIntegerXLNumberTypeConverter())
      .with(new ObjectArrayXLArrayTypeConverter(EXCEL))
      .with(new PrimitiveIntegerArrayXLArrayTypeConverter())
      .with(new PrimitiveDoubleArrayXLArrayTypeConverter())
      .build();

  /**
   * Tests that the name is created by using the class name if this is the only / first constructor
   * without the name set in the annotation.
   * @throws NoSuchMethodException  if the constructor cannot be found
   * @throws SecurityException  if the constructor cannot be found
   */
  @Test
  public void testRegisterConstructor() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass1.class.getConstructor(new Class<?>[0]))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition.getConstructorInvoker() instanceof ObjectConstructorInvoker);
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.CONSTRUCTOR);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertFalse(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "TestClass1");
  }

  /**
   * Tests that the name in the annotation is used if available.
   * @throws NoSuchMethodException  if the constructor cannot be found
   * @throws SecurityException  if the constructor cannot be found
   */
  @Test
  public void testRegisterNamedConstructor() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass1.class.getConstructor(new Class<?>[] {int[].class}))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition.getConstructorInvoker() instanceof ObjectConstructorInvoker);
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.CONSTRUCTOR);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertFalse(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "Constructor1_1");
  }

  /**
   * Test that unnamed parameters do not have a name generated.
   * @throws NoSuchMethodException  if the constructor cannot be found
   * @throws SecurityException  if the constructor cannot be found
   */
  @Test
  public void testRegisterUnnamedParametersForConstructor() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass1.class.getConstructor(new Class<?>[] {double[].class, String[].class}))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition.getConstructorInvoker() instanceof ObjectConstructorInvoker);
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.CONSTRUCTOR);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertFalse(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "Constructor1_2");
    final XLParameter[] parameters = metadata.getParameters();
    assertEquals(parameters.length, 2);
    assertEquals(parameters[0].name(), "");
    assertEquals(parameters[1].name(), "");
  }

  /**
   * Tests that the names in the parameter annotation are used.
   * @throws NoSuchMethodException  if the constructor cannot be found
   * @throws SecurityException  if the constructor cannot be found
   */
  @Test
  public void testRegisterNamedParametersForConstructor() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass1.class.getConstructor(new Class<?>[] {Integer.TYPE, String[].class}))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition.getConstructorInvoker() instanceof ObjectConstructorInvoker);
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.CONSTRUCTOR);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertFalse(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "Constructor1_3");
    final XLParameter[] parameters = metadata.getParameters();
    assertEquals(parameters.length, 2);
    assertEquals(parameters[0].name(), "param1_1");
    assertEquals(parameters[1].name(), "param1_2");
  }

  /**
   * Tests that the name is created by using the class and method name if this is the only / first method
   * without the name set in the annotation.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testRegisterMethod() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass1.class.getMethod("method1", new Class<?>[0]))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition.getMethodInvoker() instanceof SimpleResultMethodInvoker);
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.METHOD);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertFalse(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "TestClass1.method1");
  }

  /**
   * Tests that the name in the annotation is used if available.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testRegisterNamedMethod() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass1.class.getMethod("method2", new Class<?>[0]))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition.getMethodInvoker() instanceof SimpleResultMethodInvoker);
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.METHOD);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertFalse(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "Method1_1");
  }

  /**
   * Test that unnamed parameters do not have a name generated.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testRegisterUnnamedParametersForMethod() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass1.class.getMethod("method3", new Class<?>[] {String.class}))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition.getMethodInvoker() instanceof SimpleResultMethodInvoker);
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.METHOD);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertFalse(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "Method1_3");
    final XLParameter[] parameters = metadata.getParameters();
    assertEquals(parameters.length, 1);
    assertEquals(parameters[0].name(), "");
  }

  /**
   * Tests that the names in the parameter annotation are used.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testRegisterNamedParametersForMethod() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass1.class.getMethod("method4", new Class<?>[] {double[].class, Integer.TYPE, int[].class}))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition.getMethodInvoker() instanceof SimpleResultMethodInvoker);
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.METHOD);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertFalse(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "Method1_4");
    final XLParameter[] parameters = metadata.getParameters();
    assertEquals(parameters.length, 3);
    assertEquals(parameters[0].name(), "param1_3");
    assertEquals(parameters[1].name(), "param1_4");
    assertNull(parameters[2]);
  }

  /**
   * Tests that the same constructor names are used when the names aren't set in the function annotations.
   * This means that only one of these constructors will eventually be available.
   * @throws NoSuchMethodException  if the constructor cannot be found
   * @throws SecurityException  if the constructor cannot be found
   */
  @Test
  public void testUnnamedConstructors() throws NoSuchMethodException, SecurityException {
    MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass2.class.getConstructor(new Class<?>[0]))
        .build();
    InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "TestClass2");
    registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass2.class.getConstructor(new Class<?>[] {String.class}))
        .build();
    invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "TestClass2");
    registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass2.class.getConstructor(new Class<?>[] {String[].class}))
        .build();
    invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "TestClass2");
  }

  /**
   * Tests that overloaded method names. Only of of these methods will eventually be available because
   * the generated names will be the same.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testUnnamedMethods() throws NoSuchMethodException, SecurityException {
    MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass2.class.getMethod("method", new Class<?>[] {Integer.TYPE}))
        .build();
    InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "TestClass2.method");
    registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass2.class.getMethod("method", new Class<?>[] {String.class}))
        .build();
    invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "TestClass2.method");
    registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass2.class.getMethod("method", new Class<?>[] {String[].class}))
        .build();
    invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "TestClass2.method");
  }

  /**
   * Tests that the name is created by using the namespace annotation and class name.
   * @throws NoSuchMethodException  if the constructor cannot be found
   * @throws SecurityException  if the constructor cannot be found
   */
  @Test
  public void testRegisterConstructorWithNamespace() throws NoSuchMethodException, SecurityException {
    MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass3.class.getConstructor(new Class<?>[0]))
        .build();
    InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "Namespace3-TestClass3");
    registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass3.class.getConstructor(new Class<?>[] {int[].class}))
        .build();
    invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "Namespace3-Constructor3_1");
  }

  /**
   * Tests that the name is created by using the namespace annotation and method name.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testRegisterMethodWithNamespace() throws NoSuchMethodException, SecurityException {
    MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass3.class.getMethod("method1", new Class<?>[0]))
        .build();
    InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "Namespace3-TestClass3.method1");
    registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass3.class.getMethod("method2", new Class<?>[0]))
        .build();
    invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "Namespace3-Method3_1");
  }

  /**
   * Tests that the same constructor names are used when the names aren't set in the function annotations.
   * This means that only one of these constructors will eventually be available.
   * @throws NoSuchMethodException  if the constructor cannot be found
   * @throws SecurityException  if the constructor cannot be found
   */
  @Test
  public void testUnnamedConstructorsWithNamespace() throws NoSuchMethodException, SecurityException {
    MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass4.class.getConstructor(new Class<?>[0]))
        .build();
    InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "Namespace4-TestClass4");
    registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass4.class.getConstructor(new Class<?>[] {String.class}))
        .build();
    invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "Namespace4-TestClass4");
  }

  /**
   * Tests that the same constructor names are used when the names aren't set in the function annotations.
   * This means that only of of these methods will eventually be available.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testUnnamedMethodsWithNamespace() throws NoSuchMethodException, SecurityException {
    MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass4.class.getMethod("method", new Class<?>[] {Integer.TYPE}))
        .build();
    InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "Namespace4-TestClass4.method");
    registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass4.class.getMethod("method", new Class<?>[] {String.class}))
        .build();
    invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "Namespace4-TestClass4.method");
  }

  // CHECKSTYLE:OFF
  public static class TestClass1 {
    @XLFunction() public TestClass1() {}
    @XLFunction(name = "Constructor1_1") public TestClass1(final int[] is) {}
    @XLFunction(name = "Constructor1_2") public TestClass1(@XLParameter final double[] ds, @XLParameter final String[] s) {}
    @XLFunction(name = "Constructor1_3") public TestClass1(@XLParameter(name = "param1_1") final int i, @XLParameter(name = "param1_2") final String[] s) {}
    @XLFunction public static int method1() { return 0; }
    @XLFunction(name = "Method1_1") public static int method2() { return 1; }
    @XLFunction(name = "Method1_3") public static int method3(@XLParameter final String s) { return 3; }
    @XLFunction(name = "Method1_4") public static int method4(@XLParameter(name = "param1_3") final double[] ds, @XLParameter(name = "param1_4") final int i, final int[] is) { return 4; }
  }

  public static class TestClass2 {
    @XLFunction public TestClass2() {}
    @XLFunction public TestClass2(final String s) {}
    @XLFunction public TestClass2(final String[] ss) {}
    @XLFunction public static int method(final int i) { return 1; }
    @XLFunction public static int method(final String s) { return 2; }
    @XLFunction public static int method(final String[] ss) { return 2; }
  }

  @XLNamespace(value = "Namespace3-")
  public static class TestClass3 {
    @XLFunction() public TestClass3() {}
    @XLFunction(name = "Constructor3_1") public TestClass3(final int[] is) {}
    @XLFunction public static int method1() { return 0; }
    @XLFunction(name = "Method3_1") public static int method2() { return 1; }
  }

  @XLNamespace(value = "Namespace4-")
  public static class TestClass4 {
    @XLFunction public TestClass4() {}
    @XLFunction public TestClass4(final String s) {}
    @XLFunction public static int method(final int i) { return 1; }
    @XLFunction public static int method(final String s) { return 2; }
  }
}
