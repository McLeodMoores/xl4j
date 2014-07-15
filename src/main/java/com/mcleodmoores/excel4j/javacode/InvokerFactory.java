/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import com.mcleodmoores.excel4j.values.XLObject;
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
   * @param argTypes a VarArg of the classes of the XLValues to be marshaled into Java types
   * @return a ConstructorTypeConverter that can perform the necessary conversions each time the constructor is invoked
   * @throws ClassNotFoundException if the specified class cannot be found
   */
  ConstructorInvoker getConstructorTypeConverter(final XLString className, 
                                                 @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes) 
                                                 throws ClassNotFoundException;

  /**
   * Return a method type converter read to process calls for a given static method.
   * @param className  the name of the clalss as an XLString
   * @param methodName  the name of the method as an XLString
   * @param argTypes a VarArg of the classes of the XLValues to be marshaled into Java types
   * @return a MethodTypeConverter that can perform the necessary type conversions each time the method is invoked
   * @throws ClassNotFoundException if the specified class cannot be found
   * throws Excel4JRuntimeException if a matching method cannot be found
   */
  MethodInvoker getStaticMethodTypeConverter(XLString className, XLString methodName, 
                                             @SuppressWarnings("unchecked") Class<? extends XLValue>... argTypes) 
                                             throws ClassNotFoundException;

  /**
   * Return a method type converter read to process calls for a given method.
   * @param objectHandle  the handle for the object on which the method is to be invoked (XLObject)
   * @param methodName  the name of the method as an XLString
   * @param argTypes a VarArg of the classes of the XLValues to be marshaled into Java types
   * @return a MethodTypeConverter that can perform the necessary type conversions each time the method is invoked
   * @throws ClassNotFoundException if the specified class cannot be found
   * throws Excel4JRuntimeException if a matching method cannot be found
   */
  MethodInvoker getMethodTypeConverter(XLObject objectHandle, XLString methodName, 
                                       Class<? extends XLValue>... argTypes) 
                                       throws ClassNotFoundException;

  




}