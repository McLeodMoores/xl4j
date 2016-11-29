/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
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

import com.mcleodmoores.xl4j.callback.ExcelCallback;
import com.mcleodmoores.xl4j.javacode.ConstructorInvoker;
import com.mcleodmoores.xl4j.javacode.InvokerFactory;
import com.mcleodmoores.xl4j.javacode.MethodInvoker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;

/**
 * Class to scan for @XLFunction annotations and register each function with Excel.
 */
public class FunctionRegistry {
  private static final Set<String> EXCLUDED_METHOD_NAMES = new HashSet<>();

  static {
    EXCLUDED_METHOD_NAMES.add("clone");
    EXCLUDED_METHOD_NAMES.add("equals");
    EXCLUDED_METHOD_NAMES.add("finalize");
    EXCLUDED_METHOD_NAMES.add("getClass");
    EXCLUDED_METHOD_NAMES.add("hashCode");
    EXCLUDED_METHOD_NAMES.add("notify");
    EXCLUDED_METHOD_NAMES.add("notifyAll");
    EXCLUDED_METHOD_NAMES.add("toString");
    EXCLUDED_METHOD_NAMES.add("wait");
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(FunctionRegistry.class);
  // REVIEW: is this the best structure to use?
  private final Set<FunctionDefinition> _functionDefinitions = Collections.synchronizedSet(new HashSet<FunctionDefinition>());
  private final AtomicInteger _exportCounter = new AtomicInteger();
  private final ConcurrentMap<Integer, FunctionDefinition> _functionDefinitionLookup = new ConcurrentHashMap<>();
  private final BlockingQueue<Collection<FunctionDefinition>> _finishedScan = new ArrayBlockingQueue<>(1);

  /**
   * Default constructor.
   *
   * @param invokerFactory
   *          invoker factory used to create method and constructor invokers to perform type conversions
   */
  public FunctionRegistry(final InvokerFactory invokerFactory) {
    final Thread scanningThread = new Thread(new ReflectionScanner(invokerFactory));
    scanningThread.start();
  }

  /**
   * Thread (well, Runnable) that scans the annotations in the background and sends the results to registerFunctions, which will block until
   * the results arrive. We could make it streaming fairly easily.
   */
  private class ReflectionScanner implements Runnable {
    private final InvokerFactory _invokerFactory;

    public ReflectionScanner(final InvokerFactory invokerFactory) {
      _invokerFactory = invokerFactory;
    }

    @Override
    public void run() {
      scanAndCreateFunctions(_invokerFactory);
      LOGGER.info("Scan and create finished, putting to Blocking Queue");
      try {
        _finishedScan.put(_functionDefinitions);
      } catch (final InterruptedException e) {
        throw new Excel4JRuntimeException("Unexpected interrupt while sending function definitions over queue");
      }
    }
  }

  /**
   * Register functions and constructors.
   *
   * @param callback
   *          the Excel callback interface
   */
  public void registerFunctions(final ExcelCallback callback) {
    LOGGER.info("registerFunctions called with {}", callback);
    try {
      final Collection<FunctionDefinition> take = _finishedScan.take();
      LOGGER.info("got collection from finishedScan queue, iterating over them...");
      for (final FunctionDefinition functionDefinition : take) {
        try {
          callback.registerFunction(functionDefinition);
        } catch (final Excel4JRuntimeException xl4jre) {
          LOGGER.error("Problem registering function, skipping", xl4jre);
        }
      }
      LOGGER.info("finished registering functions");
    } catch (final InterruptedException e) {
      throw new Excel4JRuntimeException("Unexpected interrupt while waiting for function definitions from queue");
    }
  }

  private void scanAndCreateFunctions(final InvokerFactory invokerFactory) {
    final Set<String> registeredFunctionNames = new HashSet<>();
    // TODO: don't limit to our package when scanning for functions. #45
    final Reflections reflections = new Reflections(new ConfigurationBuilder().addUrls(ClasspathHelper.forJavaClassPath())
        .addScanners(new MethodAnnotationsScanner(), new MethodParameterScanner(), new TypeAnnotationsScanner()));
    final Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(XLFunction.class);
    // deal with methods
    for (final Method method : methodsAnnotatedWith) {
      final XLFunction functionAnnotation = method.getAnnotation(XLFunction.class);
      XLNamespace namespaceAnnotation = null;
      if (method.getDeclaringClass().isAnnotationPresent(XLNamespace.class)) {
        namespaceAnnotation = method.getDeclaringClass().getAnnotation(XLNamespace.class);
      }
      final XLArgument[] xlArgumentAnnotations = getXLArgumentAnnotations(method);
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
        final MethodInvoker methodInvoker = invokerFactory.getMethodTypeConverter(method, resultType);
        // build the meta-data data structure and store it all in a FunctionDefinition
        final FunctionMetadata functionMetadata = FunctionMetadata.of(namespaceAnnotation, functionAnnotation, xlArgumentAnnotations);
        final int allocatedExportNumber = allocateExport();
        final FunctionDefinition functionDefinition = FunctionDefinition.of(functionMetadata, methodInvoker, allocatedExportNumber);
        if (registeredFunctionNames.contains(functionDefinition.getMethodOrConstructorName().toUpperCase())) {
          LOGGER.warn("Have already registered a function called {}, the previous method will be overwritten", functionDefinition.getMethodOrConstructorName());
        } else {
          registeredFunctionNames.add(functionDefinition.getMethodOrConstructorName().toUpperCase());
        }
        // put the definition in some look-up tables.
        LOGGER.info("Allocating export number {} to function {}", allocatedExportNumber, functionAnnotation == null ? method.getName() : functionAnnotation.name());
        _functionDefinitionLookup.put(allocatedExportNumber, functionDefinition);
        _functionDefinitions.add(functionDefinition);
      } catch (final Exception e) {
        LOGGER.error("Exception while scanning annotated method", e);
      }
    }
    // deal with constructors
    @SuppressWarnings("rawtypes")
    final Set<Constructor> constructorsAnnotatedWith = reflections.getConstructorsAnnotatedWith(XLFunction.class);
    for (final Constructor<?> constructor : constructorsAnnotatedWith) {
      final XLFunction constructorAnnotation = constructor.getAnnotation(XLFunction.class);
      XLNamespace namespaceAnnotation = null;
      if (constructor.getDeclaringClass().isAnnotationPresent(XLNamespace.class)) {
        namespaceAnnotation = constructor.getDeclaringClass().getAnnotation(XLNamespace.class);
      }
      final XLArgument[] xlArgumentAnnotations = getXLArgumentAnnotations(constructor);
      // build a constructor invoker
      try {
        final ConstructorInvoker constructorInvoker = invokerFactory.getConstructorTypeConverter(constructor);
        // build the meta-data data structure and store it all in a FunctionDefinition
        final FunctionMetadata classMetadata = FunctionMetadata.of(namespaceAnnotation, constructorAnnotation, xlArgumentAnnotations);
        final int allocatedExportNumber = allocateExport();
        final FunctionDefinition functionDefinition = FunctionDefinition.of(classMetadata, constructorInvoker, allocatedExportNumber);
        if (registeredFunctionNames.contains(functionDefinition.getMethodOrConstructorName().toUpperCase())) {
          LOGGER.warn("Have already registered a function called {}, the previous method will be overwritten", functionDefinition.getMethodOrConstructorName());
        } else {
          registeredFunctionNames.add(functionDefinition.getMethodOrConstructorName().toUpperCase());
        }
        // put the definition in some look-up tables.
        LOGGER.info("Allocating export number {} to {}", allocatedExportNumber, constructorAnnotation == null ? functionDefinition.getMethodOrConstructorName() : constructorAnnotation.name());
        _functionDefinitionLookup.put(allocatedExportNumber, functionDefinition);
        _functionDefinitions.add(functionDefinition);
      } catch (final Exception e) {
        LOGGER.error("Exception while scanning annotated constructor", e);
      }
    }
  }

