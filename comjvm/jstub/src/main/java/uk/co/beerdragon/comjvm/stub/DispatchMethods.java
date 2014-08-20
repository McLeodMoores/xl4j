/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.stub;

import org.apache.bcel.generic.ConstantPoolGen;

import uk.co.beerdragon.comjvm.COMHostSession;
import uk.co.beerdragon.comjvm.util.ArgumentType;

/**
 * Helper class for forming calls to the dispatch methods available on {@link COMHostSession}.
 */
/* package */final class DispatchMethods {

  private final ConstantPoolGen _cp;

  private final String _name;

  private final String _signature;

  private int _v;

  private int _a;

  private int _i;

  private int _l;

  private int _f;

  private int _d;

  private DispatchMethods (final ConstantPoolGen cp, final String name, final String signature) {
    _cp = cp;
    _name = name;
    _signature = signature;
  }

  /**
   * Obtains an instance for dispatching to zero-arg methods.
   * 
   * @param cp
   *          the constant pool
   * @return the dispatcher
   */
  public static DispatchMethods dispatch0 (final ConstantPoolGen cp) {
    return new DispatchMethods (cp, "dispatch0", "(II)");
  }

  /**
   * Obtains an instance for dispatching to methods with one argument type.
   * 
   * @param cp
   *          the constant pool
   * @return the dispatcher
   */
  public static DispatchMethods dispatch1 (final ConstantPoolGen cp) {
    return new DispatchMethods (cp, "dispatch1", "(IILjava/lang/Object;)");
  }

  /**
   * Obtains an instance for dispatching to methods with two argument types.
   * 
   * @param cp
   *          the constant pool
   * @return the dispatcher
   */
  public static DispatchMethods dispatch2 (final ConstantPoolGen cp) {
    return new DispatchMethods (cp, "dispatch2", "(IILjava/lang/Object;Ljava/lang/Object;)");
  }

  /**
   * Obtains an instance for dispatching to methods with three or more argument types.
   * 
   * @param cp
   *          the constant pool
   * @return the dispatcher
   */
  public static DispatchMethods dispatchN (final ConstantPoolGen cp) {
    return new DispatchMethods (cp, "dispatch", "(II[Ljava/lang/Object;)");
  }

  /**
   * Obtains the method index for the form with a given return type.
   * 
   * @param returnType
   *          the return type
   * @return the method index
   */
  public int getMethod (final int returnType) {
    switch (returnType) {
    case ArgumentType.VOID:
      return voidReturn ();
    case ArgumentType.REF:
      return refReturn ();
    case ArgumentType.WORD:
      return wordReturn ();
    case ArgumentType.DWORD:
      return dwordReturn ();
    case ArgumentType.FWORD:
      return fwordReturn ();
    case ArgumentType.FDWORD:
      return fdwordReturn ();
    default:
      throw new UnsupportedOperationException ();
    }
  }

  private int voidReturn () {
    if (_v == 0) {
      _v = _cp.addMethodref (COMHostSession.class.getName (), _name + "V", _signature + "V");
    }
    return _v;
  }

  private int refReturn () {
    if (_a == 0) {
      _a = _cp.addMethodref (COMHostSession.class.getName (), _name + "A", _signature
          + "Ljava/lang/Object;");
    }
    return _a;
  }

  private int wordReturn () {
    if (_i == 0) {
      _i = _cp.addMethodref (COMHostSession.class.getName (), _name + "I", _signature + "I");
    }
    return _i;
  }

  private int dwordReturn () {
    if (_l == 0) {
      _l = _cp.addMethodref (COMHostSession.class.getName (), _name + "L", _signature + "J");
    }
    return _l;
  }

  private int fwordReturn () {
    if (_f == 0) {
      _f = _cp.addMethodref (COMHostSession.class.getName (), _name + "F", _signature + "F");
    }
    return _f;
  }

  private int fdwordReturn () {
    if (_d == 0) {
      _d = _cp.addMethodref (COMHostSession.class.getName (), _name + "D", _signature + "D");
    }
    return _d;
  }

}