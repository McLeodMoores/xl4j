/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import com.mcleodmoores.xl4j.javacode.ConstructorInvoker;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.ExportUtils;

/**
 * Class that stores meta-data and argument conversion information about constructors that can be
 * accessed from Excel.
 */
public final class ConstructorDefinition {
  /** Meta data about the constructor */
  private final ConstructorMetadata _constructorMetadata;
  /** The constructor invoker */
  private final ConstructorInvoker _constructorInvoker;
  /** Constructor export number in registry */
  private final int _exportNumber;

  /**
   * Creates an instance.
   * @param constructorMetadata  the meta-data
   * @param constructorInvoker  the constructor invoker
   * @param exportNumber  the export number
   */
  private ConstructorDefinition(final ConstructorMetadata constructorMetadata, final ConstructorInvoker constructorInvoker, final int exportNumber) {
    _constructorMetadata = constructorMetadata;
    _constructorInvoker = constructorInvoker;
    _exportNumber = exportNumber;
  }

  /**
   * Static factory method to create an instance.
   * @param constructorMetadata  the annotation-based meta-data about the function, not null
   * @param constructorInvoker  the type conversion and new instance binding for this constructor, not null
   * @param exportNumber  the number of the DLL export that handles this constructor (only unique to the number of parameters used)
   * @return  an instance of this constructor definition
   */
  public static ConstructorDefinition of(final ConstructorMetadata constructorMetadata, final ConstructorInvoker constructorInvoker, final int exportNumber) {
    ArgumentChecker.notNull(constructorMetadata, "constructorMetadata");
    ArgumentChecker.notNull(constructorInvoker, "constructorInvoker");
    return new ConstructorDefinition(constructorMetadata, constructorInvoker, exportNumber);
  }

  /**
   * @return the constructor meta-data, not null
   */
  public ConstructorMetadata getConstructorMetadata() {
    return _constructorMetadata;
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
