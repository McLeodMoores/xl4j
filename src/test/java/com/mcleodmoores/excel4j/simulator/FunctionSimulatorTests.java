package com.mcleodmoores.excel4j.simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private static final String CLASSNAME_LIST = "java.util.ArrayList";
  //private static final String CLASSNAME2 = "java.util.HashSet";
  private static final String CLASSNAME_INTEGER = "java.lang.Integer";
  private static final String CLASSNAME_MAP = "java.util.HashMap";

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
    final XLValue result = _processor.invoke("JConstruct", XLString.of(CLASSNAME_LIST));
    Assert.assertEquals(result.getClass(), XLObject.class);
    final XLObject arrayListObj = (XLObject) result;
    final Object arrayList = _heap.getObject(arrayListObj.getHandle());
    Assert.assertEquals(arrayList.getClass(), ArrayList.class);
  }

  @Test
  public void testGetElem() {
    final XLValue result = _processor.invoke("MakeList", XLArray.of(new XLValue[][] { { XLString.of("One"), XLString.of("Two") } } ));
    Assert.assertEquals(result.getClass(), XLObject.class);
    final XLObject arrayListObj = (XLObject) result;
    final Object arrayList = _heap.getObject(arrayListObj.getHandle());
    Assert.assertEquals(arrayList.getClass(), ArrayList.class);
    Assert.assertEquals(((List<?>)arrayList).size(), 2);
    final XLValue result2 = _processor.invoke("ListElement",  result, XLNumber.of(1));
    Assert.assertEquals(result2.getClass(), XLString.class);
    Assert.assertEquals(((XLString) result2).getValue(), "Two");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSequence() {
    final XLValue list = _processor.invoke("JConstruct", XLString.of(CLASSNAME_LIST));
    final XLValue integer = _processor.invoke("JConstruct", XLString.of(CLASSNAME_INTEGER), XLNumber.of(6));
    _processor.invoke("JMethod", list, XLString.of("add"), integer);
    final List<Integer> listObj = (List<Integer>) ExcelFactory.getInstance().getHeap().getObject(((XLObject) list).getHandle());
    Assert.assertEquals(listObj, Arrays.asList(6));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMap() {
    final XLValue map = _processor.invoke("JConstruct", XLString.of(CLASSNAME_MAP));
    final XLValue key = _processor.invoke("JConstruct", XLString.of(CLASSNAME_INTEGER), XLNumber.of(10));
    final XLValue value = _processor.invoke("JConstruct", XLString.of(CLASSNAME_LIST));
    final XLValue integer = _processor.invoke("JStaticMethodX", XLString.of(CLASSNAME_INTEGER), XLString.of("valueOf"), XLNumber.of(100));
    _processor.invoke("JMethod", value, XLString.of("add"), integer);
    _processor.invoke("JMethod", map, XLString.of("put"), key, value);
    final Integer keyObj = (Integer) ExcelFactory.getInstance().getHeap().getObject(((XLObject) key).getHandle());
    Assert.assertEquals(keyObj.intValue(), 10);
    final List<Integer> valueObj = (List<Integer>) ExcelFactory.getInstance().getHeap().getObject(((XLObject) value).getHandle());
    Assert.assertEquals(valueObj, Arrays.asList(100));
    final Map<Integer, List<Integer>> mapObj = (Map<Integer, List<Integer>>) ExcelFactory.getInstance().getHeap().getObject(((XLObject) map).getHandle());
    final Map<Integer, List<Integer>> expectedMap = new HashMap<>();
    expectedMap.put(10, Arrays.asList(100));
    Assert.assertEquals(mapObj, expectedMap);
  }
}
