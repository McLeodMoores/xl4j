/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLConstant;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.core.ConstructorInvoker;
import com.mcleodmoores.xl4j.v1.api.core.FieldGetter;
import com.mcleodmoores.xl4j.v1.api.core.FunctionDefinition;
import com.mcleodmoores.xl4j.v1.api.core.FunctionRegistry;
import com.mcleodmoores.xl4j.v1.api.core.InvokerFactory;
import com.mcleodmoores.xl4j.v1.api.core.MethodInvoker;

/**
 * Parent of classes that register functions annotated by {@link XLConstant}, {@link XLFunction} or
 * {@link XLFunctions}. This base class extracts information from these annotations and constructs
 * the {@link FunctionDefinition}.
 */
public abstract class AbstractFunctionRegistry implements FunctionRegistry {
  private static final Set<String> EXCLUDED_METHOD_NAMES = new HashSet<>();
  private static final XLParameter[] EMPTY_PARAMETER_ARRAY = new XLParameter[0];
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
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFunctionRegistry.class);

  /**
   * Allocate an export number for the function.
   * @return  the export number
   */
  protected abstract int allocateExport();

  /**
   * Create function definitions and register them.
   *
   * @param invokerFactory
   *          the invoker factory, not null
   */
  protected abstract void createAndRegisterFunctions(InvokerFactory invokerFactory);

  /**
   * For all types (class or enum) annotated with {@link XLConstant}, retrieve each public field and create a {@link FunctionDefinition}.
   * The name of the function is generated using the rule <br>
   * <code>[Optional namespace value]([Name] OR [Simple class name]).[Field name]</code>.
   *
   * @param invokerFactory
   *          the invoker factory, not null
   * @param typeAnnotatedWith
   *          the types annotated with {@link XLConstant}, not null
   * @return
   *          a list of function definitions
   */
  protected List<FunctionDefinition> getConstantsForTypes(final InvokerFactory invokerFactory,
      final Collection<Class<?>> typeAnnotatedWith) {
    final List<FunctionDefinition> definitions = new ArrayList<>();
    for (final Class<?> clazz : typeAnnotatedWith) {
      try {
        final XLConstant constantAnnotation = clazz.getAnnotation(XLConstant.class);
        XLNamespace namespaceAnnotation = null;
        if (clazz.isAnnotationPresent(XLNamespace.class)) {
          namespaceAnnotation = clazz.getAnnotation(XLNamespace.class);
        }
        for (final Field field : clazz.getFields()) {
          final String functionName = generateFunctionNameForField(namespaceAnnotation, constantAnnotation, clazz.getSimpleName(),
              field.getName(), true);
          definitions.add(generateDefinition(field, invokerFactory, constantAnnotation, namespaceAnnotation, functionName));
        }
      } catch (final Exception e) {
        LOGGER.error("Exception while creating function definition for field in class " + clazz, e);
        continue;
      }
    }
    return definitions;
  }

  /**
   * For all fields annotated with {@link XLConstant}, create a {@link FunctionDefinition}. The name of the function is generated
   * using the rule <br>
   * <code>[Optional namespace value].([Name] OR ([Class name].[Field name]))</code>
   *
   * @param invokerFactory
   *          the invoker factory, not null
   * @param fieldsAnnotatedWith
   *          the fields annotated with {@link XLConstant}, not null
   * @return
   *          a list of function definitions
   */
  protected List<FunctionDefinition> getConstantsForFields(final InvokerFactory invokerFactory,
      final Collection<Field> fieldsAnnotatedWith) {
    final List<FunctionDefinition> definitions = new ArrayList<>();
    for (final Field field : fieldsAnnotatedWith) {
      try {
        XLNamespace namespaceAnnotation = null;
        if (field.getDeclaringClass().isAnnotationPresent(XLNamespace.class)) {
          namespaceAnnotation = field.getDeclaringClass().getAnnotation(XLNamespace.class);
        }
        final XLConstant constantAnnotation = field.getAnnotation(XLConstant.class);
        final String functionName = generateFunctionNameForField(namespaceAnnotation, constantAnnotation,
            field.getDeclaringClass().getSimpleName(), field.getName(), constantAnnotation.name().isEmpty());
        definitions.add(generateDefinition(field, invokerFactory, constantAnnotation, namespaceAnnotation, functionName));
      } catch (final Exception e) {
        LOGGER.error("Exception while creating function definition for field " + field, e);
        continue;
      }
    }
    return definitions;
  }

  /**
   * For all methods annotated with {@link XLFunction}, create a {@link FunctionDefinition}. The name of the function is generated
   * using the rule <br>
   * <code>[Optional namespace value].([Name] OR ([Class name].[Method name]))</code>
   *
   * @param invokerFactory
   *          the invoker factory, not null
   * @param methodsAnnotatedWith
   *          the methods annotated with {@link XLFunction}, not null
   * @return
   *          a list of function definitions
   */
  protected List<FunctionDefinition> getFunctionsForMethods(final InvokerFactory invokerFactory,
      final Collection<Method> methodsAnnotatedWith) {
    final List<FunctionDefinition> definitions = new ArrayList<>();
    for (final Method method : methodsAnnotatedWith) {
      try {
        XLNamespace namespaceAnnotation = null;
        if (method.getDeclaringClass().isAnnotationPresent(XLNamespace.class)) {
          namespaceAnnotation = method.getDeclaringClass().getAnnotation(XLNamespace.class);
        }
        final XLFunction functionAnnotation = method.getAnnotation(XLFunction.class);
        final XLParameter[] xlParameterAnnotations = getXLParameterAnnotations(method.getParameterAnnotations());
        final String functionName = generateFunctionNameForMethod(namespaceAnnotation, functionAnnotation.name(),
            method.getDeclaringClass().getSimpleName(), method.getName(), false, functionAnnotation.name().isEmpty(), 1);
        definitions.add(generateDefinition(method, invokerFactory, functionAnnotation, namespaceAnnotation, xlParameterAnnotations, functionName));
      } catch (final Exception e) {
        LOGGER.error("Exception while creating function definition for method {}", method, e);
        continue;
      }
    }
    return definitions;
  }

  /**
   * For all constructors annotated with {@link XLFunction}, create a {@link FunctionDefinition}. The name of the function is generated
   * using the rule <br>
   * <code>[Optional namespace value].([Name] OR [Class name])</code>
   *
   * @param invokerFactory
   *          the invoker factory, not null
   * @param constructorsAnnotatedWith
   *          the constructors annotated with {@link XLFunction}, not null
   * @return
   *          a list of function definitions
   */
  protected List<FunctionDefinition> getFunctionsForConstructors(final InvokerFactory invokerFactory,
      final Collection<Constructor<?>> constructorsAnnotatedWith) {
    final List<FunctionDefinition> definitions = new ArrayList<>();
    for (final Constructor<?> constructor : constructorsAnnotatedWith) {
      try {
        XLNamespace namespaceAnnotation = null;
        if (constructor.getDeclaringClass().isAnnotationPresent(XLNamespace.class)) {
          namespaceAnnotation = constructor.getDeclaringClass().getAnnotation(XLNamespace.class);
        }
        final XLFunction functionAnnotation = constructor.getAnnotation(XLFunction.class);
        final XLParameter[] xlParameterAnnotations = getXLParameterAnnotations(constructor.getParameterAnnotations());
        final String functionName = generateFunctionNameForConstructor(namespaceAnnotation, functionAnnotation.name(),
            constructor.getDeclaringClass().getSimpleName(), false, 1);
        definitions.add(generateDefinition(constructor, invokerFactory, functionAnnotation, namespaceAnnotation, xlParameterAnnotations, functionName));
      } catch (final Exception e) {
        LOGGER.error("Exception while creating function definition for constructor {}", constructor, e);
        continue;
      }
    }
    return definitions;
  }

  /**
   * For all classes annotated with {@link XLFunctions}, create a {@link FunctionDefinition}. The name(s) of the functions are generated
   * using the rule <br>
   * <code>[Optional namespace value].([Name] OR [Class name])</code>
   * <br>
   * while the name(s) of the methods are generated using the rule <br>
   * <code>[Optional namespace value].([Name] OR ([Class name].[Method name]))</code>
   *
   * @param invokerFactory
   *          the invoker factory, not null
   * @param classesAnnotatedWith
   *          the classes annotated with {@link XLFunctions}, not null
   * @return
   *          a list of function definitions
   */
  protected List<FunctionDefinition> getFunctionsForTypes(final InvokerFactory invokerFactory,
      final Collection<Class<?>> classesAnnotatedWith) {
    final List<FunctionDefinition> definitions = new ArrayList<>();
    for (final Class<?> clazz : classesAnnotatedWith) {
      final boolean isAbstract = Modifier.isAbstract(clazz.getModifiers());
      XLNamespace namespaceAnnotation = null;
      final String className = clazz.getSimpleName();
      try {
        if (clazz.isAnnotationPresent(XLNamespace.class)) {
          namespaceAnnotation = clazz.getAnnotation(XLNamespace.class);
        }
        final XLFunctions classAnnotation = clazz.getAnnotation(XLFunctions.class);
        // if a superclass is annotated, classAnnotation will be null
        if (classAnnotation == null) {
          LOGGER.error("Could not get @XLFunctions annotation for {}; is the annotation on a superclass?", className);
          continue;
        }
        final boolean useClassName = classAnnotation.prefix() == null || classAnnotation.prefix().isEmpty();
        if (!isAbstract) {
          // build the constructor invokers
          final Constructor<?>[] constructors = clazz.getConstructors();
          int count = 1;
          for (final Constructor<?> constructor : constructors) {
            if (constructor.getAnnotation(XLFunction.class) != null) {
              // this will already have been registered, so skip
              continue;
            }
            final String functionName = generateFunctionNameForConstructor(namespaceAnnotation, classAnnotation.prefix(), className, useClassName, count);
            definitions.add(generateDefinition(constructor, invokerFactory, classAnnotation, namespaceAnnotation, EMPTY_PARAMETER_ARRAY, functionName));
            count++;
          }
        }
        // build the method invokers
        final Method[] methods = clazz.getMethods();
        final Map<String, Integer> methodNames = new HashMap<>();
        for (final Method method : methods) {
          if (method.getAnnotation(XLFunction.class) != null) {
            // this will already have been registered, so skip
            continue;
          }
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
          final String functionName = generateFunctionNameForMethod(namespaceAnnotation, classAnnotation.prefix(), className, methodName,
              useClassName, true, methodNameCount);
          definitions.add(generateDefinition(method, invokerFactory, classAnnotation, namespaceAnnotation, EMPTY_PARAMETER_ARRAY, functionName));
        }
      } catch (final Exception e) {
        LOGGER.error("Exception while creating function definition for constructor / method in {}", className, e);
        continue;
      }
    }
    return definitions;
  }

  private FunctionDefinition generateDefinition(final Field field, final InvokerFactory invokerFactory, final XLConstant constantAnnotation,
      final XLNamespace namespaceAnnotation, final String functionName) {
    final TypeConversionMode resultType = constantAnnotation.typeConversionMode();
    final FunctionMetadata metadata = FunctionMetadata.of(namespaceAnnotation, constantAnnotation, functionName);
    final FieldGetter fieldInvoker = invokerFactory.getFieldTypeConverter(field, resultType);
    final int allocatedExportNumber = allocateExport();
    return FunctionDefinition.of(metadata, fieldInvoker, allocatedExportNumber);
  }

  private FunctionDefinition generateDefinition(final Method method, final InvokerFactory invokerFactory, final XLFunction functionAnnotation,
      final XLNamespace namespaceAnnotation, final XLParameter[] parameterAnnotations, final String functionName) {
    final TypeConversionMode resultType = functionAnnotation.typeConversionMode();
    final MethodInvoker methodInvoker = invokerFactory.getMethodTypeConverter(method, resultType);
    final FunctionMetadata functionMetadata = FunctionMetadata.of(namespaceAnnotation, functionAnnotation, parameterAnnotations, functionName);
    final int allocatedExportNumber = allocateExport();
    return FunctionDefinition.of(functionMetadata, methodInvoker, allocatedExportNumber);
  }

  private FunctionDefinition generateDefinition(final Method method, final InvokerFactory invokerFactory,
      final XLFunctions functionsAnnotation, final XLNamespace namespaceAnnotation, final XLParameter[] parameterAnnotations,
      final String functionName) {
    final TypeConversionMode resultType = functionsAnnotation.typeConversionMode();
    final MethodInvoker methodInvoker = invokerFactory.getMethodTypeConverter(method, resultType);
    final FunctionMetadata functionMetadata = FunctionMetadata.of(namespaceAnnotation, functionsAnnotation, parameterAnnotations, functionName);
    final int allocatedExportNumber = allocateExport();
    return FunctionDefinition.of(functionMetadata, methodInvoker, allocatedExportNumber);
  }

  private FunctionDefinition generateDefinition(final Constructor<?> constructor, final InvokerFactory invokerFactory, final XLFunction functionAnnotation,
      final XLNamespace namespaceAnnotation, final XLParameter[] parameterAnnotations, final String functionName) {
    final ConstructorInvoker constructorInvoker = invokerFactory.getConstructorTypeConverter(constructor);
    final FunctionMetadata functionMetadata = FunctionMetadata.of(namespaceAnnotation, functionAnnotation, parameterAnnotations, functionName);
    final int allocatedExportNumber = allocateExport();
    return FunctionDefinition.of(functionMetadata, constructorInvoker, allocatedExportNumber);
  }

  private FunctionDefinition generateDefinition(final Constructor<?> constructor, final InvokerFactory invokerFactory,
      final XLFunctions functionsAnnotation, final XLNamespace namespaceAnnotation, final XLParameter[] parameterAnnotations,
      final String functionName) {
    final ConstructorInvoker constructorInvoker = invokerFactory.getConstructorTypeConverter(constructor);
    final FunctionMetadata functionMetadata = FunctionMetadata.of(namespaceAnnotation, functionsAnnotation, parameterAnnotations, functionName);
    final int allocatedExportNumber = allocateExport();
    return FunctionDefinition.of(functionMetadata, constructorInvoker, allocatedExportNumber);
  }

  private static XLParameter[] getXLParameterAnnotations(final Annotation[][] allParameterAnnotations) {
    final XLParameter[] xlParameterAnnotations = new XLParameter[allParameterAnnotations.length];
    // we rely here on the array being initialized to null
    for (int i = 0; i < allParameterAnnotations.length; i++) {
      if (allParameterAnnotations[i] != null) {
        for (int j = 0; j < allParameterAnnotations[i].length; j++) {
          if (allParameterAnnotations[i][j].annotationType().equals(XLParameter.class)) {
            xlParameterAnnotations[i] = (XLParameter) allParameterAnnotations[i][j];
            break;
          }
        }
      }
    }
    return xlParameterAnnotations;
  }

  private static String generateFunctionNameForField(final XLNamespace namespace, final XLConstant constant, final String className,
      final String fieldName, final boolean appendFieldName) {
    final StringBuilder functionName = new StringBuilder();
    if (namespace != null) {
      functionName.append(namespace.value());
    }
    if (constant.name().isEmpty()) {
      functionName.append(className);
    } else {
      functionName.append(constant.name());
    }
    if (appendFieldName) {
      functionName.append('.');
      functionName.append(fieldName);
    }
    return functionName.toString();
  }

  private static String generateFunctionNameForMethod(final XLNamespace namespace, final String nameOrPrefix, final String className,
      final String methodName, final boolean appendClassName, final boolean appendMethodName, final int methodNumber) {
    final StringBuilder functionName = new StringBuilder();
    if (namespace != null) {
      functionName.append(namespace.value());
    }
    if (nameOrPrefix.isEmpty()) {
      functionName.append(className);
    } else {
      functionName.append(nameOrPrefix);
      if (appendClassName) {
        functionName.append(className);
      }
    }
    if (appendMethodName) {
      functionName.append('.');
      functionName.append(methodName);
    }
    if (methodNumber != 1) {
      functionName.append("_$");
      functionName.append(methodNumber);
    }
    return functionName.toString();
  }

  private static String generateFunctionNameForConstructor(final XLNamespace namespace, final String nameOrPrefix, final String className,
      final boolean appendClassName, final int constructorNumber) {
    final StringBuilder functionName = new StringBuilder();
    if (namespace != null) {
      functionName.append(namespace.value());
    }
    if (nameOrPrefix == null || nameOrPrefix.isEmpty()) {
      functionName.append(className);
    } else {
      functionName.append(nameOrPrefix);
      if (appendClassName) {
        functionName.append(className);
      }
    }
    if (constructorNumber != 1) {
      functionName.append("_$");
      functionName.append(constructorNumber);
    }
    return functionName.toString();
  }
}
