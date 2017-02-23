/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.heap.Heap;
import com.mcleodmoores.xl4j.javacode.InvokerFactory;
import com.mcleodmoores.xl4j.javacode.JavaTypeForFunction;
import com.mcleodmoores.xl4j.javacode.ObjectFieldInvoker;
import com.mcleodmoores.xl4j.javacode.ReflectiveInvokerFactory;
import com.mcleodmoores.xl4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.typeconvert.converters.StringXLStringTypeConverter;

/**
 * Tests registration of fields or enums annotated with {@link XLConstant} in {@link AbstractFunctionRegistry}.
 */
public class XLConstantRegisteringTest {
  private static final Excel EXCEL = ExcelFactory.getInstance();
  private static final Heap HEAP = EXCEL.getHeap();
  private static final TypeConverterRegistry TYPE_CONVERTERS = MockTypeConverterRegistry.builder()
      .with(new StringXLStringTypeConverter())
      .build();

  @Test
  public void testRegisterNamedConstant() throws NoSuchFieldException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlConstant(TestClass1.class.getField("FIELD1"))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition.isStatic());
    assertTrue(definition.getFieldInvoker() instanceof ObjectFieldInvoker);
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.FIELD);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertTrue(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "Field1_1");
  }

  @Test
  public void testRegisterConstant() throws NoSuchFieldException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlConstant(TestClass1.class.getField("FIELD2"))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition.isStatic());
    assertTrue(definition.getFieldInvoker() instanceof ObjectFieldInvoker);
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.FIELD);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertTrue(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "TestClass1.FIELD2");
  }

  @Test
  public void testRegisterNamedConstantWithNamespace() throws NoSuchFieldException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlConstant(TestClass2.class.getField("FIELD1"))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition.isStatic());
    assertTrue(definition.getFieldInvoker() instanceof ObjectFieldInvoker);
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.FIELD);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertTrue(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "Namespace2.Field2_1");
  }

  @Test
  public void testRegisterConstantWithNamespace() throws NoSuchFieldException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlConstant(TestClass2.class.getField("FIELD2"))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition.isStatic());
    assertTrue(definition.getFieldInvoker() instanceof ObjectFieldInvoker);
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.FIELD);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertTrue(metadata.isConstantSpec());
    // note that getSimpleName() is used but inner classes use the fully-qualified name
    assertEquals(metadata.getName(), "Namespace2.TestClass2.FIELD2");
  }

  @Test
  public void testRegisterAllConstants() throws NoSuchFieldException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlConstant(TestClass3.class)
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 2);
    for (final FunctionDefinition definition : definitions) {
      assertTrue(definition.isStatic());
      assertTrue(definition.getFieldInvoker() instanceof ObjectFieldInvoker);
      assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.FIELD);
      final FunctionMetadata metadata = definition.getFunctionMetadata();
      assertTrue(metadata.isConstantSpec());
      final String expected = "TestClass3.FIELD1";
      if (!metadata.getName().equals(expected)) {
        assertEquals(metadata.getName(), "TestClass3.FIELD2");
      }
    }
  }

  @Test
  public void testRegisterAllConstantsWithName() throws NoSuchFieldException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlConstant(TestClass4.class)
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 2);
    for (final FunctionDefinition definition : definitions) {
      assertTrue(definition.isStatic());
      assertTrue(definition.getFieldInvoker() instanceof ObjectFieldInvoker);
      assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.FIELD);
      final FunctionMetadata metadata = definition.getFunctionMetadata();
      assertTrue(metadata.isConstantSpec());
      final String expected = "Class4.FIELD1";
      if (!metadata.getName().equals(expected)) {
        assertEquals(metadata.getName(), "Class4.FIELD2");
      }
    }
  }

  @Test
  public void testRegisterAllConstantsWithNamespace() throws NoSuchFieldException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlConstant(TestClass5.class)
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 2);
    for (final FunctionDefinition definition : definitions) {
      assertTrue(definition.isStatic());
      assertTrue(definition.getFieldInvoker() instanceof ObjectFieldInvoker);
      assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.FIELD);
      final FunctionMetadata metadata = definition.getFunctionMetadata();
      assertTrue(metadata.isConstantSpec());
      final String expected = "Namespace5TestClass5.FIELD1";
      if (!metadata.getName().equals(expected)) {
        assertEquals(metadata.getName(), "Namespace5TestClass5.FIELD2");
      }
    }
  }

  @Test
  public void testRegisterAllConstantsWithNameAndNamespace() throws NoSuchFieldException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlConstant(TestClass6.class)
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    registry.createAndRegisterFunctions(invokerFactory);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 2);
    for (final FunctionDefinition definition : definitions) {
      assertTrue(definition.isStatic());
      assertTrue(definition.getFieldInvoker() instanceof ObjectFieldInvoker);
      assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.FIELD);
      final FunctionMetadata metadata = definition.getFunctionMetadata();
      assertTrue(metadata.isConstantSpec());
      final String expected = "Namespace6-Class6.FIELD1";
      if (!metadata.getName().equals(expected)) {
        assertEquals(metadata.getName(), "Namespace6-Class6.FIELD2");
      }
    }
  }

  // CHECKSTYLE:OFF
  public static class TestClass1 {
    @XLConstant(name = "Field1_1") public static final String FIELD1 = "FIELD1";
    @XLConstant public static final String FIELD2 = "FIELD2";
  }

  @XLNamespace(value = "Namespace2.") public static class TestClass2 {
    @XLConstant(name = "Field2_1") public static final String FIELD1 = "FIELD1";
    @XLConstant public static final String FIELD2 = "FIELD2";
  }

  @XLConstant public static class TestClass3 {
    public static final String FIELD1 = "FIELD1";
    public static final String FIELD2 = "FIELD2";
  }

  @XLConstant(name = "Class4") public static class TestClass4 {
    public static final String FIELD1 = "FIELD1";
    public static final String FIELD2 = "FIELD2";
  }

  @XLNamespace(value = "Namespace5") @XLConstant public static class TestClass5 {
    public static final String FIELD1 = "FIELD1";
    public static final String FIELD2 = "FIELD2";
  }

  @XLNamespace(value = "Namespace6-") @XLConstant(name = "Class6") public static class TestClass6 {
    public static final String FIELD1 = "FIELD1";
    public static final String FIELD2 = "FIELD2";
  }

}
