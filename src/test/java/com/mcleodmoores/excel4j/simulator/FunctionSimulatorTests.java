package com.mcleodmoores.excel4j.simulator;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.heap.Heap;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Tests using the function simulator.
 */
public class FunctionSimulatorTests {
  private static final String CLASSNAME = "java.util.ArrayList";
  //private static final String CLASSNAME2 = "java.util.HashSet";
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
  
  @Test
  public void testGetElem() {
    XLValue result = _processor.invoke("MakeList", XLArray.of(new XLValue[][] { { XLString.of("One"), XLString.of("Two") } } ));
    Assert.assertEquals(result.getClass(), XLObject.class);
    XLObject arrayListObj = (XLObject) result;
    Object arrayList = _heap.getObject(arrayListObj.getHandle());
    Assert.assertEquals(arrayList.getClass(), ArrayList.class);
    Assert.assertEquals(((List<?>)arrayList).size(), 2);
    XLValue result2 = _processor.invoke("ListElement",  result, XLNumber.of(1));
    Assert.assertEquals(result2.getClass(), XLString.class);
    Assert.assertEquals(((XLString) result2).getValue(), "Two");   
  }
  
  
  @SuppressWarnings("unchecked")
  @Test
  public void testSequence() {
    XLValue list = _processor.invoke("JConstruct", XLString.of(CLASSNAME));
    XLValue integer = _processor.invoke("JConstruct", XLString.of(CLASSNAME_INTEGER), XLNumber.of(6));
    _processor.invoke("JMethod", list, XLString.of("add"), integer);
    List<Integer> listObj = (List<Integer>) ExcelFactory.getInstance().getHeap().getObject(((XLObject) list).getHandle());
    System.err.println(listObj);
  }
}
