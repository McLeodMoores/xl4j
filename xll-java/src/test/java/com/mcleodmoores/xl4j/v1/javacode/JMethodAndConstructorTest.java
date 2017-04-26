/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;

/**
 * Unit tests for {@link JConstruct} and {@link JMethod} that show how these classes must be called using reflection.
 */
public class JMethodAndConstructorTest {
  private static final String CLASSNAME_ARRAYLIST = "java.util.ArrayList";
  private static final String CLASSNAME_HASHSET = "java.util.HashSet";
  private static final String CLASSNAME_INTEGER = "java.lang.Integer";

  /**
   * Tests the logic that checks whether Excel and Java types are assignable from each other.
   */
  @Test
  public void testIsAssignableFrom() {
    final Integer x = 8;
    final ExcelToJavaTypeMapping mapping1 = ExcelToJavaTypeMapping.of(XLNumber.class, x.getClass());
    final ExcelToJavaTypeMapping mapping2 = ExcelToJavaTypeMapping.of(XLNumber.class, XLNumber.class);
    assertFalse(mapping1.isAssignableFrom(mapping2));
    assertFalse(mapping2.isAssignableFrom(mapping1));
  }

  /**
   * Tests construction of a list using new ArrayList(int) and a set using new HashSet(List).
   */
  @Test
  public void testJConstruct() {
    final XLValue jconstruct1 = JConstruct.jconstruct(XLString.of(CLASSNAME_ARRAYLIST), XLNumber.of(6d));
    assertEquals(jconstruct1.getClass(), XLObject.class);
    final XLObject arrayListXlObject = (XLObject) jconstruct1;
    final Object arrayListObject = ExcelFactory.getInstance().getHeap().getObject(arrayListXlObject.getHandle());
    assertTrue(arrayListObject instanceof ArrayList);
    final List<?> arrayList = (List<?>) arrayListObject;
    assertEquals(arrayList, new ArrayList<>(6));
    final XLValue jconstruct2 = JConstruct.jconstruct(XLString.of(CLASSNAME_HASHSET), arrayListXlObject);
    Assert.assertEquals(jconstruct2.getClass(), XLObject.class);
    final XLObject hashSetXlObject = (XLObject) jconstruct2;
    final Object hashSetObject = ExcelFactory.getInstance().getHeap().getObject(hashSetXlObject.getHandle());
    assertTrue(hashSetObject instanceof HashSet);
    final Set<?> hashSet = (Set<?>) hashSetObject;
    final Set<?> expectedHashSet = new HashSet<>(arrayList);
    assertEquals(hashSet, expectedHashSet);
  }

  /**
   * Tests construction of an empty list and adding an integer to it.
   */
  @Test
  public void testJMethod() {
    final XLObject arrayListXlObject = (XLObject) JConstruct.jconstruct(XLString.of(CLASSNAME_ARRAYLIST), XLNumber.of(6d));
    final Object arrayListObject = ExcelFactory.getInstance().getHeap().getObject(arrayListXlObject.getHandle());
    assertTrue(arrayListObject instanceof ArrayList);
    final List<?> arrayList = (List<?>) arrayListObject;
    assertEquals(arrayList, new ArrayList<>(6));
    final XLObject integerXlObject = (XLObject) JConstruct.jconstruct(XLString.of(CLASSNAME_INTEGER), XLNumber.of(3d));
    final Object integerObject = ExcelFactory.getInstance().getHeap().getObject(integerXlObject.getHandle());
    assertTrue(integerObject instanceof Integer);
    final Integer integer = (Integer) integerObject;
    JMethod.jMethod(arrayListXlObject, XLString.of("add"), integerXlObject);
    final Object arrayListObjectWithValue = ExcelFactory.getInstance().getHeap().getObject(arrayListXlObject.getHandle());
    assertTrue(arrayListObjectWithValue instanceof ArrayList);
    assertEquals(arrayListObjectWithValue, Arrays.asList(integer));
  }

  /**
   * Tries to run the JConstruct.jconstruct method using reflection. This method fails because the last parameter is
   * not an array.
   * @throws Exception  if there is a  problem calling the method
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJMethodReflective1() throws Exception {
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    method.invoke(null, XLString.of(CLASSNAME_ARRAYLIST), XLNumber.of(6d));
  }

  /**
   * Runs the JConstruct.jconstruct method using reflection and creates an empty array list with a given expected capacity.
   * @throws Exception  if there is a problem calling the method
   */
  @Test
  public void testJMethodReflective1_5() throws Exception {
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    final XLObject arrayListXlObject = (XLObject) method.invoke(null, XLString.of(CLASSNAME_ARRAYLIST), new XLValue[] { XLNumber.of(6d) });
    final List<?> arrayList = (List<?>) ExcelFactory.getInstance().getHeap().getObject(arrayListXlObject.getHandle());
    assertEquals(arrayList, new ArrayList<>());
  }

  /**
   * Runs the JConstruct.jconstruct method using reflection and creates an empty array list.
   * @throws Exception  if there is a problem calling the method
   */
  @Test
  public void testJMethodReflective1_75() throws Exception {
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    final XLObject arrayListXlObject = (XLObject) method.invoke(null, XLString.of(CLASSNAME_ARRAYLIST), new XLValue[] { });
    final List<?> arrayList = (List<?>) ExcelFactory.getInstance().getHeap().getObject(arrayListXlObject.getHandle());
    assertEquals(arrayList, new ArrayList<>());
  }

  /**
   * Tries to run the JConstruct.jconstruct method using reflection. This method fails because the arguments are put into
   * an array whose elements do not match the signature of (XLString, XLValue...).
   * @throws Exception  if there is a problem calling the method
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJMethodReflective2() throws Exception {
    final Object[] args = new Object[] { XLString.of(CLASSNAME_ARRAYLIST), XLNumber.of(6d) };
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    method.invoke(null, args);
  }

  /**
   * Tries to run the JConstruct.jconstruct method using reflection. This method fails because the arguments are put into
   * an array whose elements do not match the signature of (XLString, XLValue...).
   * @throws Exception  if there is a problem calling the method
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJMethodReflective3() throws Exception {
    final Object[] args = new Object[] { XLString.of(CLASSNAME_ARRAYLIST) };
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    method.invoke(null, args);
  }

  /**
   * Tries to run the JConstruct.jconstruct method using reflection. This method fails because the vararg arguments are put into
   * an array of Object, which does not match the signature of (XLString, XLValue...).
   * @throws Exception  if there is a problem calling the method
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJMethodReflective4() throws Exception {
    final Object[] args = new Object[] { XLString.of(CLASSNAME_ARRAYLIST), new Object[0] };
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    method.invoke(null, args);
  }

  /**
   * Runs the JConstruct.jconstruct method using reflection and creates an empty array list.
   * @throws Exception  if there is a problem calling the method
   */
  @Test
  public void testJMethodReflective5() throws Exception {
    final Object[] args = new Object[] { XLString.of(CLASSNAME_ARRAYLIST), new XLValue[0] };
    final Method method = JConstruct.class.getMethod("jconstruct", XLString.class, XLValue[].class);
    final XLObject arrayListXlObject = (XLObject) method.invoke(null, args);
    final List<?> arrayList = (List<?>) ExcelFactory.getInstance().getHeap().getObject(arrayListXlObject.getHandle());
    assertEquals(arrayList, new ArrayList<>());
  }
}
