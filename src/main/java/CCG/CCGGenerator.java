package CCG;

import AnnotatedSentence.*;

import java.util.*;

public class CCGGenerator {

    private static ArrayList<CCGWordPair> setWords(AnnotatedSentence sentence) {
        HashMap<Integer, Integer> map = new HashMap<>();
        ArrayList<CCGWordPair> words = new ArrayList<>();
        for (int i = 0; i < sentence.wordCount(); i++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (word.getUniversalDependency().to() != 0 && !word.getUniversalDependency().toString().equals("PARATAXIS")) {
                CCGWordPair ccgWordPair = new CCGWordPair(word, (AnnotatedWord) sentence.getWord(word.getUniversalDependency().to() - 1), i);
                int toIndex = word.getUniversalDependency().to() - 1;
                if (ccgWordPair.isToRoot() || ccgWordPair.getToUniversalDependency().equals("PARATAXIS")) {
                    if (!map.containsKey(toIndex)) {
                        map.put(toIndex, 0);
                    }
                    if (ccgWordPair.getUniversalDependency().equals("OBJ") || ccgWordPair.getUniversalDependency().equals("OBL") || ccgWordPair.getUniversalDependency().equals("NSUBJ") || ccgWordPair.getUniversalDependency().equals("CSUBJ") || ccgWordPair.getUniversalDependency().endsWith("COMP")) {
                        map.put(toIndex, map.get(toIndex) + 1);
                    }
                }
                words.add(ccgWordPair);
            }
        }
        setRoots(map, sentence);
        return words;
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

    private static String setRoot(int count, String root) {
        if (count == 0) {
            return root;
        }
        return setRoot(count - 1, "(" + root + "\\NP)");
    }

    private static void setRoots(HashMap<Integer, Integer> map, AnnotatedSentence sentence) {
        for (Integer key : map.keySet()) {
            ((AnnotatedWord) sentence.getWord(key)).setCcg(setRoot(map.get(key), "S"));
        }
    }

    private static ArrayList<CCGWordPair> setSentence(ArrayList<CCGWordPair> sentence) {
        ArrayList<CCGWordPair> words = new ArrayList<>();
        for (CCGWordPair ccgWordPair : sentence) {
            switch (ccgWordPair.getUniversalDependency()) {
                case "NSUBJ":
                case "CSUBJ":
                case "OBL":
                case "OBJ":
                case "CCOMP":
                case "XCOMP":
                    ccgWordPair.setCcg("NP");
                    break;
                case "DET":
                case "ACL":
                case "AMOD":
                case "NMOD":
                case "NUMMOD":
                    if (ccgWordPair.getToCcg() != null) {
                        ccgWordPair.setCcg("(" + ccgWordPair.getToCcg() + "/" + ccgWordPair.getToCcg() + ")");
                    } else {
                        ccgWordPair.setCcg("(NP/NP)");
                        if (ccgWordPair.getToCcg() == null) {
                            ccgWordPair.setToCcg("NP");
                        }
                    }
                    break;
                case "MARK":
                case "DISCOURSE":
                    ccgWordPair.setCcg("(S/S)");
                    if (ccgWordPair.getToCcg() == null) {
                        ccgWordPair.setToCcg("S");
                    }
                    break;
                case "ORPHAN":
                case "APPOS":
                    ccgWordPair.setCcg("(NP\\NP)");
                    if (ccgWordPair.getToCcg() == null) {
                        ccgWordPair.setToCcg("NP");
                    }
                    break;
                case "PUNCT":
                    if (ccgWordPair.getToCcg() != null) {
                        if (ccgWordPair.isToRoot()) {
                            ccgWordPair.setCcg("(S" + ccgWordPair.findSlash() + "S)");
                        } else {
                            ccgWordPair.setCcg("(" + ccgWordPair.getToCcg() + ccgWordPair.findSlash() + ccgWordPair.getToCcg() + ")");
                        }
                    } else {
                        if (ccgWordPair.getCcg() == null) {
                            words.add(ccgWordPair);
                        }
                    }
                    break;
                case "ADVCL":
                case "ADVMOD":
                case "COMPOUND":
                case "REPARANDUM":
                case "CC":
                    if (ccgWordPair.getToCcg() != null) {
                        ccgWordPair.setCcg("(" + ccgWordPair.getToCcg() + "/" + ccgWordPair.getToCcg() + ")");
                    } else {
                        if (ccgWordPair.getCcg() == null) {
                            words.add(ccgWordPair);
                        }
                    }
                    break;
                case "AUX":
                case "LIST":
                case "GOESWITH":
                case "FIXED":
                case "CONJ":
                    if (ccgWordPair.getToCcg() != null) {
                        ccgWordPair.setCcg("(" + ccgWordPair.getToCcg() + "\\" + ccgWordPair.getToCcg() + ")");
                    } else {
                        if (ccgWordPair.getCcg() == null) {
                            words.add(ccgWordPair);
                        }
                    }
                    break;
                case "FLAT":
                    if (ccgWordPair.getToCcg() != null) {
                        ccgWordPair.setCcg(ccgWordPair.getToCcg());
                    }
                    break;
                case "VOCATIVE":
                    ccgWordPair.setCcg("(S" + ccgWordPair.findSlash() + "S)");
                    if (ccgWordPair.getToCcg() == null) {
                        ccgWordPair.setToCcg("S");
                    }
                    break;
                case "CASE":
                    ccgWordPair.setCcg("(NP\\NP)");
                    break;
                case "DISLOCATED":
                    ccgWordPair.setCcg("(NP" + ccgWordPair.findSlash() + "S)");
                    if (ccgWordPair.getToCcg() == null) {
                        ccgWordPair.setToCcg("S");
                    }
                    break;
                case "DEP":
                    ccgWordPair.setCcg("(NP/S)");
                    if (ccgWordPair.getToCcg() == null) {
                        ccgWordPair.setToCcg("S");
                    }
                    break;
                default:
                    break;
            }
        }
        return generateSentence(words);
    }

    public static void generate(AnnotatedSentence sentence) {
        for (int i = 0; i < sentence.getWords().size(); i++) {
            ((AnnotatedWord) sentence.getWord(i)).setCcg(null);
        }
        ArrayList<CCGWordPair> words = setWords(sentence);
        int lastSize = words.size();
        do {
            words = setSentence(words);
            if (lastSize == words.size()) {
                System.out.println(sentence.getFileName() + " not done.");
                break;
            }
            lastSize = words.size();
        } while (!words.isEmpty());
        if (words.isEmpty()) {
            for (int i = 0; i < sentence.getWords().size(); i++) {
                AnnotatedWord word = ((AnnotatedWord) sentence.getWord(i));
                if (word.getUniversalDependency().to() != 0) {
                    AnnotatedWord toWord = ((AnnotatedWord) sentence.getWord(word.getUniversalDependency().to() - 1));
                    if (!toWord.getUniversalDependency().toString().equals("ROOT") && (word.getUniversalDependency().toString().equals("OBJ") || word.getUniversalDependency().toString().equals("OBL") || word.getUniversalDependency().toString().contains("SUBJ") || word.getUniversalDependency().toString().endsWith("COMP"))) {
                        if (toWord.getCcg() == null) {
                            toWord.setCcg("(S\\NP)");
                        } else {
                            toWord.setCcg("(" + toWord.getCcg() + "\\NP)");
                        }
                    }
                }
            }
        }
    }
}
