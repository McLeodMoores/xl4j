/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.beerdragon.comjvm.jtool.filename.FilenameStrategy;
import uk.co.beerdragon.comjvm.jtool.post.PostProcessingStrategy;

/**
 * Tests the {@link FileGenerator} class.
 */
@Test
public class FileGeneratorTest {

  /**
   * Tests opening, writing to, and closing a file.
   */
  public void testBasicFile () {
    final FilenameStrategy filenames = Mockito.mock (FilenameStrategy.class);
    Mockito.when (filenames.nameFor ("java.lang.String")).thenReturn ("DeleteMe.txt");
    final CharArrayWriter output = new CharArrayWriter ();
    final FileGenerator files = new FileGenerator (new File ("."), filenames,
        Mockito.mock (PostProcessingStrategy.class)) {

      @Override
      protected Writer openFileForWriting (final File file, final boolean append) {
        Assert.assertEquals (file.getPath (), "." + File.separator + "DeleteMe.txt");
        Assert.assertFalse (append);
        return output;
      }

    };
    final PrintWriter writer = files.openFileFor ("java.lang.String");
    writer.print ("Foo");
    writer.close ();
    Assert.assertEquals (output.toCharArray (), "Foo".toCharArray ());
  }

  /**
   * Tests opening the same file twice, interleaving the writes, and then closing each. The
   * underlying file should not have any interleaves.
   */
  public void testMultipleWrites () {
    final FilenameStrategy filenames = Mockito.mock (FilenameStrategy.class);
    Mockito.when (filenames.nameFor ("java.lang.String")).thenReturn ("DeleteMe.txt");
    final CharArrayWriter output = new CharArrayWriter ();
    final FileGenerator files = new FileGenerator (new File ("."), filenames,
        Mockito.mock (PostProcessingStrategy.class)) {

      int count;

      @Override
      protected Writer openFileForWriting (final File file, final boolean append) {
        Assert.assertEquals (file.getPath (), "." + File.separator + "DeleteMe.txt");
        count++;
        if (count == 1) {
          Assert.assertFalse (append);
        } else {
          Assert.assertTrue (append);
        }
        return output;
      }

    };
    final PrintWriter writer1 = files.openFileFor ("java.lang.String");
    final PrintWriter writer2 = files.openFileFor ("java.lang.String");
    writer1.print ("Foo");
    writer1.flush ();
    writer2.print ("Foo");
    writer2.flush ();
    writer1.print ("Bar");
    writer1.flush ();
    writer2.print ("Bar");
    writer2.flush ();
    writer1.close ();
    Assert.assertEquals (output.toCharArray (), "FooBar".toCharArray ());
    writer2.close ();
    Assert.assertEquals (output.toCharArray (), "FooBarFooBar".toCharArray ());
  }

  /**
   * Tests the post-processing filter is applied to all files.
   */
  public void testPostProcessing () throws IOException {
    final String newline = System.getProperty ("line.separator");
    final FilenameStrategy filenames = Mockito.mock (FilenameStrategy.class);
    Mockito.when (filenames.nameFor ("A")).thenReturn ("A.txt");
    Mockito.when (filenames.nameFor ("B")).thenReturn ("B.txt");
    final PostProcessingStrategy postProcessing = Mockito.mock (PostProcessingStrategy.class);
    final File baseDir = new File (".");
    final ArrayList<CharArrayWriter> writers = new ArrayList<CharArrayWriter> (4);
    final FileGenerator files = new FileGenerator (baseDir, filenames, postProcessing) {

      @Override
      protected Writer openFileForWriting (final File file, final boolean append) {
        final CharArrayWriter writer = new CharArrayWriter ();
        writers.add (writer);
        return writer;
      }

      @Override
      protected Reader openFileForReading (final File file) {
        return new StringReader (file.getName () + newline + "Foo");
      }

    };
    files.openFileFor ("A").close ();
    files.openFileFor ("B").close ();
    Mockito.when (postProcessing.postProcess (Arrays.asList ("A.txt", "Foo"))).thenReturn (
        Arrays.asList ("A"));
    Mockito.when (postProcessing.postProcess (Arrays.asList ("B.txt", "Foo"))).thenReturn (
        Arrays.asList ("B"));
    files.postProcess ();
    Assert.assertEquals (writers.size (), 4);
    Assert.assertEquals (new String (writers.get (2).toCharArray ()), "A" + newline);
    Assert.assertEquals (new String (writers.get (3).toCharArray ()), "B" + newline);
  }

}