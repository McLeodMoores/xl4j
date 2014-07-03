package com.mcleodmoores.excel4j.values;

/**
 * This is the equivalent of the xloper structure on the C side.  Because we don't have
 * unions, we break it into a hierarchy of subclasses implementing this base interface.
 */
public interface XLValue {
  <E> E accept(XLValueVisitor<E> visitor);
}
