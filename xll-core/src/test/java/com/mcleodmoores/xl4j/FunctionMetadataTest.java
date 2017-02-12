/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import static com.mcleodmoores.xl4j.FunctionMetadataHelper.CONSTANT;
import static com.mcleodmoores.xl4j.FunctionMetadataHelper.FUNCTION;
import static com.mcleodmoores.xl4j.FunctionMetadataHelper.FUNCTIONS;
import static com.mcleodmoores.xl4j.FunctionMetadataHelper.NAME;
import static com.mcleodmoores.xl4j.FunctionMetadataHelper.NAMESPACE;
import static com.mcleodmoores.xl4j.FunctionMetadataHelper.PARAMETERS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;

/**
 * Unit tests for {@link FunctionMetadata}.
 */

public class FunctionMetadataTest {
  /**
   * Tests that a function annotation must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullFunctionSpec1() {
    FunctionMetadata.of(NAMESPACE, (XLFunction) null, PARAMETERS, NAME);
  }

  /**
   * Tests that a parameter annotation must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullParameters1() {
    FunctionMetadata.of(NAMESPACE, FUNCTION, null, NAME);
  }

  /**
   * Tests that a name must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullName1() {
    FunctionMetadata.of(NAMESPACE, FUNCTION, PARAMETERS, null);
  }

  /**
   * Tests that a function annotation must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullFunctionSpec2() {
    FunctionMetadata.of((XLFunction) null, PARAMETERS, NAME);
  }

  /**
   * Tests that a parameter annotation must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullParameters2() {
    FunctionMetadata.of(FUNCTION, null, NAME);
  }

  /**
   * Tests that a name must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullName2() {
    FunctionMetadata.of(FUNCTION, PARAMETERS, null);
  }

  /**
   * Tests that a constant annotation must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullConstantSpec1() {
    FunctionMetadata.of(null, NAME);
  }

  /**
   * Tests that a name must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullName3() {
    FunctionMetadata.of(CONSTANT, null);
  }

  /**
   * Tests that a constant annotation must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullConstantSpec2() {
    FunctionMetadata.of(NAMESPACE, null, NAME);
  }

  /**
   * Tests that a name must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullName4() {
    FunctionMetadata.of(NAMESPACE, CONSTANT, null);
  }

  /**
   * Tests that the parameters cannot be obtained for a constant.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testParametersForConstant() {
    FunctionMetadata.of(CONSTANT, NAME).getParameters();
  }

  /**
   * Tests that a function annotation must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullFunctionSpec3() {
    FunctionMetadata.of(NAMESPACE, (XLFunctions) null, PARAMETERS, NAME);
  }

  /**
   * Tests that a parameter annotation must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullParameters3() {
    FunctionMetadata.of(NAMESPACE, FUNCTIONS, null, NAME);
  }

  /**
   * Tests that a name must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullName5() {
    FunctionMetadata.of(NAMESPACE, FUNCTIONS, PARAMETERS, null);
  }

  /**
   * Tests that a function annotation must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullFunctionSpec4() {
    FunctionMetadata.of((XLFunctions) null, PARAMETERS, NAME);
  }

  /**
   * Tests that a parameter annotation must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullParameters4() {
    FunctionMetadata.of(FUNCTIONS, null, NAME);
  }

  /**
   * Tests that a name must be provided.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullName6() {
    FunctionMetadata.of(FUNCTIONS, PARAMETERS, null);
  }

  /**
   * Tests the metadata when all fields are available.
   */
  @Test
  public void testMetadataForFunction1() {
    final FunctionMetadata metadata = FunctionMetadata.of(NAMESPACE, FUNCTION, PARAMETERS, NAME);
    assertFalse(metadata.isConstantSpec());
    assertNull(metadata.getConstantSpec());
    assertEquals(metadata.getFunctionSpec(), FUNCTION);
    assertNull(metadata.getFunctionsSpec());
    assertEquals(metadata.getName(), NAME);
    assertEquals(metadata.getNamespace(), NAMESPACE);
    assertEquals(metadata.getParameters(), PARAMETERS);
  }

