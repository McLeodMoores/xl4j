/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLError;

/**
 * Converts {@link XLError#Div0} to <code>Double#POSITIVE_INFINITY</code> and {@link XLError#NA} to <code>Double#NaN</code>.
 * All other errors are passed through.
 */
public class InfNaNXLErrorTypeConverter extends AbstractTypeConverter {
  /** Priority for this converter */
  private static final int DEFAULT_PRIORITY = 50; // higher priority than XLValueIdentityConverters

  /**
   * Constructor.
   */
  public InfNaNXLErrorTypeConverter() {
    super(Double.class, XLError.class, DEFAULT_PRIORITY);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    throw new Excel4JRuntimeException("Should not attempt to directly convert from a Java object to XLError");
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final XLError error = (XLError) from;
    switch (error) {
      case NA:
        return Double.NaN;
      case Div0:
        return Double.POSITIVE_INFINITY;
      default:
        return from;
    }
  }
}
