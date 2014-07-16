/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * 
 */
public class JConstructTest {
  private static final String CLASSNAME = "java.util.ArrayList";

  // CHECKSTYLE:OFF
  
  @Test
  public void testIsAssignableFrom() {
    Integer x = 8;
    ExcelToJavaTypeMapping mapping1 = ExcelToJavaTypeMapping.of(XLNumber.class, x.getClass());
    ExcelToJavaTypeMapping mapping2 = ExcelToJavaTypeMapping.of(XLNumber.class, XLNumber.class);
    Assert.assertFalse(mapping1.isAssignableFrom(mapping2));
    Assert.assertFalse(mapping2.isAssignableFrom(mapping1));
  }
  @Test
  public void testJConstruct() {
    JConstruct construct = new JConstruct();
    XLValue jconstruct = construct.jconstruct(XLString.of(CLASSNAME), XLNumber.of(6d));
    Assert.assertEquals(jconstruct.getClass(), XLObject.class);
    System.err.println(jconstruct.toString());
  }
  
}
