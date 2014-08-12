package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.heap.Heap;
import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLObject;

/**
 * Type converter for general objects into XLObject handles.  Note the lower priority,
 * which means all the other converters get a crack at doing something nicer first.
 */
public class ObjectXLObjectTypeConverter extends AbstractTypeConverter {

  private static final int OBJECT_CONVERTER_PRIORITY = 5;
  private final Heap _heap;

  /**
   * Default constructor.
   * @param heap  the excel object heap 
   */
  public ObjectXLObjectTypeConverter(final Heap heap) {
    super(Object.class, XLObject.class, OBJECT_CONVERTER_PRIORITY);
    _heap = heap;
  }

  @Override
  public Object toXLValue(final Class<?> expectedClass, final Object from) {
    return XLObject.of(from.getClass(), _heap.getHandle(from));
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final Object from) {
    XLObject xlObj = (XLObject) from;
    return _heap.getObject(xlObj.getHandle());
  }

}
