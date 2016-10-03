/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import uk.co.beerdragon.comjvm.jtool.filename.FilenameStrategy;
import uk.co.beerdragon.comjvm.jtool.post.PostProcessingStrategy;

/**
 * Opens and manages writes to files.
 */
public class FileGenerator {

  /**
   * State marker for a file that is being, or has been, written.
   */
  private class OpenFile {

    private final File _file;

    private boolean _append;

    /**
     * Creates a new instance.
     * 
     * @param file
     *          file to create, never {@code null}
     */
    public OpenFile (final File file) {
      _file = file;
    }

    /**
     * Returns the file.
     * 
     * @return the file, never {@code null}
     */
    private File getFile () {
      return _file;
    }

    /**
     * Creates a writing instance. The data will be written to the underlying file when the writer
     * is closed, allowing multiple writers on the same file to be opened concurrently.
     * 
     * @return the writer, not {@code null}
     */
    public Writer writer () {
      final CharArrayWriter buffer = new CharArrayWriter ();
      return new Writer () {

        @Override
        public void write (final char[] cbuf, final int off, final int len) throws IOException {
          buffer.write (cbuf, off, len);
        }

        @Override
        public void flush () throws IOException {
          // No-op
        }

        @Override
        public void close () throws IOException {
          OpenFile.this.write (buffer.toCharArray ());
        }

      };
    }

    /**
     * Writes the data to the underlying file.
     * 
     * @param buffer
     *          data to write, not {@code null}
     */
    private synchronized void write (final char[] buffer) throws IOException {
      final Writer output = openFileForWriting (getFile (), _append);
      output.write (buffer);
      output.close ();
      _append = true;
    }

  }

  private final File _baseDir;

  private final FilenameStrategy _filenameStrategy;

  private final PostProcessingStrategy _postProcessingStrategy;

  private final Map<String, OpenFile> _open = new HashMap<String, OpenFile> ();

  /**
   * Creates a new instance.
   * 
   * @param baseDir
   *          base directory in which files should be generated, not {@code null}
   * @param filenameStrategy
   *          naming strategy to use for files, not {@code null}
   * @param postProcessingStrategy
   *          post processing strategy for generated files, not {@code null}
   */
  public FileGenerator (final File baseDir, final FilenameStrategy filenameStrategy,
      final PostProcessingStrategy postProcessingStrategy) {
    _baseDir = Objects.requireNonNull (baseDir);
    _filenameStrategy = Objects.requireNonNull (filenameStrategy);
    _postProcessingStrategy = Objects.requireNonNull (postProcessingStrategy);
  }

  /**
   * Returns the base directory that files will be created in.
   * 
   * @return the base directory, never {@code null}
   */
  protected File getBaseDir () {
    return _baseDir;
  }

  /**
   * Returns the file naming strategy used.
   * 
   * @return the naming strategy, never {@code null}
   */
  public FilenameStrategy getFilenameStrategy () {
    return _filenameStrategy;
  }

  /**
   * Returns the post processing strategy.
   * 
   * @return the post processing strategy, never {@code null}
   */
  protected PostProcessingStrategy getPostProcessingStrategy () {
    return _postProcessingStrategy;
  }

  /**
   * Opens the underlying file, for writing.
   * 
   * @param file
   *          file to create or open, not {@code null}
   * @param append
   *          {@code true} to append to the existing file, {@code false} to overwrite
   * @return the file writer, never {@code null}
   */
  protected Writer openFileForWriting (final File file, final boolean append) throws IOException {
    return new FileWriter (Objects.requireNonNull (file), append);
  }

  /**
   * Opens the underlying file, for reading.
   * 
   * @param file
   *          file to open, not {@code null}
   * @return the file reader, never {@code null}
   */
  protected Reader openFileForReading (final File file) throws IOException {
    return new FileReader (Objects.requireNonNull (file));
  }

  /**
   * Opens the file to be written for the class, as per the naming strategy.
   * <p>
   * It is safe to use a {@code FileGenerator} from multiple threads; if the file is already open
   * then the writer returned by other calls will be one that buffers until it is safe to write to
   * the underlying resource. Anything passed to a single writer returned by this method will be
   * written atomically, with respect to other writers returned from this method, to the underlying
   * file.
   * 
   * @param className
   *          class name to open the file for, not {@code null}
   * @return a writer resource, not {@code null}
   */
  public synchronized PrintWriter openFileFor (final String className) {
    final String filename = getFilenameStrategy ().nameFor (className);
    OpenFile open = _open.get (Objects.requireNonNull (filename));
    if (open == null) {
      final File file = new File (getBaseDir (), filename);
      open = new OpenFile (file);
      _open.put (filename, open);
    }
    return new PrintWriter (open.writer ());
  }

  /**
   * Returns the set of generated files.
   * <p>
   * These are the files that were identified by previous calls to {@link #openFileFor}. The files
   * may not exist on disk if the writers returned for any have not yet been closed.
   * 
   * @return the files, never {@code null} or containing {@code null}
   */
  private synchronized Collection<File> getGeneratedFiles () {
    final Collection<OpenFile> files = _open.values ();
    final List<File> result = new ArrayList<File> (files.size ());
    for (final OpenFile file : files) {
      result.add (file.getFile ());
    }
    Collections.sort (result);
    return result;
  }

  /**
   * Runs all generated files through the registered post processor.
   */
  public void postProcess () throws IOException {
    for (final File file : getGeneratedFiles ()) {
      final BufferedReader reader = new BufferedReader (openFileForReading (file));
      List<String> fileContent;
      try {
        final long fileSize = file.length ();
        final ArrayList<String> fileContentBuffer = new ArrayList<String> ();
        String line;
        long charsRead = 0;
        while ((line = reader.readLine ()) != null) {
          charsRead += line.length ();
          final long avgLineLength = charsRead / (fileContentBuffer.size () + 1);
          fileContentBuffer.ensureCapacity ((int)(fileSize / avgLineLength));
          fileContentBuffer.add (line);
        }
        fileContent = fileContentBuffer;
      } finally {
        reader.close ();
      }
      fileContent = getPostProcessingStrategy ().postProcess (fileContent);
      final PrintWriter writer = new PrintWriter (openFileForWriting (file, false));
      try {
        for (final String line : fileContent) {
          writer.println (line);
        }
      } finally {
        writer.close ();
      }
    }
  }
}