/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.heap.Heap;
import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLObject;

/**
 * Type converter for general objects into XLObject handles.  Note the lower priority,
 * which means all the other converters get a crack at doing something nicer first.
 */
public class ObjectXLObjectTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int OBJECT_CONVERTER_PRIORITY = 5;
  /** Heap containing XLObjects */
  private final Heap _heap;

  /**
   * Default constructor.
   * @param excel  the excel object to allow heap access
   */
  public ObjectXLObjectTypeConverter(final Excel excel) {
    super(Object.class, XLObject.class, OBJECT_CONVERTER_PRIORITY);
    _heap = excel.getHeap();
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLObject.of(from.getClass().getSimpleName(), _heap.getHandle(from));
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final XLObject xlObj = (XLObject) from;
    return _heap.getObject(xlObj.getHandle());
  }

}
