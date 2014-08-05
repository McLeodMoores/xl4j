package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Method;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result. 
 */
public class ObjectResultMethodInvoker extends AbstractMethodInvoker {
  private static final AbstractTypeConverter OBJECT_XLOBJECT_CONVERTER = new ObjectXLObjectTypeConverter();

  /**
   * Constructor.
   * @param method  the method to call.
   * @param argumentConverters  the converters required to call the method
   * @param returnConverter  the converter required to convert he result back to an Excel type
   */
  public ObjectResultMethodInvoker(final Method method, final TypeConverter[] argumentConverters, 
                       final TypeConverter returnConverter) {
    super(method, argumentConverters, returnConverter);
  }

  @Override
  protected XLValue convertResult(final Object object, final TypeConverter returnConverter) {
    if (object instanceof XLObject) { 
      // if multiple layers of method invoker, don't put already encoded XLObjects on heap.
      // this happens in case of JMethod/JConstruct etc.
      return (XLValue) object;
    } else {
      return (XLValue) OBJECT_XLOBJECT_CONVERTER.toXLValue(null, object);
    }
  }

}
