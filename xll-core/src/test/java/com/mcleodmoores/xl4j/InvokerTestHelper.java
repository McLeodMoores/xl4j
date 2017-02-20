/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import java.util.ArrayList;
import java.util.List;

import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Helper class.
 */
//CHECKSTYLE:OFF
public class InvokerTestHelper {
  public static final int INT_FIELD = -100;
  public static final XLValue XL_VALUE_FIELD = XLString.of("200");
  private final List<Object> _inputs = new ArrayList<>();

  public InvokerTestHelper() {
  }

  public InvokerTestHelper(final int i) {
    _inputs.add(i);
  }

  public InvokerTestHelper(final int i, final int j) {
    _inputs.add(i);
    _inputs.add(j);
  }

  public InvokerTestHelper(final int[] i, final int[] j) {
    _inputs.add(i);
    _inputs.add(j);
  }

  public InvokerTestHelper(final int... ints) {
    _inputs.add(ints);
  }

  public InvokerTestHelper(final int i, final int... ints) {
    _inputs.add(i);
    _inputs.add(ints);
  }

  public InvokerTestHelper(final int i, final int j, final int... ints) {
    _inputs.add(i);
    _inputs.add(j);
    _inputs.add(ints);
  }

  public InvokerTestHelper(final XLValue value) {
    _inputs.add(value);
  }

  public InvokerTestHelper(final XLValue value1, final XLValue value2) {
    _inputs.add(value1);
    _inputs.add(value2);
  }

  public InvokerTestHelper(final XLValue... values) {
    _inputs.add(values);
  }

  public InvokerTestHelper(final XLValue value1, final XLValue value2, final XLValue... values) {
    _inputs.add(value1);
    _inputs.add(value2);
    _inputs.add(values);
  }

  public List<Object> getInputs() {
    return _inputs;
  }

  public void voidMethod() {
  }

  public static void voidStaticMethod() {
    return;
  }

  public static boolean noArgsMethod() {
    return false;
  }

  public static XLValue noArgsXlMethod() {
    return XLBoolean.FALSE;
  }

  public static boolean singleArgMethod(final int i) {
    return i > 0;
  }

  public static XLValue singleArgXlMethod(final XLValue xlValue) {
    return XLBoolean.from(xlValue instanceof XLNumber);
  }

  public static boolean multiArgsMethod(final int i, final int j) {
    return i * j > 0;
  }

  public static XLValue multiArgsXlMethod(final XLString string, final XLNumber number) {
    return XLBoolean.from(Double.doubleToLongBits(Double.valueOf(string.getValue())) == Double.doubleToLongBits(number.getAsDouble()));
  }

  public static boolean arrayArgsMethod(final int[] is, final int[] js) {
    if (is == null || js == null) {
      return false;
    }
    for (final int i : is) {
      for (final int j : js) {
        if (i * j < 0) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean varArgsMethod1(final int... ints) {
    for (final int i : ints) {
      if (i < 0) {
        return false;
      }
    }
    return true;
  }

  public static XLValue varArgsXlMethod1(final XLValue... values) {
    for (final XLValue value : values) {
      if (!(value instanceof XLNumber)) {
        return XLBoolean.FALSE;
      }
    }
    return XLBoolean.TRUE;
  }

  public static boolean varArgsMethod2(final int i, final int j, final int... ints) {
    for (final int ii : ints) {
      if (ii < 0) {
        return false;
      }
    }
    return i > 0 && j > 0;
  }

  public static XLValue varArgsXlMethod2(final XLValue value1, final XLValue value2, final XLValue... values) {
    for (final XLValue value : values) {
      if (!(value instanceof XLNumber)) {
        return XLBoolean.FALSE;
      }
    }
    return XLBoolean.from(value1 instanceof XLNumber && value2 instanceof XLNumber);
  }

  public static boolean passthroughMethod1(final XLValue xlValue) {
    if (xlValue == null) {
      return false;
    }
    return true;
  }

  public static XLValue passthroughMethod2(final XLValue xlValue) {
    if (xlValue instanceof XLObject) {
      return xlValue;
    }
    return XLBoolean.TRUE;
  }

  public static boolean overloadedMethodName(final int i) {
    return i < 0;
  }

  public static boolean overloadedMethodName(final int i, final int j) {
    return i * j < 0;
  }

  public static boolean overloadedMethodName(final int... ints) {
    return ints.length > 3;
  }

  public static boolean overloadedMethodName(final int i, final int... ints) {
    return i < 0 && ints.length > 5;
  }

  public static boolean overloadedMethodName(final int i, final int j, final int... ints) {
    return i > 0 && j < 0 && ints.length > 10;
  }

  public static XLBoolean overloadedMethodName(final XLValue xlValue) {
    return XLBoolean.from(xlValue instanceof XLNumber);
  }

  public static XLBoolean overloadedMethodName(final XLValue xlValue1, final XLValue xlValue2) {
    return XLBoolean.from(xlValue1 instanceof XLString && xlValue2 instanceof XLNumber);
  }

  public static XLBoolean overloadedMethodName(final XLValue... xlValues) {
    return XLBoolean.from(xlValues.length > 0);
  }

  public static XLBoolean overloadedMethodName(final XLValue xlValue, final XLValue... xlValues) {
    return XLBoolean.from(xlValue instanceof XLNumber && xlValues.length > 7);
  }

  public static XLBoolean overloadedMethodName(final XLValue xlValue1, final XLValue xlValue2, final XLValue... xlValues) {
    return XLBoolean.from(xlValue1 instanceof XLString && xlValue2 instanceof XLString && xlValues.length > 0);
  }
}
