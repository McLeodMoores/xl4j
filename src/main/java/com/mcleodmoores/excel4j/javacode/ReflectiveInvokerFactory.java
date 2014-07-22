/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.heap.WorksheetHeap;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.excel4j.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * 
 */
public class ReflectiveInvokerFactory implements InvokerFactory {
  private TypeConverterRegistry _typeConverterRegistry = new TypeConverterRegistry();
  private static final TypeConverter OBJECT_XLOBJECT_CONVERTER = new ObjectXLObjectTypeConverter();
  
  @Override
  public ConstructorInvoker getConstructorTypeConverter(final XLString className, 
                                                        @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes) 
                                                        throws ClassNotFoundException {
    Class<?> clazz = resolveClass(className);
    outer:
    for (Constructor<?> constructor : clazz.getConstructors()) {
      Class<?>[] genericParameterTypes = constructor.getParameterTypes();
      if (argTypes.length != genericParameterTypes.length) {
        continue; // number of arguments don't match so skip this one.
      }
      TypeConverter[] argumentConverters = new TypeConverter[genericParameterTypes.length];
      for (int i = 0; i < genericParameterTypes.length; i++) {
        argumentConverters[i] = _typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(argTypes[i], genericParameterTypes[i]));
        if (argumentConverters[i] == null) {
          continue outer;
        }
      }
      return new ConstructorInvoker(constructor, argumentConverters, OBJECT_XLOBJECT_CONVERTER);
    }
    throw new Excel4JRuntimeException("Could not find matching constructor");
  }
  
  @Override
  public MethodInvoker getMethodTypeConverter(final XLObject objectHandle, final XLString methodName, 
                                              @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes) 
                                              throws ClassNotFoundException {
    Excel excel = ExcelFactory.getInstance();
    WorksheetHeap worksheetHeap = excel.getWorksheetHeap();
    Object object = worksheetHeap.getObject(objectHandle.getHandle());
    Class<?> clazz = object.getClass();
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
      TypeConverter[] argumentConverters = new TypeConverter[genericParameterTypes.length];
      for (int i = 0; i < genericParameterTypes.length; i++) {
        argumentConverters[i] = _typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(argTypes[i], genericParameterTypes[i]));
        if (argumentConverters[i] == null) {
          continue outer;
        }
      }
      TypeConverter resultConverter = _typeConverterRegistry.findConverter(method.getReturnType());  // this might be swapped out for OBJECT_XLOBJECT_CONVERTER at run-time.
      return new MethodInvoker(method, argumentConverters, resultConverter);
    }
    throw new Excel4JRuntimeException("Could not find matching method");
  }

  /**
   * Invoke a static method.
   * @param className  the name of the class
   * @param methodName  the name of the method to invoke
   * @param argTypes  the arguments to pass to the method
   * @return a Method invoker
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public MethodInvoker getStaticMethodTypeConverter(final XLString className, final XLString methodName, 
                                                    @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes) 
                                                    throws ClassNotFoundException {
    Class<?> clazz = resolveClass(className); 
    // TODO: we should probably check here that object.getClass().getSimpleName() == objectHandle.getClazz()
    outer:
    for (Method method : clazz.getMethods()) {
      if (!method.getName().equals(methodName)) {
        continue; // method name doesn't match
      }
      Class<?>[] genericParameterTypes = method.getParameterTypes();
      if (argTypes.length != genericParameterTypes.length) {
        continue; // number of arguments don't match so skip this one.
      }
      TypeConverter[] argumentConverters = new TypeConverter[genericParameterTypes.length];
      for (int i = 0; i < genericParameterTypes.length; i++) {
        argumentConverters[i] = _typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(argTypes[i], genericParameterTypes[i]));
        if (argumentConverters[i] == null) {
          continue outer;
        }
      }
      
      TypeConverter resultConverter = _typeConverterRegistry.findConverter(method.getReturnType());
      return new MethodInvoker(method, argumentConverters, resultConverter);
    }
    throw new Excel4JRuntimeException("Could not find matching constructor");
  }
  
  /**
   * This is a separate method so we can do shorthand lookups later on (e.g. String instead of java.util.String).
   * @param className
   * @return a resolved class
   * @throws ClassNotFoundException 
   */
  private Class<?> resolveClass(final XLString className) throws ClassNotFoundException {
    return Class.forName(className.getValue());
  }

  /**
   * Return a method type converter read to process calls for a given static method.
   * This method is used when you already know the correct method to bind, such is when scanning annotated methods.
   * @param method  the method to be bound to.
   * @return a MethodTypeConverter that can perform the necessary type conversions each time the method is invoked
   * throws Excel4JRuntimeException if a type converter cannot be found
   */
  @Override
  public MethodInvoker getStaticMethodTypeConverter(final Method method) {
    Class<?>[] genericParameterTypes = method.getParameterTypes();
    TypeConverter[] argumentConverters = new TypeConverter[genericParameterTypes.length];
    for (int i = 0; i < genericParameterTypes.length; i++) {
      argumentConverters[i] = _typeConverterRegistry.findConverter(genericParameterTypes[i]);
      if (argumentConverters[i] == null) {
        throw new Excel4JRuntimeException("Could not find type converter for " + genericParameterTypes[i] + " (param " + i + ") method " + method.getName());
      }
    }
    
    TypeConverter resultConverter = _typeConverterRegistry.findConverter(method.getReturnType());
    return new MethodInvoker(method, argumentConverters, resultConverter);
  }
}
