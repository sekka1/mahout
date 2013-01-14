/*
* Copyright 2013 Algorithms.io. All Rights Reserved.
*/
package io.algorithms.text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.IStemmer;
import edu.mit.jwi.morph.WordnetStemmer;

/**
 * Exposes wordnet functionality.
 */
public class Wordnet {
    public static final String WORDNET_LOCATION = "/usr/share/wordnet";
    
    /**
     * Returns a list of words related to the given word. This includes synonyms and hypernyms.
     * @param word
     * @return
     */
    public List<String> getRelatedWords(String word) throws IOException {
        List<String> result = new ArrayList<String>();
        IDictionary dict = new Dictionary(new File(WORDNET_LOCATION));
        try {
            dict.open();
            IStemmer stemmer = new WordnetStemmer(dict);
            for (POS pos : EnumSet.of(POS.ADJECTIVE, POS.ADVERB, POS.NOUN, POS.VERB)) {
                List<String> resultForPos = new ArrayList<String>();
                List<String> stems = new ArrayList<String>();
                stems.add(word);
                for (String stem : stemmer.findStems(word,  pos)) {
                    if (!stems.contains(stem))
                        stems.add(stem);
                }
                for (String stem : stems) {
                    if (!resultForPos.contains(stem)) {
                        resultForPos.add(stem);
                        IIndexWord idxWord = dict.getIndexWord(stem, pos);
                        if (idxWord == null) continue;
                        List<IWordID> wordIDs = idxWord.getWordIDs();
                        if (wordIDs == null) continue;
                        IWordID wordID = wordIDs.get(0);
                        IWord iword = dict.getWord(wordID);
        
                        ISynset synonyms = iword.getSynset();
                        List<IWord> iRelatedWords = synonyms.getWords();
                        if (iRelatedWords != null) {
                            for (IWord iRelatedWord : iRelatedWords) {
                                String relatedWord = iRelatedWord.getLemma();
                                if (!resultForPos.contains(relatedWord))
                                    resultForPos.add(relatedWord);
                            }
                        }
                        
                        List<ISynsetID> hypernymIDs = synonyms.getRelatedSynsets();
                        if (hypernymIDs != null) {
                            for (ISynsetID relatedSynsetID : hypernymIDs) {
                                ISynset relatedSynset = dict.getSynset(relatedSynsetID);
                                if (relatedSynset != null) {
                                    iRelatedWords = relatedSynset.getWords();
                                    if (iRelatedWords != null) {
                                        for (IWord iRelatedWord : iRelatedWords) {
                                            String relatedWord = iRelatedWord.getLemma();
                                            if (!resultForPos.contains(relatedWord))
                                                resultForPos.add(relatedWord);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for (String relatedWord : resultForPos) {
                    if (relatedWord.length() > 3
                            && !relatedWord.contains("-")
                            && !result.contains(relatedWord)) {
                        // TODO: Hack alert!
                        // The - check is to prevent lucene from interpreting hyphenated words as negative search terms
                        // Fix!
                        result.add(relatedWord);
                    }
                }
            }
        } finally {
            dict.close();
        }
        return result;
    }
    

}
