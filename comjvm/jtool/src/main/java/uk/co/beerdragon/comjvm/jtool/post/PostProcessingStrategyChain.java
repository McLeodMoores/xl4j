/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;

/**
 * Sequential composition of two or more post processing strategies.
 */
public class PostProcessingStrategyChain implements PostProcessingStrategy {

  private final List<PostProcessingStrategy> _strategies;

  /**
   * Creates a new instance, composing the list of other strategies.
   * <p>
   * Each strategy will be applied, in the order it is present in the list. The first strategy
   * receives the original input. All other strategies receive, as input, the output from the
   * previous strategy. The output from the final strategy in the list is returned as the overall
   * result.
   * 
   * @param strategies
   *          strategies to compose, not {@code null}, and not containing {@code null}
   */
  public PostProcessingStrategyChain (final List<PostProcessingStrategy> strategies) {
    Validate.noNullElements (strategies);
    _strategies = new ArrayList<PostProcessingStrategy> (strategies);
  }

  /**
   * Creates a new instance, composing a vararg list of other strategies.
   * <p>
   * Each strategy will be applied, in the order it is present in the arguments. The first strategy
   * receives the original input. All other strategies receive, as input, the output from the
   * previous strategy. The output from the final strategy in the list is returned as the overall
   * result.
   * 
   * @param strategies
   *          strategies to compose, not {@code null}, empty, and not containing {@code null}
   */
  public PostProcessingStrategyChain (final PostProcessingStrategy... strategies) {
    this (Arrays.asList (strategies));
  }

  // PostProcessingStrategy

  @Override
  public List<String> postProcess (List<String> content) {
    for (final PostProcessingStrategy strategy : _strategies) {
      content = strategy.postProcess (content);
    }
    return content;
  }

}