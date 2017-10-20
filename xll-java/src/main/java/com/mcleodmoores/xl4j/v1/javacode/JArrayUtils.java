/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;

/**
 * Array utilities for Excel. Contains functions that allow the creation of arrays (primitive and object).
 */
@XLNamespace("J")
public final class JArrayUtils {

  private JArrayUtils() { }
  
  /**
   * Create a primitive double array from a sequence of numbers.
   * @param values a sequence of number parameters
   * @return a primitive double array containing the numbers
   */
  @XLFunction(
      name = "PrimitiveDoubleArray",
      description = "Return a double[]",
      category = "Java",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public static double[] primitiveDoubleArray(
      @XLParameter(name = "Values") final XLNumber... values) {
    final int n = values.length;
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = values[i].getAsDouble();
    }
    return result;
  }
  
  /**
   * Create a primitive double array from a range of numbers.
   * @param values a range of numbers
   * @return a primitive double array containing the numbers
   */
  @XLFunction(
      name = "PrimitiveDoubleArrayFromRange",
      description = "Return a double[]",
      category = "Java",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public static double[] primitiveDoubleArrayFromRange(
      @XLParameter(name = "Values") final XLArray values) {
    final XLValue[][] array = values.getArray();
    if (values.isArea()) {
      final int n = array.length * array[0].length;
      final double[] result = new double[n];
      for (int i = 0, k = 0; i < array.length; i++, k++) {
        for (int j = 0; j < array[0].length; j++, k++) {
          result[k] = ((XLNumber) array[i][j]).getAsDouble();
        }
      }
      return result;
    }
    if (values.isColumn()) {
      final int n = array[0].length;
      final double[] result = new double[n];
      for (int i = 0; i < n; i++) {
        result[i] = ((XLNumber) array[0][i]).getAsDouble();
      }
      return result;
    }
    final int n = array.length;
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = ((XLNumber) array[i][0]).getAsDouble();
    }
    return result;
  }

  /**
   * Create a 2D primitive double array from a range of numbers.
   * @param values a range of numbers
   * @return a 2D primitive double array containing the numbers
   */
  @XLFunction(
      name = "PrimitiveDoubleArray2D",
      description = "Return a double[][]",
      category = "Java",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public static double[][] primitiveDoubleArray2d(
      @XLParameter(name = "Values") final XLArray values) {
    final XLValue[][] array = values.getArray();
    final double[][] result = new double[array.length][array[0].length];
    for (int i = 0; i < array.length; i++) {
      for (int j = 0; j < array[0].length; j++) {
        result[i][j] = ((XLNumber) array[i][j]).getAsDouble();
      }
    }
    return result;
  }

  /**
   * Create a primitive float array from a sequence of numbers.
   * @param values a sequence of number parameters
   * @return a primitive float array containing the numbers
   */
  @XLFunction(
      name = "PrimitiveFloatArray",
      description = "Return a float[]",
      category = "Java")
  public static float[] primitiveFloatArray(
      @XLParameter(name = "Values") final XLNumber... values) {
    final int n = values.length;
    final float[] result = new float[n];
    for (int i = 0; i < n; i++) {
      result[i] = values[i].getAsFloat();
    }
    return result;
  }

  /**
   * Create a primitive long array from a sequence of numbers.
   * @param values a sequence of numbers
   * @return a primitive long array containing the numbers
   */
  @XLFunction(
      name = "PrimitiveLongArray",
      description = "Return a long[]",
      category = "Java")
  public static long[] primitiveLongArray(
      @XLParameter(name = "Values") final XLNumber... values) {
    final int n = values.length;
    final long[] result = new long[n];
    for (int i = 0; i < n; i++) {
      result[i] = values[i].getAsLong();
    }
    return result;
  }
  
  /**
   * Create a primitive int array from a sequence of numbers.
   * @param values a sequence of numbers
   * @return a primitive int array containing the numbers
   */
  @XLFunction(
      name = "PrimitiveIntArray",
      description = "Return a int[]",
      category = "Java")
  public static int[] primitiveIntArray(
      @XLParameter(name = "Values") final XLNumber... values) {
    final int n = values.length;
    final int[] result = new int[n];
    for (int i = 0; i < n; i++) {
      result[i] = values[i].getAsInt();
    }
    return result;
  }
}
