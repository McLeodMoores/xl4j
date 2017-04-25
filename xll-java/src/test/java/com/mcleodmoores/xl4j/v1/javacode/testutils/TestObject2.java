/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode.testutils;

import java.util.Arrays;

/**
 * Test object.
 */
public class TestObject2 {
  /** Objects */
  private final Object[] _objects;

  /**
   * @param s
   *          a string
   */
  public TestObject2(final String s) {
    _objects = new Object[] {s};
  }

  /**
   * @param objects
   *          varargs array of object
   */
  public TestObject2(final Object... objects) {
    _objects = objects;
  }

  /**
   * Gets the array of object.
   * @return
   *          the objects
   */
  public Object[] getObjects() {
    return _objects;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_objects);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TestObject2)) {
      return false;
    }
    final TestObject2 other = (TestObject2) obj;
    if (!Arrays.equals(_objects, other._objects)) {
      return false;
    }
    return true;
  }


}
