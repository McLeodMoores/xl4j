package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Method;

import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result. 
 */
public class SimpleResultMethodInvoker extends AbstractMethodInvoker {
  /**
   * Constructor.
   * @param method  the method to call.
   * @param argumentConverters  the converters required to call the method
   * @param returnConverter  the converter required to convert he result back to an Excel type
   */
  public SimpleResultMethodInvoker(final Method method, final TypeConverter[] argumentConverters, 
                       final TypeConverter returnConverter) {
    super(method, argumentConverters, returnConverter);
  }

  @Override
  protected XLValue convertResult(final Object object, final TypeConverter returnConverter) {
    return returnConverter.toXLValue(null, object);
  }
}
