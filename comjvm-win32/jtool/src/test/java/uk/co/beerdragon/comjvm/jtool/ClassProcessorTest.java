/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.logging.Logger;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.beerdragon.comjvm.jtool.msvc.UUIDManager;

/**
 * Tests the {@link ClassProcessor} class.
 */
@Test
public class ClassProcessorTest {

  // TODO: Change this to just make sure that IDL, C++ and HPP are written. Move the exact
  // tests for content into IDL, C++ and HPP specific tests now that the code has moved out
  // of ClassProcessor into specific service classes.

  /**
   * Tests processing an interface issues the expected IDL, header and C++ fragments and that any
   * dependencies are requested as additional classes.
   */
  public void testProcessInterface () {
    final ClassProcessor instance = new ClassProcessor ();
    final AdditionalClasses additional = Mockito.mock (AdditionalClasses.class);
    instance.setAdditionalClasses (additional);
    final FileGenerator idlGenerator = Mockito.mock (FileGenerator.class);
    final CharArrayWriter idlJavaUtil = new CharArrayWriter ();
    Mockito.when (idlGenerator.openFileFor ("java.util.Set")).thenReturn (
        new PrintWriter (idlJavaUtil));
    instance.setIdlGenerator (idlGenerator);
    final FileGenerator hppGenerator = Mockito.mock (FileGenerator.class);
    final CharArrayWriter hppJavaUtil = new CharArrayWriter ();
    Mockito.when (hppGenerator.openFileFor ("java.util.Set")).thenReturn (
        new PrintWriter (hppJavaUtil));
    instance.setHppGenerator (hppGenerator);
    final FileGenerator cppGenerator = Mockito.mock (FileGenerator.class);
    final CharArrayWriter cppJavaUtilSet = new CharArrayWriter ();
    Mockito.when (cppGenerator.openFileFor ("java.util.Set")).thenReturn (
        new PrintWriter (cppJavaUtilSet));
    instance.setCppGenerator (cppGenerator);
    instance.setUUIDManager (new UUIDManager ());
    instance.process ("java.util.Set");
    System.out.println ();
    System.out.println ("IDL:");
    System.out.println (idlJavaUtil.toString ());
    System.out.println ();
    System.out.println ("Header:");
    System.out.println (hppJavaUtil.toString ());
    System.out.println ();
    System.out.println ("C++:");
    System.out.println (cppJavaUtilSet.toString ());
    System.out.println ();
    // Verify IDL for IJavaUtilSet and IJJavaUtilSet
    Assert.assertTrue (idlJavaUtil.toString ().contains ("interface IJavaUtilSet"));
    Assert.assertTrue (idlJavaUtil.toString ().contains ("interface IJJavaUtilSet"));
    // Verify header file for COM->Java and Java->COM methods
    Assert.assertTrue (hppJavaUtil.toString ().contains (
        "HRESULT COMJVM_WRAPPER_API COMJVM_WRAPPER_CAST(JavaUtilSet)"));
    Assert.assertTrue (hppJavaUtil.toString ().contains (
        "HRESULT COMJVM_WRAPPER_API COMJVM_WRAPPER_WRAP(JavaUtilSet)"));
    // Verify C++ contains class definition and methods
    Assert.assertTrue (cppJavaUtilSet.toString ().contains (
        "HRESULT COMJVM_WRAPPER_API COMJVM_WRAPPER_CAST(JavaUtilSet)"));
    Assert.assertTrue (cppJavaUtilSet.toString ().contains (
        "HRESULT COMJVM_WRAPPER_API COMJVM_WRAPPER_WRAP(JavaUtilSet)"));
    Assert.assertTrue (cppJavaUtilSet.toString ().contains (
        "class CJavaUtilSet : public IJJavaUtilSet {"));
    // Verify additional classes were requested
    Mockito.verify (additional).add ("java.lang.Object");
    Mockito.verify (additional).add ("java.util.Collection");
  }

  /**
   * Tests processing a class issues the expected IDL, header, and C++ fragments and that any
   * dependencies are requested as additional classes.
   */
  public void testProcessClass () {
    final ClassProcessor instance = new ClassProcessor ();
    final AdditionalClasses additional = Mockito.mock (AdditionalClasses.class);
    instance.setAdditionalClasses (additional);
    final FileGenerator idlGenerator = Mockito.mock (FileGenerator.class);
    final CharArrayWriter idlJavaUtil = new CharArrayWriter ();
    Mockito.when (idlGenerator.openFileFor ("java.util.HashSet")).thenReturn (
        new PrintWriter (idlJavaUtil));
    instance.setIdlGenerator (idlGenerator);
    final FileGenerator hppGenerator = Mockito.mock (FileGenerator.class);
    final CharArrayWriter hppJavaUtil = new CharArrayWriter ();
    Mockito.when (hppGenerator.openFileFor ("java.util.HashSet")).thenReturn (
        new PrintWriter (hppJavaUtil));
    instance.setHppGenerator (hppGenerator);
    final FileGenerator cppGenerator = Mockito.mock (FileGenerator.class);
    final CharArrayWriter cppJavaUtilSet = new CharArrayWriter ();
    Mockito.when (cppGenerator.openFileFor ("java.util.HashSet")).thenReturn (
        new PrintWriter (cppJavaUtilSet));
    instance.setCppGenerator (cppGenerator);
    instance.setUUIDManager (new UUIDManager ());
    instance.process ("java.util.HashSet");
    System.out.println ();
    System.out.println ("IDL:");
    System.out.println (idlJavaUtil.toString ());
    System.out.println ();
    System.out.println ("Header:");
    System.out.println (hppJavaUtil.toString ());
    System.out.println ();
    System.out.println ("C++:");
    System.out.println (cppJavaUtilSet.toString ());
    System.out.println ();
    // TODO: Verify IDL for IJavaUtilHashSet was posted to JavaUtil.idl
    // TODO: Verify IDL for IJJavaUtilHashSet was posted to JavaUtil.idl
    // TODO: Verify MakeJavaUtilHashSet(IJObject *, IJJavaUtilHashSet **) was
    // posted to JavaUtil.h
    // TODO: Verify AttachJavaUtilHashSet(IJavaUtilHashSet *, IJVM *,
    // IJJavaUtilHashSet **) was posted to JavaUtil.h
    // TODO: Verify methods written to JavaUtilHashSet.cpp
    Mockito.verify (additional).add ("java.util.AbstractSet");
    // TODO: Verify any other additional classes are requested
  }

  /**
   * Tests an invalid class name is handled gracefully.
   */
  public void testProcessInvalidClass () {
    final ClassProcessor instance = new ClassProcessor ();
    final Logger logger = Mockito.mock (Logger.class);
    instance.setLogger (logger);
    instance.process ("CLASS_NAME_THAT_DOESNT_EXIST");
    Mockito.verify (logger).warning (Matchers.anyString ());
  }

}