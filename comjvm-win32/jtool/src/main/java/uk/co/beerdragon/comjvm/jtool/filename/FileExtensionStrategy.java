/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.filename;

import java.util.Objects;

/**
 * File naming strategy that appends a period character and fixed extension to the name produced by
 * an underlying strategy.
 */
public class FileExtensionStrategy implements FilenameStrategy {

  private final FilenameStrategy _underlying;

  private final String _ext;

  /**
   * Creates a new instance.
   * 
   * @param underlying
   *          underlying strategy, used to create the main part of the filename, not {@code null}
   * @param ext
   *          fixed extension, not {@code null}
   */
  public FileExtensionStrategy (final FilenameStrategy underlying, final String ext) {
    _underlying = Objects.requireNonNull (underlying);
    _ext = Objects.requireNonNull (ext);
  }

  /**
   * Returns the underlying naming strategy.
   * 
   * @return the underlying strategy, never {@code null}
   */
  protected FilenameStrategy getUnderlying () {
    return _underlying;
  }

  /**
   * Returns the fixed extension.
   * 
   * @return the fixed extension, never {@code null}
   */
  protected String getExt () {
    return _ext;
  }

  // FilenameStrategy

  @Override
  public String nameFor (final String className) {
    return getUnderlying ().nameFor (className) + "." + getExt ();
  }

}