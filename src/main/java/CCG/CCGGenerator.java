package CCG;

import AnnotatedSentence.*;

import java.util.*;

public class CCGGenerator {

    private static ArrayList<CCGWordPair> setWords(AnnotatedSentence sentence) {
        HashSet<String> set = new HashSet<>();
        CCGWordPair root = null;
        ArrayList<CCGWordPair> words = new ArrayList<>();
        int lastIndex = findLastIndex(sentence, words);
        int lastAddIndex = words.size();
        for (int i = 0; i < lastIndex; i++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (word.getUniversalDependency().to() == 0) {
                root = new CCGWordPair(word, null, i);
                words.add(words.size() - lastAddIndex, root);
            } else {
                CCGWordPair ccgWordPair = new CCGWordPair(word, (AnnotatedWord) sentence.getWord(word.getUniversalDependency().to() - 1), i);
                if (ccgWordPair.isToRoot()) {
                    set.add(ccgWordPair.getUniversalDependency());
                }
                words.add(words.size() - lastAddIndex, ccgWordPair);
            }
        }
        setRoot(root, set);
        return words;
    }

    private static int findLastIndex(AnnotatedSentence sentence, ArrayList<CCGWordPair> words) {
        int lastIndex = sentence.wordCount();
        for (int i = sentence.wordCount() - 1; i > -1; i--) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (word.isPunctuation()) {
                lastIndex = i;
                if (!word.getName().equals(".") && !word.getName().equals(":") && !word.getName().equals("...") && !word.getName().equals("?") && !word.getName().equals("!")) {
                    if (word.getUniversalDependency().to() == 0) {
                        words.add(new CCGWordPair(word, null, i));
                    } else {
                        words.add(new CCGWordPair(word, (AnnotatedWord) sentence.getWord(word.getUniversalDependency().to() - 1), i));
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return lastIndex;
    }

    private static ArrayList<CCGWordPair> generateSentence(ArrayList<CCGWordPair> sentence) {
        ArrayList<CCGWordPair> current = new ArrayList<>();
        for (CCGWordPair ccgWordPair : sentence) {
            if (ccgWordPair.getCcg() == null) {
                current.add(ccgWordPair);
            }
        }
        return current;
    }

    private static void setRoot(CCGWordPair root, HashSet<String> set) {
        if (set.contains("OBJ")) {
            if (set.contains("NSUBJ") || set.contains("CSUBJ")) {
                if (set.contains("OBL")) {
                    root.setCcg("((S\\NP)\\NP)\\NP)");
                } else {
                    root.setCcg("(S\\NP[nom])\\NP");
                }
            } else {
                root.setCcg("S\\NP");
            }
        } else {
            if (set.contains("NSUBJ") || set.contains("CSUBJ")) {
                root.setCcg("S\\NP[nom]");
            } else {
                root.setCcg("S");
            }
        }
    }

    private static void setSentence(ArrayList<CCGWordPair> sentence) {
        for (CCGWordPair ccgWordPair : sentence) {
            switch (ccgWordPair.getUniversalDependency()) {
                case "NSUBJ":
                case "CSUBJ":
                    ccgWordPair.setCcg("NP[nom]");
                    break;
                case "DET":
                case "NUMMOD":
                    ccgWordPair.setCcg("NP/NP");
                    if (!ccgWordPair.isToRoot()) {
                        ccgWordPair.setToCcg("NP");
                    }
                    break;
                case "ACL":
                case "AMOD":
                    ccgWordPair.setCcg("(NP/NP)");
                    if (!ccgWordPair.isToRoot()) {
                        ccgWordPair.setToCcg("NP");
                    }
                    break;
                case "MARK":
                case "PARATAXIS":
                    ccgWordPair.setCcg("S/S");
                    if (!ccgWordPair.isToRoot()) {
                        ccgWordPair.setToCcg("S");
                    }
                    break;
                case "DISCOURSE":
                    ccgWordPair.setCcg("(S/S)");
                    if (!ccgWordPair.isToRoot()) {
                        ccgWordPair.setToCcg("S");
                    }
                    break;
                case "ORPHAN":
                case "APPOS":
                    ccgWordPair.setCcg("NP\\NP");
                    if (!ccgWordPair.isToRoot()) {
                        ccgWordPair.setToCcg("NP");
                    }
                    break;
                case "PUNCT":
                    if (ccgWordPair.getToCcg() != null) {
                        if (ccgWordPair.isToRoot()) {
                            ccgWordPair.setCcg("S" + ccgWordPair.findSlash() + "S");
                        } else {
                            ccgWordPair.setCcg(ccgWordPair.getToCcg() + ccgWordPair.findSlash() + ccgWordPair.getToCcg());
                        }
                    }
                    break;
                case "ADVCL":
                case "ADVMOD":
                case "COMPOUND":
                case "REPARANDUM":
                    if (ccgWordPair.getToCcg() != null) {
                        ccgWordPair.setCcg(ccgWordPair.getToCcg() + "/" + ccgWordPair.getToCcg());
                    }
                    break;
                case "AUX":
                case "LIST":
                case "GOESWITH":
                case "FIXED":
                    if (ccgWordPair.getToCcg() != null) {
                        ccgWordPair.setCcg(ccgWordPair.getToCcg() + "\\" + ccgWordPair.getToCcg());
                    }
                    break;
                case "OBL":
                case "OBJ":
                case "XCOMP":
                case "CCOMP":
                    // temporary solution
                case "FLAT":
                    ccgWordPair.setCcg("NP");
                    break;
                case "VOCATIVE":
                    ccgWordPair.setCcg("S" + ccgWordPair.findSlash() + "S");
                    if (!ccgWordPair.isToRoot()) {
                        ccgWordPair.setToCcg("S");
                    }
                    break;
                case "CC":
                    if (ccgWordPair.getToCcg() != null) {
                        if (ccgWordPair.getToUniversalDependency().equals("CONJ")) {
                            ccgWordPair.setCcg("(" + ccgWordPair.getToCcg() + "\\" + ccgWordPair.getToCcg() + ")" + "/" + ccgWordPair.getToCcg());
                        } else {
                            ccgWordPair.setCcg(ccgWordPair.getToCcg() + "/" + ccgWordPair.getToCcg());
                        }
                    }
                    break;
                case "NMOD":
                    if (ccgWordPair.getToUniversalDependency().equals("NSUBJ") || ccgWordPair.getToUniversalDependency().equals("CSUBJ")) {
                        ccgWordPair.setCcg("NP[nom]/NP[nom]");
                        if (!ccgWordPair.isToRoot()) {
                            ccgWordPair.setToCcg("NP[nom]");
                        }
                    } else {
                        ccgWordPair.setCcg("NP/NP");
                        if (!ccgWordPair.isToRoot()) {
                            ccgWordPair.setToCcg("NP");
                        }
                    }
                    break;
                case "CASE":
                    ccgWordPair.setCcg("PP\\NP");
                    if (!ccgWordPair.isToRoot()) {
                        ccgWordPair.setToCcg("NP");
                    }
                    break;
                case "CONJ":
                    if (ccgWordPair.getToCcg() != null) {
                        if (ccgWordPair.getWord().getUniversalDependencyPos().equals(ccgWordPair.getToWord().getUniversalDependencyPos())) {
                            ccgWordPair.setCcg("(" + ccgWordPair.getToCcg() + "\\" + ccgWordPair.getToCcg() + ")" + "/" + ccgWordPair.getToCcg());
                        } else {
                            ccgWordPair.setCcg("(" + ccgWordPair.getToCcg() + "\\" + ccgWordPair.getToCcg() + ")" + "/" + ccgWordPair.getWord().getParse().getPos());
                        }
                    }
                    break;
                case "DISLOCATED":
                    ccgWordPair.setCcg("NP" + ccgWordPair.findSlash() + "S");
                    if (!ccgWordPair.isToRoot()) {
                        ccgWordPair.setToCcg("S");
                    }
                    break;
                case "DEP":
                    ccgWordPair.setCcg("NP/S");
                    if (!ccgWordPair.isToRoot()) {
                        ccgWordPair.setToCcg("S");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static void generate(AnnotatedSentence sentence) {
        ArrayList<CCGWordPair> words = setWords(sentence);
        ArrayList<CCGWordPair> currentSentence;
        do {
            currentSentence = generateSentence(words);
            setSentence(currentSentence);
        } while (currentSentence.size() > 0);
    }
}
