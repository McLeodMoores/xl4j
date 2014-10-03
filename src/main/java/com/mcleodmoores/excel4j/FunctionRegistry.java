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
import java.util.concurrent.atomic.AtomicInteger;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.excel4j.callback.ExcelCallback;
import com.mcleodmoores.excel4j.javacode.InvokerFactory;
import com.mcleodmoores.excel4j.javacode.MethodInvoker;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
/**
 * Class to scan for @XLFunction annotations and register each function with Excel.
 */
public class FunctionRegistry {
  private static Logger s_logger = LoggerFactory.getLogger(FunctionRegistry.class);
  // REVIEW: is this the best structure to use?
  private Set<FunctionDefinition> _functionDefinitions = Collections.synchronizedSet(new HashSet<FunctionDefinition>());
  private AtomicInteger _exportCounter = new AtomicInteger();
  private ConcurrentMap<Integer, FunctionDefinition> _functionDefinitionLookup = new ConcurrentHashMap<>();
  private BlockingQueue<Collection<FunctionDefinition>> _finishedScan = new ArrayBlockingQueue<>(1);
  /**
   * Default constructor.
   * @param invokerFactory  invoker factory used to create method and constructor invokers to perform type conversions
   */
  public FunctionRegistry(final InvokerFactory invokerFactory) {
    Thread scanningThread = new Thread(new ReflectionScanner(invokerFactory));
    scanningThread.start();
  }
  
  /**
   * Thread (well, Runnable) that scans the annotations in the background and sends the results to registerFunctions, 
   * which will block until the results arrive.  We could make it streaming fairly easily.
   */
  private class ReflectionScanner implements Runnable {
    private final InvokerFactory _invokerFactory;
    
    public ReflectionScanner(final InvokerFactory invokerFactory) {
      _invokerFactory = invokerFactory;
    }
    
    @Override
    public void run() {
      scanAndCreateFunctions(_invokerFactory);
      s_logger.info("Scan and create finished, putting to Blocking Queue");
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
    s_logger.info("registerFunctions called with {}", callback);
    try {
      Collection<FunctionDefinition> take = _finishedScan.take();
      s_logger.info("got colleciton from finishedScan queue, iterating over them...");
      for (FunctionDefinition functionDefinition : take) {
        try {
          callback.registerFunction(functionDefinition);
        } catch (Excel4JRuntimeException xl4jre) {
          s_logger.error("Problem registering function, skipping", xl4jre);
        }
      }
      s_logger.info("finsihed registering functions");
    } catch (InterruptedException e) {
      throw new Excel4JRuntimeException("Unexpected interrupt while waiting for function definitions from queue");
    }
  }
  
  private void scanAndCreateFunctions(final InvokerFactory invokerFactory) {
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
      TypeConversionMode resultType;
      if (functionAnnotation != null) {
        resultType = functionAnnotation.typeConversionMode();
      } else {
        resultType = TypeConversionMode.SIMPLEST_RESULT;
      }
      // build a method invoker
      try {
        MethodInvoker methodInvoker = invokerFactory.getMethodTypeConverter(method, resultType);
        // build the meta-data data structure and store it all in a FunctionDefinition
        FunctionMetadata functionMetadata = FunctionMetadata.of(namespaceAnnotation, functionAnnotation, xlArgumentAnnotations);
        int allocatedExportNumber = allocateExport(methodInvoker, functionAnnotation);
        FunctionDefinition functionDefinition = FunctionDefinition.of(functionMetadata, methodInvoker, allocatedExportNumber);
        // put the definition in some look-up tables.
        s_logger.info("Allocating export number {} function {}", allocatedExportNumber, methodInvoker.getMethodName());
        _functionDefinitionLookup.put(allocatedExportNumber, functionDefinition);
        _functionDefinitions.add(functionDefinition);
      } catch (Exception e) {
        s_logger.error("Exception while scanning annotated method", e);
      }
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
  
  /**
   * This allocates an export number for the number of parameters required. 
   * @param invoker  the method invoker, not null
   * @param functionAnnotation  the function annotation, can be null
   * @return the allocated export number
   */
  private int allocateExport(final MethodInvoker invoker, final XLFunction functionAnnotation) {
    int exportNumber = _exportCounter.getAndIncrement();
    return exportNumber;
  }
  
  /**
   * Look up the function definition, from the allocated.
   * @param exportNumber  the number of the export in the parameter size block
   * @return the function definition, not null
   * throws Excel4JRuntimeException if function definition could not be found
   */
  public FunctionDefinition getFunctionDefinition(final int exportNumber) {
    FunctionDefinition functionDefinition = _functionDefinitionLookup.get(exportNumber);
    if (functionDefinition != null) {
      return functionDefinition;
    } else {
      throw new Excel4JRuntimeException("Cannot find function definition with export number"
                                        + exportNumber);
    }
  }
  


}
