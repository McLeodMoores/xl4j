/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.testutil;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 */
// CHECKSTYLE:OFF
public class TestObject {
  private final int _number;
  private final Double[] _doubles;
  private final String _name;

  // TODO add a builder

  public static TestObject of() {
    return new TestObject(-1000, new Double[0], "static no args constructor");
  }

  public static TestObject of(final Object doubleObject) {
    Double[] doubles = new Double[1];
    if (doubleObject instanceof Number) {
      doubles = new Double[] { ((Number) doubleObject).doubleValue() };
    } else if (doubleObject instanceof String) {
      doubles = new Double[] { Double.parseDouble((String) doubleObject) };
    } else {
      throw new IllegalArgumentException();
    }
    return new TestObject(-2000, doubles, "static Object constructor");
  }

  public static TestObject of(final String doubleString) {
    return new TestObject(-3000, new Double[] { Double.parseDouble(doubleString) }, "static String constructor");
  }

  public static TestObject of(final Double doubleValue) {
    return new TestObject(-4000, new Double[] { doubleValue }, "static Double constructor");
  }

  public static TestObject of(final int number) {
    return new TestObject(number, new Double[0], "static int constructor");
  }

  public static TestObject of(final int number, final String doubleString) {
    return new TestObject(number, new Double[] { Double.parseDouble(doubleString) }, "static int, String constructor");
  }

  public static TestObject of(final int number, final Double doubleValue) {
    return new TestObject(number, new Double[] { doubleValue }, "static int, Double constructor");
  }

  public static TestObject of(final int number, final Object doubleObject) {
    Double[] doubles = new Double[1];
    if (doubleObject instanceof Number) {
      doubles = new Double[] { ((Number) doubleObject).doubleValue() };
    } else if (doubleObject instanceof String) {
      doubles = new Double[] { Double.parseDouble((String) doubleObject) };
    } else {
      throw new IllegalArgumentException();
    }
    return new TestObject(number, doubles, "static int, Object constructor");
  }

  public static TestObject of(final String... doubleStrings) {
    final Double[] doubles = new Double[doubleStrings.length];
    for (int i = 0; i < doubleStrings.length; i++) {
      doubles[i] = Double.parseDouble(doubleStrings[i]);
    }
    return new TestObject(-5000, doubles, "static String... constructor");
  }

  public static TestObject of(final int number, final String... doubleStrings) {
    final Double[] doubles = new Double[doubleStrings.length];
    for (int i = 0; i < doubleStrings.length; i++) {
      doubles[i] = Double.parseDouble(doubleStrings[i]);
    }
    return new TestObject(number, doubles, "static int, String... constructor");
  }

  public static TestObject of(final Double... doubleValues) {
    final Double[] doubles = new Double[doubleValues.length];
    for (int i = 0; i < doubleValues.length; i++) {
      doubles[i] = doubleValues[i];
    }
    return new TestObject(-6000, doubles, "static Double... constructor");
  }

