/**
 *
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

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
   * Tests construction of a String using new String(char[]).
   */
  @Test
  public void testJConstructCharArray() {
    fail();
    final char[] charArray = {'H', 'e', 'l', 'l', 'o' , ' ', 'W', 'o', 'r', 'l', 'd'};
    final XLValue[][] xlCharArray = new XLValue[1][charArray.length];
    for (int i = 0; i < charArray.length; i++) {
      xlCharArray[0][i] = null; //TODO
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
    assertEquals(string, String.valueOf(10d));
  }
}
