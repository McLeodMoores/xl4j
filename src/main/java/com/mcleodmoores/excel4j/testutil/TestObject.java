package com.mcleodmoores.excel4j.testutil;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 */
//CHECKSTYLE:OFF
public class TestObject {
  private final int _number;
  private final Double[] _doubles;
  private final String _name;

  public TestObject() {
    _number = -100;
    _doubles = new Double[0];
    _name = "no args constructor";
  }

  public TestObject(final Object doubleObject) {
    _number = -200;
    if (doubleObject instanceof Number) {
      _doubles = new Double[] {((Number) doubleObject).doubleValue()};
    } else if (doubleObject instanceof String) {
      _doubles = new Double[] {Double.parseDouble((String) doubleObject)};
    } else {
      throw new IllegalArgumentException();
    }
    _name = "Object constructor";
  }

  public TestObject(final String doubleString) {
    _number = -300;
    _doubles = new Double[] {Double.parseDouble(doubleString)};
    _name = "String constructor";
  }

  public TestObject(final Double doubleValue) {
    _number = -400;
    _doubles = new Double[] {doubleValue};
    _name = "Double constructor";
  }

  public TestObject(final int number) {
    _number = number;
    _doubles = new Double[0];
    _name = "int constructor";
  }

  public TestObject(final int number, final String doubleString) {
    _number = number;
    _doubles = new Double[] {Double.parseDouble(doubleString)};
    _name = "int, String constructor";
  }

  public TestObject(final int number, final Double doubleValue) {
    _number = number;
    _doubles = new Double[] {doubleValue};
    _name = "int, Double constructor";
  }

  public TestObject(final int number, final Object doubleObject) {
    _number = number;
    if (doubleObject instanceof Number) {
      _doubles = new Double[] {((Number) doubleObject).doubleValue()};
    } else if (doubleObject instanceof String) {
      _doubles = new Double[] {Double.parseDouble((String) doubleObject)};
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

  public Double getDouble(final int index) {
    return _doubles[index];
  }

  public Double getDouble(final String indexString) {
    final int index = Integer.parseInt(indexString);
    return _doubles[index];
  }

  public Double getDouble(final Object indexObject) {
    if (indexObject instanceof Integer) {
      return _doubles[(Integer) indexObject];
    } else if (indexObject instanceof String) {
      return _doubles[Integer.parseInt((String) indexObject)];
    }
    throw new IllegalArgumentException();
  }

  public Double[] getDoubles(final int... indices) {
    final Double[] result = new Double[indices.length];
    for (int i = 0; i < indices.length; i++) {
      result[i] = _doubles[indices[i]];
    }
    return result;
  }

  public Double[] getDoubles(final String... indexStrings) {
    final Double[] result = new Double[indexStrings.length];
    for (int i = 0; i < indexStrings.length; i++) {
      result[i] = _doubles[Integer.parseInt(indexStrings[i])];
    }
    return result;
  }

  public Double[] getDoubles(final Object... indexObjects) {
    final Double[] result = new Double[indexObjects.length];
    for (int i = 0; i < indexObjects.length; i++) {
      final Object indexObject = indexObjects[i];
      if (indexObject instanceof Integer) {
        result[i] = _doubles[(Integer) indexObject];
      } else if (indexObject instanceof String) {
        result[i] = _doubles[Integer.parseInt((String) indexObject)];
      } else {
        throw new IllegalArgumentException();
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
