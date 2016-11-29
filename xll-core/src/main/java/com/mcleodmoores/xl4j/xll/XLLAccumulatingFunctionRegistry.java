/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.xll;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.lowlevel.LowLevelExcelCallback;

/**
 *
 */
public class XLLAccumulatingFunctionRegistry implements LowLevelExcelCallback {
  private static final Logger LOGGER = LoggerFactory.getLogger(XLLAccumulatingFunctionRegistry.class);

  /**
   * Native accessed data structure (public fields for speed).
   */
  public class LowLevelEntry {
    // CHECKSTYLE:OFF

    public int _exportNumber;
    public String _functionExportName;
    public boolean _isVarArgs;
    public boolean _isLongRunning;
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

  private final List<LowLevelEntry> _entries = new ArrayList<>();

  @Override
  // CHECKSTYLE:OFF can't control signature.
  public int xlfRegister(final int exportNumber, final String functionExportName, final boolean isVarArgs, final boolean isLongRunning,
      final String functionSignature, final String functionWorksheetName, final String argumentNames, final int functionType,
      final String functionCategory, final String acceleratorKey, final String helpTopic, final String description, final String... argsHelp) {
    final LowLevelEntry entry = new LowLevelEntry();
    entry._exportNumber = exportNumber;
    entry._functionExportName = functionExportName;
    entry._isVarArgs = isVarArgs;
    entry._isLongRunning = isLongRunning;
    entry._functionSignature = functionSignature;
    entry._functionWorksheetName = functionWorksheetName;
    entry._argumentNames = argumentNames;
    entry._functionType = functionType;
    entry._functionCategory = functionCategory;
    entry._acceleratorKey = acceleratorKey == null ? "" : acceleratorKey;
    entry._helpTopic = helpTopic == null ? "" : helpTopic;
    entry._description = description == null ? "" : description;
    // replace any missing help strings with empty string.
    final String[] argsHelpCp = new String[argsHelp.length];
    for (int i = 0; i < argsHelp.length; i++) {
      argsHelpCp[i] = argsHelp[i] == null ? "" : argsHelp[i];
    }
    entry._argsHelp = argsHelpCp;
    _entries.add(entry);
    LOGGER.info("just added entry to entries table");
    return 0;
  }

  /**
   * @return the function entries
   */
  public LowLevelEntry[] getEntries() {
    final LowLevelEntry[] array = _entries.toArray(new LowLevelEntry[] {});
    LOGGER.info("getEntries() called, returning {} items", array.length);
    return array;
  }
}
