/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.core;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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

import com.mcleodmoores.xl4j.v1.api.annotations.XLConstant;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.core.ExcelCallback;
import com.mcleodmoores.xl4j.v1.api.core.FunctionDefinition;
import com.mcleodmoores.xl4j.v1.api.core.InvokerFactory;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Class to scan for {@link XLFunction}, {@link XLConstant} and {@link XLFunctions} annotations and register each function with Excel.
 */
public class ReflectiveFunctionRegistry extends AbstractFunctionRegistry {
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

  private static final Logger LOGGER = LoggerFactory.getLogger(ReflectiveFunctionRegistry.class);
  // REVIEW: is this the best structure to use?
  private final Set<FunctionDefinition> _functionDefinitions =
      Collections.synchronizedSet(new TreeSet<>(new Comparator<FunctionDefinition>() {

        @Override
        public int compare(final FunctionDefinition arg0, final FunctionDefinition arg1) {
          return arg1.getFunctionMetadata().getName().compareTo(arg0.getFunctionMetadata().getName());
        }

      }));
  private final AtomicInteger _exportCounter = new AtomicInteger();
  private final ConcurrentMap<Integer, FunctionDefinition> _functionDefinitionLookup = new ConcurrentHashMap<>();
  private final BlockingQueue<Collection<FunctionDefinition>> _finishedScan = new ArrayBlockingQueue<>(1);

  /**
   * Default constructor.
   *
   * @param invokerFactory
   *          invoker factory used to create method and constructor invokers to perform type conversions
   */
  public ReflectiveFunctionRegistry(final InvokerFactory invokerFactory) {
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
      createAndRegisterFunctions(_invokerFactory);
      LOGGER.info("Scan and create finished, putting to Blocking Queue");
      try {
        _finishedScan.put(_functionDefinitions);
      } catch (final InterruptedException e) {
        throw new XL4JRuntimeException("Unexpected interrupt while sending function definitions over queue");
      }
    }
  }

  /**
   * Register functions and constants.
   *
   * @param callback
   *          the Excel callback interface
   */
  @Override
  public void registerFunctions(final ExcelCallback callback) {
    LOGGER.info("registerFunctions called with {}", callback);
    try {
      final Collection<FunctionDefinition> functionDefinitions = _finishedScan.take();
      LOGGER.info("got collection from finishedFunctionScan queue, iterating over them...");
      for (final FunctionDefinition functionDefinition : functionDefinitions) {
        try {
          callback.registerFunction(functionDefinition);
        } catch (final XL4JRuntimeException xl4jre) {
          LOGGER.error("Problem registering function, skipping", xl4jre);
        }
      }
      LOGGER.info("finished registering functions");
    } catch (final InterruptedException e) {
      throw new XL4JRuntimeException("Unexpected interrupt while waiting for function definitions from queue");
    }
  }

  @Override
  protected void createAndRegisterFunctions(final InvokerFactory invokerFactory) {
    final Set<String> registeredFunctionNames = new HashSet<>();
    final Reflections reflections = new Reflections(new ConfigurationBuilder().addUrls(ClasspathHelper.forJavaClassPath())
        .addScanners(new MethodAnnotationsScanner(), new MethodParameterScanner(), new TypeAnnotationsScanner(), new FieldAnnotationsScanner()));
    try {
      addDefinitions(getFunctionsForMethods(invokerFactory, reflections.getMethodsAnnotatedWith(XLFunction.class)), registeredFunctionNames);
    } catch (final Exception e) {
      LOGGER.error("Exception while scanning XLFunction-annotated methods", e);
    }
    try {
      @SuppressWarnings("rawtypes")
      final Set<Constructor> constructorsAnnotatedWithFunction = reflections.getConstructorsAnnotatedWith(XLFunction.class);
      // horrible, but don't want to put a raw type in the abstract class
      final Set<Constructor<?>> constructors = new HashSet<>();
      for (final Constructor<?> constructor : constructorsAnnotatedWithFunction) {
        constructors.add(constructor);
      }
      addDefinitions(getFunctionsForConstructors(invokerFactory, constructors), registeredFunctionNames);
    } catch (final Exception e) {
      LOGGER.error("Exception while scanning XLFunction-annotated constructors", e);
    }
    try {
      addDefinitions(getFunctionsForTypes(invokerFactory, reflections.getTypesAnnotatedWith(XLFunctions.class)), registeredFunctionNames);
    } catch (final Exception e) {
      LOGGER.error("Exceptions while scanning XLFunctions-annotation methods", e);
    }
    try {
      final Set<Class<?>> classesAnnotatedWithConstant = reflections.getTypesAnnotatedWith(XLConstant.class);
      addDefinitions(getConstantsForTypes(invokerFactory, classesAnnotatedWithConstant), registeredFunctionNames);
    } catch (final Exception e) {
      LOGGER.error("Exception while scanning XLConstant-annotated classes", e);
    }
    try {
      addDefinitions(getConstantsForFields(invokerFactory, reflections.getFieldsAnnotatedWith(XLConstant.class)), registeredFunctionNames);
    } catch (final Exception e) {
      LOGGER.error("Exception while scanning XLConstant-annotated fields", e);
    }
  }

  /**
   * This allocates an export number.
   *
   * @return the allocated export number
   */
  @Override
  protected int allocateExport() {
    final int exportNumber = _exportCounter.getAndIncrement();
    return exportNumber;
  }

  /**
   * Look up the function definition, from the allocated.
   *
   * @param exportNumber
   *          the number of the export in the parameter size block
   * @return the function definition, not null throws XL4JRuntimeException if function definition could not be found
   */
  @Override
  public FunctionDefinition getFunctionDefinition(final int exportNumber) {
    final FunctionDefinition functionDefinition = _functionDefinitionLookup.get(exportNumber);
    if (functionDefinition != null) {
      return functionDefinition;
    }
    throw new XL4JRuntimeException("Cannot find function definition with export number " + exportNumber);
  }

  private void addDefinitions(final List<FunctionDefinition> definitions, final Set<String> registeredFunctionNames) {
    for (final FunctionDefinition definition : definitions) {
      checkForDuplicateFunctionNames(registeredFunctionNames, definition);
      // put the definition in some look-up tables.
      LOGGER.info("Allocating export number {} to function {}", definition.getExportNumber(), definition.getFunctionMetadata().getName());
      _functionDefinitionLookup.put(definition.getExportNumber(), definition);
      _functionDefinitions.add(definition);
    }
  }

  private static void checkForDuplicateFunctionNames(final Set<String> registeredFunctionNames, final FunctionDefinition functionDefinition) {
    final String name = functionDefinition.getFunctionMetadata().getName();
    if (registeredFunctionNames.contains(name.toUpperCase())) {
      LOGGER.warn("Have already registered a function called {}, ignoring", name);
    } else {
      registeredFunctionNames.add(name.toUpperCase());
    }
  }

}
