/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.callback.ExcelCallback;
import com.mcleodmoores.xl4j.javacode.ConstructorInvoker;
import com.mcleodmoores.xl4j.javacode.FieldInvoker;
import com.mcleodmoores.xl4j.javacode.InvokerFactory;
import com.mcleodmoores.xl4j.javacode.MethodInvoker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;

/**
 * Class to scan for @XLFunction annotations and register each function with Excel.
 */
public class FunctionRegistry {
  private static final Set<String> EXCLUDED_METHOD_NAMES = new HashSet<>();
  private static final XLArgument[] EMPTY_ARGUMENT_ARRAY = new XLArgument[0];
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

    ReflectionScanner(final InvokerFactory invokerFactory) {
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
   * Register functions and constants.
   *
   * @param callback
   *          the Excel callback interface
   */
  public void registerFunctions(final ExcelCallback callback) {
    LOGGER.info("registerFunctions called with {}", callback);
    try {
      final Collection<FunctionDefinition> take = _finishedScan.take();
      LOGGER.info("got collection from finishedFunctionScan queue, iterating over them...");
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
        .addScanners(new MethodAnnotationsScanner(), new MethodParameterScanner(), new TypeAnnotationsScanner(), new FieldAnnotationsScanner()));
    final Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(XLFunction.class);
    addAnnotatedMethods(invokerFactory, registeredFunctionNames, methodsAnnotatedWith);
    @SuppressWarnings("rawtypes")
    final Set<Constructor> constructorsAnnotatedWithFunction = reflections.getConstructorsAnnotatedWith(XLFunction.class);
    addAnnotatedConstructors(invokerFactory, registeredFunctionNames, constructorsAnnotatedWithFunction);
    final Set<Class<?>> classesAnnotatedWithFunction = reflections.getTypesAnnotatedWith(XLFunction.class);
    addAnnotatedClasses(invokerFactory, registeredFunctionNames, classesAnnotatedWithFunction);
    final Set<Class<?>> classesAnnotatedWithConstant = reflections.getTypesAnnotatedWith(XLConstant.class);
    addAnnotatedFields(invokerFactory, registeredFunctionNames, classesAnnotatedWithConstant);
    final Set<Field> fieldsAnnotatedWithConstant = reflections.getFieldsAnnotatedWith(XLConstant.class);
    addAnnotatedFields(invokerFactory, registeredFunctionNames, fieldsAnnotatedWithConstant);
  }

  private void addAnnotatedFields(final InvokerFactory invokerFactory, final Set<String> registeredFunctionNames,
      final Set<Class<?>> typeAnnotatedWith) {
    for (final Class<?> clazz : typeAnnotatedWith) {
      final XLConstant constantAnnotation = clazz.getAnnotation(XLConstant.class);
      XLNamespace namespaceAnnotation = null;
      if (clazz.isAnnotationPresent(XLNamespace.class)) {
        namespaceAnnotation = clazz.getAnnotation(XLNamespace.class);
      }
      for (final Field field : clazz.getFields()) {
        final String functionName = generateFunctionNameForField(namespaceAnnotation, constantAnnotation, clazz.getSimpleName(),
            field.getName(), true);
        addField(field, invokerFactory, registeredFunctionNames, constantAnnotation, namespaceAnnotation, functionName);
      }
    }
  }

  private void addAnnotatedFields(final InvokerFactory invokerFactory, final Set<String> registeredFunctionNames,
      final Collection<Field> fieldsAnnotatedWith) {
    for (final Field field : fieldsAnnotatedWith) {
      XLNamespace namespaceAnnotation = null;
      if (field.getDeclaringClass().isAnnotationPresent(XLNamespace.class)) {
        namespaceAnnotation = field.getDeclaringClass().getAnnotation(XLNamespace.class);
      }
      final XLConstant constantAnnotation = field.getAnnotation(XLConstant.class);
      final String functionName = generateFunctionNameForField(namespaceAnnotation, constantAnnotation,
          field.getDeclaringClass().getName(), field.getName(), false);
      addField(field, invokerFactory, registeredFunctionNames, constantAnnotation, namespaceAnnotation, functionName);
    }
  }

  private void addAnnotatedMethods(final InvokerFactory invokerFactory, final Set<String> registeredFunctionNames,
      final Set<Method> methodsAnnotatedWith) {
    for (final Method method : methodsAnnotatedWith) {
      final XLFunction functionAnnotation = method.getAnnotation(XLFunction.class);
      XLNamespace namespaceAnnotation = null;
      if (method.getDeclaringClass().isAnnotationPresent(XLNamespace.class)) {
        namespaceAnnotation = method.getDeclaringClass().getAnnotation(XLNamespace.class);
      }
      final XLArgument[] xlArgumentAnnotations = getXLArgumentAnnotations(method);
      final String functionName = generateFunctionNameForMethod(namespaceAnnotation, functionAnnotation,
          method.getDeclaringClass().getSimpleName(), method.getName(), false, 1);
      addMethod(method, invokerFactory, registeredFunctionNames, functionAnnotation, namespaceAnnotation, xlArgumentAnnotations, functionName);
    }
  }


  private void addAnnotatedConstructors(final InvokerFactory invokerFactory, final Set<String> registeredFunctionNames,
      @SuppressWarnings("rawtypes") final Set<Constructor> constructorsAnnotatedWith) {
    for (final Constructor<?> constructor : constructorsAnnotatedWith) {
      final XLFunction functionAnnotation = constructor.getAnnotation(XLFunction.class);
      XLNamespace namespaceAnnotation = null;
      if (constructor.getDeclaringClass().isAnnotationPresent(XLNamespace.class)) {
        namespaceAnnotation = constructor.getDeclaringClass().getAnnotation(XLNamespace.class);
      }
      final XLArgument[] xlArgumentAnnotations = getXLArgumentAnnotations(constructor);
      final String functionName = generateFunctionNameForConstructor(namespaceAnnotation, functionAnnotation,
          constructor.getDeclaringClass().getSimpleName(), 1);
      addConstructor(constructor, invokerFactory, registeredFunctionNames, functionAnnotation, namespaceAnnotation,
          xlArgumentAnnotations, functionName);
    }
  }

  private void addAnnotatedClasses(final InvokerFactory invokerFactory, final Set<String> registeredFunctionNames,
      final Set<Class<?>> classesAnnotatedWith) {
    for (final Class<?> clazz : classesAnnotatedWith) {
      if (Modifier.isAbstract(clazz.getModifiers())) {
        LOGGER.warn("{} is abstract, not registering functions", clazz.getSimpleName());
        continue;
      }
      final String className = clazz.getSimpleName();
      final XLFunction classAnnotation = clazz.getAnnotation(XLFunction.class);
      XLNamespace namespaceAnnotation = null;
      if (clazz.isAnnotationPresent(XLNamespace.class)) {
        namespaceAnnotation = clazz.getAnnotation(XLNamespace.class);
      }
      // build the constructor invokers
      final Constructor<?>[] constructors = clazz.getConstructors();
      int count = 1;
      for (final Constructor<?> constructor : constructors) {
        final String functionName = generateFunctionNameForConstructor(namespaceAnnotation, classAnnotation, className, count);
        addConstructor(constructor, invokerFactory, registeredFunctionNames, classAnnotation, namespaceAnnotation, EMPTY_ARGUMENT_ARRAY, functionName);
        count++;
      }
      // build the method invokers
      final Method[] methods = clazz.getMethods();
      final Map<String, Integer> methodNames = new HashMap<>();
      for (final Method method : methods) {
        final String methodName = method.getName();
        if (Modifier.isAbstract(method.getModifiers()) || method.isBridge()) {
          LOGGER.warn("{} in {} is abstract or a bridge method, not registering function", methodName, method.getDeclaringClass());
          continue;
        }
        if (EXCLUDED_METHOD_NAMES.contains(methodName)) {
          continue;
        }
        final int methodNameCount = methodNames.containsKey(methodName) ? methodNames.get(methodName) + 1 : 1;
        methodNames.put(methodName, methodNameCount);
        final String functionName = generateFunctionNameForMethod(namespaceAnnotation, classAnnotation, className, methodName, true, methodNameCount);
        addMethod(method, invokerFactory, registeredFunctionNames, classAnnotation, namespaceAnnotation, EMPTY_ARGUMENT_ARRAY, functionName);
      }
    }
  }

  private void addField(final Field field, final InvokerFactory invokerFactory, final Set<String> registeredFunctionNames,
      final XLConstant constantAnnotation, final XLNamespace namespaceAnnotation, final String functionName) {
    // scan the result type if there is one to determine whether function should return simplest type or always
    // an object type
    final TypeConversionMode resultType =
        constantAnnotation.typeConversionMode() == null ? TypeConversionMode.SIMPLEST_RESULT : constantAnnotation.typeConversionMode();
    try {
      final FieldInvoker fieldInvoker = invokerFactory.getFieldTypeConverter(field, resultType);
      final FunctionMetadata functionMetadata = FunctionMetadata.of(namespaceAnnotation, constantAnnotation);
      final int allocatedExportNumber = allocateExport();
      final FunctionDefinition functionDefinition = FunctionDefinition.of(functionMetadata, fieldInvoker, allocatedExportNumber, functionName);
      checkForDuplicateFunctionNames(registeredFunctionNames, functionDefinition);
      // put the definition in some look-up tables.
      LOGGER.info("Allocating export number {} to function {}", allocatedExportNumber, functionDefinition.getFunctionName());
      _functionDefinitionLookup.put(allocatedExportNumber, functionDefinition);
      _functionDefinitions.add(functionDefinition);
    } catch (final Exception e) {
      LOGGER.error("Exception while scanning annotated field", e);
    }
  }

  private void addMethod(final Method method, final InvokerFactory invokerFactory, final Set<String> registeredFunctionNames,
      final XLFunction functionAnnotation, final XLNamespace namespaceAnnotation, final XLArgument[] argumentAnnotations, final String functionName) {
    // scan the result type if there is one to determine whether function should return simplest type or always
    // an object type
    final TypeConversionMode resultType =
        functionAnnotation.typeConversionMode() == null ? TypeConversionMode.SIMPLEST_RESULT : functionAnnotation.typeConversionMode();
    // build a method invoker
    try {
      final MethodInvoker methodInvoker = invokerFactory.getMethodTypeConverter(method, resultType);
      // build the meta-data data structure and store it all in a FunctionDefinition
      final FunctionMetadata functionMetadata = FunctionMetadata.of(namespaceAnnotation, functionAnnotation, argumentAnnotations);
      final int allocatedExportNumber = allocateExport();
      final FunctionDefinition functionDefinition = FunctionDefinition.of(functionMetadata, methodInvoker, allocatedExportNumber, functionName);
      checkForDuplicateFunctionNames(registeredFunctionNames, functionDefinition);
      // put the definition in some look-up tables.
      LOGGER.info("Allocating export number {} to function {}", allocatedExportNumber, functionDefinition.getFunctionName());
      _functionDefinitionLookup.put(allocatedExportNumber, functionDefinition);
      _functionDefinitions.add(functionDefinition);
    } catch (final Exception e) {
      LOGGER.error("Exception while scanning annotated method", e);
    }
  }

  private void addConstructor(@SuppressWarnings("rawtypes") final Constructor constructor, final InvokerFactory invokerFactory,
      final Set<String> registeredFunctionNames, final XLFunction functionAnnotation, final XLNamespace namespaceAnnotation,
      final XLArgument[] argumentAnnotations, final String functionName) {
    try {
      final ConstructorInvoker constructorInvoker = invokerFactory.getConstructorTypeConverter(constructor);
      // build the meta-data data structure and store it all in a FunctionDefinition
      final FunctionMetadata functionMetadata = FunctionMetadata.of(namespaceAnnotation, functionAnnotation, argumentAnnotations);
      final int allocatedExportNumber = allocateExport();
      final FunctionDefinition functionDefinition = FunctionDefinition.of(functionMetadata, constructorInvoker, allocatedExportNumber, functionName);
      checkForDuplicateFunctionNames(registeredFunctionNames, functionDefinition);
      // put the definition in some look-up tables.
      LOGGER.info("Allocating export number {} to {}", allocatedExportNumber, functionDefinition.getFunctionName());
      _functionDefinitionLookup.put(allocatedExportNumber, functionDefinition);
      _functionDefinitions.add(functionDefinition);
    } catch (final Exception e) {
      LOGGER.error("Exception while scanning annotated constructor", e);
    }
  }

  private static void checkForDuplicateFunctionNames(final Set<String> registeredFunctionNames, final FunctionDefinition functionDefinition) {
    if (registeredFunctionNames.contains(functionDefinition.getFunctionName().toUpperCase())) {
      LOGGER.warn("Have already registered a function called {}, ignoring", functionDefinition.getFunctionName());
    } else {
      registeredFunctionNames.add(functionDefinition.getFunctionName().toUpperCase());
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

  private static String generateFunctionNameForField(final XLNamespace namespace, final XLConstant constant, final String className,
      final String fieldName, final boolean appendFieldName) {
    final StringBuilder functionName = new StringBuilder();
    if (namespace != null) {
      functionName.append(namespace.value());
    }
    if (!constant.name().isEmpty()) {
      functionName.append(constant.name());
    } else {
      functionName.append(className);
    }
    if (appendFieldName) {
      functionName.append(".");
      functionName.append(fieldName);
    }
    return functionName.toString();
  }

  private static String generateFunctionNameForMethod(final XLNamespace namespace, final XLFunction function, final String className,
      final String methodName, final boolean appendMethodName, final int methodNumber) {
    final StringBuilder functionName = new StringBuilder();
    if (namespace != null) {
      functionName.append(namespace.value());
    }
    if (!function.name().isEmpty()) {
      functionName.append(function.name());
    } else {
      functionName.append(className);
    }
    if (appendMethodName) {
      functionName.append(".");
      functionName.append(methodName);
    }
    if (methodNumber != 1) {
      functionName.append("_$");
      functionName.append(methodNumber);
    }
    return functionName.toString();
  }

  private static String generateFunctionNameForConstructor(final XLNamespace namespace, final XLFunction function, final String className,
      final int constructorNumber) {
    final StringBuilder functionName = new StringBuilder();
    if (namespace != null) {
      functionName.append(namespace.value());
    }
    if (!function.name().isEmpty()) {
      functionName.append(function.name());
    } else {
      functionName.append(className);
    }
    if (constructorNumber != 1) {
      functionName.append("_$");
      functionName.append(constructorNumber);
    }
    return functionName.toString();
  }
}
