/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.lang.reflect.Field;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link PassthroughFieldInvoker}.
 */
public class PassthroughFieldInvokerTest {
  private static final Field FIELD;
  private static final PassthroughFieldInvokerTest OBJECT = new PassthroughFieldInvokerTest();
  private static final FieldInvoker INVOKER;
  /** Visible field. */
  public final XLNumber _xlNumber1 = XLNumber.of(4);
  /** Inaccessible field */
  private final XLNumber _xlNumber2 = XLNumber.of(2);

  static {
    Field field;
    try {
      field = PassthroughFieldInvokerTest.class.getDeclaredField("_xlNumber1");
    } catch (NoSuchFieldException | SecurityException e) {
      field = null;
    }
    FIELD = field;
    INVOKER = new PassthroughFieldInvoker(FIELD);
  }

  /**
   * Tests that the Field cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullField() {
    new PassthroughFieldInvoker(null);
  }

  /**
   * Tests the behaviour when the field is not accessible.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testPrivateField() {
    Field field;
    try {
      field = PassthroughFieldInvokerTest.class.getDeclaredField("_xlNumber2");
    } catch (NoSuchFieldException | SecurityException e) {
      field = null;
    }
    final PassthroughFieldInvoker invoker = new PassthroughFieldInvoker(field);
    invoker.invoke(OBJECT);
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
    final XLValue xlValue = INVOKER.invoke(OBJECT);
    assertEquals(xlValue,_xlNumber1);
  }
}
