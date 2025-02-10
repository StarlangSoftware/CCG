package CCG;

import AnnotatedSentence.AnnotatedWord;

public class CCGWordPair {

    private final AnnotatedWord word;
    private final AnnotatedWord toWord;
    private final int no;

    public CCGWordPair(AnnotatedWord word, AnnotatedWord toWord, int no) {
        this.word = word;
        this.toWord = toWord;
        this.no = no;
    }

    public int no() {
        return no;
    }

    public AnnotatedWord getWord() {
        return word;
    }

    public AnnotatedWord getToWord() {
        return toWord;
    }

    public int to() {
        return word.getUniversalDependency().to();
    }

    public String getUniversalDependency() {
        return word.getUniversalDependency().toString();
    }

    public boolean isToRoot() {
        return getToUniversalDependency().equals("ROOT");
    }

    public String getToUniversalDependency() {
        return toWord.getUniversalDependency().toString();
    }

    public String getCcg() {
        return word.getCcg();
    }

    public String getToCcg() {
        return toWord.getCcg();
    }

    public void setCcg(String ccg) {
        word.setCcg(ccg);
    }

    public void setToCcg(String ccg) {
        toWord.setCcg(ccg);
    }

    public String findSlash() {
        if (no > to() - 1) {
            return "\\";
        }
        return "/";
    }

    @Override
    public String toString() {
        return word.toString();
    }

    @Override
    public int hashCode() {
        if (toWord != null) {
            return word.hashCode() ^ toWord.hashCode() ^ no;
        }
        return word.hashCode() ^ no;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CCGWordPair)) {
            return false;
        }
        CCGWordPair pair = (CCGWordPair) obj;
        return this.word.equals(pair.word) && this.no == pair.no;
    }
}
