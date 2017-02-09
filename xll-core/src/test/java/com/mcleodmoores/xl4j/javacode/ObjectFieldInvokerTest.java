/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Field;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.IntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link ObjectFieldInvoker}.
 */
public class ObjectFieldInvokerTest {
  private static final Field FIELD;
  private static final ObjectFieldInvokerTest OBJECT = new ObjectFieldInvokerTest();
  private static final TypeConverter CONVERTER = new IntegerXLNumberTypeConverter();
  private static final FieldInvoker INVOKER;
  /** Visible field. */
  public final Integer _integer1 = 4;
  /** Inaccessible field */
  private final Integer _integer2 = 2;

  static {
    Field field;
    try {
      field = ObjectFieldInvokerTest.class.getDeclaredField("_integer1");
    } catch (NoSuchFieldException | SecurityException e) {
      field = null;
    }
    FIELD = field;
    INVOKER = new ObjectFieldInvoker(FIELD, CONVERTER);
  }

  /**
   * Tests that the Field cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullField() {
    new ObjectFieldInvoker(null, CONVERTER);
  }

  /**
   * Tests that the converter cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullConverter() {
    new ObjectFieldInvoker(FIELD, null);
  }

  /**
   * Tests the behaviour when the field is not accessible.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testPrivateField() {
    Field field;
    try {
      field = ObjectFieldInvokerTest.class.getDeclaredField("_integer2");
    } catch (NoSuchFieldException | SecurityException e) {
      field = null;
    }
    final ObjectFieldInvoker invoker = new ObjectFieldInvoker(field, CONVERTER);
    invoker.invoke(OBJECT);
  }

  /**
   * Tests field information.
   */
  @Test
  public void testFieldInformation() {
    assertEquals(INVOKER.getExcelReturnType(), Integer.class);
    assertEquals(INVOKER.getFieldDeclaringClass(), ObjectFieldInvokerTest.class.getName());
    assertEquals(INVOKER.getFieldName(), "_integer1");
    assertEquals(INVOKER.getFieldType(), Integer.class);
  }

  /**
   * Tests the field invoker.
   */
  @Test
  public void testInvoke() {
    final XLValue xlValue = INVOKER.invoke(OBJECT);
    assertEquals(xlValue, XLNumber.of(_integer1));
  }
}
