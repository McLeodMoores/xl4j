/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.msvc;

import uk.co.beerdragon.comjvm.jtool.JavaType;

/**
 * Returns the prefix used under the naming convention used by Windows APIs.
 */
/* package */class HungarianNamingPrefix implements JavaType.Visitor<String> {

  @Override
  public String visitBoolean () {
    return "f";
  }

  @Override
  public String visitChar () {
    return "ch";
  }

  @Override
  public String visitByte () {
    return "b";
  }

  @Override
  public String visitShort () {
    return "w";
  }

  @Override
  public String visitInt () {
    return "l";
  }

  @Override
  public String visitLong () {
    return "ll";
  }

  @Override
  public String visitFloat () {
    return "f";
  }

  @Override
  public String visitDouble () {
    return "d";
  }

  @Override
  public String visitObject (final String className) {
    return "p";
  }

  @Override
  public String visitArray (final JavaType element) {
    return "a";
  }

}
