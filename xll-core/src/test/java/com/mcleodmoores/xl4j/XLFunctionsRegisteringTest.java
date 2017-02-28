/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.javacode.InvokerFactory;
import com.mcleodmoores.xl4j.javacode.ReflectiveInvokerFactory;
import com.mcleodmoores.xl4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.typeconvert.converters.DoubleXLNumberTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.ObjectArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.PrimitiveDoubleArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.PrimitiveIntegerArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.PrimitiveIntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.StringXLStringTypeConverter;

/**
 * Tests registration of methods or constructors annotated with {@link XLFunctions}.
 */
public class XLFunctionsRegisteringTest {
  private static final Excel EXCEL = ExcelFactory.getInstance();
  private static final TypeConverterRegistry TYPE_CONVERTERS = MockTypeConverterRegistry.builder()
      .with(new DoubleXLNumberTypeConverter())
      .with(new StringXLStringTypeConverter())
      .with(new PrimitiveIntegerXLNumberTypeConverter())
      .with(new ObjectArrayXLArrayTypeConverter(EXCEL))
      .with(new PrimitiveIntegerArrayXLArrayTypeConverter())
      .with(new PrimitiveDoubleArrayXLArrayTypeConverter())
      .build();

  /**
   * Tests that abstract classes are not added to the registry.
   */
  @Test
  public void testAbstractClassNotAdded() {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunctions(TestClass5.class)
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 0);
  }

  /**
   * Tests that static methods in abstract classes are added to the registry, but that abstract or instance methods are not.
   */
  @Test
  public void testMethodsInAbstractClass() {
    assertEquals(TestClass6.class.getMethods().length, 12);
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunctions(TestClass6.class)
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    assertEquals(definitions.iterator().next().getFunctionMetadata().getName(), "TestClass6.method1");
  }

  /**
   * Tests that bridge methods are not added. For example, TestClass7 implements Function<Double, Double>, and there is
   * a bridge method (Object apply(Object)) that should not be added to the registry.
   */
  @Test
  public void testBridgeMethodNotAdded() {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunctions(TestClass7.class)
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 2);
  }

  /**
   * Tests that the names have a counter appended for multiple constructors and overloaded methods.
   */
  @Test
  public void testNoNamespace() {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunctions(TestClass1.class)
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 8); // note methods from Object are excluded
    final List<String> expectedNames = new ArrayList<>(Arrays.asList(
        "TestClass1", "TestClass1_$2", "TestClass1_$3", "TestClass1_$4",
        "TestClass1.method1", "TestClass1.method2", "TestClass1.method2_$2", "TestClass1.method2_$3"));
    for (final FunctionDefinition definition : definitions) {
      final String name = definition.getFunctionMetadata().getName();
      assertTrue(expectedNames.contains(name));
      expectedNames.remove(name);
    }
    assertTrue(expectedNames.isEmpty());
  }

  /**
   * Tests that the names have a counter appended for multiple constructors and overloaded methods
   * and that the function names are prepended with the namespace value.
   */
  @Test
  public void testWithPrefix() {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunctions(TestClass2.class)
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 8); // note methods from Object are excluded
    final List<String> expectedNames = new ArrayList<>(Arrays.asList(
        "Prefix2-TestClass2", "Prefix2-TestClass2_$2", "Prefix2-TestClass2_$3", "Prefix2-TestClass2_$4",
        "Prefix2-TestClass2.method1", "Prefix2-TestClass2.method2", "Prefix2-TestClass2.method2_$2", "Prefix2-TestClass2.method2_$3"));
    for (final FunctionDefinition definition : definitions) {
      final String name = definition.getFunctionMetadata().getName();
      assertTrue(expectedNames.contains(name));
      expectedNames.remove(name);
    }
    assertTrue(expectedNames.isEmpty());
  }

  /**
   * Tests that the names have a counter appended for multiple constructors and overloaded methods
   * and that the function names are prepended with the annotation prefix.
   */
  @Test
  public void testWithNamespace() {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunctions(TestClass3.class)
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 4); // note methods from Object are excluded
    final List<String> expectedNames = new ArrayList<>(Arrays.asList(
        "Namespace3-TestClass3", "Namespace3-TestClass3_$2",
        "Namespace3-TestClass3.method", "Namespace3-TestClass3.method_$2"));
    for (final FunctionDefinition definition : definitions) {
      final String name = definition.getFunctionMetadata().getName();
      assertTrue(expectedNames.contains(name));
      expectedNames.remove(name);
    }
    assertTrue(expectedNames.isEmpty());
  }

  /**
   * Tests that the names have a counter appended for multiple constructors and overloaded methods
   * and that the function names are prepended with the namespace value and annotation prefix.
   */
  @Test
  public void testWithNamespaceAndPrefix() {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunctions(TestClass4.class)
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 4); // note methods from Object are excluded
    final List<String> expectedNames = new ArrayList<>(Arrays.asList(
        "Namespace4-Prefix4-TestClass4", "Namespace4-Prefix4-TestClass4_$2",
        "Namespace4-Prefix4-TestClass4.method", "Namespace4-Prefix4-TestClass4.method_$2"));
    for (final FunctionDefinition definition : definitions) {
      final String name = definition.getFunctionMetadata().getName();
      assertTrue(expectedNames.contains(name));
      expectedNames.remove(name);
    }
    assertTrue(expectedNames.isEmpty());
  }

  // CHECKSTYLE:OFF
  @XLFunctions
  public static class TestClass1 {
    public TestClass1() {}
    public TestClass1(final int[] is) {}
    public TestClass1(@XLParameter final double[] ds, @XLParameter final String[] s) {}
    public TestClass1(final int i, final String[] s) {}
    public static int method1() { return 0; }
    public static int method2() { return 1; }
    public static int method2(@XLParameter final String s) { return 3; }
    public static int method2(final double[] ds, final int i, final int[] is) { return 4; }
  }

  @XLFunctions(prefix = "Prefix2-")
  public static class TestClass2 {
    public TestClass2() {}
    public TestClass2(final int[] is) {}
    public TestClass2(@XLParameter final double[] ds, @XLParameter final String[] s) {}
    public TestClass2(final int i, final String[] s) {}
    public static int method1() { return 0; }
    public static int method2() { return 1; }
    public static int method2(@XLParameter final String s) { return 3; }
    public static int method2(final double[] ds, final int i, final int[] is) { return 4; }
  }

  @XLNamespace(value = "Namespace3-")
  @XLFunctions
  public static class TestClass3 {
    public TestClass3() {}
    public TestClass3(final int[] is) {}
    public static int method() { return 0; }
    public static int method(final int i) { return 1; }
  }

  @XLNamespace(value = "Namespace4-")
  @XLFunctions(prefix = "Prefix4-")
  public static class TestClass4 {
    public TestClass4() {}
    public TestClass4(final int[] is) {}
    public static int method() { return 0; }
    public static int method(final int i) { return 1; }
  }

  @XLFunctions
  public abstract static class TestClass5 {
    public TestClass5(final int i) {}
  }

  @XLFunctions
  public abstract static class TestClass6 {
    public TestClass6(final int i) {}
    public static int method1(final int i) { return 1; }
    public int method2(final int i) { return 2; }
    public abstract int method3(int i);
  }

  @XLFunctions
  public static class TestClass7 implements Function<Double, Double> {
    @Override public Double apply(final Double arg0) { return arg0; }
  }
}
