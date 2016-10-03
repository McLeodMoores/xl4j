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
 * Tests the {@link HRESULT} class.
 */
@Test
public class HRESULTTest {

  /**
   * Tests access of the S bit.
   */
  public void testS () {
    HRESULT hr = new HRESULT (0x80000000);
    Assert.assertTrue (hr.getS ());
    Assert.assertFalse (hr.isSuccess ());
    Assert.assertTrue (hr.isFailure ());
    hr = new HRESULT (HRESULT.S_OK);
    Assert.assertFalse (hr.getS ());
    Assert.assertTrue (hr.isSuccess ());
    Assert.assertFalse (hr.isFailure ());
  }

  /**
   * Tests access of the R bit.
   */
  public void testR () {
    HRESULT hr = new HRESULT (0xC0000000);
    Assert.assertTrue (hr.getR ());
    Assert.assertTrue (hr.isSevereFailure ());
    hr = new HRESULT (HRESULT.S_OK);
    Assert.assertFalse (hr.getR ());
    Assert.assertFalse (hr.isSevereFailure ());
  }

  /**
   * Tests access of the C bit.
   */
  public void testC () {
    HRESULT hr = new HRESULT (0xA0000000);
    Assert.assertTrue (hr.getC ());
    Assert.assertTrue (hr.isCustomer ());
    Assert.assertFalse (hr.isMicrosoft ());
    hr = new HRESULT (HRESULT.S_OK);
    Assert.assertFalse (hr.getC ());
    Assert.assertFalse (hr.isCustomer ());
    Assert.assertTrue (hr.isMicrosoft ());
  }

  /**
   * Tests access of the N bit.
   */
  public void testN () {
    HRESULT hr = new HRESULT (0x90000000);
    Assert.assertTrue (hr.getN ());
    Assert.assertTrue (hr.isNTStatus ());
    hr = new HRESULT (HRESULT.S_OK);
    Assert.assertFalse (hr.getN ());
    Assert.assertFalse (hr.isNTStatus ());
  }

  /**
   * Tests access of the X bit.
   */
  public void testX () {
    HRESULT hr = new HRESULT (0x88000000);
    Assert.assertTrue (hr.getX ());
    Assert.assertTrue (hr.isMessageID ());
    hr = new HRESULT (HRESULT.S_OK);
    Assert.assertFalse (hr.getX ());
    Assert.assertFalse (hr.isMessageID ());
  }

  /**
   * Tests access of the facility code.
   */
  public void testFacility () {
    final HRESULT hr = new HRESULT (0x80070005);
    Assert.assertEquals (hr.getFacility (), 7);
  }

  /**
   * Tests access of the error/status code.
   */
  public void testCode () {
    final HRESULT hr = new HRESULT (0x80070005);
    Assert.assertEquals (hr.getCode (), 5);
  }

  /**
   * Tests the {@link Object#toString} form.
   */
  public void testToString () {
    Assert.assertEquals (new HRESULT (0xFFFFFFFF).toString (), "FRCNX-2047-65535");
    Assert.assertEquals (new HRESULT (HRESULT.S_OK).toString (), "S-0-0");
  }

  /**
   * Tests the {@link Object#hashCode} form.
   */
  public void testHashCode () {
    Assert.assertEquals (new HRESULT (0xFFFFFFFF).hashCode (), -1);
    Assert.assertEquals (new HRESULT (HRESULT.S_OK).hashCode (), 0);
  }

  /**
   * Tests the {@link Object#equals} implementation.
   */
  public void testEquals () {
    final HRESULT hrA = new HRESULT (HRESULT.S_OK);
    final HRESULT hrB = new HRESULT (HRESULT.S_FALSE);
    Assert.assertTrue (hrA.equals (hrA));
    Assert.assertFalse (hrA.equals (hrB));
    Assert.assertTrue (hrA.equals (new HRESULT (HRESULT.S_OK)));
    Assert.assertFalse (hrA.equals (null));
  }

}