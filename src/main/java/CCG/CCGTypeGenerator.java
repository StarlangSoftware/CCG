package CCG;

import AnnotatedSentence.*;
import Cookies.Set.DisjointSet;
import Cookies.Tuple.Pair;

import java.util.*;

public abstract class CCGTypeGenerator {

    protected static ArrayList<CCGWord> constructCCGWord(AnnotatedSentence sentence) {
        ArrayList<CCGWord> words = new ArrayList<>();
        for (int i = 0; i < sentence.wordCount(); i++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (!word.isPunctuation()) {
                words.add(new CCGWord(word.getCcg(), word.getUniversalDependency().toString()));
            }
        }
        return words;
    }

    protected static boolean checkCCGs(AnnotatedSentence sentence) {
        for (int i = 0; i < sentence.wordCount(); i++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (word.getCcg() == null) {
                return true;
            }
        }
        return false;
    }

    protected static ArrayList<AnnotatedSentence> splitSentence(AnnotatedSentence sentence) {
        ArrayList<AnnotatedSentence> sentences = new ArrayList<>();
        ArrayList<Pair<AnnotatedWord, Integer>> words = new ArrayList<>();
        for (int i = 0; i < sentence.wordCount(); i++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            words.add(new Pair<>(word, i));
        }
        DisjointSet<Pair<AnnotatedWord, Integer>> set = new DisjointSet<>(words);
        for (Pair<AnnotatedWord, Integer> pair : words) {
            if (!pair.getKey().getUniversalDependency().toString().equals("PARATAXIS") && !pair.getKey().getUniversalDependency().toString().equals("ROOT")) {
                set.union(pair, words.get(pair.getKey().getUniversalDependency().to() - 1));
            }
        }
        sentences.add(new AnnotatedSentence());
        sentences.get(0).addWord(words.get(0).getKey());
        for (int i = 1; i < words.size(); i++) {
            if (set.findSet(words.get(i - 1)) != set.findSet(words.get(i))) {
                sentences.add(new AnnotatedSentence());
            }
            sentences.get(sentences.size() - 1).addWord(words.get(i).getKey());
        }
        return sentences;
    }
}
