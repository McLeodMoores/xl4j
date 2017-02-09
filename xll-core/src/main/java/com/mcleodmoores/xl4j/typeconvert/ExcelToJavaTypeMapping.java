/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JReflectionUtils;

/**
 * Class to represent a Java source and Excel destination type or vice versa.
 */
public final class ExcelToJavaTypeMapping {
  /** The Excel source class */
  private final Class<?> _excelType;
  /** The Java destination type */
  private final Type _javaType;
  /** The Java destination class */
  private final Class<?> _javaClass;

  /**
   * @param excelType
   *          the Excel type, not null
   * @param javaType
   *          the Java type, not null
   */
  private ExcelToJavaTypeMapping(final Class<?> excelType, final Type javaType) {
    ArgumentChecker.notNull(excelType, "excelType");
    ArgumentChecker.notNull(javaType, "javaType");
    _javaType = javaType;
    _javaClass = Excel4JReflectionUtils.reduceToClass(javaType);
    _excelType = excelType;
  }

  /**
   * Static factory method.
   *
   * @param excelType
   *          the Excel type
   * @param javaType
   *          the Java type
   * @return an instance
   */
  public static ExcelToJavaTypeMapping of(final Class<?> excelType, final Type javaType) {
    return new ExcelToJavaTypeMapping(excelType, javaType);
  }

  /**
   * @return the excel Class in this key
   */
  public Class<?> getExcelClass() {
    return _excelType;
  }

  /**
   * @return the java type in this key
   */
  public Type getJavaType() {
    return _javaType;
  }

  /**
   * @return the java class in this key
   */
  public Class<?> getJavaClass() {
    return _javaClass;
  }

  /**
   * Checks whether both the excel type and java type are assignable from the other type (i.e. are the types compatible).
   *
   * @param other
   *          the ExcelToJavaTypeMapping to compare against
   * @return true, if both the excel and java types are assignable from
   */
  public boolean isAssignableFrom(final ExcelToJavaTypeMapping other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (!_excelType.isAssignableFrom(other.getExcelClass())) {
      return false;
    }
    if (!_javaClass.isAssignableFrom(other.getJavaClass())) {
      if (other.getJavaType() instanceof ParameterizedType) {
        final ParameterizedType otherParameterizedType = (ParameterizedType) other.getJavaType();
        final Type[] genericTypes = _javaClass.getGenericInterfaces();
        if (matchesGenericSignature(genericTypes, otherParameterizedType)) {
          return true;
        }
        // try super classes
        Class<?> superClass = _javaClass.getSuperclass();
        while (superClass != null) {
          final Type[] superClassGenericTypes = superClass.getGenericInterfaces();
          if (matchesGenericSignature(superClassGenericTypes, otherParameterizedType)) {
            return true;
          }
          superClass = superClass.getSuperclass();
        }
      }
      return false;
    }
    return true;
  }

  private boolean matchesGenericSignature(final Type[] genericTypes, final ParameterizedType otherParameterizedType) {
    if (genericTypes.length == 0) {
      return false;
    }
    for (final Type genericType : genericTypes) {
      if (genericType instanceof ParameterizedType) {
        final ParameterizedType parameterizedType = (ParameterizedType) genericType;
        if (parameterizedType.getRawType().equals(otherParameterizedType.getRawType())) {
          final Type[] typeArguments = parameterizedType.getActualTypeArguments();
          final Type[] otherTypeArguments = otherParameterizedType.getActualTypeArguments();
          if (typeArguments.length == otherTypeArguments.length) {
            for (int i = 0; i < typeArguments.length; i++) {
              final Type typeArgument = typeArguments[i];
              final Type otherTypeArgument = otherTypeArguments[i];
              if (typeArgument instanceof Class) {
                final Class<?> typeClass = (Class<?>) typeArgument;
                if (otherTypeArgument instanceof Class) {
                  final Class<?> otherTypeClass = (Class<?>) otherTypeArgument;
                  if (!typeClass.isAssignableFrom(otherTypeClass)) {
                    return false;
                  }
                } else if (otherTypeArgument instanceof TypeVariable) {
                  final Type[] bounds = ((TypeVariable<?>) otherTypeArgument).getBounds();
                  for (final Type bound : bounds) {
                    if (bound instanceof Class && !((Class<?>) bound).isAssignableFrom(typeClass)) {
                      return false;
                    }
                  }
                  return true;
                } else if (otherTypeArgument instanceof WildcardType) {
                } else if (otherTypeArgument instanceof GenericArrayType) {
                }
              }
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _excelType.hashCode();
    result = prime * result + _javaType.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ExcelToJavaTypeMapping)) {
      return false;
    }
    final ExcelToJavaTypeMapping other = (ExcelToJavaTypeMapping) obj;
    if (!_excelType.equals(other.getExcelClass())) {
      return false;
    }
    if (!_javaType.equals(other.getJavaType())) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ExcelToJavaTypeMapping[excelType=" + _excelType + ", javaType=" + _javaType + "]";
  }

}
