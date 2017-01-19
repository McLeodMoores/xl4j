/**
 *
 */
package com.mcleodmoores.xl4j.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Tests construction of Strings from the function processor.
 */
public class StringConstructionTest extends TypeConstructionTests {

  /**
   * Tests construction of a String using new String().
   */
  @Test
  public void testJConstruct() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of("java.lang.String"));
    assertTrue(xlValue instanceof XLObject);
    final Object object = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(object instanceof String);
    final String string = (String) object;
    assertTrue(string.isEmpty());
  }

  /**
   * Tests construction of a String using new String(int[], int, int).
   */
  @Test
  public void testJConstructWithParameters() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of("java.lang.String"),
        XLArray.of(new XLValue[][] {new XLValue[]{XLNumber.of(41), XLNumber.of(42), XLNumber.of(43)}}), XLNumber.of(0), XLNumber.of(2));
    assertTrue(xlValue instanceof XLObject);
    final Object object = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(object instanceof String);
    final String expectedString = new String(new int[] {41, 42, 43}, 0, 2); // *) apparently
    assertEquals(object, expectedString);
  }

  /**
   * Tests construction of a String using new String(char[]).
   */
  @Test
  public void testJConstructCharArray() {
    final char[] charArray = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
    final XLValue[][] xlCharArray = new XLValue[1][charArray.length];
    for (int i = 0; i < charArray.length; i++) {
      xlCharArray[0][i] = XLString.of(charArray[i]);
    }
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of("java.lang.String"), XLArray.of(xlCharArray));
    assertTrue(xlValue instanceof XLObject);
    final Object object = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(object instanceof String);
    final String string = (String) object;
    assertEquals(string, new String(charArray));
  }

  /**
   * Tests construction of a String using String.valueOf(double).
   */
  @Test
  public void testJMethod() {
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of("java.lang.String"), XLString.of("valueOf"), XLNumber.of(10));
    assertTrue(xlValue instanceof XLObject);
    final Object object = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(object instanceof String);
    final String string = (String) object;
    assertEquals(string, String.valueOf(10));
  }
}
