/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode.testutils;

import java.util.Arrays;
import java.util.Objects;

/**
 * Test object.
 */
// CHECKSTYLE:OFF
public class TestObject {
  public static final double MAGIC_NUMBER = 1.234567;
  private static final double PRIVATE_FIELD = 3.21;

  public final int _number;
  public final double[] _doubles;
  public final String _name;
  private final String _privateName;

  // TODO add a builder

  public static TestObject of() {
    return new TestObject(-1000, new double[0], "static no args constructor");
  }

  public static TestObject ofObject(final Object doubleObject) {
    double[] doubles = new double[1];
    if (doubleObject instanceof Number) {
      doubles = new double[] { ((Number) doubleObject).doubleValue() };
    } else if (doubleObject instanceof String) {
      doubles = new double[] { Double.parseDouble((String) doubleObject) };
    } else {
      throw new IllegalArgumentException();
    }
    return new TestObject(-2000, doubles, "static Object constructor");
  }

  public static TestObject ofString(final String doubleString) {
    return new TestObject(-3000, new double[] { Double.parseDouble(doubleString) }, "static String constructor");
  }

  public static TestObject ofDouble(final Double doubleValue) {
    return new TestObject(-4000, new double[] { doubleValue }, "static Double constructor");
  }

  public static TestObject ofPrimitiveInt(final int number) {
    return new TestObject(number, new double[0], "static int constructor");
  }

  public static TestObject ofPrimitiveIntString(final int number, final String doubleString) {
    return new TestObject(number, new double[] { Double.parseDouble(doubleString) }, "static int, String constructor");
  }

  public static TestObject ofPrimitiveIntDouble(final int number, final Double doubleValue) {
    return new TestObject(number, new double[] { doubleValue }, "static int, Double constructor");
  }

  public static TestObject ofPrimitiveIntObject(final int number, final Object doubleObject) {
    double[] doubles = new double[1];
    if (doubleObject instanceof Number) {
      doubles = new double[] { ((Number) doubleObject).doubleValue() };
    } else if (doubleObject instanceof String) {
      doubles = new double[] { Double.parseDouble((String) doubleObject) };
    } else {
      throw new IllegalArgumentException();
    }
    return new TestObject(number, doubles, "static int, Object constructor");
  }

  public static TestObject ofStrings(final String... doubleStrings) {
    final double[] doubles = new double[doubleStrings.length];
    for (int i = 0; i < doubleStrings.length; i++) {
      doubles[i] = Double.parseDouble(doubleStrings[i]);
    }
    return new TestObject(-5000, doubles, "static String... constructor");
  }

  public static TestObject ofPrimitiveIntStrings(final int number, final String... doubleStrings) {
    final double[] doubles = new double[doubleStrings.length];
    for (int i = 0; i < doubleStrings.length; i++) {
      doubles[i] = Double.parseDouble(doubleStrings[i]);
    }
    return new TestObject(number, doubles, "static int, String... constructor");
  }

  public static TestObject ofDoubles(final Double... doubleValues) {
    final double[] doubles = new double[doubleValues.length];
    for (int i = 0; i < doubleValues.length; i++) {
      doubles[i] = doubleValues[i];
    }
    return new TestObject(-6000, doubles, "static Double... constructor");
  }

  public static TestObject ofPrimitiveIntObjects(final int number, final Object... doubleObjects) {
    final double[] doubles = new double[doubleObjects.length];
    for (int i = 0; i < doubleObjects.length; i++) {
      final Object doubleObject = doubleObjects[i];
      if (doubleObject instanceof Number) {
        doubles[i] = ((Number) doubleObject).doubleValue();
      } else if (doubleObject instanceof String) {
        doubles[i] = Double.parseDouble((String) doubleObject);
      } else {
        throw new IllegalArgumentException();
      }
    }
    return new TestObject(number, doubles, "static int, Object... constructor");
  }

  public static TestObject ofPrimitiveIntDoubles(final int number, final Double... doubleValues) {
    final double[] doubles = new double[doubleValues.length];
    for (int i = 0; i < doubleValues.length; i++) {
      doubles[i] = doubleValues[i];
    }
    return new TestObject(number, doubles, "static int, Double... constructor");
  }

