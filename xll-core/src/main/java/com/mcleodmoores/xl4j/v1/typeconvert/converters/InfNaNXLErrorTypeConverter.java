/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Converts {@link XLError#Div0} to <code>Double#POSITIVE_INFINITY</code> and {@link XLError#NA} to <code>Double#NaN</code>. All other
 * errors are passed through.
 */
public class InfNaNXLErrorTypeConverter extends AbstractTypeConverter {
  /** Priority for this converter */
  private static final int DEFAULT_PRIORITY = 5; // lower priority than XLValueIdentityConverters

  /**
   * Constructor.
   */
  public InfNaNXLErrorTypeConverter() {
    super(Double.class, XLError.class, DEFAULT_PRIORITY);
  }

  @Override
  public Object toXLValue(final Object from) {
    if (from instanceof Double) {
      final Double fromd = (Double) from;
      if (fromd.isNaN()) {
        return XLError.NA;
      } else if (fromd.isInfinite()) {
        return XLError.Div0;
      }
      return XLNumber.of(fromd);
    }
    throw new XL4JRuntimeException("Should not attempt to directly convert from a Java object to XLError");
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
