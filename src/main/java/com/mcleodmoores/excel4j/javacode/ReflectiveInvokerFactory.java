/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.mcleodmoores.excel4j.ResultType;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.excel4j.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * 
 */
public class ReflectiveInvokerFactory implements InvokerFactory {
  private TypeConverterRegistry _typeConverterRegistry = new TypeConverterRegistry();
  private static final TypeConverter OBJECT_XLOBJECT_CONVERTER = new ObjectXLObjectTypeConverter();
  
  @Override
  public ConstructorInvoker getConstructorTypeConverter(final Class<?> clazz, 
                                                        @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes) 
                                                        throws ClassNotFoundException {
    outer:
    for (Constructor<?> constructor : clazz.getConstructors()) {
      Class<?>[] genericParameterTypes = constructor.getParameterTypes();
      if (argTypes.length != genericParameterTypes.length) {
        continue; // number of arguments don't match so skip this one.
      }
      TypeConverter[] argumentConverters = buildArgumentConverters(genericParameterTypes, argTypes);
      if (argumentConverters == null) {
        continue outer;
      }
      return new ConstructorInvoker(constructor, argumentConverters, OBJECT_XLOBJECT_CONVERTER);
    }
    throw new Excel4JRuntimeException("Could not find matching constructor");
  }
  
  @Override
  public MethodInvoker getMethodTypeConverter(final Class<?> clazz, final XLString methodName, final ResultType resultType,
                                               @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes) 
                                               throws ClassNotFoundException {
    // TODO: we should probably check here that object.getClass().getSimpleName() == objectHandle.getClazz()
    outer:
    for (Method method : clazz.getMethods()) {
      if (!method.getName().equals(methodName.getValue())) {
        continue; // name of method doesn't match.
      }
      Class<?>[] genericParameterTypes = (Class<?>[]) method.getParameterTypes();
      if (argTypes.length != genericParameterTypes.length) {
        continue; // number of arguments don't match so skip this one.
      }
      TypeConverter[] argumentConverters = buildArgumentConverters(genericParameterTypes, argTypes);
      if (argumentConverters == null) {
        continue outer;
      }
      // this might be swapped out for OBJECT_XLOBJECT_CONVERTER at run-time.
      TypeConverter resultConverter = _typeConverterRegistry.findConverter(method.getReturnType());  
      if (resultType == ResultType.SIMPLEST) {
        return new SimpleResultMethodInvoker(method, argumentConverters, resultConverter);
      } else {
        return new ObjectResultMethodInvoker(method, argumentConverters, resultConverter);
      }
    }
    throw new Excel4JRuntimeException("Could not find matching method");
  }
  
  private TypeConverter[] buildArgumentConverters(final Class<?>[] targetArgTypes, final Class<? extends XLValue>[] argTypes) {
    TypeConverter[] argumentConverters = new TypeConverter[targetArgTypes.length];
    for (int i = 0; i < targetArgTypes.length; i++) {
      argumentConverters[i] = _typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(argTypes[i], targetArgTypes[i]));
      if (argumentConverters[i] == null) {
        return null;
      }
    }
    return argumentConverters;
  }
  
  private TypeConverter[] buildArgumentConverters(final Class<?>[] targetArgTypes) {
    TypeConverter[] argumentConverters = new TypeConverter[targetArgTypes.length];
    for (int i = 0; i < targetArgTypes.length; i++) {
      argumentConverters[i] = _typeConverterRegistry.findConverter(targetArgTypes[i]);
      if (argumentConverters[i] == null) {
        throw new Excel4JRuntimeException("Could not find type converter for " + targetArgTypes[i] + " (param " + i + ")");
      }
    }
    return argumentConverters;
  }
  

  @Override
  public MethodInvoker getMethodTypeConverter(final Method method, final ResultType resultType) {
    Class<?>[] genericParameterTypes = method.getParameterTypes();
    try {
      TypeConverter[] argumentConverters = buildArgumentConverters(genericParameterTypes);
      TypeConverter resultConverter = _typeConverterRegistry.findConverter(method.getReturnType());
      if (resultType == ResultType.SIMPLEST) {
        return new SimpleResultMethodInvoker(method, argumentConverters, resultConverter);
      } else {
        return new ObjectResultMethodInvoker(method, argumentConverters, resultConverter);
      }
    } catch (Excel4JRuntimeException e) {
      // here we chain on the exception that details the parameter name that doesn't match.
      throw new Excel4JRuntimeException("Could not find matching method " + method, e);
    }
    
  }
}
