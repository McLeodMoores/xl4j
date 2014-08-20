/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.stub;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.DLOAD;
import org.apache.bcel.generic.FLOAD;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.IFLT;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LLOAD;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.Type;

import uk.co.beerdragon.comjvm.COMHostSession;
import uk.co.beerdragon.comjvm.ex.NotImplementedException;
import uk.co.beerdragon.comjvm.util.ArgumentBuffer;
import uk.co.beerdragon.comjvm.util.ArgumentType;
import uk.co.beerdragon.comjvm.util.ClassMethods;

/**
 * Creates class and interface stubs. This is not normally used directly but via
 * {@link COMStubClass}.
 * 
 * @param <T>
 *          the stubbed class or interface
 */
public abstract class StubBuilder<T> {

  private static final boolean DEBUG_STDOUT = false;

  private static final String DEBUG_FILESYSTEM = "/tmp";

  private static final AtomicInteger s_unique = new AtomicInteger ();

  private static final AtomicReference<ByteCodeClassLoader> s_loader = new AtomicReference<ByteCodeClassLoader> (
      new ByteCodeClassLoader (StubBuilder.class.getClassLoader ()));

  private static final Type SESSION_TYPE = new ObjectType (COMHostSession.class.getName ());

  private static final Type DISPIDS_TYPE = new ArrayType (Type.INT, 1);

  private static final Type OBJECTID_TYPE = Type.INT;

  private static final String SESSION = "session";

  private static final String DISPIDS = "dispIds";

  private static final String OBJECTID = "objectId";

  private static final String SESSION_SIGNATURE = SESSION_TYPE.getSignature ();

  private static final String DISPIDS_SIGNATURE = DISPIDS_TYPE.getSignature ();

  private static final String OBJECTID_SIGNATURE = OBJECTID_TYPE.getSignature ();

  private static final String SESSION_FIELD = "_" + SESSION;

  private static final String DISPIDS_FIELD = "_" + DISPIDS;

  private static final String OBJECTID_FIELD = "_" + OBJECTID;

  private static final Type[] CONSTRUCTOR_ARG_TYPES = new Type[] { SESSION_TYPE, DISPIDS_TYPE,
      OBJECTID_TYPE };

  private static final String[] CONSTRUCTOR_ARG_NAMES = new String[] { SESSION, DISPIDS, OBJECTID };

  private static final String ARGUMENT_BUFFER_SIGNATURE = new ObjectType (
      ArgumentBuffer.class.getName ()).getSignature ();

  private static final String ARGUMENT_BUFFER_ALLOC_SIGNATURE = "(I)" + ARGUMENT_BUFFER_SIGNATURE;

  private static final ObjectType NOT_IMPLEMENTED_EXCEPTION_TYPE = new ObjectType (
      NotImplementedException.class.getName ());

  private final Class<T> _clazz;

  /* package */StubBuilder (final Class<T> clazz) {
    _clazz = clazz;
  }

  /**
   * Obtains an instance.
   * 
   * @param <T>
   *          the class or interface type to stub
   * @param clazz
   *          the class or interface to stub
   * @return the builder instance
   */
  public static <T> StubBuilder<T> of (final Class<T> clazz) {
    if (clazz.isInterface ()) {
      return new InterfaceStubBuilder<T> (clazz);
    } else {
      return new ClassStubBuilder<T> (clazz);
    }
  }

  /**
   * Allows any previously generated stub classes to be made available for garbage collection if
   * possible.
   * <p>
   * This method is provided to support test cases and some very specific memory optimisations.
   * Details of the current implementation are given below, but this may change without notice at
   * any time. If, for whatever reason, your application needs to call this method or is otherwise
   * working with these behavioural assumptions then please contact the developers. If there is a
   * fundamental issue in this package that is being worked-around by making these calls then that
   * issue should be addressed.
   * <p>
   * The current implementation drops the reference to the class loader used to create the stub
   * classes, and creates a new class loader for any subsequence calls. Any previously generated
   * stub classes will become candidates for garbage collection when there are no longer any strong
   * references to any of them. Such strong references are always held by instances of those stub
   * classes, but may also be held by other application logic or any service layers (for example
   * within COMJVM) that provide caching or similar lookups.
   */
  public static void gc () {
    final ClassLoader cl = s_loader.get ();
    s_loader.set (new ByteCodeClassLoader (cl.getParent ()));
  }

