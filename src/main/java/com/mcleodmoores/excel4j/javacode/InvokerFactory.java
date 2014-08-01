/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Method;

import com.mcleodmoores.excel4j.ResultType;
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
   * @param clazz  the class to get the constructor for
   * @param argTypes  a VarArg of the classes of the XLValues to be marshaled into Java types
   * @return a ConstructorTypeConverter that can perform the necessary conversions each time the constructor is invoked
   * @throws ClassNotFoundException if the specified class cannot be found
   */
  ConstructorInvoker getConstructorTypeConverter(final Class<?> clazz, 
                                                 @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes) 
                                                 throws ClassNotFoundException;

  /**
   * Return a method type converter read to process calls for a given method.
   * @param clazz  the class the method belongs to
   * @param methodName  the name of the method as an XLString
   * @param resultType  whether the result should be an object or simplified if possible
   * @param argTypes a VarArg of the classes of the XLValues to be marshaled into Java types
   * @return a MethodTypeConverter that can perform the necessary type conversions each time the method is invoked
   * @throws ClassNotFoundException if the specified class cannot be found
   * throws Excel4JRuntimeException if a matching method cannot be found
   */
  MethodInvoker getMethodTypeConverter(Class<?> clazz, XLString methodName, ResultType resultType,
                                       @SuppressWarnings("unchecked") Class<? extends XLValue>... argTypes) 
                                       throws ClassNotFoundException;

  /**
   * Return a method type converter read to process calls for a given static method.
   * This method is used when you already know the correct method to bind, such is when scanning annotated methods.
   * @param method  the method to be bound to.
   * @param resultType  whether the result should be an object or simplified if possible
   * @return a MethodTypeConverter that can perform the necessary type conversions each time the method is invoked
   * throws Excel4JRuntimeException if a type converter cannot be found
   */
  MethodInvoker getMethodTypeConverter(Method method, ResultType resultType);
}