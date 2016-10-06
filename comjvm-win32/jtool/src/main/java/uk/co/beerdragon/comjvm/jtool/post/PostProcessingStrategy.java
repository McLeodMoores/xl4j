/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.post;

import java.util.List;

/**
 * Strategy for post processing generating files. Files may, for example, have a header prepended to
 * them.
 */
public interface PostProcessingStrategy {

  /**
   * Processes content that has previously been generated.
   * 
   * @param content
   *          generated content, not {@code null} and not containing {@code null}
   * @return modified content, not {@code null} and not containing {@code null}
   */
  List<String> postProcess (List<String> content);

}