  private static XLArgument[] getXLArgumentAnnotations(final Method method) {
    return getXLArgumentAnnotations(method.getParameterAnnotations());
  }

  private static XLArgument[] getXLArgumentAnnotations(final Constructor<?> constructor) {
    return getXLArgumentAnnotations(constructor.getParameterAnnotations());
  }

  private static XLArgument[] getXLArgumentAnnotations(final Annotation[][] allParameterAnnotations) {
    final XLArgument[] xlArgumentAnnotations = new XLArgument[allParameterAnnotations.length];
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
   * This allocates an export number.
   *
   * @return the allocated export number
   */
  private int allocateExport() {
    final int exportNumber = _exportCounter.getAndIncrement();
    return exportNumber;
  }

  /**
   * Look up the function definition, from the allocated.
   *
   * @param exportNumber
   *          the number of the export in the parameter size block
   * @return the function definition, not null throws Excel4JRuntimeException if function definition could not be found
   */
  public FunctionDefinition getFunctionDefinition(final int exportNumber) {
    final FunctionDefinition functionDefinition = _functionDefinitionLookup.get(exportNumber);
    if (functionDefinition != null) {
      return functionDefinition;
    }
    throw new Excel4JRuntimeException("Cannot find function definition with export number " + exportNumber);
  }

}
