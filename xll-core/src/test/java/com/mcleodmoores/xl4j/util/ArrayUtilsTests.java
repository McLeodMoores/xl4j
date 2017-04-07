package com.mcleodmoores.xl4j.util;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class ArrayUtilsTests {
  @Test
  public void testTranspose() {
    Integer[][] arr = new Integer[][] { { 0, 1 }, { 2, 3 }, { 4, 5 } };
    Assert.assertEquals(arr.length, 3);
    Assert.assertEquals(arr[0].length, 2);
    Assert.assertEquals((int) arr[0][0], 0);
    Assert.assertEquals((int) arr[1][0], 2);
    Assert.assertEquals((int) arr[2][0], 4);
    Assert.assertEquals((int) arr[0][1], 1);
    Assert.assertEquals((int) arr[1][1], 3);
    Assert.assertEquals((int) arr[2][1], 5);
    Integer[][] trans = ArrayUtils.transpose(arr);
    Assert.assertEquals(trans.length, 2);
    Assert.assertEquals(trans[0].length, 3);
    Assert.assertEquals((int) trans[0][0], 0);
    Assert.assertEquals((int) trans[0][1], 2);
    Assert.assertEquals((int) trans[0][2], 4);
    Assert.assertEquals((int) trans[1][0], 1);
    Assert.assertEquals((int) trans[1][1], 3);
    Assert.assertEquals((int) trans[1][2], 5);
  }

  @Test
  public void testTransposeEmptyFirstDim() {
    Integer[][] arr = new Integer[0][6];
    Assert.assertEquals(arr.length, 0);
    try {
      Assert.assertEquals(arr[0].length, 6);
      Assert.fail();
    } catch (ArrayIndexOutOfBoundsException aioobe) {
    }
    Integer[][] trans = ArrayUtils.transpose(arr);
    Assert.assertEquals(trans.length, 0); // information was lost, you'd expect 6.
  }
  
  @Test
  public void testTransposeEmptySecondDim() {
    Integer[][] arr = new Integer[6][0];
    Assert.assertEquals(arr.length, 6);
    Assert.assertEquals(arr[0].length, 0);
    Integer[][] trans = ArrayUtils.transpose(arr);
    Assert.assertEquals(trans.length, 0);
    try {
      Assert.assertEquals(trans[0].length, 3);
      Assert.fail("Exception not thrown");
    } catch (ArrayIndexOutOfBoundsException aioobe) {
    }
  }
  
  @Test
  public void testTransposeEmptyBothDims() {
    Integer[][] arr = new Integer[0][0];
    Assert.assertEquals(arr.length, 0);
    try {
      Assert.assertEquals(arr[0].length, 0);
      Assert.fail("Exception not thrown");
    } catch (ArrayIndexOutOfBoundsException aioobe) {
    }
    Integer[][] trans = ArrayUtils.transpose(arr);
    Assert.assertEquals(trans.length, 0);
    try {
      Assert.assertEquals(trans[0].length, 0);
      Assert.fail("Exception not thrown");
    } catch (ArrayIndexOutOfBoundsException aioobe) {
    }
  }
  
  @Test
  public void testIsRectangular() {
    Integer[][] arr = new Integer[10][];
    for (int i = 0; i < 10; i++) {
      arr[i] = new Integer[i];
    }
    Assert.assertFalse(ArrayUtils.isRectangular(arr));
    Integer[][] arr2 = new Integer[10][10];
    Assert.assertTrue(ArrayUtils.isRectangular(arr2));
    arr2[9] = new Integer[9];
    Assert.assertFalse(ArrayUtils.isRectangular(arr2));
    Integer[][] arr3 = new Integer[0][0];
    Assert.assertTrue(ArrayUtils.isRectangular(arr3));
    Integer[][] arr4 = new Integer[10][0];
    Assert.assertTrue(ArrayUtils.isRectangular(arr4));
    arr4[9] = new Integer[1];
    Assert.assertFalse(ArrayUtils.isRectangular(arr4));
    Integer[][] arr5 = new Integer[0][10];
    Assert.assertTrue(ArrayUtils.isRectangular(arr5));
    // can't change the shape.
  }
}
