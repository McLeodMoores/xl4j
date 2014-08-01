package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.heap.Heap;
import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter for general objects into XLObject handles.  Note the lower priority,
 * which means all the other converters get a crack at doing something nicer first.
 */
public class ObjectXLObjectTypeConverter extends AbstractTypeConverter {

  private static final int OBJECT_CONVERTER_PRIORITY = 5;

  /**
   * Default constructor.
   */
  public ObjectXLObjectTypeConverter() {
    super(Object.class, XLObject.class, OBJECT_CONVERTER_PRIORITY);
  }

  @Override
  public XLValue toXLValue(final Class<? extends XLValue> expectedClass, final Object from) {
    Heap heap = ExcelFactory.getInstance().getHeap();
    return XLObject.of(from.getClass(), heap.getHandle(from));
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final XLValue from) {
    Heap heap = ExcelFactory.getInstance().getHeap();
    XLObject xlObj = (XLObject) from;
    return heap.getObject(xlObj.getHandle());
  }

}
