/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.post;

import java.util.List;

/**
 * No-op post processing strategy. The file is unchanged.
 */
public final class NullPostProcessingStrategy implements PostProcessingStrategy {

  // PostProcessingStrategy

  @Override
  public List<String> postProcess (final List<String> content) {
    return content;
  }

}