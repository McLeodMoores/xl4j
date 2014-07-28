/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.reflections.Reflections;

import com.mcleodmoores.excel4j.javacode.MethodInvoker;
/**
 * 
 */
public class FunctionRegistry {
  // REVIEW: is this the best structure to use?
  private Set<FunctionDefinition> _functionDefinitions = new ConcurrentSkipListSet<FunctionDefinition>();
  private ConcurrentMap<Integer, AtomicInteger> _exportCounters = new ConcurrentHashMap<Integer, AtomicInteger>();
  /**
   * Default no-arg constructor.
   */
  public FunctionRegistry() {
    scanAndCreateFunctions();
  }
  
  private void scanAndCreateFunctions() {
    // TODO: don't limit to our package.
    Reflections reflections = new Reflections("com.mcleodmoores");
    Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(XLFunction.class);
    for (Method method : methodsAnnotatedWith) {
      XLFunction functionAnnotation = method.getAnnotation(XLFunction.class);
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
      FunctionMetadata functionMetadata = FunctionMetadata.of(namespaceAnnotation, functionAnnotation, xlArgumentAnnotations);
      _functionDefinitions.add(FunctionDefinition.of(functionMetadata, methodInvoker, allocateExport(methodInvoker, functionAnnotation)));
    }
  }
  
  /**
   * This allocates an export number for the number of parameters required. 
   * @param invoker  the method invoker, not null
   * @param functionAnnotation  the function annotation, can be null
   * @return the allocated export number
   */
  private int allocateExport(final MethodInvoker invoker, final XLFunction functionAnnotation) {
    int params = invoker.getExcelParameterTypes().length;
    if (functionAnnotation.isAsynchronous()) {
      params++; // asynchronous functions require a handle to be passed, which is hidden from Java
    }
    AtomicInteger exportCounter = null;
    if (_exportCounters.containsKey(params)) {
      exportCounter = new AtomicInteger();
    }
    AtomicInteger existingExportCounter = _exportCounters.putIfAbsent(params, exportCounter); // won't ever put null.
    if (existingExportCounter != null) {
      exportCounter = existingExportCounter;
    }
    int exportNumber = exportCounter.incrementAndGet();
    return exportNumber;
  }

}
