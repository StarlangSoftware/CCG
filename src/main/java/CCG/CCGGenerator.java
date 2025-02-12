package CCG;

import AnnotatedSentence.*;

import java.util.*;

public class CCGGenerator {

    private static ArrayList<CCGWordPair> setWords(AnnotatedSentence sentence, ArrayList<HashMap<Integer, Integer>> mapList) {
        HashMap<Integer, Integer> map = new HashMap<>();
        ArrayList<CCGWordPair> words = new ArrayList<>();
        for (int i = 0; i < sentence.wordCount(); i++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (word.getUniversalDependency().to() != 0 && !word.getUniversalDependency().toString().equals("PARATAXIS")) {
                CCGWordPair ccgWordPair = new CCGWordPair(word, (AnnotatedWord) sentence.getWord(word.getUniversalDependency().to() - 1), i);
                int toIndex = word.getUniversalDependency().to() - 1;
                if (ccgWordPair.isToRoot() || ccgWordPair.getToUniversalDependency().equals("PARATAXIS") || ccgWordPair.getToUniversalDependency().endsWith("COMP")) {
                    if (!map.containsKey(toIndex)) {
                        map.put(toIndex, 0);
                    }
                    if (ccgWordPair.getUniversalDependency().equals("IOBJ") || ccgWordPair.getUniversalDependency().equals("OBJ") || ccgWordPair.getUniversalDependency().equals("OBL") || ccgWordPair.getUniversalDependency().equals("NSUBJ") || ccgWordPair.getUniversalDependency().equals("CSUBJ") || ccgWordPair.getUniversalDependency().endsWith("COMP")) {
                        map.put(toIndex, map.get(toIndex) + 1);
                    }
                }
                words.add(ccgWordPair);
            } else {
                if (!map.containsKey(i)) {
                    map.put(i, 0);
                }
            }
            mapList.add((HashMap<Integer, Integer>) map.clone());
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
            AnnotatedWord word = ((AnnotatedWord) sentence.getWord(key));
            if (word.getUniversalDependency().toString().endsWith("COMP")) {
                word.setCcg(setRoot(map.get(key), "NP"));
            } else {
                word.setCcg(setRoot(map.get(key), "S"));
            }
        }
    }

    private static String removeNPs(int count, String ccg) {
        if (count == 0) {
            return ccg;
        }
        return removeNPs(count - 1, ccg.substring(1, ccg.length() - 4));
    }

    private static ArrayList<CCGWordPair> setSentence(ArrayList<CCGWordPair> sentence, ArrayList<HashMap<Integer, Integer>> mapList) {
        ArrayList<CCGWordPair> words = new ArrayList<>();
        for (CCGWordPair ccgWordPair : sentence) {
            switch (ccgWordPair.getUniversalDependency()) {
                case "NSUBJ":
                case "CSUBJ":
                case "OBL":
                case "OBJ":
                case "IOBJ":
                    ccgWordPair.setCcg("NP");
                    break;
                case "XCOMP":
                case "CCOMP":
                    if (ccgWordPair.getCcg() == null) {
                        ccgWordPair.setCcg("NP");
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
                case "DET":
                case "ACL":
                case "AMOD":
                case "NMOD":
                case "NUMMOD":
                    if (ccgWordPair.getToCcg() != null) {
                        if (mapList.get(ccgWordPair.no()).containsKey(ccgWordPair.to() - 1)) {
                            String toCCG = removeNPs(Math.abs(mapList.get(ccgWordPair.to() - 1).getOrDefault(ccgWordPair.to() - 1, 0) - mapList.get(ccgWordPair.no()).get(ccgWordPair.to() - 1)), ccgWordPair.getToCcg());
                            ccgWordPair.setCcg("(" + toCCG + "/" + toCCG + ")");
                        } else {
                            ccgWordPair.setCcg("(" + ccgWordPair.getToCcg() + "/" + ccgWordPair.getToCcg() + ")");
                        }
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
                    } else {
                        words.add(ccgWordPair);
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
        ArrayList<HashMap<Integer, Integer>> mapList = new ArrayList<>();
        ArrayList<CCGWordPair> words = setWords(sentence, mapList);
        int lastSize = words.size();
        do {
            words = setSentence(words, mapList);
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
                    if ((!toWord.getUniversalDependency().toString().endsWith("COMP") && !toWord.getUniversalDependency().toString().equals("PARATAXIS") && !toWord.getUniversalDependency().toString().equals("ROOT")) && (word.getUniversalDependency().toString().equals("IOBJ") || word.getUniversalDependency().toString().equals("OBJ") || word.getUniversalDependency().toString().equals("OBL") || word.getUniversalDependency().toString().contains("SUBJ") || word.getUniversalDependency().toString().endsWith("COMP"))) {
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
