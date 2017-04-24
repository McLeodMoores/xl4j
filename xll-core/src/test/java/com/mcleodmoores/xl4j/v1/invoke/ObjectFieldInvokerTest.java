/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.lang.reflect.Field;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.core.FieldGetter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.IntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link ObjectFieldGetter}.
 */
public class ObjectFieldInvokerTest {
  private static final Field FIELD;
  private static final ObjectFieldInvokerTest OBJECT = new ObjectFieldInvokerTest();
  private static final TypeConverter CONVERTER = new IntegerXLNumberTypeConverter();
  private static final FieldGetter INVOKER;
  /** Visible field. */
  public final Integer _integer1 = 4;
  /** Inaccessible field */
  @SuppressWarnings("unused")
  private final Integer _integer2 = 2;

  static {
    Field field;
    try {
      field = ObjectFieldInvokerTest.class.getDeclaredField("_integer1");
    } catch (NoSuchFieldException | SecurityException e) {
      field = null;
    }
    FIELD = field;
    INVOKER = new ObjectFieldGetter(FIELD, CONVERTER);
  }

  /**
   * Tests that the Field cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullField() {
    new ObjectFieldGetter(null, CONVERTER);
  }

  /**
   * Tests that the converter cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullConverter() {
    new ObjectFieldGetter(FIELD, null);
  }

  /**
   * Tests the behaviour when the field is not accessible.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testPrivateField() {
    Field field;
    try {
      field = ObjectFieldInvokerTest.class.getDeclaredField("_integer2");
    } catch (NoSuchFieldException | SecurityException e) {
      field = null;
    }
    final ObjectFieldGetter invoker = new ObjectFieldGetter(field, CONVERTER);
    invoker.get(OBJECT);
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
    assertFalse(INVOKER.isStatic());
  }

  /**
   * Tests the field invoker.
   */
  @Test
  public void testInvoke() {
    final XLValue xlValue = INVOKER.get(OBJECT);
    assertEquals(xlValue, XLNumber.of(_integer1));
  }
}
