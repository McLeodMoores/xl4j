/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import static com.mcleodmoores.xl4j.FunctionMetadataHelper.FUNCTION_METADATA;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.javacode.ConstructorInvoker;
import com.mcleodmoores.xl4j.javacode.FieldInvoker;
import com.mcleodmoores.xl4j.javacode.JavaTypeForFunction;
import com.mcleodmoores.xl4j.javacode.MethodInvoker;
import com.mcleodmoores.xl4j.javacode.ObjectConstructorInvoker;
import com.mcleodmoores.xl4j.javacode.ObjectFieldInvoker;
import com.mcleodmoores.xl4j.javacode.SimpleResultMethodInvoker;
import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.PrimitiveBooleanXLBooleanTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.PrimitiveIntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;

/**
 * Unit tests for {@link FunctionDefinition}.
 */
public class FunctionDefinitionTest {
  private static final MethodInvoker METHOD_INVOKER;
  private static final ConstructorInvoker CONSTRUCTOR_INVOKER;
  private static final FieldInvoker FIELD_INVOKER;

  static {
    final TypeConverter intConverter = new PrimitiveIntegerXLNumberTypeConverter();
    final TypeConverter booleanConverter = new PrimitiveBooleanXLBooleanTypeConverter();
    final Method method;
    final Constructor<?> constructor;
    final Field field;
    try {
      method = InvokerTestHelper.class.getMethod("multiArgsMethod", new Class<?>[] {Integer.TYPE, Integer.TYPE});
      constructor = InvokerTestHelper.class.getConstructor(new Class<?>[] {Integer.TYPE, Integer.TYPE});
      field = InvokerTestHelper.class.getField("FIELD");
    } catch (NoSuchMethodException | SecurityException | NoSuchFieldException e) {
      throw new Excel4JRuntimeException("", e);
    }
    METHOD_INVOKER = new SimpleResultMethodInvoker(method, new TypeConverter[] {intConverter, intConverter},
        booleanConverter);
    CONSTRUCTOR_INVOKER = new ObjectConstructorInvoker(constructor,
        new TypeConverter[] {intConverter, intConverter}, new ObjectXLObjectTypeConverter(ExcelFactory.getInstance()));
    FIELD_INVOKER = new ObjectFieldInvoker(field, intConverter);
  }

  /**
   * Tests that the metadata must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullMetadata1() {
    FunctionDefinition.of(null, METHOD_INVOKER, 1);
  }

  /**
   * Tests that the metadata must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullMetadata2() {
    FunctionDefinition.of(null, CONSTRUCTOR_INVOKER, 1);
  }

  /**
   * Tests that the metadata must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullMetadata3() {
    FunctionDefinition.of(null, FIELD_INVOKER, 1);
  }

  /**
   * Tests that the method invoker cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullMethodInvoker() {
    FunctionDefinition.of(FUNCTION_METADATA, (MethodInvoker) null, 1);
  }

  /**
   * Tests that the constructor invoker cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullConstructorInvoker() {
    FunctionDefinition.of(FUNCTION_METADATA, (ConstructorInvoker) null, 1);
  }

  /**
   * Tests that the field invoker cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullFieldInvoker() {
    FunctionDefinition.of(FUNCTION_METADATA, (FieldInvoker) null, 1);
  }

  /**
   * Tests that the wrong type of invoker cannot be retrieved.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testWrongInvokerType1() {
    FunctionDefinition.of(FUNCTION_METADATA, CONSTRUCTOR_INVOKER, 1).getMethodInvoker();
  }

  /**
   * Tests that the wrong type of invoker cannot be retrieved.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testWrongInvokerType2() {
    FunctionDefinition.of(FUNCTION_METADATA, CONSTRUCTOR_INVOKER, 1).getFieldInvoker();
  }

  /**
   * Tests that the wrong type of invoker cannot be retrieved.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testWrongInvokerType3() {
    FunctionDefinition.of(FUNCTION_METADATA, METHOD_INVOKER, 1).getConstructorInvoker();
  }

  /**
   * Tests that the wrong type of invoker cannot be retrieved.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testWrongInvokerType4() {
    FunctionDefinition.of(FUNCTION_METADATA, METHOD_INVOKER, 1).getFieldInvoker();
  }

  /**
   * Tests that the wrong type of invoker cannot be retrieved.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testWrongInvokerType5() {
    FunctionDefinition.of(FUNCTION_METADATA, FIELD_INVOKER, 1).getMethodInvoker();
  }

  /**
   * Tests that the wrong type of invoker cannot be retrieved.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testWrongInvokerType6() {
    FunctionDefinition.of(FUNCTION_METADATA, FIELD_INVOKER, 1).getConstructorInvoker();
  }

  /**
   * Tests that the isStatic() method cannot be called for a constructor definition.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testIsStaticForConstructor() {
    FunctionDefinition.of(FUNCTION_METADATA, CONSTRUCTOR_INVOKER, 1).isStatic();
  }

  /**
   * Tests that the isVarArgs() method cannot be called for a field definition.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testIsVarArgsForField() {
    FunctionDefinition.of(FUNCTION_METADATA, FIELD_INVOKER, 1).isVarArgs();
  }

  /**
   * Tests the definition for a method.
   */
  @Test
  public void testMethodDefinition() {
    final FunctionDefinition definition = FunctionDefinition.of(FUNCTION_METADATA, METHOD_INVOKER, 1);
    assertTrue(definition.isStatic());
    assertFalse(definition.isVarArgs());
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.METHOD);
    assertEquals(definition.getExportName(), "UDF_1");
    assertEquals(definition.getExportNumber(), 1);
  }

  /**
   * Tests the definition for a constructor.
   */
  @Test
  public void testConstructorDefinition() {
    final FunctionDefinition definition = FunctionDefinition.of(FUNCTION_METADATA, CONSTRUCTOR_INVOKER, 1);
    assertFalse(definition.isVarArgs());
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.CONSTRUCTOR);
    assertEquals(definition.getExportName(), "UDF_1");
    assertEquals(definition.getExportNumber(), 1);
  }

  /**
   * Tests the definition for a field.
   */
  @Test
  public void testFieldDefinition() {
    final FunctionDefinition definition = FunctionDefinition.of(FUNCTION_METADATA, FIELD_INVOKER, 1);
    assertTrue(definition.isStatic());
    assertEquals(definition.getJavaTypeForFunction(), JavaTypeForFunction.FIELD);
    assertEquals(definition.getExportName(), "UDF_1");
    assertEquals(definition.getExportNumber(), 1);
  }
}
