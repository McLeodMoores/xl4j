package com.mcleodmoores.xl4j.values;

/**
 * This is the equivalent of the xloper structure on the C side.  Because we don't have
 * unions, we break it into a hierarchy of subclasses implementing this base interface.
 */
public interface XLValue {
  /**
   * Double-dispatch/visitor pattern generic accept method.  
   * Implementers should call the appropriate visitXXX method in the XLValueVisitor interface. 
   * @param visitor the visitor
   * @param <E> the type of the result
   * @return the result
   */
  <E> E accept(XLValueVisitor<E> visitor);
}