  public static TestObject of(final int number, final Object... doubleObjects) {
    final Double[] doubles = new Double[doubleObjects.length];
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

  public static TestObject of(final int number, final Double... doubleValues) {
    final Double[] doubles = new Double[doubleValues.length];
    for (int i = 0; i < doubleValues.length; i++) {
      doubles[i] = doubleValues[i];
    }
    return new TestObject(number, doubles, "static int, Double... constructor");
  }

  public static TestObject of(final Object... doubleObjects) {
    final Double[] doubles = new Double[doubleObjects.length];
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

  public static TestObject of(final String[] number, final String... doubleStrings) {
    final Double[] doubles = new Double[doubleStrings.length];
    for (int i = 0; i < doubleStrings.length; i++) {
      doubles[i] = Double.parseDouble(doubleStrings[i]);
    }
    return new TestObject(Integer.parseInt(number[0]), doubles, "static int, String... constructor");
  }

  private TestObject(final int number, final Double[] doubles, final String name) {
    _number = number;
    _doubles = doubles;
    _name = name;
  }

  public TestObject() {
    _number = -100;
    _doubles = new Double[0];
    _name = "no args constructor";
  }

  public TestObject(final Object doubleObject) {
    _number = -200;
    if (doubleObject instanceof Number) {
      _doubles = new Double[] { ((Number) doubleObject).doubleValue() };
    } else if (doubleObject instanceof String) {
      _doubles = new Double[] { Double.parseDouble((String) doubleObject) };
    } else {
      throw new IllegalArgumentException();
    }
    _name = "Object constructor";
  }

  public TestObject(final String doubleString) {
    _number = -300;
    _doubles = new Double[] { Double.parseDouble(doubleString) };
    _name = "String constructor";
  }

  public TestObject(final Double doubleValue) {
    _number = -400;
    _doubles = new Double[] { doubleValue };
    _name = "Double constructor";
  }

  public TestObject(final int number) {
    _number = number;
    _doubles = new Double[0];
    _name = "int constructor";
  }

  public TestObject(final int number, final String doubleString) {
    _number = number;
    _doubles = new Double[] { Double.parseDouble(doubleString) };
    _name = "int, String constructor";
  }

  public TestObject(final int number, final Double doubleValue) {
    _number = number;
    _doubles = new Double[] { doubleValue };
    _name = "int, Double constructor";
  }

  public TestObject(final int number, final Object doubleObject) {
    _number = number;
    if (doubleObject instanceof Number) {
      _doubles = new Double[] { ((Number) doubleObject).doubleValue() };
    } else if (doubleObject instanceof String) {
      _doubles = new Double[] { Double.parseDouble((String) doubleObject) };
    } else {
      throw new IllegalArgumentException();
    }
    _name = "int, Object constructor";
  }

  public TestObject(final String... doubleStrings) {
    _number = -500;
    _doubles = new Double[doubleStrings.length];
    for (int i = 0; i < doubleStrings.length; i++) {
      _doubles[i] = Double.parseDouble(doubleStrings[i]);
    }
    _name = "String... constructor";
  }

  public TestObject(final int number, final String... doubleStrings) {
    _number = number;
    _doubles = new Double[doubleStrings.length];
    for (int i = 0; i < doubleStrings.length; i++) {
      _doubles[i] = Double.parseDouble(doubleStrings[i]);
    }
    _name = "int, String... constructor";
  }

  public TestObject(final Double... doubleValues) {
    _number = -600;
    _doubles = new Double[doubleValues.length];
    for (int i = 0; i < doubleValues.length; i++) {
      _doubles[i] = doubleValues[i];
    }
    _name = "Double... constructor";
  }

  public TestObject(final int number, final Object... doubleObjects) {
    _number = number;
    _doubles = new Double[doubleObjects.length];
    for (int i = 0; i < doubleObjects.length; i++) {
      final Object doubleObject = doubleObjects[i];
      if (doubleObject instanceof Number) {
        _doubles[i] = ((Number) doubleObject).doubleValue();
      } else if (doubleObject instanceof String) {
        _doubles[i] = Double.parseDouble((String) doubleObject);
      } else {
        throw new IllegalArgumentException();
      }
    }
    _name = "int, Object... constructor";
  }

  public TestObject(final int number, final Double... doubleValues) {
    _number = number;
    _doubles = new Double[doubleValues.length];
    for (int i = 0; i < doubleValues.length; i++) {
      _doubles[i] = doubleValues[i];
    }
    _name = "int, Double... constructor";
  }

  public TestObject(final Object... doubleObjects) {
    _number = -700;
    _doubles = new Double[doubleObjects.length];
    for (int i = 0; i < doubleObjects.length; i++) {
      final Object doubleObject = doubleObjects[i];
      if (doubleObject instanceof Number) {
        _doubles[i] = ((Number) doubleObject).doubleValue();
      } else if (doubleObject instanceof String) {
        _doubles[i] = Double.parseDouble((String) doubleObject);
      } else {
        throw new IllegalArgumentException();
      }
    }
    _name = "Object... constructor";
  }

  public TestObject(final String[] number, final String... doubleStrings) {
    _number = Integer.parseInt(number[0]);
    _doubles = new Double[doubleStrings.length];
    for (int i = 0; i < doubleStrings.length; i++) {
      _doubles[i] = Double.parseDouble(doubleStrings[i]);
    }
    _name = "int, String... constructor";
  }

  public int getNumber() {
    return _number;
  }

  public Double[] getDoubles() {
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
