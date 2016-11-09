/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
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
  private final Set<ConstructorDefinition> _constructorDefinitions = Collections.synchronizedSet(new HashSet<ConstructorDefinition>());
  private final Set<ClassConstructorDefinition> _classConstructorDefinitions = Collections
      .synchronizedSet(new HashSet<ClassConstructorDefinition>());
  private final Set<ClassMethodDefinition> _classMethodDefinitions = Collections.synchronizedSet(new HashSet<ClassMethodDefinition>());
  private final AtomicInteger _exportCounter = new AtomicInteger();
  private final ConcurrentMap<Integer, FunctionDefinition> _functionDefinitionLookup = new ConcurrentHashMap<>();
  private final BlockingQueue<Collection<FunctionDefinition>> _finishedScan = new ArrayBlockingQueue<>(1);
  private final ConcurrentMap<Integer, ConstructorDefinition> _constructorDefinitionLookup = new ConcurrentHashMap<>();
  private final BlockingQueue<Collection<ConstructorDefinition>> _finishedConstructorScan = new ArrayBlockingQueue<>(1);
  private final ConcurrentMap<Integer, ClassConstructorDefinition> _classConstructorDefinitionLookup = new ConcurrentHashMap<>();
  private final BlockingQueue<Collection<ClassConstructorDefinition>> _finishedClassConstructorScan = new ArrayBlockingQueue<>(1);
  private final ConcurrentMap<Integer, ClassMethodDefinition> _classMethodDefinitionLookup = new ConcurrentHashMap<>();
  private final BlockingQueue<Collection<ClassMethodDefinition>> _finishedClassMethodScan = new ArrayBlockingQueue<>(1);

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
        _finishedConstructorScan.put(_constructorDefinitions);
        _finishedClassConstructorScan.put(_classConstructorDefinitions);
        _finishedClassMethodScan.put(_classMethodDefinitions);
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
      final Collection<ConstructorDefinition> takeConstructors = _finishedConstructorScan.take();
      LOGGER.info("got collection from finishedConstructorScan queue, iterating over them...");
      for (final ConstructorDefinition classDefinition : takeConstructors) {
        try {
          callback.registerConstructor(classDefinition);
        } catch (final Excel4JRuntimeException xl4jre) {
          LOGGER.error("Problem registering constructor, skipping", xl4jre);
        }
      }
      LOGGER.info("finished registering constructors");
      final Collection<ClassConstructorDefinition> takeClassConstructors = _finishedClassConstructorScan.take();
      LOGGER.info("got collection from finishedClassConstructorScan queue, iterating over them...");
      for (final ClassConstructorDefinition classDefinition : takeClassConstructors) {
        try {
          callback.registerConstructorsForClass(classDefinition);
        } catch (final Excel4JRuntimeException xl4jre) {
          LOGGER.error("Problem registering constructor, skipping", xl4jre);
        }
      }
      final Collection<ClassMethodDefinition> takeClassMethods = _finishedClassMethodScan.take();
      LOGGER.info("got collection from finishedClassMethodScan queue, iterating over them...");
      for (final ClassMethodDefinition classDefinition : takeClassMethods) {
        try {
          callback.registerMethodsForClass(classDefinition);
        } catch (final Excel4JRuntimeException xl4jre) {
          LOGGER.error("Problem registering method, skipping", xl4jre);
        }
      }
      LOGGER.info("finished registering classes");
    } catch (final InterruptedException e) {
      throw new Excel4JRuntimeException("Unexpected interrupt while waiting for constructor definitions from queue");
    }
  }

  private void scanAndCreateFunctions(final InvokerFactory invokerFactory) {
    // TODO: don't limit to our package when scanning for functions. #45
    final Reflections reflections = new Reflections(new ConfigurationBuilder().addUrls(ClasspathHelper.forJavaClassPath())
        .addScanners(new MethodAnnotationsScanner(), new MethodParameterScanner(), new TypeAnnotationsScanner()));
    final Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(XLFunction.class);
    @SuppressWarnings("rawtypes")
    final Set<Constructor> constructorsAnnotatedWith = reflections.getConstructorsAnnotatedWith(XLConstructor.class);
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
        // put the definition in some look-up tables.
        LOGGER.info("Allocating export number {} to function {}", allocatedExportNumber, functionAnnotation == null ? method.getName() : functionAnnotation.name());
        _functionDefinitionLookup.put(allocatedExportNumber, functionDefinition);
        _functionDefinitions.add(functionDefinition);
      } catch (final Exception e) {
        LOGGER.error("Exception while scanning annotated method", e);
      }
    }
    // deal with constructors
    for (final Constructor<?> constructor : constructorsAnnotatedWith) {
      final XLConstructor constructorAnnotation = constructor.getAnnotation(XLConstructor.class);
      XLNamespace namespaceAnnotation = null;
      if (constructor.getDeclaringClass().isAnnotationPresent(XLNamespace.class)) {
        namespaceAnnotation = constructor.getDeclaringClass().getAnnotation(XLNamespace.class);
      }
      final XLArgument[] xlArgumentAnnotations = getXLArgumentAnnotations(constructor);
      // build a constructor invoker
      try {
        final ConstructorInvoker constructorInvoker = invokerFactory.getConstructorTypeConverter(constructor);
        // build the meta-data data structure and store it all in a FunctionDefinition
        final ConstructorMetadata classMetadata = ConstructorMetadata.of(namespaceAnnotation, constructorAnnotation, xlArgumentAnnotations);
        final int allocatedExportNumber = allocateExport();
        final ConstructorDefinition classDefinition = ConstructorDefinition.of(classMetadata, constructorInvoker, allocatedExportNumber);
        // put the definition in some look-up tables.
        LOGGER.info("Allocating export number {} to ", allocatedExportNumber, constructorInvoker.getClass().getSimpleName());
        _constructorDefinitionLookup.put(allocatedExportNumber, classDefinition);
        _constructorDefinitions.add(classDefinition);
      } catch (final Exception e) {
        LOGGER.error("Exception while scanning annotated constructor", e);
      }
    }
    final Set<Class<?>> classesAnnotatedWith = reflections.getTypesAnnotatedWith(XLClass.class);
    for (final Class<?> clazz : classesAnnotatedWith) {
      // constructors first
      final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
      final XLClass classAnnotation = clazz.getAnnotation(XLClass.class);
      XLNamespace namespaceAnnotation = null;
      if (clazz.isAnnotationPresent(XLNamespace.class)) {
        namespaceAnnotation = clazz.getAnnotation(XLNamespace.class);
      }
      final int constructorNumber = 1;
      for (final Constructor<?> constructor : constructors) {
        // build a constructor invoker
        try {
          final ConstructorInvoker constructorInvoker = invokerFactory.getConstructorTypeConverter(constructor);
          // build the meta-data data structure and store it all in a FunctionDefinition
          final int allocatedExportNumber = allocateExport();
          final ClassMetadata constructorMetadata = ClassMetadata.of(classAnnotation, namespaceAnnotation);
          final ClassConstructorDefinition constructorDefinition = constructors.length == 1
              ? ClassConstructorDefinition.of(constructorMetadata, constructorInvoker, allocatedExportNumber)
                  : ClassConstructorDefinition.of(constructorMetadata, constructorInvoker, constructorNumber, allocatedExportNumber);
              // put the definition in some look-up tables.
              LOGGER.info("Allocating export number {} to ", allocatedExportNumber, constructorInvoker.getClass().getSimpleName());
              _classConstructorDefinitionLookup.put(allocatedExportNumber, constructorDefinition);
              _classConstructorDefinitions.add(constructorDefinition);
        } catch (final Exception e) {
          LOGGER.error("Exception while scanning constructor for annotated class", e);
        }
      }
      // all methods, excluding those from Object if required
      final Method[] methods = clazz.getMethods();
      final boolean includeObjectMethods = classAnnotation.includeObjectMethods();
      final Set<String> methodsToExclude = new HashSet<>(Arrays.asList(classAnnotation.excludedMethods()));
      for (final Method method : methods) {
        if (!includeObjectMethods && EXCLUDED_METHOD_NAMES.contains(method.getName())) {
          // skip object method names
          continue;
        }
        if (methodsToExclude.contains(method.getName())) {
          // skip unwanted methods
          continue;
        }
        // build a method invoker
        try {
          // all methods will return the simplest type
          final MethodInvoker methodInvoker = invokerFactory.getMethodTypeConverter(method, TypeConversionMode.SIMPLEST_RESULT);
          // build the meta-data data structure and store it all in a FunctionDefinition
          final ClassMetadata functionMetadata = ClassMetadata.of(classAnnotation, namespaceAnnotation);
          final int allocatedExportNumber = allocateExport();
          final ClassMethodDefinition functionDefinition = ClassMethodDefinition.of(functionMetadata, methodInvoker, allocatedExportNumber);
          // put the definition in some look-up tables.
          LOGGER.info("Allocating export number {} to function {}", allocatedExportNumber, methodInvoker.getMethodName());
          _classMethodDefinitionLookup.put(allocatedExportNumber, functionDefinition);
          _classMethodDefinitions.add(functionDefinition);
        } catch (final Exception e) {
          LOGGER.error("Exception while scanning annotated method", e);
        }
      }
    }
  }

  private static XLArgument[] getXLArgumentAnnotations(final Method method) {
    return getXLArgumentAnnotations(method.getParameterAnnotations());
  }

  private static XLArgument[] getXLArgumentAnnotations(final Constructor constructor) {
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

  public ConstructorDefinition getConstructorDefinition(final int exportNumber) {
    final ConstructorDefinition constructorDefinition = _constructorDefinitionLookup.get(exportNumber);
    if (constructorDefinition != null) {
      return constructorDefinition;
    }
    throw new Excel4JRuntimeException("Cannot find constructor definition with export number " + exportNumber);
  }

  public ClassConstructorDefinition getClassConstructorDefinition(final int exportNumber) {
    final ClassConstructorDefinition classDefinition = _classConstructorDefinitionLookup.get(exportNumber);
    if (classDefinition != null) {
      return classDefinition;
    }
    throw new Excel4JRuntimeException("Cannot find constructor definition with export number " + exportNumber);
  }

}
