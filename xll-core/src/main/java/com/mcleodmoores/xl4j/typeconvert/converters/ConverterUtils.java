/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility classes for converters.
 */
public final class ConverterUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConverterUtils.class);
  /**
   * Gets the most specific bound for a wildcard type, or returns the type of the input.
   * @param type  the type
   * @return the bound
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
   * Restricted constructor.
   */
  private ConverterUtils() {
  }
}