  /* package */final Class<T> getClazz () {
    return _clazz;
  }

  /* package */abstract String getSuperClassName ();

  /* package */abstract String[] getInterfaceNames ();

  private ClassGen createClassGen () {
    final String name = getClass ().getName () + "$_" + s_unique.incrementAndGet ();
    return new ClassGen (name, getSuperClassName (), name + ".java", Constants.ACC_PUBLIC,
        getInterfaceNames ());
  }

  private void addFields (final ClassGen classGen) {
    final ConstantPoolGen cp = classGen.getConstantPool ();
    classGen.addField (new FieldGen (Constants.ACC_PRIVATE | Constants.ACC_FINAL, SESSION_TYPE,
        SESSION_FIELD, cp).getField ());
    classGen.addField (new FieldGen (Constants.ACC_PRIVATE | Constants.ACC_FINAL, DISPIDS_TYPE,
        DISPIDS_FIELD, cp).getField ());
    classGen.addField (new FieldGen (Constants.ACC_PRIVATE | Constants.ACC_FINAL, OBJECTID_TYPE,
        OBJECTID_FIELD, cp).getField ());
  }

  private void addConstructor (final ClassGen classGen) {
    final InstructionList code = new InstructionList ();
    final ConstantPoolGen cp = classGen.getConstantPool ();
    // this.super();
    code.append (InstructionConstants.THIS);
    code.append (new INVOKESPECIAL (cp.addMethodref (classGen.getSuperclassName (),
        Constants.CONSTRUCTOR_NAME, "()V")));
    // this._dispatcher = dispatcher;
    code.append (InstructionConstants.THIS);
    code.append (InstructionConstants.ALOAD_1);
    code.append (new PUTFIELD (cp.addFieldref (classGen.getClassName (), SESSION_FIELD,
        SESSION_SIGNATURE)));
    // this._dispIds = dispIds;
    code.append (InstructionConstants.THIS);
    code.append (InstructionConstants.ALOAD_2);
    code.append (new PUTFIELD (cp.addFieldref (classGen.getClassName (), DISPIDS_FIELD,
        DISPIDS_SIGNATURE)));
    // this._objectId = objectId;
    code.append (InstructionConstants.THIS);
    code.append (new ILOAD (3));
    code.append (new PUTFIELD (cp.addFieldref (classGen.getClassName (), OBJECTID_FIELD,
        OBJECTID_SIGNATURE)));
    // return;
    code.append (InstructionConstants.RETURN);
    final MethodGen constructor = new MethodGen (Constants.ACC_PUBLIC, Type.VOID,
        CONSTRUCTOR_ARG_TYPES, CONSTRUCTOR_ARG_NAMES, Constants.CONSTRUCTOR_NAME,
        classGen.getClassName (), code, cp);
    // go
    constructor.setMaxLocals ();
    constructor.setMaxStack ();
    classGen.addMethod (constructor.getMethod ());
  }

  private static Instruction loadUnsigned (final int i, final ConstantPoolGen cp) {
    if (i <= 5) {
      return new ICONST (i);
    } else if (i < 128) {
      return new BIPUSH ((byte)i);
    } else {
      return new LDC (cp.addInteger (i));
    }
  }

