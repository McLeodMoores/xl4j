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
  private static final String CLASSNAME2 = "java.util.HashSet";

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
    XLValue jconstruct = JConstruct.jconstruct(XLString.of(CLASSNAME), XLNumber.of(6d));
    Assert.assertEquals(jconstruct.getClass(), XLObject.class);
    System.err.println(jconstruct.toString());
    XLObject arrayList = (XLObject) jconstruct;
    XLValue jconstruct2 = JConstruct.jconstruct(XLString.of(CLASSNAME2), arrayList);
    System.err.println(jconstruct2.toString());
    XLObject hashSet = (XLObject) jconstruct2;
    Assert.assertEquals(hashSet.getClass(), XLObject.class);
  }
  
  @Test
  public void testJMethod() {
    XLObject three = (XLObject) JConstruct.jconstruct(XLString.of("java.lang.Integer"), XLNumber.of(3d));
    XLObject arrayList = (XLObject) JConstruct.jconstruct(XLString.of(CLASSNAME), XLNumber.of(6d));
    JMethod.jMethod(arrayList, XLString.of("add"), three);
  }
  
}
