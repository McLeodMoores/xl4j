/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.msvc;

import uk.co.beerdragon.comjvm.jtool.JavaType;
import uk.co.beerdragon.comjvm.jtool.filename.FilePerClassStrategy;

/**
 * Returns the IDL type equivalent to the Java type.
 */
/* package */class IdlParameterType implements JavaType.Visitor<String> {

  @Override
  public String visitBoolean () {
    return "BOOL";
  }

  @Override
  public String visitChar () {
    return "char";
  }

  @Override
  public String visitByte () {
    return "byte";
  }

  @Override
  public String visitShort () {
    return "short";
  }

  @Override
  public String visitInt () {
    return "long";
  }

  @Override
  public String visitLong () {
    return "hyper";
  }

  @Override
  public String visitFloat () {
    return "float";
  }

  @Override
  public String visitDouble () {
    return "double";
  }

  // TODO: Should we recognise java.lang.String as BSTR ?
  // TODO: Should we recognise any of the boxed types ?

  @Override
  public String visitObject (final String className) {
    return "IJ" + FilePerClassStrategy.createName (className) + "*";
  }

  @Override
  public String visitArray (final JavaType element) {
    return "SAFEARRAY(" + element.accept (new SafeArrayElementType ()) + ")";
  }

}
