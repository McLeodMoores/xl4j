/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Stores meta-data about a constructor that can be accessed from Excel. This is used for argument checking and registration.
 */
public final class ClassMetadata {
  /** The class annotation */
  private final XLClass _classSpec;
  /** The prefix for constructor names */
  private final XLNamespace _namespace;

  /**
   * Creates an instance.
   * @param classSpec  the class annotation
   * @param namespace  the namespace
   */
  private ClassMetadata(final XLClass classSpec, final XLNamespace namespace) {
    _classSpec = classSpec;
    _namespace = namespace;
  }

  /**
   * Create an instance given a namespace, constructor specification and arguments.
   * @param classSpec  an XLClass annotation, not null
   * @param namespace  an XLNamespace annotation or null if no name space
   * @return  an instance of a ClassMetadata
   */
  public static ClassMetadata of(final XLClass classSpec, final XLNamespace namespace) {
    ArgumentChecker.notNull(classSpec, "classSpec");
    return new ClassMetadata(classSpec, namespace);
  }

  /**
   * @return  the XLClass annotation, not null
   */
  public XLClass getClassSpec() {
    return _classSpec;
  }

  /**
   * @return  the XLNamespace annotation or null if no namespace
   */
  public XLNamespace getNamespace() {
    return _namespace;
  }

}
