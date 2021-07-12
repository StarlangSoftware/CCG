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

    public int no() {
        return no;
    }

    @Override
    public String toString() {
        return word.toString();
    }
}
