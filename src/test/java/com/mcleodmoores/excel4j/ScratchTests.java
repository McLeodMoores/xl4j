package com.mcleodmoores.excel4j;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests to prove API behaviour if not clear from docs.
 */
public class ScratchTests {
  
  public void test1D(Collection<Integer>[] oneD) {
    
  }
  
  public void test2D(Collection<Integer>[][] twoD) {
    
  }
  // CHECKSTYLE:OFF
  @Test
  public void testArrayReflection() throws NoSuchMethodException, SecurityException {
    Assert.assertEquals(Integer[].class.getComponentType(), Integer.class);
    Assert.assertEquals(Integer[][].class.getComponentType(), Integer[].class);
    Assert.assertEquals(Integer[][][].class.getComponentType(), Integer[][].class);
    Type[] params = getClass().getMethod("test1D", Collection[].class).getGenericParameterTypes();
    Assert.assertTrue(params[0] instanceof GenericArrayType);
  }
}
