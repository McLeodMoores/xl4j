/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import java.lang.reflect.Method;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.values.XLMissing;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result.
 */
public class SimpleResultMethodInvoker extends AbstractMethodInvoker {

  /**
   * Constructor.
   *
   * @param method
   *          the method to call.
   * @param argumentConverters
   *          the converters required to call the method
   * @param returnConverter
   *          the converter required to convert he result back to an Excel type
   */
  public SimpleResultMethodInvoker(final Method method, final TypeConverter[] argumentConverters, final TypeConverter returnConverter,
      final TypeConverter objectXlObjectConverter) {
    super(method, argumentConverters, returnConverter, objectXlObjectConverter);
  }

  @Override
  protected XLValue convertResult(final Object object, final TypeConverter returnConverter) {
    if (object != null) {
      final AbstractTypeConverter scalarTypeConverter = (AbstractTypeConverter) returnConverter;
      return (XLValue) scalarTypeConverter.toXLValue(object);
    }
    return XLMissing.INSTANCE;
  }
}
