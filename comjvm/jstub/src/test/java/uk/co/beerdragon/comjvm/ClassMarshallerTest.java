/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm;

import static org.testng.Assert.assertSame;

import java.io.FilterInputStream;
import java.util.Iterator;

import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * Tests the {@link ClassMarshaller} class.
 */
@Test
public class ClassMarshallerTest {

  @Test (expectedExceptions = NullPointerException.class)
  public void testNullConstruction () {
    ClassMarshaller.forClass (null);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testInvalidClass_final () {
    ClassMarshaller.forClass (String.class);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testInvalidClass_noconstructor () {
    ClassMarshaller.forClass (FilterInputStream.class);
  }

  public void testInitialise () {
    @SuppressWarnings ("rawtypes")
    final ClassMarshaller<Iterable> marshaller = ClassMarshaller.forClass (Iterable.class);
    final MethodDispatcher dispatcher = Mockito.mock (MethodDispatcher.class);
    marshaller.initialise (dispatcher);
    Mockito.verify (dispatcher).notifyDispatchId (0, "clone()Ljava/lang/Object;");
    Mockito.verify (dispatcher).notifyDispatchId (1, "equals(Ljava/lang/Object;)Z");
    Mockito.verify (dispatcher).notifyDispatchId (2, "hashCode()I");
    Mockito.verify (dispatcher).notifyDispatchId (3, "iterator()Ljava/util/Iterator;");
  }

  public void testCreateInstance () {
    @SuppressWarnings ("rawtypes")
    final ClassMarshaller<Iterable> marshaller = ClassMarshaller.forClass (Iterable.class);
    final MethodDispatcher dispatcher = Mockito.mock (MethodDispatcher.class);
    final COMObject underlying = new COMObject (0, Mockito.mock (COMJvmSession.class));
    final Iterator<?> expectedResult = Mockito.mock (Iterator.class);
    Mockito.when (dispatcher.invokeObject (Mockito.mock (Iterable.class), 1, underlying))
        .thenReturn (expectedResult);
    final Iterable<?> instance = marshaller.createInstance (dispatcher, underlying);
    final Iterator<?> iterator = instance.iterator ();
    assertSame (iterator, expectedResult);
  }
}
