package com.mcleodmoores.excel4j;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
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
    
    // test 1D
    Type[] params = getClass().getMethod("test1D", Collection[].class).getGenericParameterTypes();
    Assert.assertTrue(params[0] instanceof GenericArrayType);
    GenericArrayType gat = (GenericArrayType) params[0];
    Assert.assertTrue(gat.getGenericComponentType() instanceof ParameterizedType);
    ParameterizedType pt = (ParameterizedType) gat.getGenericComponentType();
    Assert.assertTrue(pt.getRawType() instanceof Class);
    Class<?> rawCls = (Class<?>) pt.getRawType();
    Assert.assertTrue(pt.getActualTypeArguments()[0] instanceof Class);
    Class<?> paramCls= (Class<?>) pt.getActualTypeArguments()[0];
    Assert.assertEquals(rawCls, Collection.class);
    Assert.assertEquals(paramCls, Integer.class);
    
    // now test 2D
    Type[] params2D = getClass().getMethod("test2D", Collection[][].class).getGenericParameterTypes();
    Assert.assertTrue(params2D[0] instanceof GenericArrayType);
    GenericArrayType gat2D = (GenericArrayType) params[0];
    Assert.assertTrue(gat2D.getGenericComponentType() instanceof ParameterizedType);
    ParameterizedType pt2D = (ParameterizedType) gat2D.getGenericComponentType();
    Assert.assertTrue(pt2D.getRawType() instanceof Class);
    Class<?> rawCls2D = (Class<?>) pt2D.getRawType();
    Assert.assertTrue(pt2D.getActualTypeArguments()[0] instanceof Class);
    Class<?> paramCls2D= (Class<?>) pt2D.getActualTypeArguments()[0];
    Assert.assertEquals(rawCls2D, Collection.class);
    Assert.assertEquals(paramCls2D, Integer.class);
    
  }
}
