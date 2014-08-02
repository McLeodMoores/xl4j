/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.mcleodmoores.excel4j.callback.ExcelCallback;
import com.mcleodmoores.excel4j.javacode.MethodInvoker;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
/**
 * 
 */
public class FunctionRegistry {

  // REVIEW: is this the best structure to use?
  private Set<FunctionDefinition> _functionDefinitions = Collections.synchronizedSet(new HashSet<FunctionDefinition>());
  private ConcurrentMap<Integer, AtomicInteger> _exportCounters = new ConcurrentHashMap<Integer, AtomicInteger>();
  private ConcurrentMap<Long, FunctionDefinition> _functionDefinitionLookup = new ConcurrentHashMap<Long, FunctionDefinition>();
  private BlockingQueue<Collection<FunctionDefinition>> _finishedScan = new ArrayBlockingQueue<>(1);
  /**
   * Default no-arg constructor.
   */
  public FunctionRegistry() {
    Thread scanningThread = new Thread(new ReflectionScanner());
    scanningThread.start();
  }
  
  /**
   * Thread (well, Runnable) that scans the annotations in the background and sends the results to registerFunctions, 
   * which will block until the results arrive.  We could make it streaming fairly easily.
   */
  private class ReflectionScanner implements Runnable {
    @Override
    public void run() {
      scanAndCreateFunctions();
      try {
        _finishedScan.put(_functionDefinitions);
      } catch (InterruptedException e) {
        throw new Excel4JRuntimeException("Unexpected interrupt while sending function definitions over queue");
      }
    }
  }
  
  /**
   * Register functions.
   * @param callback the Excel callback interface
   */
  public void registerFunctions(final ExcelCallback callback) {
    try {
      Collection<FunctionDefinition> take = _finishedScan.take();
      for (FunctionDefinition functionDefinition : take) {
        callback.registerFunction(functionDefinition);
      }
    } catch (InterruptedException e) {
      throw new Excel4JRuntimeException("Unexpected interrupt while waiting for function definitions from queue");
    }
  }
  
  private void scanAndCreateFunctions() {
    // TODO: don't limit to our package.
    Reflections reflections = new Reflections(new ConfigurationBuilder().addUrls(ClasspathHelper.forJavaClassPath())
                                                                        .addScanners(new MethodAnnotationsScanner(), 
                                                                                     new MethodParameterScanner(),
                                                                                     new TypeAnnotationsScanner()));
    Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(XLFunction.class);
    for (Method method : methodsAnnotatedWith) {
      XLFunction functionAnnotation = method.getAnnotation(XLFunction.class);
      XLNamespace namespaceAnnotation = null;
      if (method.getDeclaringClass().isAnnotationPresent(XLNamespace.class)) {
        namespaceAnnotation = method.getDeclaringClass().getAnnotation(XLNamespace.class);
      }
      XLArgument[] xlArgumentAnnotations = getXLArgumentAnnotations(method);
      // scan the result type if there is one to determine whether function should return simplest type or always
      // an object type
      ResultType resultType;
      if (functionAnnotation != null) {
        resultType = functionAnnotation.resultType();
      } else {
        resultType = ResultType.SIMPLEST;
      }
      // build a method invoker
      MethodInvoker methodInvoker = ExcelFactory.getInstance().getInvokerFactory().getMethodTypeConverter(method, resultType);
      // build the meta-data data structure and store it all in a FunctionDefinition
      FunctionMetadata functionMetadata = FunctionMetadata.of(namespaceAnnotation, functionAnnotation, xlArgumentAnnotations);
      int allocatedExportNumber = allocateExport(methodInvoker, functionAnnotation);
      FunctionDefinition functionDefinition = FunctionDefinition.of(functionMetadata, methodInvoker, allocatedExportNumber);
      long key = makeKey(getNumParamPointers(methodInvoker, functionAnnotation), allocatedExportNumber);
      // put the definition in some look-up tables.
      _functionDefinitionLookup.put(key, functionDefinition);
      _functionDefinitions.add(functionDefinition);
    }
  }
  
  private XLArgument[] getXLArgumentAnnotations(final Method method) {
    Annotation[][] allParameterAnnotations = method.getParameterAnnotations();
    XLArgument[] xlArgumentAnnotations = new XLArgument[allParameterAnnotations.length];
    // we rely here on the array being initialized to null
    for (int i = 0; i < allParameterAnnotations.length; i++) {
      if (allParameterAnnotations[i] != null) {
        for (int j = 0; j < allParameterAnnotations[i].length; j++) {
          if (allParameterAnnotations[i][j].annotationType().equals(XLArgument.class)) {
            xlArgumentAnnotations[i] = (XLArgument) allParameterAnnotations[i][j];
            break;
          }
        }
      }
    }
    return xlArgumentAnnotations;
  }
  
  private int getNumParamPointers(final MethodInvoker invoker, final XLFunction functionAnnotation) {
    final int params = invoker.getExcelParameterTypes().length;
    if (functionAnnotation.isAsynchronous()) {
      return params + 1; // asynchronous functions require a handle to be passed, which is hidden from Java
    } else {
      return params;
    }
  }
  
  /**
   * This allocates an export number for the number of parameters required. 
   * @param invoker  the method invoker, not null
   * @param functionAnnotation  the function annotation, can be null
   * @return the allocated export number
   */
  private int allocateExport(final MethodInvoker invoker, final XLFunction functionAnnotation) {
    int params = getNumParamPointers(invoker, functionAnnotation);
    AtomicInteger exportCounter = new AtomicInteger();
    AtomicInteger existingExportCounter = _exportCounters.putIfAbsent(params, exportCounter);
    if (existingExportCounter != null) {
      exportCounter = existingExportCounter;
    }
    int exportNumber = exportCounter.incrementAndGet();
    return exportNumber;
  }
  
  /**
   * Look up the function definition, from the allocated.
   * @param numberOfParameterPointers  the number of parameters (all machine word width) to pass on the stack
   * @param exportNumber  the number of the export in the parameter size block
   * @return the function definition, not null
   * throws Excel4JRuntimeException if function definition could not be found
   */
  public FunctionDefinition getFunctionDefinition(final int numberOfParameterPointers, final int exportNumber) {
    FunctionDefinition functionDefinition = _functionDefinitionLookup.get(makeKey(numberOfParameterPointers, exportNumber));
    if (functionDefinition != null) {
      return functionDefinition;
    } else {
      throw new Excel4JRuntimeException("Cannot find function definition with "
                                        + numberOfParameterPointers + " function params, and {} export number"
                                        + exportNumber);
    }
  }
  
  /**
   * Build key for lookup of function.  Should be no collisions.
   * @param numberOfParameterPointers
   * @param exportNumber
   * @return a hash of the number of parameter pointer and export number
   */
  private long makeKey(final int numberOfParameterPointers, final int exportNumber) {
    return ((long) numberOfParameterPointers) + (((long) exportNumber) * (long) Integer.MAX_VALUE);
  }

}
