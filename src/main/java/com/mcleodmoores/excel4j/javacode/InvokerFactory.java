/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Interface for factory that provides pre-bound method/constructor/field type converters.
 * The implementation may directly query the object model, or be a caching layer.
 */
public interface InvokerFactory {
  /**
   * Return a constructor type converter ready to process calls for a given constructor.
   * Throws a Excel4JRuntimeException if it can't find a matching constructor.
   * @param className the class name as an XLString
   * @param args a VarArg of XLValues to be marshaled into Java types
   * @return a ConstructorTypeConverter that can perform the necessary conversions when necessary
   * @throws ClassNotFoundException if the specified class cannot be found
   */
  ConstructorInvoker getConstructorTypeConverter(final XLString className, final XLValue... args) throws ClassNotFoundException;
}