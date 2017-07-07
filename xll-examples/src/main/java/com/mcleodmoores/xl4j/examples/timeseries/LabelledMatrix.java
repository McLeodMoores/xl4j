/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.Arrays;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Represents a labelled square matrix.
 */
public final class LabelledMatrix {

  /**
   * Constructs a labelled matrix. The input array must be square.
   *
   * @param names
   *          the labels
   * @param matrix
   *          the matrix
   * @return
   *          a labelled matrix
   */
  public static LabelledMatrix of(final String[] names, final double[][] matrix) {
    ArgumentChecker.notNull(names, "names");
    ArgumentChecker.notNull(matrix, "matrix");
    final int n = names.length;
    ArgumentChecker.isTrue(n > 0, "Matrix size must be greater than 0");
    ArgumentChecker.isTrue(n == matrix.length, "Names array and matrix rows must be the same length, have {} and {}", n, matrix.length);
    final String[] copyNames = new String[names.length];
    final double[][] copyMatrix = new double[matrix.length][];
    int i = 0;
    for (final double[] row : matrix) {
      ArgumentChecker.isTrue(n == row.length, "Names array and matrix columns must be the same, have {} and {}", n, row.length);
      copyMatrix[i] = new double[row.length];
      copyNames[i] = names[i];
      System.arraycopy(row, 0, copyMatrix[i++], 0, row.length);
    }
    return new LabelledMatrix(copyNames, copyMatrix);
  }

  private final String[] _names;
  private final double[][] _matrix;

  private LabelledMatrix(final String[] names, final double[][] matrix) {
    _names = names;
    _matrix = matrix;
  }

  /**
   * Expands the matrix as an array. The first row and column contain the labels.
   *
   * @return
   *          the matrix as an array
   */
  @XLFunction(name = "Matrix.ExpandAsArray", description = "Expands the matrix as an array", category = "Matrix")
  public Object[][] expandAsArray() {
    final int n = _names.length;
    final Object[][] result = new Object[n + 1][n + 1];
    for (int i = 0; i < n + 1; i++) {
      if (i == 0) {
        result[i][0] = "";
        for (int j = 0; j < n; j++) {
          result[i][j + 1] = _names[j];
        }
      } else {
        result[i][0] = _names[i - 1];
        for (int j = 1; j < n + 1; j++) {
          result[i][j] = _matrix[i - 1][j - 1];
        }
      }
    }
    return result;
  }

  /**
   * Gets the size of the square matrix.
   *
   * @return
   *          the size
   */
  @XLFunction(name = "Matrix.Size", description = "Gets the matrix size", category = "Matrix")
  public int getSize() {
    return _names.length;
  }

  /**
   * Gets the ith label.
   *
   * @param i
   *          the index
   * @return
   *          the label
   */
  @XLFunction(name = "Matrix.LabelAt", description = "Gets the ith label", category = "Matrix")
  public String getLabelAt(@XLParameter(name = "i") final int i) {
    return _names[i];
  }

  /**
   * Gets the (row, column)th value.
   *
   * @param row
   *          the row index
   * @param column
   *          the column index
   * @return
   *          the value
   */
  @XLFunction(name = "Matrix.ValueAt", description = "Gets the (row, column)th value", category = "Matrix")
  public double getValueAt(@XLParameter(name = "row") final int row, @XLParameter(name = "column") final int column) {
    return _matrix[row][column];
  }

  /**
   * Gets a copy of the underlying matrix.
   * @return
   *          a copy of the underlying matrix
   */
  public double[][] getUnderlyingMatrix() {
    final double[][] copy = new double[_matrix.length][];
    int i = 0;
    for (final double[] row : _matrix) {
      copy[i] = new double[row.length];
      System.arraycopy(row, 0, copy[i++], 0, row.length);
    }
    return copy;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.deepHashCode(_matrix);
    result = prime * result + Arrays.hashCode(_names);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LabelledMatrix)) {
      return false;
    }
    final LabelledMatrix other = (LabelledMatrix) obj;
    if (!Arrays.equals(_names, other._names)) {
      return false;
    }
    if (!Arrays.deepEquals(_matrix, other._matrix)) {
      return false;
    }
    return true;
  }


}
