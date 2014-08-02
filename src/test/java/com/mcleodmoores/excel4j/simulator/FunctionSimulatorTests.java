package com.mcleodmoores.excel4j.simulator;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.heap.Heap;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Tests using the function simulator.
 */
public class FunctionSimulatorTests {
  private static final String CLASSNAME = "java.util.ArrayList";
  private static final String CLASSNAME2 = "java.util.HashSet";
  private static final String CLASSNAME_INTEGER = "java.lang.Integer";
  private MockFunctionProcessor _processor;
  private Heap _heap;
  // CHECKSTYLE:OFF
  @BeforeTest
  public void initFunctionProcessor() {
    _processor = new MockFunctionProcessor();
    _heap = ExcelFactory.getInstance().getHeap();
  }
  @Test
  public void testJConstruct() {
    XLValue result = _processor.invoke("JConstruct", XLString.of(CLASSNAME));
    Assert.assertEquals(result.getClass(), XLObject.class);
    XLObject arrayListObj = (XLObject) result;
    Object arrayList = _heap.getObject(arrayListObj.getHandle());
    Assert.assertEquals(arrayList.getClass(), ArrayList.class);
  }
}
