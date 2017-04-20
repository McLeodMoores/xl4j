/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.values;

import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Adapter containing method to throw XL4JRuntimeException if a visitor is not implemented, but called.
 * 
 * @param <T>
 *          the type of data returned by the visitor
 */
public class XLValueVisitorAdapter<T> implements XLValueVisitor<T> {

  @Override
  public T visitXLString(final XLString value) {
    throw new XL4JRuntimeException("XLString visitor not implemented");
  }

  @Override
  public T visitXLBoolean(final XLBoolean value) {
    throw new XL4JRuntimeException("XLBoolean visitor not implemented");
  }

  @Override
  public T visitXLBigData(final XLBigData value) {
    throw new XL4JRuntimeException("XLBigData visitor not implemented");
  }

  @Override
  public T visitXLError(final XLError value) {
    throw new XL4JRuntimeException("XLError visitor not implemented");
  }

  @Override
  public T visitXLInteger(final XLInteger value) {
    throw new XL4JRuntimeException("XLInteger visitor not implemented");
  }

  @Override
  public T visitXLLocalReference(final XLLocalReference value) {
    throw new XL4JRuntimeException("XLLocalReference visitor not implemented");
  }

  @Override
  public T visitXLMissing(final XLMissing value) {
    throw new XL4JRuntimeException("XLMissing visitor not implemented");
  }

  @Override
  public T visitXLNil(final XLNil value) {
    throw new XL4JRuntimeException("XLNil visitor not implemented");
  }

  @Override
  public T visitXLNumber(final XLNumber value) {
    throw new XL4JRuntimeException("XLNumber visitor not implemented");
  }

  @Override
  public T visitXLMultiReference(final XLMultiReference value) {
    throw new XL4JRuntimeException("XLMultiReference visitor not implemented");
  }

  @Override
  public T visitXLArray(final XLArray value) {
    throw new XL4JRuntimeException("XLArray visitor not implemented");
  }

  @Override
  public T visitXLObject(final XLObject value) {
    throw new XL4JRuntimeException("XLObject visitor not implemented");
  }
}