  /**
   * Generates all stub methods. A stub method, with no arguments, is of the form:
   * 
   * <pre class="code java">
   * <span class="k">int</span> <span class="i">foo</span> () {
   *   <span class="k">int</span> <span class="i">dispId</span> = <span class="i">_dispId</span>[<span class="i">$index</span>];
   *   <span class="k">if</span> (<span class="i">dispId</span> >= <span class="l">0</span>) {
   *     <span class="k">try</span> {
   *       <span class="k">return</span> <span class="i">_session</span>.<span class="i">dispatch0I</span> (<span class="i">_objectId</span>, <span class="i">dispId</span>);
   *     } <span class="k">catch</span> (<span class="i">NotImplementedException e</span>) {
   *       <span class="i">_dispId</span>[<span class="i">$index</span>] = <span class="l">-1</span>;
   *     }
   *   }
   *   <span class="k">return</span> <span class="k">super</span>.<span class="i">foo</span> ();
   * </pre>
   * 
   * @param classGen
   *          the class generator
   * @param methods
   *          the identified methods to stub
   */
  private void addMethods (final ClassGen classGen, final Iterable<ClassMethods.MethodInfo> methods) {
    final ConstantPoolGen cp = classGen.getConstantPool ();
    final int refDISPIDS = cp.addFieldref (classGen.getClassName (), DISPIDS_FIELD,
        DISPIDS_SIGNATURE);
    final int refSESSION = cp.addFieldref (classGen.getClassName (), SESSION_FIELD,
        SESSION_SIGNATURE);
    final int refOBJECTID = cp.addFieldref (classGen.getClassName (), OBJECTID_FIELD,
        OBJECTID_SIGNATURE);
    int refARG_A = 0;
    int refARG_I = 0;
    int refARG_L = 0;
    int refARG_F = 0;
    int refARG_D = 0;
    final int methodARG_ALLOC = cp.addMethodref (ArgumentBuffer.class.getName (), "alloc",
        ARGUMENT_BUFFER_ALLOC_SIGNATURE);
    final int methodINIT_NOT_IMPLEMENTED_EXCEPTION = cp.addMethodref (
        NotImplementedException.class.getName (), Constants.CONSTRUCTOR_NAME, "()V");
    final int typeOBJECT = cp.addClass (String.class.getName ());
    final int typeNOT_IMPLEMENTED_EXCEPTION = cp
        .addClass (NotImplementedException.class.getName ());
    DispatchMethods dispatch0 = null;
    DispatchMethods dispatch1 = null;
    DispatchMethods dispatch2 = null;
    DispatchMethods dispatchN = null;
    for (final ClassMethods.MethodInfo info : methods) {
      final Method method = info.getMethod ();
      final int[] argTypes = ArgumentType.mask (method.getParameterTypes ());
      final InstructionList code = new InstructionList ();
      // Local variables
      int localCount = 1;
      final int[] localARG = new int[argTypes.length];
      for (int i = 0; i < argTypes.length; i++) {
        localARG[i] = localCount;
        localCount += ArgumentType.width (argTypes[i]);
      }
      final int localDISPID = localCount++;
      final int localARGS = localCount++;
      final int localTS = argTypes.length > 2 ? localCount++ : 0;
      // int dispId = _dispIds[$index];
      code.append (InstructionConstants.THIS);
      code.append (new GETFIELD (refDISPIDS));
      final int methodIndex = info.getIndex ();
      code.append (loadUnsigned (methodIndex, cp));
      code.append (InstructionConstants.IALOAD);
      code.append (InstructionConstants.DUP);
      code.append (new ISTORE (localDISPID));
      // if (dispId < 0) goto callSuper;
      final BranchInstruction gotoCALLSUPER = new IFLT (null);
      code.append (gotoCALLSUPER);
      // _session.dispatch??? (objectId, dispId, ...
      final InstructionHandle ihBEGIN_TRY = code.append (InstructionConstants.THIS);
      code.append (new GETFIELD (refSESSION));
      code.append (InstructionConstants.THIS);
      code.append (new GETFIELD (refOBJECTID));
      code.append (new ILOAD (localDISPID));
      final int returnType = ArgumentType.mask (method.getReturnType ());
      if (argTypes.length > 0) {
        final int mask = ArgumentType.mask (argTypes);
        // ArgumentBuffer arg = ArgumentBuffer.alloc (argTypes.length);
        code.append (new ICONST (argTypes.length));
        if (argTypes.length > 2) {
          code.append (InstructionConstants.DUP);
        }
        code.append (new INVOKESTATIC (methodARG_ALLOC));
        code.append (new ASTORE (localARGS));
        if (argTypes.length > 2) {
          // ts[] = new Object[argTypes.length];
          code.append (new ANEWARRAY (typeOBJECT));
          code.append (new ASTORE (localTS));
        }
        int ts = 0;
        if ((mask & ArgumentType.REF) != 0) {
          if (argTypes.length > 2) {
            // ts[$ts] = ...
            code.append (new ALOAD (localTS));
            code.append (new ICONST (ts++));
          }
          // arg.a[$n] = parameter
          code.append (new ALOAD (localARGS));
          if (refARG_A == 0) {
            refARG_A = cp.addFieldref (ArgumentBuffer.class.getName (), ArgumentBuffer.NAME_REF,
                ArgumentBuffer.SIGNATURE_REF);
          }
          code.append (new GETFIELD (refARG_A));
          for (int i = 0; i < argTypes.length; i++) {
            if ((argTypes[i] & ArgumentType.REF) != 0) {
              code.append (InstructionConstants.DUP);
              code.append (loadUnsigned (i, cp));
              code.append (new ALOAD (localARG[i]));
              code.append (InstructionConstants.AASTORE);
            }
          }
          // Finish assignment to `ts` OR leave `a` on the stack
          if (argTypes.length > 2) {
            code.append (InstructionConstants.AASTORE);
          }
        }
        if ((mask & ArgumentType.WORD) != 0) {
          if (argTypes.length > 2) {
            // ts[$ts] = ...
            code.append (new ALOAD (localTS));
            code.append (new ICONST (ts++));
          }
          // arg.i[$n] = parameter
          code.append (new ALOAD (localARGS));
          if (refARG_I == 0) {
            refARG_I = cp.addFieldref (ArgumentBuffer.class.getName (), ArgumentBuffer.NAME_WORD,
                ArgumentBuffer.SIGNATURE_WORD);
          }
          code.append (new GETFIELD (refARG_I));
          for (int i = 0; i < argTypes.length; i++) {
            if ((argTypes[i] & ArgumentType.WORD) != 0) {
              code.append (InstructionConstants.DUP);
              code.append (loadUnsigned (i, cp));
              code.append (new ILOAD (localARG[i]));
              code.append (InstructionConstants.IASTORE);
            }
          }
          // Finish assignment to `ts` OR leave `i` on the stack
          if (argTypes.length > 2) {
            code.append (InstructionConstants.AASTORE);
          }
        }
        if ((mask & ArgumentType.DWORD) != 0) {
          if (argTypes.length > 2) {
            // ts[$ts] = ...
            code.append (new ALOAD (localTS));
            code.append (new ICONST (ts++));
          }
          // arg.l[$n] = parameter
          code.append (new ALOAD (localARGS));
          if (refARG_L == 0) {
            refARG_L = cp.addFieldref (ArgumentBuffer.class.getName (), ArgumentBuffer.NAME_DWORD,
                ArgumentBuffer.SIGNATURE_DWORD);
          }
          code.append (new GETFIELD (refARG_L));
          for (int i = 0; i < argTypes.length; i++) {
            if ((argTypes[i] & ArgumentType.DWORD) != 0) {
              code.append (InstructionConstants.DUP);
              code.append (loadUnsigned (i, cp));
              code.append (new LLOAD (localARG[i]));
              code.append (InstructionConstants.LASTORE);
            }
          }
          // Finish assignment to `ts` OR leave `l` on the stack
          if (argTypes.length > 2) {
            code.append (InstructionConstants.AASTORE);
          }
        }
        if ((mask & ArgumentType.FWORD) != 0) {
          if (argTypes.length > 2) {
            // ts[$ts] = ...
            code.append (new ALOAD (localTS));
            code.append (new ICONST (ts++));
          }
          // arg.f[$n] = parameter
          code.append (new ALOAD (localARGS));
          if (refARG_F == 0) {
            refARG_F = cp.addFieldref (ArgumentBuffer.class.getName (), ArgumentBuffer.NAME_FWORD,
                ArgumentBuffer.SIGNATURE_FWORD);
          }
          code.append (new GETFIELD (refARG_F));
          for (int i = 0; i < argTypes.length; i++) {
            if ((argTypes[i] & ArgumentType.FWORD) != 0) {
              code.append (InstructionConstants.DUP);
              code.append (loadUnsigned (i, cp));
              code.append (new FLOAD (localARG[i]));
              code.append (InstructionConstants.FASTORE);
            }
          }
          // Finish assignment to `ts` OR leave `f` on the stack
          if (argTypes.length > 2) {
            code.append (InstructionConstants.AASTORE);
          }
        }
        if ((mask & ArgumentType.FDWORD) != 0) {
          // arg.d[$n] = parameter
          code.append (new ALOAD (localARGS));
          if (refARG_D == 0) {
            refARG_D = cp.addFieldref (ArgumentBuffer.class.getName (), ArgumentBuffer.NAME_FDWORD,
                ArgumentBuffer.SIGNATURE_FDWORD);
          }
          code.append (new GETFIELD (refARG_D));
          for (int i = 0; i < argTypes.length; i++) {
            if ((argTypes[i] & ArgumentType.FDWORD) != 0) {
              code.append (InstructionConstants.DUP);
              code.append (loadUnsigned (i, cp));
              code.append (new DLOAD (localARG[i]));
              code.append (InstructionConstants.DASTORE);
            }
          }
          // Finish assignment to `ts` OR leave `d` on the stack
          if (argTypes.length > 2) {
            code.append (InstructionConstants.AASTORE);
          }
        }
        // Either arguments are on the stack in order `a`, `i`, `l`, `f`, `d` or
        // there is a single `ts` argument
        switch (argTypes.length) {
        case 1:
          // $ = dispatch1(...);
          if (dispatch1 == null) {
            dispatch1 = DispatchMethods.dispatch1 (cp);
          }
          code.append (new INVOKEVIRTUAL (dispatch1.getMethod (returnType)));
          break;
        case 2:
          // $ = dispatch2(...);
          if (dispatch2 == null) {
            dispatch2 = DispatchMethods.dispatch2 (cp);
          }
          code.append (new INVOKEVIRTUAL (dispatch2.getMethod (returnType)));
          break;
        default:
          // $ = dispatchN(...);
          if (dispatchN == null) {
            dispatchN = DispatchMethods.dispatchN (cp);
          }
          code.append (new INVOKEVIRTUAL (dispatchN.getMethod (returnType)));
          break;
        }
      } else {
        // $ = dispatch0 (...);
        if (dispatch0 == null) {
          dispatch0 = DispatchMethods.dispatch0 (cp);
        }
        code.append (new INVOKEVIRTUAL (dispatch0.getMethod (returnType)));
      }
      final InstructionHandle ihEND_TRY;
      switch (returnType) {
      case ArgumentType.VOID: // Void
        // return;
        ihEND_TRY = code.append (InstructionConstants.RETURN);
        break;
      case ArgumentType.WORD:
        // return $;
        ihEND_TRY = code.append (InstructionConstants.IRETURN);
        break;
      case ArgumentType.DWORD:
        // return $;
        ihEND_TRY = code.append (InstructionConstants.LRETURN);
        break;
      case ArgumentType.FWORD:
        // return $;
        ihEND_TRY = code.append (InstructionConstants.FRETURN);
        break;
      case ArgumentType.FDWORD:
        // return $;
        ihEND_TRY = code.append (InstructionConstants.DRETURN);
        break;
      case ArgumentType.REF:
        // return $;
        if (method.getReturnType () != Object.class) {
          code.append (new CHECKCAST (cp.addClass (method.getReturnType ().getName ())));
        }
        ihEND_TRY = code.append (InstructionConstants.ARETURN);
        break;
      default:
        throw new UnsupportedOperationException ();
      }
      // _dispIds[$index] = -1;
      final InstructionHandle ihCATCH = code.append (InstructionConstants.POP);
      code.append (InstructionConstants.THIS);
      code.append (new GETFIELD (refDISPIDS));
      code.append (loadUnsigned (methodIndex, cp));
      code.append (new BIPUSH ((byte)-1));
      code.append (InstructionConstants.IASTORE);
      // parameters
      final String[] argNames = new String[argTypes.length];
      for (int i = 0; i < argNames.length; i++) {
        argNames[i] = "p" + i;
      }
      final MethodGen gen = new MethodGen (Constants.ACC_PUBLIC, Type.getType (method
          .getReturnType ()), Type.getTypes (method.getParameterTypes ()), argNames,
          method.getName (), classGen.getClassName (), code, cp);
      // try { ... } catch { ... }
      gen.addExceptionHandler (ihBEGIN_TRY, ihEND_TRY, ihCATCH, NOT_IMPLEMENTED_EXCEPTION_TYPE);
      // callSuper:
      final InstructionHandle labelCALLSUPER;
      if (Modifier.isAbstract (method.getModifiers ())) {
        // throw new NotImplementedException();
        labelCALLSUPER = code.append (new NEW (typeNOT_IMPLEMENTED_EXCEPTION));
        code.append (InstructionConstants.DUP);
        code.append (new INVOKESPECIAL (methodINIT_NOT_IMPLEMENTED_EXCEPTION));
        code.append (InstructionConstants.ATHROW);
      } else {
        // $ = super.method (...)
        labelCALLSUPER = code.append (InstructionConstants.THIS);
        for (int i = 0; i < argTypes.length; i++) {
          switch (argTypes[i]) {
          case ArgumentType.REF:
            code.append (new ALOAD (localARG[i]));
            break;
          case ArgumentType.WORD:
            code.append (new ILOAD (localARG[i]));
            break;
          case ArgumentType.DWORD:
            code.append (new LLOAD (localARG[i]));
            break;
          case ArgumentType.FWORD:
            code.append (new FLOAD (localARG[i]));
            break;
          case ArgumentType.FDWORD:
            code.append (new DLOAD (localARG[i]));
            break;
          default:
            throw new UnsupportedOperationException ();
          }
        }
        code.append (new INVOKESPECIAL (cp.addMethodref (classGen.getSuperclassName (),
            method.getName (), gen.getSignature ())));
        switch (returnType) {
        case 0: // Void
          // return;
          code.append (InstructionConstants.RETURN);
          break;
        case ArgumentType.WORD:
          // return $;
          code.append (InstructionConstants.IRETURN);
          break;
        case ArgumentType.DWORD:
          // return $;
          code.append (InstructionConstants.LRETURN);
          break;
        case ArgumentType.FWORD:
          // return $;
          code.append (InstructionConstants.FRETURN);
          break;
        case ArgumentType.FDWORD:
          // return $;
          code.append (InstructionConstants.DRETURN);
          break;
        case ArgumentType.REF:
          // return $;
          code.append (InstructionConstants.ARETURN);
          break;
        default:
          throw new UnsupportedOperationException ();
        }
      }
      gotoCALLSUPER.setTarget (labelCALLSUPER);
      // Generate the method
      gen.setMaxLocals (localCount);
      gen.setMaxStack (); // TODO: Work this out statically above
      classGen.addMethod (gen.getMethod ());
    }
  }

