/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

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
  private TypeConverterRegistry _typeConverterRegistry;
  private static final TypeConverter OBJECT_XLOBJECT_CONVERTER = new ObjectXLObjectTypeConverter();
  
  @Override
  public ConstructorInvoker getConstructorTypeConverter(final XLString className, 
                                                        @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes) 
                                                        throws ClassNotFoundException {
    Class<?> clazz = resolveClass(className);
    for (Constructor<?> constructor : clazz.getConstructors()) {
      Type[] genericParameterTypes = constructor.getGenericParameterTypes();
      if (argTypes.length != genericParameterTypes.length) {
        continue; // number of arguments don't match so skip this one.
      }
      TypeConverter[] argumentConverters = new TypeConverter[genericParameterTypes.length];
      for (int i = 0; i < genericParameterTypes.length; i++) {
        argumentConverters[i] = _typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(argTypes[i], genericParameterTypes[i]));
      }
      return new ConstructorInvoker(constructor, argumentConverters, OBJECT_XLOBJECT_CONVERTER);
    }
    throw new Excel4JRuntimeException("Could not find matching constructor");
  }
  
  @Override
  public MethodInvoker getMethodTypeConverter(final XLObject objectHandle, final XLString methodName, 
                                              @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes) 
                                              throws ClassNotFoundException {
    Excel excel = ExcelFactory.getMockInstance();
    WorksheetHeap worksheetHeap = excel.getWorksheetHeap();
    Object object = worksheetHeap.getObject(objectHandle.getHandle());
    Class<?> clazz = object.getClass();

    // TODO: we should probably check here that object.getClass().getSimpleName() == objectHandle.getClazz()
    
    for (Method method : clazz.getMethods()) {
      Type[] genericParameterTypes = method.getGenericParameterTypes();
      if (argTypes.length != genericParameterTypes.length) {
        continue; // number of arguments don't match so skip this one.
      }
      TypeConverter[] argumentConverters = new TypeConverter[genericParameterTypes.length];
      for (int i = 0; i < genericParameterTypes.length; i++) {
        argumentConverters[i] = _typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(argTypes[i], genericParameterTypes[i]));
      }
      TypeConverter resultConverter = _typeConverterRegistry.findConverter(clazz);  // this might be swapped out for OBJECT_XLOBJECT_CONVERTER at run-time.
      return new MethodInvoker(method, argumentConverters, resultConverter);
    }
    throw new Excel4JRuntimeException("Could not find matching constructor");
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
    
    for (Method method : clazz.getMethods()) {
      if (!method.getName().equals(methodName)) {
        continue; // method name doesn't match
      }
      Type[] genericParameterTypes = method.getGenericParameterTypes();
      if (argTypes.length != genericParameterTypes.length) {
        continue; // number of arguments don't match so skip this one.
      }
      TypeConverter[] argumentConverters = new TypeConverter[genericParameterTypes.length];
      for (int i = 0; i < genericParameterTypes.length; i++) {
        argumentConverters[i] = _typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(argTypes[i], genericParameterTypes[i]));
      }
      TypeConverter resultConverter = _typeConverterRegistry.findConverter(clazz);
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
}
