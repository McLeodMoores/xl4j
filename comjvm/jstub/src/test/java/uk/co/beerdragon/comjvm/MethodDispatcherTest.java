/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * Tests the {@link MethodDispatcher} class
 */
@Test
public class MethodDispatcherTest {

  private static class MockMethodDispatcher extends MethodDispatcher {

    private char _type;

    private Object _self;

    private int _dispatchId;

    private COMObject _object;

    private Variant[] _params;

    private void record (final char type, final Object self, final int dispatchId,
        final COMObject object, final Variant[] params) {
      _type = type;
      _self = self;
      _dispatchId = dispatchId;
      _object = object;
      _params = params;
    }

    private void verify (final char type, final Object self, final int dispatchId,
        final COMObject object, final int count) {
      assertEquals (_type, type);
      assertEquals (_self, self);
      assertEquals (_dispatchId, dispatchId);
      assertSame (_object, object);
      assertEquals (_params.length, count);
    }

    @Override
    public void notifyDispatchId (final int dispatchId, final String methodSignature) {
      throw new UnsupportedOperationException ();
    }

    @Override
    public boolean isDispatch (final int dispatchId) {
      throw new UnsupportedOperationException ();
    }

    @Override
    public void invokeVoid (final Object self, final int dispatchId, final COMObject object,
        final Variant[] params) {
      record ('V', self, dispatchId, object, params);
    }

    @Override
    public boolean invokeBoolean (final Object self, final int dispatchId, final COMObject object,
        final Variant[] params) {
      record ('Z', self, dispatchId, object, params);
      return false;
    }

    @Override
    public char invokeChar (final Object self, final int dispatchId, final COMObject object,
        final Variant[] params) {
      record ('C', self, dispatchId, object, params);
      return 0;
    }

    @Override
    public byte invokeByte (final Object self, final int dispatchId, final COMObject object,
        final Variant[] params) {
      record ('B', self, dispatchId, object, params);
      return 0;
    }

    @Override
    public short invokeShort (final Object self, final int dispatchId, final COMObject object,
        final Variant[] params) {
      record ('S', self, dispatchId, object, params);
      return 0;
    }

    @Override
    public int invokeInt (final Object self, final int dispatchId, final COMObject object,
        final Variant[] params) {
      record ('I', self, dispatchId, object, params);
      return 0;
    }

    @Override
    public long invokeLong (final Object self, final int dispatchId, final COMObject object,
        final Variant[] params) {
      record ('J', self, dispatchId, object, params);
      return 0;
    }

    @Override
    public float invokeFloat (final Object self, final int dispatchId, final COMObject object,
        final Variant[] params) {
      record ('F', self, dispatchId, object, params);
      return 0;
    }

    @Override
    public double invokeDouble (final Object self, final int dispatchId, final COMObject object,
        final Variant[] params) {
      record ('D', self, dispatchId, object, params);
      return 0;
    }

    @Override
    public Object invokeObject (final Object self, final int dispatchId, final COMObject object,
        final Variant[] params) {
      record ('L', self, dispatchId, object, params);
      return null;
    }

  }

