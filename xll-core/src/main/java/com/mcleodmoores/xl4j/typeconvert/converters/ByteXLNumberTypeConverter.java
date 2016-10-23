/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.values.XLNumber;

/**
 * Type converter to convert from bytes to Excel Numbers and back again.
 */
public final class ByteXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public ByteXLNumberTypeConverter() {
    super(Byte.class, XLNumber.class);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of((Byte) from);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return (byte) ((XLNumber) from).getValue();
  }
}
