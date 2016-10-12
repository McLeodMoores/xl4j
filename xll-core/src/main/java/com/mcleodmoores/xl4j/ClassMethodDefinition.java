/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import com.mcleodmoores.xl4j.javacode.MethodInvoker;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.ExportUtils;

/**
 *  Class that stores meta-data and argument conversion information about methods that can be accessed
 *  from Excel.
 */
public final class ClassMethodDefinition {
  /** Meta data about the method */
  private final ClassMetadata _classMetadata;
  /** The method invoker */
  private final MethodInvoker _methodInvoker;
  /** Method export number in registry */
  private final int _exportNumber;

  /**
   * Creates an instance.
   * @param classMetadata  the meta-data
   * @param methodInvoker  the method invoker
   * @param exportNumber  the export number
   */
  private ClassMethodDefinition(final ClassMetadata classMetadata, final MethodInvoker methodInvoker, final int exportNumber) {
    _classMetadata = classMetadata;
    _methodInvoker = methodInvoker;
    _exportNumber = exportNumber;
  }

  /**
   * Static factory method to create an instance.
   * @param classMetadata  the annotation-based meta-data about the function, not null
   * @param methodInvoker  the type conversion and method invocation binding for this function, not null
   * @param exportNumber  the number of the DLL export that handles this function
   * @return  an instance of this method definition
   */
  public static ClassMethodDefinition of(final ClassMetadata classMetadata, final MethodInvoker methodInvoker, final int exportNumber) {
    ArgumentChecker.notNull(classMetadata, "classMetadata");
    ArgumentChecker.notNull(methodInvoker, "methodInvoker");
    return new ClassMethodDefinition(classMetadata, methodInvoker, exportNumber);
  }

  /**
   * @return  the method meta-data, not null
   */
  public ClassMetadata getClassMetadata() {
    return _classMetadata;
  }

  /**
   * @return  the method invoker, not null
   */
  public MethodInvoker getMethodInvoker() {
    return _methodInvoker;
  }

  /**
   * @return  the export name, not null
   */
  public String getExportName() {
    return ExportUtils.buildExportName(_exportNumber);
  }

  /**
   * @return  the export number
   */
  public int getExportNumber() {
    return _exportNumber;
  }
}
