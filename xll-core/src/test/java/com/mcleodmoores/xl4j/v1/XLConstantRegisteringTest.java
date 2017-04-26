/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.annotations.XLConstant;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.api.core.CallTarget;
import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.core.FunctionDefinition;
import com.mcleodmoores.xl4j.v1.api.core.InvokerFactory;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.core.FunctionMetadata;
import com.mcleodmoores.xl4j.v1.invoke.ObjectFieldGetter;
import com.mcleodmoores.xl4j.v1.invoke.ReflectiveInvokerFactory;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.StringXLStringTypeConverter;

/**
 * Tests registration of fields or enums annotated with {@link XLConstant}.
 */
public class XLConstantRegisteringTest {
  private static final Excel EXCEL = ExcelFactory.getInstance();
  private static final TypeConverterRegistry TYPE_CONVERTERS = MockTypeConverterRegistry.builder()
      .with(new StringXLStringTypeConverter())
      .build();

  /**
   * Tests that the name in the annotation is used if available.
   * @throws NoSuchFieldException  if the field cannot be found
   * @throws SecurityException  if the field cannot be found
   */
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
    assertTrue(definition.getFieldInvoker() instanceof ObjectFieldGetter);
    assertEquals(definition.getCallTargetForFunction(), CallTarget.FIELD);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertTrue(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "Field1_1");
  }

  /**
   * Tests that the name is created by appending the field name to the class name if the name
   * parameter is not set in the annotation.
   * @throws NoSuchFieldException  if the field is not found
   * @throws SecurityException  if the field is not found
   */
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
    assertTrue(definition.getFieldInvoker() instanceof ObjectFieldGetter);
    assertEquals(definition.getCallTargetForFunction(), CallTarget.FIELD);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertTrue(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "TestClass1.FIELD2");
  }

  /**
   * Tests that the namespace value is prepended to the name parameter.
   * @throws NoSuchFieldException  if the field is not found
   * @throws SecurityException  if the field is not found
   */
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
    assertTrue(definition.getFieldInvoker() instanceof ObjectFieldGetter);
    assertEquals(definition.getCallTargetForFunction(), CallTarget.FIELD);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertTrue(metadata.isConstantSpec());
    assertEquals(metadata.getName(), "Namespace2-Field2_1");
  }

  /**
   * Tests that the namespace value is prepended to the class name and field name if the name
   * parameter is not set in the constant annotation.
   * @throws NoSuchFieldException  if the field is not found
   * @throws SecurityException  if the field is not found
   */
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
    assertTrue(definition.getFieldInvoker() instanceof ObjectFieldGetter);
    assertEquals(definition.getCallTargetForFunction(), CallTarget.FIELD);
    final FunctionMetadata metadata = definition.getFunctionMetadata();
    assertTrue(metadata.isConstantSpec());
    // note that getSimpleName() is used but inner classes use the fully-qualified name
    assertEquals(metadata.getName(), "Namespace2-TestClass2.FIELD2");
  }

  /**
   * Tests that all public fields in a class are registered and their names generated by appending
   * the class name to the field name if the constant annotation is class-level and the name is not
   * set.
   * @throws NoSuchFieldException  if the field is not found
   * @throws SecurityException  if the field is not found
   */
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
      assertTrue(definition.getFieldInvoker() instanceof ObjectFieldGetter);
      assertEquals(definition.getCallTargetForFunction(), CallTarget.FIELD);
      final FunctionMetadata metadata = definition.getFunctionMetadata();
      assertTrue(metadata.isConstantSpec());
      final String expected = "TestClass3.FIELD1";
      if (!metadata.getName().equals(expected)) {
        assertEquals(metadata.getName(), "TestClass3.FIELD2");
      }
    }
  }

  /**
   * Tests that all public fields in a class are registered and the name field in the class-level
   * annotation is prepended to the field names.
   * @throws NoSuchFieldException  if the field is not found
   * @throws SecurityException  if the field is not found
   */
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
      assertTrue(definition.getFieldInvoker() instanceof ObjectFieldGetter);
      assertEquals(definition.getCallTargetForFunction(), CallTarget.FIELD);
      final FunctionMetadata metadata = definition.getFunctionMetadata();
      assertTrue(metadata.isConstantSpec());
      final String expected = "Class4.FIELD1";
      if (!metadata.getName().equals(expected)) {
        assertEquals(metadata.getName(), "Class4.FIELD2");
      }
    }
  }

  /**
   * Tests that all public fields in a class are registered and the namespace annotation is
   * prepended to the generated field names.
   * @throws NoSuchFieldException  if the field is not found
   * @throws SecurityException  if the field is not found
   */
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
      assertTrue(definition.getFieldInvoker() instanceof ObjectFieldGetter);
      assertEquals(definition.getCallTargetForFunction(), CallTarget.FIELD);
      final FunctionMetadata metadata = definition.getFunctionMetadata();
      assertTrue(metadata.isConstantSpec());
      final String expected = "Namespace5TestClass5.FIELD1";
      if (!metadata.getName().equals(expected)) {
        assertEquals(metadata.getName(), "Namespace5TestClass5.FIELD2");
      }
    }
  }

  /**
   * Tests that all public fields in a class a registered and the namespace value and constant
   * annotation name are prepended to the field name.
   * @throws NoSuchFieldException  if the field is not found
   * @throws SecurityException  if the field is not found
   */
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
      assertTrue(definition.getFieldInvoker() instanceof ObjectFieldGetter);
      assertEquals(definition.getCallTargetForFunction(), CallTarget.FIELD);
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

  @XLNamespace(value = "Namespace2-") public static class TestClass2 {
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
    @SuppressWarnings("unused")
    private static final String FIELD3 = "FIELD3";
  }

  @XLNamespace(value = "Namespace6-") @XLConstant(name = "Class6") public static class TestClass6 {
    public static final String FIELD1 = "FIELD1";
    public static final String FIELD2 = "FIELD2";
    protected static final String FIELD3 = "FIELD3";
  }

}
