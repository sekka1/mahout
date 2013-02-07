package io.algorithms.clustering;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import java.io.IOException;

/**
 * Filters speakers in the script. For instance, in the sentence 'JERRY: Hello, Newman...'
 * The token 'JERRY:' will be removed.
 *
 * @author Frank Scholten
 */
public class SpeakerFilter extends TokenFilter {

  private TermAttribute termAttribute;

  /**
   * Construct a token stream filtering the given input.
   *
   * @param input tokenstream to filter
   */
  protected SpeakerFilter(TokenStream input) {
    super(input);

    termAttribute = addAttribute(TermAttribute.class);
  }

  @Override
  public boolean incrementToken() throws IOException {
    while (input.incrementToken()) {
      String term = termAttribute.term();
      if (!term.equals(term.toUpperCase())) {
        return true;
      }
    }
    return false;
  }
}
