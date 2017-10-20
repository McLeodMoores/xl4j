/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.lang.reflect.Field;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.core.FieldGetter;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link PassthroughFieldGetter}.
 */
public class PassthroughFieldInvokerTest {
  private static final Field FIELD;
  private static final PassthroughFieldInvokerTest OBJECT = new PassthroughFieldInvokerTest();
  private static final FieldGetter INVOKER;
  // CHECKSTYLE:OFF
  /** Visible field. */
  public final XLNumber _xlNumber1 = XLNumber.of(4);
  // CHECKSTYLE:ON
  /** Inaccessible field */
  @SuppressWarnings("unused")
  private final XLNumber _xlNumber2 = XLNumber.of(2);

  static {
    Field field;
    try {
      field = PassthroughFieldInvokerTest.class.getDeclaredField("_xlNumber1");
    } catch (NoSuchFieldException | SecurityException e) {
      field = null;
    }
    FIELD = field;
    INVOKER = new PassthroughFieldGetter(FIELD);
  }

  /**
   * Tests that the Field cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullField() {
    new PassthroughFieldGetter(null);
  }

  /**
   * Tests the behaviour when the field is not accessible.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testPrivateField() {
    Field field;
    try {
      field = PassthroughFieldInvokerTest.class.getDeclaredField("_xlNumber2");
    } catch (NoSuchFieldException | SecurityException e) {
      field = null;
    }
    final PassthroughFieldGetter invoker = new PassthroughFieldGetter(field);
    invoker.get(OBJECT);
  }

  /**
   * Tests field information.
   */
  @Test
  public void testFieldInformation() {
    assertEquals(INVOKER.getExcelReturnType(), XLNumber.class);
    assertEquals(INVOKER.getFieldDeclaringClass(), PassthroughFieldInvokerTest.class.getName());
    assertEquals(INVOKER.getFieldName(), "_xlNumber1");
    assertEquals(INVOKER.getFieldType(), XLNumber.class);
    assertFalse(INVOKER.isStatic());
  }

  /**
   * Tests the field invoker.
   */
  @Test
  public void testInvoke() {
    final XLValue xlValue = INVOKER.get(OBJECT);
    assertEquals(xlValue, _xlNumber1);
  }
}
