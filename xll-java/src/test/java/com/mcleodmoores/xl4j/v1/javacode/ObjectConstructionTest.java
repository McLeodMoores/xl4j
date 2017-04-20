/**
 *
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;


/**
 * Tests construction of Objects from the function processor.
 */
public class ObjectConstructionTest extends TypeConstructionTests {

  /**
   * Tests construction of an object using new Object().
   */
  @Test
  public void testJConstructObject() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of("java.lang.Object"));
    assertTrue(xlValue instanceof XLObject);
    final Object object = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertEquals(object.getClass(), Object.class);
  }

  /**
   * Tests construction of an object using new ArrayList().
   */
  @Test
  public void testJConstructList() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of("java.util.ArrayList"));
    assertTrue(xlValue instanceof XLObject);
    final Object object = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(object instanceof ArrayList);
    final List<?> list = (ArrayList<?>) object;
    assertTrue(list.isEmpty());
  }

  /**
   * Tests construction of an object using new Random(long).
   */
  @Test
  public void testJConstructRandom() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of("java.util.Random"), XLNumber.of(123456L));
    final Object object = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(object instanceof Random);
    final Random random = (Random) object;
    final Random sameSeed = new Random(123456L);
    // not exhaustive but is different to sequence from new Random(0).
    for (int i = 0; i < 100; i++) {
      assertEquals(random.nextInt(), sameSeed.nextInt());
    }
  }

  /**
   * Tests construction of an object using Lists.newArrayList().
   */
  @Test
  public void testJMethodLists() {
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of("com.google.common.collect.Lists"), XLString.of("newArrayList"));
    assertTrue(xlValue instanceof XLObject);
    final Object object = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(object instanceof ArrayList);
    final List<?> list = (ArrayList<?>) object;
    assertTrue(list.isEmpty());
  }

  /**
   * Tests construction of an object using Collections.singletonList(Double). Note that even though a long is used when constructing
   * the XLNumber, the returned list is List<Double>.
   */
  @Test
  public void testJMethodSingletonList() {
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of("java.util.Collections"), XLString.of("singletonList"), XLNumber.of(1L));
    final Object object = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(object instanceof List);
    final List<?> list = (List<?>) object;
    assertEquals(list.size(), 1);
    assertEquals(list, Collections.singletonList(1d));
  }

  /**
   * Tests construction of an object using Lists.newArrayList(Integer...).
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testJMethodListsWithVarArgs() {
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of("com.google.common.collect.Lists"), XLString.of("newArrayList"),
        XLNumber.of(1), XLNumber.of(2));
    assertTrue(xlValue instanceof XLObject);
    final Object object = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(object instanceof ArrayList);
    final List<Double> list = (ArrayList<Double>) object;
    // list will contain doubles rather than ints, as XLNumber stores everything as doubles
    assertEquals(list, Arrays.asList(1.0, 2.0));
  }

}
