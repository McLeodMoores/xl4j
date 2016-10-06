/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.stub;

import org.apache.bcel.generic.ConstantPoolGen;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.beerdragon.comjvm.COMHostSession;
import uk.co.beerdragon.comjvm.util.ArgumentType;

/**
 * Tests the {@link COMStubClass} class.
 */
@Test
public class DispatchMethodsTest {

  private void test (final ConstantPoolGen cp, final int returnType) {
    DispatchMethods d = DispatchMethods.dispatch0 (cp);
    Assert.assertEquals (d.getMethod (returnType), 1);
    Assert.assertEquals (d.getMethod (returnType), 1);
    d = DispatchMethods.dispatch1 (cp);
    Assert.assertEquals (d.getMethod (returnType), 2);
    Assert.assertEquals (d.getMethod (returnType), 2);
    d = DispatchMethods.dispatch2 (cp);
    Assert.assertEquals (d.getMethod (returnType), 3);
    Assert.assertEquals (d.getMethod (returnType), 3);
    d = DispatchMethods.dispatchN (cp);
    Assert.assertEquals (d.getMethod (returnType), 4);
    Assert.assertEquals (d.getMethod (returnType), 4);
  }

  /**
   * Tests the void returns.
   */
  public void testVoid () {
    final ConstantPoolGen cp = Mockito.mock (ConstantPoolGen.class);
    Mockito.when (cp.addMethodref (COMHostSession.class.getName (), "dispatch0V", "(II)V"))
        .thenReturn (1);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch1V", "(IILjava/lang/Object;)V"))
        .thenReturn (2);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch2V",
            "(IILjava/lang/Object;Ljava/lang/Object;)V")).thenReturn (3);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatchV", "(II[Ljava/lang/Object;)V"))
        .thenReturn (4);
    test (cp, 0);
  }

  /**
   * Tests the reference returns.
   */
  public void testRef () {
    final ConstantPoolGen cp = Mockito.mock (ConstantPoolGen.class);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch0A", "(II)Ljava/lang/Object;"))
        .thenReturn (1);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch1A",
            "(IILjava/lang/Object;)Ljava/lang/Object;")).thenReturn (2);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch2A",
            "(IILjava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")).thenReturn (3);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatchA",
            "(II[Ljava/lang/Object;)Ljava/lang/Object;")).thenReturn (4);
    test (cp, ArgumentType.REF);
  }

  /**
   * Tests the integral word returns.
   */
  public void testWord () {
    final ConstantPoolGen cp = Mockito.mock (ConstantPoolGen.class);
    Mockito.when (cp.addMethodref (COMHostSession.class.getName (), "dispatch0I", "(II)I"))
        .thenReturn (1);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch1I", "(IILjava/lang/Object;)I"))
        .thenReturn (2);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch2I",
            "(IILjava/lang/Object;Ljava/lang/Object;)I")).thenReturn (3);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatchI", "(II[Ljava/lang/Object;)I"))
        .thenReturn (4);
    test (cp, ArgumentType.WORD);
  }

  /**
   * Tests the integral double-word returns.
   */
  public void testDWord () {
    final ConstantPoolGen cp = Mockito.mock (ConstantPoolGen.class);
    Mockito.when (cp.addMethodref (COMHostSession.class.getName (), "dispatch0L", "(II)J"))
        .thenReturn (1);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch1L", "(IILjava/lang/Object;)J"))
        .thenReturn (2);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch2L",
            "(IILjava/lang/Object;Ljava/lang/Object;)J")).thenReturn (3);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatchL", "(II[Ljava/lang/Object;)J"))
        .thenReturn (4);
    test (cp, ArgumentType.DWORD);
  }

  /**
   * Tests the floating-point word returns.
   */
  public void testFWord () {
    final ConstantPoolGen cp = Mockito.mock (ConstantPoolGen.class);
    Mockito.when (cp.addMethodref (COMHostSession.class.getName (), "dispatch0F", "(II)F"))
        .thenReturn (1);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch1F", "(IILjava/lang/Object;)F"))
        .thenReturn (2);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch2F",
            "(IILjava/lang/Object;Ljava/lang/Object;)F")).thenReturn (3);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatchF", "(II[Ljava/lang/Object;)F"))
        .thenReturn (4);
    test (cp, ArgumentType.FWORD);
  }

  /**
   * Tests the floating-point double-word returns.
   */
  public void testFDWord () {
    final ConstantPoolGen cp = Mockito.mock (ConstantPoolGen.class);
    Mockito.when (cp.addMethodref (COMHostSession.class.getName (), "dispatch0D", "(II)D"))
        .thenReturn (1);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch1D", "(IILjava/lang/Object;)D"))
        .thenReturn (2);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatch2D",
            "(IILjava/lang/Object;Ljava/lang/Object;)D")).thenReturn (3);
    Mockito.when (
        cp.addMethodref (COMHostSession.class.getName (), "dispatchD", "(II[Ljava/lang/Object;)D"))
        .thenReturn (4);
    test (cp, ArgumentType.FDWORD);
  }

}