/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.examples;

/**
 * Simple mathematical operations.
 * @param <T>  the type of the object being operated on
 */
public interface Operation<T> {

  /**
   * Adds the input to this object.
   * @param other  the other object
   * @return  the sum
   */
  T add(T other);

  /**
   * Subtracts the input from this object.
   * @param other  the other object
   * @return  the difference
   */
  T subtract(T other);

  /**
   * Multiplies this object by the input.
   * @param other  the other object
   * @return  the product
   */
  T multiply(T other);

  /**
   * Divides this object by the input.
   * @param other  the other object
   * @return  the quotient
   */
  T divide(T other);

  /**
   * Scales this object by a constant value.
   * @param scale  the scale
   * @return  the scaled object
   */
  T scale(double scale);

  /**
   * Gets the absolute value of this object.
   * @return  the absolute value
   */
  T abs();

  /**
   * Gets the reciprocal of this object.
   * @return  the reciprocal
   */
  T reciprocal();
}