  public static TestObject ofObjects(final Object... doubleObjects) {
    final double[] doubles = new double[doubleObjects.length];
    for (int i = 0; i < doubleObjects.length; i++) {
      final Object doubleObject = doubleObjects[i];
      if (doubleObject instanceof Number) {
        doubles[i] = ((Number) doubleObject).doubleValue();
      } else if (doubleObject instanceof String) {
        doubles[i] = Double.parseDouble((String) doubleObject);
      } else {
        throw new IllegalArgumentException();
      }
    }
    return new TestObject(-7000, doubles, "static Object... constructor");
  }

  public static TestObject ofStringsStrings(final String[] number, final String... doubleStrings) {
    final double[] doubles = new double[doubleStrings.length];
    for (int i = 0; i < doubleStrings.length; i++) {
      doubles[i] = Double.parseDouble(doubleStrings[i]);
    }
    return new TestObject(Integer.parseInt(number[0]), doubles, "static int, String... constructor");
  }

  public TestObject(final int number, final double[] doubles, final String name) {
    _number = number;
    _doubles = doubles;
    _name = name;
    _privateName = name;
  }

  public TestObject() {
    _number = -100;
    _doubles = new double[0];
    _name = "no args constructor";
    _privateName = _name;
  }

  public TestObject(final String doubleString) {
    _number = -300;
    _doubles = new double[] { Double.parseDouble(doubleString) };
    _name = "String constructor";
    _privateName = _name;
  }

  public TestObject(final int number) {
    _number = number;
    _doubles = new double[0];
    _name = "int constructor";
    _privateName = _name;
  }

  public int getNumber() {
    return _number;
  }

  public double[] getDoubles() {
    return _doubles;
  }

  public Double getDouble() {
    return _doubles[0];
  }

  public Double getDouble(final int index) {
    return _doubles[index];
  }

  public Double getDouble(final String indexString) {
    final int index = Integer.parseInt(indexString);
    return _doubles[index];
  }

  public Double getDouble(final Object indexObject) {
    if (indexObject instanceof Number) {
      return _doubles[((Number) indexObject).intValue()];
    } else if (indexObject instanceof String) {
      return _doubles[Integer.parseInt((String) indexObject)];
    }
    throw new IllegalArgumentException();
  }

  public double getDoublesSum(final int offset, final String forHashCode, final int... indices) {
    double result = _number + offset + forHashCode.hashCode();
    for (final int index : indices) {
      result += _doubles[index];
    }
    return result;
  }

  public double getDoublesSum(final int offset, final String forHashCode, final String... indexStrings) {
    double result = _number + offset + forHashCode.hashCode();
    for (final String indexString : indexStrings) {
      result += _doubles[Integer.parseInt(indexString)];
    }
    return result;
  }

  public double getDoublesSum(final int offset, final String forHashCode, final Object... indexObjects) {
    double result = _number + offset + forHashCode.hashCode();
    for (final Object indexObject : indexObjects) {
      if (indexObject instanceof Number) {
        result += _doubles[((Number) indexObject).intValue()];
      } else if (indexObject instanceof String) {
        result += _doubles[Integer.parseInt((String) indexObject)];
      } else {
        throw new IllegalArgumentException("Unsupported type " + indexObject.getClass());
      }
    }
    return result;
  }

  public double getDoublesSum(final int... indices) {
    double result = _number;
    for (final int index : indices) {
      result += _doubles[index];
    }
    return result;
  }

  public double getDoublesSum(final String... indexStrings) {
    double result = _number;
    for (final String indexString : indexStrings) {
      result += _doubles[Integer.parseInt(indexString)];
    }
    return result;
  }

  public double getDoublesSum(final Object... indexObjects) {
    double result = _number;
    for (final Object indexObject : indexObjects) {
      if (indexObject instanceof Number) {
        result += _doubles[((Number) indexObject).intValue()];
      } else if (indexObject instanceof String) {
        result += _doubles[Integer.parseInt((String) indexObject)];
      } else {
        throw new IllegalArgumentException("Unsupported type " + indexObject.getClass());
      }
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_doubles);
    result = prime * result + _number;
    result = prime * result + _name.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TestObject other = (TestObject) obj;
    if (_number != other._number) {
      return false;
    }
    if (!Objects.equals(_name, other._name)) {
      return false;
    }
    if (!Arrays.equals(_doubles, other._doubles)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(_name);
    sb.append(" [doubles=");
    sb.append(Arrays.toString(_doubles));
    sb.append(" number=");
    sb.append(_number);
    sb.append("]");
    return sb.toString();
  }
}
