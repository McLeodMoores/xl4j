/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Interface for factory that provides pre-bound method/constructor/field type converters. The implementation may directly query the object
 * model, or be a caching layer.
 */
public interface InvokerFactory {

  /**
   * Return a constructor type converter ready to process calls for a given constructor. Throws a XL4JRuntimeException if it can't find a
   * matching constructor.
   *
   * @param clazz
   *          the class to get the constructor for
   * @param typeConversionMode
   *          whether to convert parameters, whether to simplify results, return objects or just pass through
   * @param argTypes
   *          a VarArg of the classes of the XLValues to be marshaled into Java types
   * @return a ConstructorInvoker that can perform the necessary conversions each time the constructor is invoked
   */
  ConstructorInvoker[] getConstructorTypeConverter(Class<?> clazz, TypeConversionMode typeConversionMode,
      @SuppressWarnings("unchecked") Class<? extends XLValue>... argTypes);

  /**
   * Return a method type converter ready to process calls for a given method.
   *
   * @param clazz
   *          the class the method belongs to
   * @param methodName
   *          the name of the method as an XLString
   * @param typeConversionMode
   *          whether the result should be an object, simplified if possible, or passed through
   * @param argTypes
   *          a VarArg of the classes of the XLValues to be marshaled into Java types
   * @return a MethodInvoder that can perform the necessary type conversions each time the method is invoked
   */
  MethodInvoker[] getMethodTypeConverter(Class<?> clazz, XLString methodName, TypeConversionMode typeConversionMode,
      @SuppressWarnings("unchecked") Class<? extends XLValue>... argTypes);

  /**
   * Return a constructor type converter ready to process calls for a given constructor. This method is used when you already know the
   * correct constructor to bind, such as when scanning annotated constructors.
   *
   * @param constructor
   *          the constructor to be bound to
   * @return a ConstructorInvoker that can perform the necessary type conversions each time the method is invoked
   * @throws
   *         XL4JRuntimeException if a type converter cannot be found
   */
  ConstructorInvoker getConstructorTypeConverter(Constructor<?> constructor);

  /**
   * Return a method type converter ready to process calls for a given method. This method is used when you already know the correct method
   * to bind, such as when scanning annotated methods.
   *
   * @param method
   *          the method to be bound to
   * @param typeConversionMode
   *          whether the result should be an object, simplified if possible or passed through unchanged
   * @return a MethodInvoker that can perform the necessary type conversions each time the method is invoked
   * @throws XL4JRuntimeException
   *         if a type converter cannot be found
   */
  MethodInvoker getMethodTypeConverter(Method method, TypeConversionMode typeConversionMode);

  /**
   * Return a field type converter ready to process calls for a given field.
   *
   * @param field
   *          the field
   * @param typeConversionMode
   *          whether the result should be an object, simplified or passed through unchanged
   * @return a FieldGetter that can perform the necessary type conversions each time the field is requested
   * @throws XL4JRuntimeException
   *          if a type conversion cannot be found
   */
  FieldGetter getFieldTypeConverter(Field field, TypeConversionMode typeConversionMode);
}