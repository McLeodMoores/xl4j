/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.util;

import java.lang.reflect.Array;

/**
 * Array utilities.
 */
public final class ArrayUtils {

  /**
   * Transpose a two dimensional array.  Corner cases are if the first dimension is of zero length, a zero-by-zero length array
   * is returned as it's not possible to determine the original second dimension length.  If the second dimension is zero length,
   * a zero by &gt;first-dim-length&lt; array is created and returned.
   * @param <T>  the type of the array
   * @param arr  the source array, not null
   * @return the transpose of the supplied array, see caveats above
   */
  @SuppressWarnings("unchecked")
  public static <T> T[][] transpose(final T[][] arr) {
    ArgumentChecker.notNull(arr, "arr");
    T[][] transposed;
    if (arr.length > 0) {
      // note we have to call getComponentType on the first dimension to get the core component type.
      transposed = (T[][]) Array.newInstance(arr.getClass().getComponentType().getComponentType(), arr[0].length, arr.length);
    } else {
      // cover the 0 by 0 case.
      transposed = (T[][]) Array.newInstance(arr.getClass().getComponentType().getComponentType(), 0, 0);
    }
    for (int j = 0; j < arr.length; j++) {
      for (int i = 0; i < arr[0].length; i++) {
        transposed[i][j] = arr[j][i];
      }
    }
    return transposed;
  }

  /**
   * Checks if an array is rectangular.  If the first dimension is 0 length, that is considered rectangular.  If the second dimension is
   * 0, all rows must be of zero length to qualify.
   * @param <T>  the type of the array
   * @param arr  a two dimensional array, not null
   * @return true, if the array is rectangular
   */
  public static <T> boolean isRectangular(final T[][] arr) {
    ArgumentChecker.notNull(arr, "arr");
    if (arr.length == 0) {
      return true; // well, it's certainly not stepped
    }
    // invariant that arr.length > 0
    final int len = arr[0].length;
    for (int i = 1; i < arr.length; i++) {
      if (arr[i].length != len) {
        return false;
      }
    }
    return true;
  }

  /**
   * Specify the expected orientation of a transposed array.
   * @see #transposeIfNeeded
   */
  public enum FixedDimension {
    /** Column. */
    COLUMNS,
    /** Row. */
    ROWS
  };

  /**
   * Provide a normalised array orientation.  You tell the method what fixed dimension you want and which dimension it is and
   * it will transpose the array if necessary.  Let's say we want to read a time series and so input data can will be date/value.
   * Therefore we specify it as (2xn), so we set fixedDimensionSize as 2 and fixedDimension as COLUMNS, and we always get a (2xn)
   * array, even if the input array was (nx2).
   * @param <T>  the type of the array
   * @param arr  the array to transpose if necessary
   * @param fixedDimensionSize  the size of the dimension that's fixed
   * @param fixedDimension  whether the fixed dimension size is the number of columns or number of rows
   * @return an array with of (fixedDimensionSize x n) if fixedDimension == COLUMNS), an array of (n x fixedDimensionSize) if fixedDimension == ROWS
   * @throws ArrayWrongSizeException  if one or other of the dimension of the input array is not equals to fixedDimensionSize
   */
  public static <T> T[][] transposeIfNeeded(final T[][] arr, final int fixedDimensionSize, final FixedDimension fixedDimension) throws ArrayWrongSizeException {
    ArgumentChecker.notNull(arr, "arr");
    ArgumentChecker.notNegative(fixedDimensionSize, "expectedDimensionSize");
    ArgumentChecker.notNull(fixedDimension, "fixedDimension");
    if (arr.length == 0) {
      return arr;
    }
    if (fixedDimension == FixedDimension.ROWS) {
      if (arr.length == fixedDimensionSize) {
        return arr;
      } else if (arr[0].length == fixedDimensionSize) {
        return transpose(arr);
      } else {
        throw new ArrayWrongSizeException("Array needed to be of size " + fixedDimensionSize
            + " in at least one dimension, but was " + arr.length + " x " + arr[0].length);
      }
    }
    // we already ruled out null...
    if (arr[0].length == fixedDimensionSize) {
      return arr;
    } else if (arr.length == fixedDimensionSize) {
      return transpose(arr);
    } else {
      throw new ArrayWrongSizeException("Array needed to be of size " + fixedDimensionSize
          + " in at least one dimension, but was " + arr.length + " x " + arr[0].length);
    }
  }
  
  public static <T> T[] makeRowOrColumnArray(final T[][] arr) throws ArrayWrongSizeException {
    if (arr.length == 1) {
      return arr[0];
    } else if (arr.length == 0) {
      throw new ArrayWrongSizeException("Array needed have at least one element");
    } else {
      if (isRectangular(arr)) {
        return transposeIfNeeded(arr, 1, FixedDimension.ROWS)[0];
      } else {
        throw new ArrayWrongSizeException("Array isn't rectangular");
      }
    }
  }

  private ArrayUtils() {
  }
}
