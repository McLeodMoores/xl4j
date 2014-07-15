package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.heap.WorksheetHeap;
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
  public XLValue toXLValue(final Object from) {
    WorksheetHeap heap = ExcelFactory.getInstance().getWorksheetHeap();
    return XLObject.of(from.getClass(), heap.getHandle(from));
  }

  @Override
  public Object toJavaObject(final XLValue from) {
    WorksheetHeap heap = ExcelFactory.getInstance().getWorksheetHeap();
    XLObject xlObj = (XLObject) from;
    return heap.getObject(xlObj.getHandle());
  }

}
