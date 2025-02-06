package CCG;

import AnnotatedSentence.*;

import java.util.*;

public class CCGGenerator {

    private static ArrayList<CCGWordPair> setWords(AnnotatedSentence sentence) {
        HashMap<Integer, ArrayList<String>> map = new HashMap<>();
        ArrayList<CCGWordPair> words = new ArrayList<>();
        for (int i = 0; i < sentence.wordCount(); i++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (word.getUniversalDependency().to() != 0 && !word.getUniversalDependency().toString().equals("PARATAXIS") && !word.getUniversalDependency().toString().endsWith("COMP")) {
                CCGWordPair ccgWordPair = new CCGWordPair(word, (AnnotatedWord) sentence.getWord(word.getUniversalDependency().to() - 1), i);
                int toIndex = word.getUniversalDependency().to() - 1;
                AnnotatedWord toWord = (AnnotatedWord) sentence.getWord(toIndex);
                if (ccgWordPair.isToRoot() || ccgWordPair.getToUniversalDependency().equals("PARATAXIS")) {
                    if (!map.containsKey(toIndex)) {
                        map.put(toIndex, new ArrayList<>());
                    }
                    map.get(toIndex).add(ccgWordPair.getUniversalDependency());
                } else if ((word.getUniversalDependency().toString().equals("OBJ") || word.getUniversalDependency().toString().equals("OBL") || word.getUniversalDependency().toString().contains("SUBJ")) && toWord.getUniversalDependency().toString().endsWith("COMP")) {
                    if (word.getCcg() == null) {
                        toWord.setCcg("(S/NP)");
                    } else {
                        toWord.setCcg("(" + toWord.getCcg() + "/NP)");
                    }
                }
                words.add(ccgWordPair);
            } else {
                if (word.getCcg() == null) {
                    word.setCcg("S");
                }
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

    private static void setRoots(HashMap<Integer, ArrayList<String>> map, AnnotatedSentence sentence) {
        for (Integer key : map.keySet()) {
            int npCount = 0;
            for (int i = 0; i < map.get(key).size(); i++) {
                if (map.get(key).get(i).equals("OBJ") || map.get(key).get(i).equals("OBL") || map.get(key).get(i).equals("NSUBJ") || map.get(key).get(i).equals("CSUBJ")) {
                    npCount++;
                }
            }
            if (npCount > 0) {
                ((AnnotatedWord) sentence.getWord(key)).setCcg(setRoot(npCount, "S"));
            }
        }
    }

    private static ArrayList<CCGWordPair> setSentence(ArrayList<CCGWordPair> sentence) {
        String toCCG;
        ArrayList<CCGWordPair> words = new ArrayList<>();
        for (CCGWordPair ccgWordPair : sentence) {
            switch (ccgWordPair.getUniversalDependency()) {
                case "NSUBJ":
                case "CSUBJ":
                case "OBL":
                case "OBJ":
                    ccgWordPair.setCcg("NP");
                    break;
                case "DET":
                case "ACL":
                case "AMOD":
                case "NMOD":
                    if (ccgWordPair.getToCcg() != null) {
                        ccgWordPair.setCcg("(" + ccgWordPair.getToCcg() + "/" + ccgWordPair.getToCcg() + ")");
                    } else {
                        ccgWordPair.setCcg("(NP/NP)");
                        toCCG = ccgWordPair.getToCcg();
                        if (toCCG == null) {
                            ccgWordPair.setToCcg("NP");
                        }
                    }
                    break;
                case "NUMMOD":
                    if (ccgWordPair.isToRoot() && (ccgWordPair.getToWord().getUniversalDependencyPos().equals("NOUN") || ccgWordPair.getToWord().getUniversalDependencyPos().equals("NUM"))) {
                        ccgWordPair.setCcg("(" + ccgWordPair.getToCcg() + "/" + ccgWordPair.getToCcg() + ")");
                    } else {
                        ccgWordPair.setCcg("(NP/NP)");
                        toCCG = ccgWordPair.getToCcg();
                        if (toCCG == null) {
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
                    toCCG = ccgWordPair.getToCcg();
                    if (toCCG == null) {
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
                case "CC":
                    ccgWordPair.setCcg("(" + ccgWordPair.getToCcg() + "/" + ccgWordPair.getToCcg() + ")");
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
    }
}