  public void testInvokeVoid () {
    final COMObject obj = new COMObject (1, Mockito.mock (COMJvmSession.class));
    final MockMethodDispatcher dispatcher = new MockMethodDispatcher ();
    dispatcher.invokeVoid (this, 1, obj);
    dispatcher.verify ('V', this, 1, obj, 0);
    dispatcher.invokeVoid (this, 2, obj, Variant.NULL);
    dispatcher.verify ('V', this, 2, obj, 1);
    dispatcher.invokeVoid (this, 3, obj, Variant.NULL, Variant.NULL);
    dispatcher.verify ('V', this, 3, obj, 2);
    dispatcher.invokeVoid (this, 4, obj, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('V', this, 4, obj, 3);
    dispatcher.invokeVoid (this, 5, obj, Variant.NULL, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('V', this, 5, obj, 4);
  }

  public void testInvokeBoolean () {
    final COMObject obj = new COMObject (1, Mockito.mock (COMJvmSession.class));
    final MockMethodDispatcher dispatcher = new MockMethodDispatcher ();
    dispatcher.invokeBoolean (this, 1, obj);
    dispatcher.verify ('Z', this, 1, obj, 0);
    dispatcher.invokeBoolean (this, 2, obj, Variant.NULL);
    dispatcher.verify ('Z', this, 2, obj, 1);
    dispatcher.invokeBoolean (this, 3, obj, Variant.NULL, Variant.NULL);
    dispatcher.verify ('Z', this, 3, obj, 2);
    dispatcher.invokeBoolean (this, 4, obj, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('Z', this, 4, obj, 3);
    dispatcher.invokeBoolean (this, 5, obj, Variant.NULL, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('Z', this, 5, obj, 4);
  }

  public void testInvokeChar () {
    final COMObject obj = new COMObject (1, Mockito.mock (COMJvmSession.class));
    final MockMethodDispatcher dispatcher = new MockMethodDispatcher ();
    dispatcher.invokeChar (this, 1, obj);
    dispatcher.verify ('C', this, 1, obj, 0);
    dispatcher.invokeChar (this, 2, obj, Variant.NULL);
    dispatcher.verify ('C', this, 2, obj, 1);
    dispatcher.invokeChar (this, 3, obj, Variant.NULL, Variant.NULL);
    dispatcher.verify ('C', this, 3, obj, 2);
    dispatcher.invokeChar (this, 4, obj, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('C', this, 4, obj, 3);
    dispatcher.invokeChar (this, 5, obj, Variant.NULL, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('C', this, 5, obj, 4);
  }

  public void testInvokeByte () {
    final COMObject obj = new COMObject (1, Mockito.mock (COMJvmSession.class));
    final MockMethodDispatcher dispatcher = new MockMethodDispatcher ();
    dispatcher.invokeByte (this, 1, obj);
    dispatcher.verify ('B', this, 1, obj, 0);
    dispatcher.invokeByte (this, 2, obj, Variant.NULL);
    dispatcher.verify ('B', this, 2, obj, 1);
    dispatcher.invokeByte (this, 3, obj, Variant.NULL, Variant.NULL);
    dispatcher.verify ('B', this, 3, obj, 2);
    dispatcher.invokeByte (this, 4, obj, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('B', this, 4, obj, 3);
    dispatcher.invokeByte (this, 5, obj, Variant.NULL, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('B', this, 5, obj, 4);
  }

  public void testInvokeShort () {
    final COMObject obj = new COMObject (1, Mockito.mock (COMJvmSession.class));
    final MockMethodDispatcher dispatcher = new MockMethodDispatcher ();
    dispatcher.invokeShort (this, 1, obj);
    dispatcher.verify ('S', this, 1, obj, 0);
    dispatcher.invokeShort (this, 2, obj, Variant.NULL);
    dispatcher.verify ('S', this, 2, obj, 1);
    dispatcher.invokeShort (this, 3, obj, Variant.NULL, Variant.NULL);
    dispatcher.verify ('S', this, 3, obj, 2);
    dispatcher.invokeShort (this, 4, obj, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('S', this, 4, obj, 3);
    dispatcher.invokeShort (this, 5, obj, Variant.NULL, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('S', this, 5, obj, 4);
  }

  public void testInvokeInt () {
    final COMObject obj = new COMObject (1, Mockito.mock (COMJvmSession.class));
    final MockMethodDispatcher dispatcher = new MockMethodDispatcher ();
    dispatcher.invokeInt (this, 1, obj);
    dispatcher.verify ('I', this, 1, obj, 0);
    dispatcher.invokeInt (this, 2, obj, Variant.NULL);
    dispatcher.verify ('I', this, 2, obj, 1);
    dispatcher.invokeInt (this, 3, obj, Variant.NULL, Variant.NULL);
    dispatcher.verify ('I', this, 3, obj, 2);
    dispatcher.invokeInt (this, 4, obj, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('I', this, 4, obj, 3);
    dispatcher.invokeInt (this, 5, obj, Variant.NULL, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('I', this, 5, obj, 4);
  }

  public void testInvokeLong () {
    final COMObject obj = new COMObject (1, Mockito.mock (COMJvmSession.class));
    final MockMethodDispatcher dispatcher = new MockMethodDispatcher ();
    dispatcher.invokeLong (this, 1, obj);
    dispatcher.verify ('J', this, 1, obj, 0);
    dispatcher.invokeLong (this, 2, obj, Variant.NULL);
    dispatcher.verify ('J', this, 2, obj, 1);
    dispatcher.invokeLong (this, 3, obj, Variant.NULL, Variant.NULL);
    dispatcher.verify ('J', this, 3, obj, 2);
    dispatcher.invokeLong (this, 4, obj, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('J', this, 4, obj, 3);
    dispatcher.invokeLong (this, 5, obj, Variant.NULL, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('J', this, 5, obj, 4);
  }

  public void testInvokeFloat () {
    final COMObject obj = new COMObject (1, Mockito.mock (COMJvmSession.class));
    final MockMethodDispatcher dispatcher = new MockMethodDispatcher ();
    dispatcher.invokeFloat (this, 1, obj);
    dispatcher.verify ('F', this, 1, obj, 0);
    dispatcher.invokeFloat (this, 2, obj, Variant.NULL);
    dispatcher.verify ('F', this, 2, obj, 1);
    dispatcher.invokeFloat (this, 3, obj, Variant.NULL, Variant.NULL);
    dispatcher.verify ('F', this, 3, obj, 2);
    dispatcher.invokeFloat (this, 4, obj, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('F', this, 4, obj, 3);
    dispatcher.invokeFloat (this, 5, obj, Variant.NULL, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('F', this, 5, obj, 4);
  }

  public void testInvokeDouble () {
    final COMObject obj = new COMObject (1, Mockito.mock (COMJvmSession.class));
    final MockMethodDispatcher dispatcher = new MockMethodDispatcher ();
    dispatcher.invokeDouble (this, 1, obj);
    dispatcher.verify ('D', this, 1, obj, 0);
    dispatcher.invokeDouble (this, 2, obj, Variant.NULL);
    dispatcher.verify ('D', this, 2, obj, 1);
    dispatcher.invokeDouble (this, 3, obj, Variant.NULL, Variant.NULL);
    dispatcher.verify ('D', this, 3, obj, 2);
    dispatcher.invokeDouble (this, 4, obj, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('D', this, 4, obj, 3);
    dispatcher.invokeDouble (this, 5, obj, Variant.NULL, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('D', this, 5, obj, 4);
  }

  public void testInvokeObject () {
    final COMObject obj = new COMObject (1, Mockito.mock (COMJvmSession.class));
    final MockMethodDispatcher dispatcher = new MockMethodDispatcher ();
    dispatcher.invokeObject (this, 1, obj);
    dispatcher.verify ('L', this, 1, obj, 0);
    dispatcher.invokeObject (this, 2, obj, Variant.NULL);
    dispatcher.verify ('L', this, 2, obj, 1);
    dispatcher.invokeObject (this, 3, obj, Variant.NULL, Variant.NULL);
    dispatcher.verify ('L', this, 3, obj, 2);
    dispatcher.invokeObject (this, 4, obj, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('L', this, 4, obj, 3);
    dispatcher.invokeObject (this, 5, obj, Variant.NULL, Variant.NULL, Variant.NULL, Variant.NULL);
    dispatcher.verify ('L', this, 5, obj, 4);
  }

}
