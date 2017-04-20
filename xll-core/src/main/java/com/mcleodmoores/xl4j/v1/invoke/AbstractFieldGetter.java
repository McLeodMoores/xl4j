/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.core.FieldGetter;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Base for classes containing information about fields and the converters required to convert them
 * to Excel-appropriate types.
 */
public abstract class AbstractFieldGetter implements FieldGetter {
  /** The field */
  private final Field _field;
  /** The class of the field */
  private final Class<?> _type;
  /** The type of the field */
  private final Type _genericType;
  /** True if the field is static */
  private final boolean _isStatic;
  /** The field name */
  private final String _name;
  /** The class that declared the field */
  private final String _declaringClass;

  /**
   * @param field
   *          the field, not null
   */
  public AbstractFieldGetter(final Field field) {
    _field = ArgumentChecker.notNull(field, "field");
    _type = field.getType();
    _genericType = field.getGenericType();
    _isStatic = Modifier.isStatic(field.getModifiers());
    _name = field.getName();
    _declaringClass = field.getDeclaringClass().getName();
  }

  @Override
  public Class<?> getExcelReturnType() {
    return _type;
  }

  @Override
  public Type getFieldType() {
    return _genericType;
  }

  @Override
  public boolean isStatic() {
    return _isStatic;
  }

  @Override
  public String getFieldName() {
    return _name;
  }

  @Override
  public String getFieldDeclaringClass() {
    return _declaringClass;
  }

  /**
   * @return  the field
   */
  public Field getField() {
    return _field;
  }
}
