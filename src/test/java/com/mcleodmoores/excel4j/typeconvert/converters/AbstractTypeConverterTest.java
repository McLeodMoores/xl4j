/**
 *
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
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
    new AbstractTypeConverter(null, XLNumber.class) {

      @Override
      public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
        return null;
      }

      @Override
      public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
        return null;
      }
    };
  }

  /**
   * Tests the exception thrown when the Excel type is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXLType() {
    new AbstractTypeConverter(Double.class, null) {

      @Override
      public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
        return null;
      }

      @Override
      public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
        return null;
      }
    };
  }
}
