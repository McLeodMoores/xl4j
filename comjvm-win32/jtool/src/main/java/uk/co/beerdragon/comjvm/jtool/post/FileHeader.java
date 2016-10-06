/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.post;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

/**
 * Post processing strategy that prepends a fixed block of text to a file.
 */
public class FileHeader implements PostProcessingStrategy {

  private final List<String> _header;

  /**
   * Creates a new instance with static text.
   * 
   * @param header
   *          header to prefix to the file, not {@code null} and not containing {@code null}
   */
  public FileHeader (final List<String> header) {
    Validate.noNullElements (header);
    _header = new ArrayList<String> (header);
  }

  /**
   * Reads the file.
   * 
   * @param reader
   *          file data, not {@code null}
   * @return the lines from the file, never {@code null} or containing {@code null}
   */
  private static List<String> readHeader (final Reader reader) throws IOException {
    final List<String> buffer = new LinkedList<String> ();
    final BufferedReader br = new BufferedReader (Objects.requireNonNull (reader));
    String line;
    while ((line = br.readLine ()) != null) {
      buffer.add (line);
    }
    return buffer;
  }

  /**
   * Creates a new instance with text read from another source.
   * 
   * @param reader
   *          source of the header text to use, not {@code null}
   */
  public FileHeader (final Reader reader) throws IOException {
    this (readHeader (reader));
  }

  // PostProcessingStrategy

  @Override
  public List<String> postProcess (final List<String> content) {
    final ArrayList<String> result = new ArrayList<String> (_header.size () + content.size ());
    result.addAll (_header);
    result.addAll (content);
    return result;
  }

}