  private void dumpClassASCII (final byte[] byteCode, final PrintStream out) {
    out.println (getClazz ().getName ());
    for (int i = 0; i < byteCode.length; i++) {
      if (i % 32 == 0) {
        out.print ('\t');
      } else {
        out.print (' ');
      }
      final int v = byteCode[i] & 255;
      if (v >= 32 && v < 127) {
        out.print (' ');
        out.print ((char)v);
        out.print ("  ");
      } else {
        out.print ("0x");
        if (v < 16) {
          out.print ('0');
        }
        out.print (Integer.toHexString (v));
      }
      if (i % 32 == 31) {
        out.println ();
      }
    }
    out.println ();
  }

  private void dumpClassBinary (final byte[] byteCode, final String path) {
    try {
      final File outputDir = new File (path);
      outputDir.mkdirs ();
      final File outputFile = new File (outputDir, getClazz ().getSimpleName () + ".class");
      final OutputStream out = new BufferedOutputStream (new FileOutputStream (outputFile));
      try {
        out.write (byteCode, 0, byteCode.length);
      } finally {
        out.close ();
      }
    } catch (final IOException e) {
      e.printStackTrace ();
    }
  }

  /**
   * Builds the stub class.
   * 
   * @param methods
   *          the methods to include in the stub, not {@code null}
   */
  @SuppressWarnings ("unchecked")
  public final Class<? extends T> build (final Iterable<ClassMethods.MethodInfo> methods) {
    final ClassGen classGen = createClassGen ();
    addFields (classGen);
    addConstructor (classGen);
    addMethods (classGen, methods);
    final ByteCodeClassLoader cl = s_loader.get ();
    final byte[] byteCode = classGen.getJavaClass ().getBytes ();
    if (DEBUG_STDOUT) {
      dumpClassASCII (byteCode, System.out);
    }
    if (DEBUG_FILESYSTEM != null) {
      dumpClassBinary (byteCode, DEBUG_FILESYSTEM);
    }
    return (Class<? extends T>)cl.load (classGen.getClassName (), byteCode);
  }

}