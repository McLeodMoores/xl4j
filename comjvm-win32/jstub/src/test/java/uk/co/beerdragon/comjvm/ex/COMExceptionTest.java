/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.ex;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link COMException} classes.
 */
@Test
public class COMExceptionTest {

  /**
   * Tests that E_ABORT is mapped.
   */
  @Test (expectedExceptions = OperationAbortedException.class)
  public void testAbort () {
    throw COMException.of (HRESULT.E_ABORT);
  }

  /**
   * Tests that E_ACESSDENIED is mapped.
   */
  @Test (expectedExceptions = GeneralAccessDeniedException.class)
  public void testAccessDenied () {
    throw COMException.of (HRESULT.E_ACCESSDENIED);
  }

  /**
   * Tests that E_FAIL is mapped.
   */
  @Test (expectedExceptions = UnspecifiedFailureException.class)
  public void testFail () {
    throw COMException.of (HRESULT.E_FAIL);
  }

  /**
   * Tests that E_HANDLE is mapped.
   */
  @Test (expectedExceptions = InvalidHandleException.class)
  public void testHandle () {
    throw COMException.of (HRESULT.E_HANDLE);
  }

  /**
   * Tests that E_INVALIDARG is mapped.
   */
  @Test (expectedExceptions = InvalidArgumentException.class)
  public void testInvalidArg () {
    throw COMException.of (HRESULT.E_INVALIDARG);
  }

  /**
   * Tests that E_NOINTERFACE is mapped.
   */
  @Test (expectedExceptions = NoSuchInterfaceException.class)
  public void testNoInterface () {
    throw COMException.of (HRESULT.E_NOINTERFACE);
  }

  /**
   * Tests that E_NOTIMPL is mapped.
   */
  @Test (expectedExceptions = NotImplementedException.class)
  public void testNotImplemented () {
    throw COMException.of (HRESULT.E_NOTIMPL);
  }

  /**
   * Tests that E_OUTOFMEMORY is mapped.
   */
  @Test (expectedExceptions = OutOfMemoryException.class)
  public void testOutOfMemory () {
    throw COMException.of (HRESULT.E_OUTOFMEMORY);
  }

  /**
   * Tests that E_POINTER is mapped.
   */
  @Test (expectedExceptions = InvalidPointerException.class)
  public void testInvalidPointerException () {
    throw COMException.of (HRESULT.E_POINTER);
  }

  /**
   * Tests that E_UNEXPECTED is mapped.
   */
  @Test (expectedExceptions = UnexpectedFailureException.class)
  public void testUnexpected () {
    throw COMException.of (HRESULT.E_UNEXPECTED);
  }

  /**
   * Tests that {@link COMException} is used for unmapped exceptions
   */
  @Test (expectedExceptions = COMException.class)
  public void testMisc () {
    throw COMException.of (0xA0001234);
  }

  /**
   * Tests the {@link Object#toString} implementation.
   */
  public void testToString () {
    Assert.assertEquals (COMException.of (HRESULT.E_ABORT).toString (), "E_ABORT");
    Assert.assertEquals (COMException.of (HRESULT.E_ACCESSDENIED).toString (), "E_ACCESSDENIED");
    Assert.assertEquals (COMException.of (HRESULT.E_FAIL).toString (), "E_FAIL");
    Assert.assertEquals (COMException.of (HRESULT.E_HANDLE).toString (), "E_HANDLE");
    Assert.assertEquals (COMException.of (HRESULT.E_INVALIDARG).toString (), "E_INVALIDARG");
    Assert.assertEquals (COMException.of (HRESULT.E_NOINTERFACE).toString (), "E_NOINTERFACE");
    Assert.assertEquals (COMException.of (HRESULT.E_NOTIMPL).toString (), "E_NOTIMPL");
    Assert.assertEquals (COMException.of (HRESULT.E_OUTOFMEMORY).toString (), "E_OUTOFMEMORY");
    Assert.assertEquals (COMException.of (HRESULT.E_POINTER).toString (), "E_POINTER");
    Assert.assertEquals (COMException.of (HRESULT.E_UNEXPECTED).toString (), "E_UNEXPECTED");
    Assert.assertEquals (COMException.of (0xA0001234).toString (), "FC-0-4660");
  }

  /**
   * Tests the HRESULT querying methods.
   */
  public void testQueries () {
    final COMException e = COMException.of (HRESULT.E_ACCESSDENIED);
    Assert.assertEquals (e.toHRESULT (), new HRESULT (HRESULT.E_ACCESSDENIED));
    Assert.assertEquals (e.isCustomer (), false);
    Assert.assertEquals (e.getFacility (), 7);
    Assert.assertEquals (e.getCode (), 5);
    Assert.assertEquals (e.hashCode (), HRESULT.E_ACCESSDENIED);
  }

  /**
   * Tests the {@link Object#equals} implementation.
   */
  public void testEquals () {
    final COMException e1 = COMException.of (HRESULT.E_ACCESSDENIED);
    final COMException e2 = COMException.of (HRESULT.E_FAIL);
    Assert.assertTrue (e1.equals (e1));
    Assert.assertFalse (e1.equals (e2));
    Assert.assertTrue (e1.equals (COMException.of (HRESULT.E_ACCESSDENIED)));
    Assert.assertFalse (e1.equals (null));
  }

}
