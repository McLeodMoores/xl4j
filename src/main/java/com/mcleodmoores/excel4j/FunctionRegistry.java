/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.reflections.Reflections;

import com.mcleodmoores.excel4j.javacode.MethodInvoker;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLValue;
/**
 * 
 */
public class FunctionRegistry {
  // REVIEW: is this the best structure to use?
  private Set<FunctionDefinition> _functionDefinitions = new ConcurrentSkipListSet<FunctionDefinition>();
  
  /**
   * Default no-arg constructor.
   */
  public FunctionRegistry() {
    scanAndCreateFunctions();
  }
  
  private void scanAndCreateFunctions() {
    Reflections reflections = new Reflections("com.mcleodmoores");
    Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(XLFunction.class);
    for (Method method : methodsAnnotatedWith) {
      XLFunction funtionAnnotation = method.getAnnotation(XLFunction.class);
      XLNamespace namespaceAnnotation = null;
      if (method.getDeclaringClass().isAnnotationPresent(XLNamespace.class)) {
        namespaceAnnotation = method.getDeclaringClass().getAnnotation(XLNamespace.class);
      }
      Annotation[][] allParameterAnnotations = method.getParameterAnnotations();
      XLArgument[] xlArgumentAnnotations = new XLArgument[allParameterAnnotations.length];
      for (int i = 0; i < allParameterAnnotations.length; i++) {
        if (allParameterAnnotations[i] != null) {
          for (int j = 0; j < allParameterAnnotations[i].length; j++) {
            if (allParameterAnnotations[i][j].annotationType().equals(XLArgument.class)) {
              xlArgumentAnnotations[i] = (XLArgument) allParameterAnnotations[i][j];
              break;
            }
          }
        } else {
          xlArgumentAnnotations[i] = null;
        }
      }
      MethodInvoker methodInvoker = ExcelFactory.getInstance().getInvokerFactory().getStaticMethodTypeConverter(method);
      FunctionMetadata functionMetadata = FunctionMetadata.of(namespaceAnnotation, funtionAnnotation, xlArgumentAnnotations);
      _functionDefinitions.add(FunctionDefinition.of(functionMetadata, methodInvoker));
    }
  }
  
  private Class<? extends XLValue>[] getExpectedExcelTypes(final Method method) {
    Type[] genericParameterTypes = method.getGenericParameterTypes();
    @SuppressWarnings("unchecked")
    Class<? extends XLValue>[] excelTypes = new Class[genericParameterTypes.length];
    TypeConverterRegistry typeConverterRegistry = new TypeConverterRegistry();
    int i = 0;
    for (Type parameterType : genericParameterTypes) {
      TypeConverter converter = typeConverterRegistry.findConverter(parameterType);
      if (converter != null) {
        Class<? extends XLValue> excelClass = converter.getJavaToExcelTypeMapping().getExcelClass();
        excelTypes[i] = excelClass;
      } else {
        throw new Excel4JRuntimeException("Can't find Java->Excel converter for parameter type " + parameterType + " (arg " + i + ") of method " + method);
      }
      i++;
    }
    return excelTypes;
  }
}
