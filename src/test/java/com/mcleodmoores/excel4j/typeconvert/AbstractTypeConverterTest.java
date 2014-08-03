/**
 *
 */
package com.mcleodmoores.excel4j.typeconvert;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Tests a dummy {@link AbstractTypeConverter} implementation.
 */
@Test
public class AbstractTypeConverterTest {

  /**
   * Tests the exception thrown when the java type is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullJavaType() {
    /**
     * Test converter.
     */
    class NullTypeConverterTest extends AbstractTypeConverter {
      /**
       * No-arg constructor.
       */
      public NullTypeConverterTest() {
        super((Class<?>) null, XLNumber.class);
      }
      
      @Override
      public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
        return null;
      }

      @Override
      public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
        return null;
      }
    }
    new NullTypeConverterTest();
  }

  /**
   * Tests the exception thrown when the Excel type is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXLType() {
    /**
     * Test converter.
     */
    class NullTypeConverterTest extends AbstractTypeConverter {
      /**
       * No-args constructor.
       */
      public NullTypeConverterTest() {
        super(Double.class, (Class<? extends XLValue>) null);
      }

      @Override
      public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
        return null;
      }

      @Override
      public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
        return null;
      }
    }
    new NullTypeConverterTest();
  }
}
