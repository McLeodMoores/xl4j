/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.Heap;
import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Type converter for general objects into XLObject handles. Note the lower priority, which means all the other converters get a crack at
 * doing something nicer first.
 */
public class ObjectXLObjectTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int OBJECT_CONVERTER_PRIORITY = 5;
  /** ConcurrentHeap containing XLObjects */
  private final Heap _heap;

  /**
   * Default constructor.
   *
   * @param excel
   *          the excel object to allow heap access
   */
  public ObjectXLObjectTypeConverter(final Excel excel) {
    super(Object.class, XLObject.class, OBJECT_CONVERTER_PRIORITY);
    _heap = ArgumentChecker.notNull(excel, "excel").getHeap();
  }

  @Override
  public Object toXLValue(final Object from) {
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
