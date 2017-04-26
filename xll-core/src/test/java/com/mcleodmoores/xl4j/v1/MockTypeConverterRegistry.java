/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JReflectionUtils;

/**
 * Creates a mock {@link TypeConverterRegistry} for use in tests.
 */
public final class MockTypeConverterRegistry implements TypeConverterRegistry {

  /**
   * Gets a builder.
   * @return
   *          the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A type converter registry builder.
   */
  public static final class Builder {
    private final SortedMap<Integer, List<TypeConverter>> _converters = new TreeMap<>();

    /**
     * Default constructor.
     */
    Builder() {
    }

    /**
     * Adds a type converter.
     * @param converter
     *          the type converter
     * @return
     *          the builder
     */
    public Builder with(final TypeConverter converter) {
      ArgumentChecker.notNull(converter, "converter");
      final int priority = -converter.getPriority(); // higher priorities should be searched first
      if (_converters.containsKey(priority)) {
        _converters.get(priority).add(converter);
      } else {
        final List<TypeConverter> converterList = new ArrayList<>();
        converterList.add(converter);
        _converters.put(priority, converterList);
      }
      return this;
    }

    /**
     * Builds the registry.
     * @return
     *          the registry
     */
    public MockTypeConverterRegistry build() {
      return new MockTypeConverterRegistry(_converters);
    }
  }

  private final Map<Integer, List<TypeConverter>> _converters;

  /**
   * @param converters
   *          the converters
   */
  MockTypeConverterRegistry(final SortedMap<Integer, List<TypeConverter>> converters) {
    _converters = Collections.unmodifiableSortedMap(converters);
  }

  @Override
  public TypeConverter findConverter(final ExcelToJavaTypeMapping requiredMapping) {
    for (final Map.Entry<Integer, List<TypeConverter>> entry : _converters.entrySet()) {
      final List<TypeConverter> converters = entry.getValue();
      for (final TypeConverter typeConverter : converters) {
        if (typeConverter.getExcelToJavaTypeMapping().isAssignableFrom(requiredMapping)) {
          return typeConverter;
        }
      }
    }
    return null;
  }

  @Override
  public TypeConverter findConverter(final Type requiredJava) {
    for (final Map.Entry<Integer, List<TypeConverter>> entry : _converters.entrySet()) {
      final List<TypeConverter> converters = entry.getValue();
      for (final TypeConverter typeConverter : converters) {
        if (typeConverter.getJavaToExcelTypeMapping().getJavaClass().isAssignableFrom(XL4JReflectionUtils.reduceToClass(requiredJava))) {
          return typeConverter;
        }
      }
    }
    return null;
  }

}
