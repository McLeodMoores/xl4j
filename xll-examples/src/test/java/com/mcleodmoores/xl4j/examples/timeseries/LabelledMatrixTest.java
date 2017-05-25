/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link LabelledMatrix}.
 */
public class LabelledMatrixTest {
  private static final String[] NAMES = new String[] {"A", "B", "C", "D", "E", "F"};
  private static final double[][] MATRIX = new double[][] {
    new double[] {1, 2, 3, 4, 5, 6},
    new double[] {7, 8, 9, 10, 11, 12},
    new double[] {13, 14, 15, 16, 17, 18},
    new double[] {19, 20, 21, 22, 23, 24},
    new double[] {25, 26, 27, 28, 29, 30},
    new double[] {31, 32, 33, 34, 35, 36}
  };

  /**
   * Tests that the names cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullNames() {
    LabelledMatrix.of(null, MATRIX);
  }

  /**
   * Tests that the matrix cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullMatrix() {
    LabelledMatrix.of(NAMES, null);
  }

  /**
   * Tests that the matrix cannot be empty.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testEmptyMatrix() {
    LabelledMatrix.of(new String[0], new double[0][0]);
  }

  /**
   * Tests that the names and matrix must be the same size.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testDifferentSizeArrays() {
    final double[][] matrix = new double[][] {
      new double[] {1, 2, 3, 4, 5, 6},
      new double[] {7, 8, 9, 10, 11, 12},
      new double[] {13, 14, 15, 16, 17, 18},
      new double[] {19, 20, 21, 22, 23, 24},
      new double[] {25, 26, 27, 28, 29, 30}
    };
    LabelledMatrix.of(NAMES, matrix);
  }

  /**
   * Tests that the matrix must be square.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testRectangularArrays() {
    final double[][] matrix = new double[][] {
      new double[] {1, 2, 3, 4, 5, 6},
      new double[] {7, 8, 9, 10, 11, 12},
      new double[] {13, 14, 15, 16, 17, 18},
      new double[] {19, 20, 21, 22, 23, 24},
      new double[] {25, 26, 27, 28, 29, 30},
      new double[] {31, 32, 33, 34, 35}
    };
    LabelledMatrix.of(NAMES, matrix);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final LabelledMatrix m = LabelledMatrix.of(NAMES, MATRIX);
    assertEquals(m, LabelledMatrix.of(NAMES, MATRIX));
    assertEquals(m.hashCode(), LabelledMatrix.of(NAMES, MATRIX).hashCode());
    assertNotEquals(m, LabelledMatrix.of(new String[] {"A", "B", "C", "D", "E", "G"}, MATRIX));
    assertNotEquals(m, LabelledMatrix.of(NAMES, new double[][] {
      new double[] {1, 2, 3, 4, 5, 6},
      new double[] {7, 8, 9, 10, 11, 12},
      new double[] {13, 14, 15, 16, 17, 18},
      new double[] {19, 20, 21, 122, 23, 24},
      new double[] {25, 26, 27, 28, 29, 30},
      new double[] {31, 32, 33, 34, 35, 36}
    }));
    assertEquals(m.getSize(), 6);
    for (int i = 0; i < 6; i++) {
      assertEquals(m.getLabelAt(i), NAMES[i]);
      for (int j = 0; j < 6; j++) {
        assertEquals(m.getValueAt(i, j), MATRIX[i][j]);
      }
    }
  }

  /**
   * Tests the expand function.
   */
  @Test
  public void testExpand() {
    final Object[][] expected = new Object[][] {
      new Object[] {"", "A", "B", "C", "D", "E", "F"},
      new Object[] {"A", 1, 2, 3, 4, 5, 6},
      new Object[] {"B", 7, 8, 9, 10, 11, 12},
      new Object[] {"C", 13, 14, 15, 16, 17, 18},
      new Object[] {"D", 19, 20, 21, 22, 23, 24},
      new Object[] {"E", 25, 26, 27, 28, 29, 30},
      new Object[] {"F", 31, 32, 33, 34, 35, 36},
    };
    final Object[][] actual = LabelledMatrix.of(NAMES, MATRIX).expandAsArray();
    for (int i = 0; i < 7; i++) {
      for (int j = 0; j < 7; j++) {
        if (actual[i][j] instanceof String) {
          assertEquals(actual[i][j], expected[i][j]);
        } else {
          assertEquals(((Number) actual[i][j]).doubleValue(), ((Number) expected[i][j]).doubleValue(), 1e-15);
        }
      }
    }
  }
}
