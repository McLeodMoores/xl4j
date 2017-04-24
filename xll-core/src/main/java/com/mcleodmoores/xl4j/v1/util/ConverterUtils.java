/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility classes for converters.
 */
public final class ConverterUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConverterUtils.class);

  /**
   * Gets the most specific bound for a wildcard type, or returns the type of the input.
   * @param type
   *          the type
   * @return
   *          the bound
   */
  public static Type getBound(final Type type) {
    if (type instanceof WildcardType) {
      final Type[] upperBounds = ((WildcardType) type).getUpperBounds();
      final Type[] lowerBounds = ((WildcardType) type).getLowerBounds();
      Type[] bounds;
      if (upperBounds.length > 0 && lowerBounds.length > 0) {
        // ? super X, so use the lower bound as it's the most specific
        bounds = lowerBounds;
      } else {
        bounds = lowerBounds.length > 0 ? lowerBounds : upperBounds;
      }
      switch (bounds.length) {
        case 0:
          return Object.class; //TODO is this possible?
        case 1:
          return bounds[0];
        default:
          // should never be reached
          LOGGER.warn("Map value parameter has multiple bounds, only considering first in conversion");
          return bounds[0];
      }
    }
    return type;
  }

  /**
   * Gets the component type of a generic array. If the array is a parameterized type (e.g. <code>public &lt;T&gt; void method(T[] array</code>)
   * then the <code>TypeVariable</code> name is matched to the component type of the array. Otherwise, the generic component type of the
   * array is returned.
   * @param genericArrayType
   *          the type, not null
   * @return
   *          the component type
   */
  public static Type getComponentTypeForGenericArray(final GenericArrayType genericArrayType) {
    ArgumentChecker.notNull(genericArrayType, "genericArrayType");
    final Type genericComponentType = genericArrayType.getGenericComponentType();
    if (genericComponentType instanceof TypeVariable) {
      final GenericDeclaration genericDeclaration = ((TypeVariable<?>) genericComponentType).getGenericDeclaration();
      final TypeVariable<?>[] typeParameters = genericDeclaration.getTypeParameters();
      for (final TypeVariable<?> typeParameter : typeParameters) {
        if (typeParameter.getName().equals(genericComponentType.getTypeName())) {
          final Type[] bounds = typeParameter.getBounds();
          if (bounds == null) {
            throw new XL4JRuntimeException("Could not get bounds for " + genericDeclaration);
          }
          if (bounds.length != 1) {
            throw new XL4JRuntimeException("Could not get bounds " + Arrays.toString(bounds));
          }
          return getBound(bounds[0]);
        }
      }
    } else if (genericComponentType instanceof ParameterizedType) {
      return genericComponentType;
    }
    throw new XL4JRuntimeException("GenericComponentType of " + genericArrayType + " was not a TypeVariable: have " + genericComponentType);
  }

  /**
   * Restricted constructor.
   */
  private ConverterUtils() {
  }
}
