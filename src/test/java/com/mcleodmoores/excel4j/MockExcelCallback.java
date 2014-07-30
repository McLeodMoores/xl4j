/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * 
 */
public class MockExcelCallback implements RawExcelCallback {

  
  @Override
  public int xlfRegister(XLString dllPath, XLString functionExportName, XLString functionSignature, XLString functionWorksheetName, XLString argumentNames,
      XLValue functionType, XLValue functionCategory, XLValue notUsed, XLValue helpTopic, XLValue description, XLValue... argsHelp) {
    // TODO Auto-generated method stub
    return 0;
  }



}
