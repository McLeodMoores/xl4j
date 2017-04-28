/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.awt.Dimension;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.core.Heap;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLNil;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link JMethodChain}.
 */
public class JMethodChainTest {

  /**
   * Tests that the constructor arguments must be in either a row or column.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testConstructorArgumentsAreArea() {
    final XLString className = XLString.of("java.awt.BasicStroke");
    final XLArray constructorArguments = XLArray.of(new XLValue[][] {
      new XLValue[] {XLNumber.of(0.02), XLNumber.of(3)},
      new XLValue[] {XLNumber.of(0), XLNumber.of(2.4)}});
    final XLArray methodNames = XLArray.of(new XLValue[][] {
      new XLValue[] {XLString.of("getDashArray"), XLString.of("toString")}});
    final XLArray methodArguments = XLArray.of(new XLValue[][] {
      new XLValue[] {XLNil.INSTANCE},
      new XLValue[] {XLNil.INSTANCE}
    });
    JMethodChain.methodChain(className, constructorArguments, methodNames, methodArguments);
  }

  /**
   * Tests that the method names must be in either a row or column.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testMethodNamesAreArea1() {
    final Dimension dim = new Dimension(0, 0);
    final Heap heap = ExcelFactory.getInstance().getHeap();
    final long handle = heap.getHandle(dim);
    final XLObject xlObject = XLObject.of(Dimension.class, handle);
    final XLArray methodNames = XLArray.of(new XLValue[][] {
      new XLValue[] {XLString.of("setSize"), XLString.of("setSize")},
      new XLValue[] {XLString.of("setSize"), XLString.of("getHeight")}});
    final XLArray methodArguments = XLArray.of(new XLValue[][] {
      new XLValue[] {XLNumber.of(10), XLNumber.of(10)},
      new XLValue[] {XLNumber.of(10), XLNumber.of(10)},
      new XLValue[] {XLNumber.of(10), XLNumber.of(10)},
      new XLValue[] {XLNil.INSTANCE},
    });
    JMethodChain.methodChain(xlObject, methodNames, methodArguments);
  }

  /**
   * Tests that the method names must be in either a row or column.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testMethodNamesAreArea2() {
    final XLString className = XLString.of("java.awt.Dimension");
    final XLArray constructorArguments = XLArray.of(new XLValue[][] {
      new XLValue[] {XLNumber.of(0), XLNumber.of(0)}});
    final XLArray methodNames = XLArray.of(new XLValue[][] {
      new XLValue[] {XLString.of("setSize"), XLString.of("setSize")},
      new XLValue[] {XLString.of("setSize"), XLString.of("getHeight")}});
    final XLArray methodArguments = XLArray.of(new XLValue[][] {
      new XLValue[] {XLNumber.of(10), XLNumber.of(10)},
      new XLValue[] {XLNumber.of(10), XLNumber.of(10)},
      new XLValue[] {XLNumber.of(10), XLNumber.of(10)},
      new XLValue[] {XLNil.INSTANCE},
    });
    JMethodChain.methodChain(className, constructorArguments, methodNames, methodArguments);
  }

  /**
   * Tests the chain of calls <code>new BasicStroke(float, int, int, float, float[], float).getDashArray().toString()</code>.
   */
  @Test
  public void testConstructorArgumentsAsRow() {
    final XLString className = XLString.of("java.awt.BasicStroke");
    final XLArray constructorArguments = XLArray.of(new XLValue[][] {
      new XLValue[] {XLNumber.of(0.02)},
      new XLValue[] {XLNumber.of(1)},
      new XLValue[] {XLNumber.of(0)},
      new XLValue[] {XLNumber.of(2.4)},
      new XLValue[] {XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(0.04), XLNumber.of(0.04)}})},
      new XLValue[] {XLNumber.of(0.06)}});
    final XLArray methodNames = XLArray.of(new XLValue[][] {
      new XLValue[] {XLString.of("getDashArray"), XLString.of("toString")}});
    final XLArray methodArguments = XLArray.of(new XLValue[][] {
      new XLValue[] {XLNil.INSTANCE},
      new XLValue[] {XLMissing.INSTANCE}
    });
    final Object result = JMethodChain.methodChain(className, constructorArguments, methodNames, methodArguments);
    assertTrue(result instanceof XLObject);
    final String s = (String) ExcelFactory.getInstance().getHeap().getObject(((XLObject) result).getHandle());
    assertTrue(s.startsWith("[F@")); // float array
  }

  /**
   * Tests the chain of calls <code>new BasicStroke(float, int, int, float, float[], float).getDashArray().toString()</code>.
   */
  @Test
  public void testConstructorArgumentsAsColumn() {
    final XLString className = XLString.of("java.awt.BasicStroke");
    final XLArray constructorArguments = XLArray.of(new XLValue[][] {
      new XLValue[] {XLNumber.of(0.02), XLNumber.of(1), XLNumber.of(0), XLNumber.of(2.4),
          XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(0.04), XLNumber.of(0.04)}}),
          XLNumber.of(0.06)}});
    final XLArray methodNames = XLArray.of(new XLValue[][] {
      new XLValue[] {XLString.of("getDashArray"), XLString.of("toString")}});
    final XLArray methodArguments = XLArray.of(new XLValue[][] {
      new XLValue[] {XLNil.INSTANCE},
      new XLValue[] {XLNil.INSTANCE}
    });
    final Object result = JMethodChain.methodChain(className, constructorArguments, methodNames, methodArguments);
    assertTrue(result instanceof XLObject);
    final String s = (String) ExcelFactory.getInstance().getHeap().getObject(((XLObject) result).getHandle());
    assertTrue(s.startsWith("[F@")); // float array
  }

  /**
   * Tests the chain of calls <code>new Dimension(int, int).getSize().toString()</code>.
   */
  @Test
  public void testMethodArgumentsAsColumn() {
    final XLString className = XLString.of("java.awt.Dimension");
    final XLArray constructorArguments = XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10), XLNumber.of(20)}});
    final XLArray methodNames = XLArray.of(new XLValue[][] {
      new XLValue[] {XLString.of("getSize")},
      new XLValue[] {XLString.of("toString")}
    });
    final XLArray methodArguments = XLArray.of(new XLValue[][] {
      new XLValue[] {XLNil.INSTANCE},
      new XLValue[] {XLNil.INSTANCE}
    });
    final Object result = JMethodChain.methodChain(className, constructorArguments, methodNames, methodArguments);
    assertTrue(result instanceof XLObject);
    final String s = (String) ExcelFactory.getInstance().getHeap().getObject(((XLObject) result).getHandle());
    assertEquals(s, "java.awt.Dimension[width=10,height=20]");
  }

  /**
   * Tests the chain of calls <code>new Dimension(int, int).getSize().toString()</code>.
   */
  @Test
  public void testMethodArgumentsAsRow() {
    final XLString className = XLString.of("java.awt.Dimension");
    final XLArray constructorArguments = XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10), XLNumber.of(20)}});
    final XLArray methodNames = XLArray.of(new XLValue[][] {
      new XLValue[] {XLString.of("getSize"), XLString.of("toString")}
    });
    final XLArray methodArguments = XLArray.of(new XLValue[][] {
      new XLValue[] {XLNil.INSTANCE, XLNil.INSTANCE}
    });
    final Object result = JMethodChain.methodChain(className, constructorArguments, methodNames, methodArguments);
    assertTrue(result instanceof XLObject);
    final String s = (String) ExcelFactory.getInstance().getHeap().getObject(((XLObject) result).getHandle());
    assertEquals(s, "java.awt.Dimension[width=10,height=20]");
  }
}
