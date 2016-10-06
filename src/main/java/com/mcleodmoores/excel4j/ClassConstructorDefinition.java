/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.javacode.ConstructorInvoker;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.ExportUtils;

/**
 * Class that stores meta-data and argument conversion information about constructors that can be
 * accessed from Excel.
 */
//TODO rename
public final class ClassConstructorDefinition {
  /** Meta data about the constructor */
  private final ClassMetadata _classMetadata;
  /** The constructor invoker */
  private final ConstructorInvoker _constructorInvoker;
  /** Constructor export number in registry */
  private final int _exportNumber;

  /**
   * Creates an instance.
   * @param classMetadata  the meta-data
   * @param constructorInvoker  the constructor invoker
   * @param exportNumber  the export number
   */
  private ClassConstructorDefinition(final ClassMetadata classMetadata, final ConstructorInvoker constructorInvoker, final int exportNumber) {
    _classMetadata = classMetadata;
    _constructorInvoker = constructorInvoker;
    _exportNumber = exportNumber;
  }

  /**
   * Static factory method to create an instance.
   * @param classMetadata  the annotation-based meta-data about the function, not null
   * @param constructorInvoker  the type conversion and new instance binding for this constructor, not null
   * @param exportNumber  the number of the DLL export that handles this constructor (only unique to the number of parameters used)
   * @return  an instance of this constructor definition
   */
  public static ClassConstructorDefinition of(final ClassMetadata classMetadata, final ConstructorInvoker constructorInvoker, final int exportNumber) {
    ArgumentChecker.notNull(classMetadata, "classMetadata");
    ArgumentChecker.notNull(constructorInvoker, "constructorInvoker");
    return new ClassConstructorDefinition(classMetadata, constructorInvoker, exportNumber);
  }

  /**
   * @return the constructor meta-data, not null
   */
  public ClassMetadata getClassMetadata() {
    return _classMetadata;
  }

  /**
   * @return  the constructor invoker, not null
   */
  public ConstructorInvoker getConstructorInvoker() {
    return _constructorInvoker;
  }

  /**
   * @return the export name, not null
   */
  public String getExportName() {
    return ExportUtils.buildExportName(_exportNumber);
  }

  /**
   * @return the export number
   */
  public int getExportNumber() {
    return _exportNumber;
  }
}