  /**
   * Tests the metadata when the namespace is null.
   */
  @Test
  public void testMetadataForFunction2() {
    final FunctionMetadata metadata = FunctionMetadata.of(null, FUNCTION, PARAMETERS, NAME);
    assertFalse(metadata.isConstantSpec());
    assertNull(metadata.getConstantSpec());
    assertEquals(metadata.getFunctionSpec(), FUNCTION);
    assertNull(metadata.getFunctionsSpec());
    assertEquals(metadata.getName(), NAME);
    assertNull(metadata.getNamespace());
    assertEquals(metadata.getParameters(), PARAMETERS);
  }

  /**
   * Tests the metadata when the namespace is not supplied.
   */
  @Test
  public void testMetadataForFunction3() {
    final FunctionMetadata metadata = FunctionMetadata.of(FUNCTION, PARAMETERS, NAME);
    assertFalse(metadata.isConstantSpec());
    assertNull(metadata.getConstantSpec());
    assertEquals(metadata.getFunctionSpec(), FUNCTION);
    assertNull(metadata.getFunctionsSpec());
    assertEquals(metadata.getName(), NAME);
    assertNull(metadata.getNamespace());
    assertEquals(metadata.getParameters(), PARAMETERS);
  }

  /**
   * Tests the metadata representing a constant.
   */
  @Test
  public void testMetadataForConstant1() {
    final FunctionMetadata metadata = FunctionMetadata.of(NAMESPACE, CONSTANT, NAME);
    assertTrue(metadata.isConstantSpec());
    assertEquals(metadata.getConstantSpec(), CONSTANT);
    assertNull(metadata.getFunctionSpec());
    assertNull(metadata.getFunctionsSpec());
    assertEquals(metadata.getName(), NAME);
    assertEquals(metadata.getNamespace(), NAMESPACE);
  }

  /**
   * Tests the metadata representing a constant when the namespace is not supplied.
   */
  @Test
  public void testMetadataForConstant2() {
    final FunctionMetadata metadata = FunctionMetadata.of(CONSTANT, NAME);
    assertTrue(metadata.isConstantSpec());
    assertEquals(metadata.getConstantSpec(), CONSTANT);
    assertNull(metadata.getFunctionSpec());
    assertNull(metadata.getFunctionsSpec());
    assertEquals(metadata.getName(), NAME);
    assertNull(metadata.getNamespace());
  }

  /**
   * Tests the metadata when all fields are available.
   */
  @Test
  public void testMetadataForFunction4() {
    final FunctionMetadata metadata = FunctionMetadata.of(NAMESPACE, FUNCTIONS, PARAMETERS, NAME);
    assertFalse(metadata.isConstantSpec());
    assertNull(metadata.getConstantSpec());
    assertNull(metadata.getFunctionSpec());
    assertEquals(metadata.getFunctionsSpec(), FUNCTIONS);
    assertEquals(metadata.getName(), NAME);
    assertEquals(metadata.getNamespace(), NAMESPACE);
    assertEquals(metadata.getParameters(), PARAMETERS);
  }

  /**
   * Tests the metadata when the namespace is null.
   */
  @Test
  public void testMetadataForFunction5() {
    final FunctionMetadata metadata = FunctionMetadata.of(null, FUNCTIONS, PARAMETERS, NAME);
    assertFalse(metadata.isConstantSpec());
    assertNull(metadata.getConstantSpec());
    assertNull(metadata.getFunctionSpec());
    assertEquals(metadata.getFunctionsSpec(), FUNCTIONS);
    assertEquals(metadata.getName(), NAME);
    assertNull(metadata.getNamespace());
    assertEquals(metadata.getParameters(), PARAMETERS);
  }

  /**
   * Tests the metadata when the namespace is not supplied.
   */
  @Test
  public void testMetadataForFunction6() {
    final FunctionMetadata metadata = FunctionMetadata.of(FUNCTIONS, PARAMETERS, NAME);
    assertFalse(metadata.isConstantSpec());
    assertNull(metadata.getConstantSpec());
    assertNull(metadata.getFunctionSpec());
    assertEquals(metadata.getFunctionsSpec(), FUNCTIONS);
    assertEquals(metadata.getName(), NAME);
    assertNull(metadata.getNamespace());
    assertEquals(metadata.getParameters(), PARAMETERS);
  }
}
