/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 *
 */
public class JMethodTest {
  private static final String CLASSNAME = "java.util.ArrayList";
  // private static final String CLASSNAME2 = "java.util.HashSet";
  private static final String CLASSNAME_INTEGER = "java.lang.Integer";

  //  @Test
  //  public void testIsAssignableFrom() {
  //    Integer x = 8;
  //    ExcelToJavaTypeMapping mapping1 = ExcelToJavaTypeMapping.of(XLNumber.class, x.getClass());
  //    ExcelToJavaTypeMapping mapping2 = ExcelToJavaTypeMapping.of(XLNumber.class, XLNumber.class);
  //    Assert.assertFalse(mapping1.isAssignableFrom(mapping2));
  //    Assert.assertFalse(mapping2.isAssignableFrom(mapping1));
  //  }
  //  @Test
  //  public void testJConstruct() {
  //    XLValue jconstruct = JConstruct.jconstruct(XLString.of(CLASSNAME), XLNumber.of(6d));
  //    Assert.assertEquals(jconstruct.getClass(), XLObject.class);
  //    System.err.println(jconstruct.toString());
  //    XLObject arrayList = (XLObject) jconstruct;
  //    XLValue jconstruct2 = JConstruct.jconstruct(XLString.of(CLASSNAME2), arrayList);
  //    System.err.println(jconstruct2.toString());
  //    XLObject hashSet = (XLObject) jconstruct2;
  //    Assert.assertEquals(jconstruct2.getClass(), XLObject.class);
  //  }

  @Test
  public void testJMethod() {
    final XLObject arrayList = (XLObject) JConstruct.jconstruct(XLString.of(CLASSNAME), XLNumber.of(6d));
    final XLObject integer = (XLObject) JConstruct.jconstruct(XLString.of(CLASSNAME_INTEGER), XLNumber.of(3d));
    JMethod.jMethod(arrayList, XLString.of("add"), integer);
    final Object arrList2 = ExcelFactory.getInstance().getHeap().getObject(arrayList.getHandle());
    System.err.println(arrList2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJMethodReflective1() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    final XLObject arrayList = (XLObject) method.invoke(null, XLString.of(CLASSNAME), XLNumber.of(6d));
    System.err.println(arrayList);
  }

  @Test
  public void testJMethodReflective1_5() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    final XLObject arrayList = (XLObject) method.invoke(null, XLString.of(CLASSNAME), new XLValue[] { XLNumber.of(6d) });
    System.err.println(arrayList);
  }

  @Test
  public void testJMethodReflective1_75() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    final XLObject arrayList = (XLObject) method.invoke(null, XLString.of(CLASSNAME), new XLValue[] { });
    System.err.println(arrayList);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJMethodReflective2() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Object[] args = new Object[] { XLString.of(CLASSNAME), XLNumber.of(6d) };
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    final XLObject arrayList = (XLObject) method.invoke(null, args);
    System.err.println(arrayList);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJMethodReflective3() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Object[] args = new Object[] { XLString.of(CLASSNAME) };
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    final XLObject arrayList = (XLObject) method.invoke(null, args);
    System.err.println(arrayList);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJMethodReflective4() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Object[] args = new Object[] { XLString.of(CLASSNAME), new Object[0] };
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    final XLObject arrayList = (XLObject) method.invoke(null, args);
    System.err.println(arrayList);
  }

  @Test
  public void testJMethodReflective5() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Object[] args = new Object[] { XLString.of(CLASSNAME), new XLValue[0] };
    System.err.println(Arrays.toString(args));
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    final XLObject arrayList = (XLObject) method.invoke(null, args);
    System.err.println(arrayList);
  }
}
