/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.xll;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.excel4j.lowlevel.LowLevelExcelCallback;

/**
 * 
 */
public class XLLAccumulatingFunctionRegistry implements LowLevelExcelCallback {
  private static Logger s_logger = LoggerFactory.getLogger(XLLAccumulatingFunctionRegistry.class);
  /**
   * Native accessed data structure (public fields for speed).
   */
  public class LowLevelEntry {
    // CHECKSTYLE:OFF
    public int _exportNumber;
    public String _functionExportName;
    public String _functionSignature;
    public String _functionWorksheetName;
    public String _argumentNames;
    public int _functionType;
    public String _functionCategory;
    public String _acceleratorKey;
    public String _helpTopic;
    public String _description;
    public String[] _argsHelp;
    // CHECKSTYLE:ON
  }
  
  private List<LowLevelEntry> _entries = new ArrayList<>();
  
  @Override
  // CHECKSTYLE:OFF can't control signature.
  public int xlfRegister(final int exportNumber, final String functionExportName, final String functionSignature, 
      final String functionWorksheetName, final String argumentNames, final int functionType, 
      final String functionCategory, final String acceleratorKey, final String helpTopic, 
      final String description, final String... argsHelp) {
    LowLevelEntry entry = new LowLevelEntry();
    entry._exportNumber = exportNumber;
    entry._functionExportName = functionExportName;
    entry._functionSignature = functionSignature;
    entry._functionWorksheetName = functionWorksheetName;
    entry._argumentNames = argumentNames;
    entry._functionType = functionType;
    entry._functionCategory = functionCategory;
    entry._acceleratorKey = acceleratorKey == null ? "" : acceleratorKey;
    entry._helpTopic = helpTopic == null ? "" : helpTopic;
    entry._description = description == null ? "" : description;
    entry._argsHelp = argsHelp;
    _entries.add(entry);
    s_logger.info("just added entry to entries table");
    return 0;
  }

  public LowLevelEntry[] getEntries() {
    LowLevelEntry[] array = _entries.toArray(new LowLevelEntry[] {});
    s_logger.info("getEntries() called, returning {} items", array.length);
    return array;
  }
}
