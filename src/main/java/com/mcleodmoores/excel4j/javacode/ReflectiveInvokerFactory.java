/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.heap.WorksheetHeap;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * 
 */
public class ReflectiveInvokerFactory implements InvokerFactory {
  private TypeConverterRegistry _typeConverterRegistry;
  
  @Override
  public ConstructorInvoker getConstructorTypeConverter(final XLString className, final XLValue... args) throws ClassNotFoundException {
    Class<?> clazz = resolveClass(className);
    for (Constructor<?> constructor : clazz.getConstructors()) {
      Type[] genericParameterTypes = constructor.getGenericParameterTypes();
      if (args.length != genericParameterTypes.length) {
        continue; // number of arguments don't match so skip this one.
      }
      TypeConverter[] argumentConverters = new TypeConverter[genericParameterTypes.length];
      for (int i = 0; i < genericParameterTypes.length; i++) {
        argumentConverters[i] = _typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(args[i].getClass(), genericParameterTypes[i]));
      }
      TypeConverter resultConverter = _typeConverterRegistry.findConverter(clazz);
      return new ConstructorInvoker(constructor, argumentConverters, resultConverter);
    }
    throw new Excel4JRuntimeException("Could not find matching constructor");
  }
  
//  @Override
//  public MethodInvoker getMethodTypeConverter(final XLString className, final XLObject objectHandle, final XLString methodName, final XLValue... args) throws ClassNotFoundException {
//    Object object;
//    Class<?> clazz;
//    if (objectHandle != null) { // or should we accept XLValue and check for XLMissing?
//      Excel excel = ExcelFactory.getMockInstance();
//      WorksheetHeap worksheetHeap = excel.getWorksheetHeap();
//      object = worksheetHeap.getObject(objectHandle.getHandle());
//      clazz = object.getClass();
//    } else {
//      clazz = resolveClass(className); 
//    }
//    // TODO: we should probably check here that object.getClass().getSimpleName() == objectHandle.getClazz()
//    
//    for (Method method : clazz.getMethods()) {
//      Type[] genericParameterTypes = constructor.getGenericParameterTypes();
//      if (args.length != genericParameterTypes.length) {
//        continue; // number of arguments don't match so skip this one.
//      }
//      TypeConverter[] argumentConverters = new TypeConverter[genericParameterTypes.length];
//      for (int i = 0; i < genericParameterTypes.length; i++) {
//        argumentConverters[i] = _typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(args[i].getClass(), genericParameterTypes[i]));
//      }
//      TypeConverter resultConverter = _typeConverterRegistry.findConverter(clazz);
//      return new MethodInvoker(constructor, argumentConverters, resultConverter);
//    }
//    throw new Excel4JRuntimeException("Could not find matching constructor");
//  }

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
