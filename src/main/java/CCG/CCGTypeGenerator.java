package CCG;

import AnnotatedSentence.*;
import Cookies.Tuple.Pair;

import java.util.ArrayList;

public class CCGTypeGenerator {

    private static void deleteWords(Pair<Pair<Integer, Integer>, CCGWord> p, ArrayList<CCGWord> words) {
        if (p.getKey().getValue() >= p.getKey().getKey()) {
            words.subList(p.getKey().getKey(), p.getKey().getValue() + 1).clear();
        }
        words.add(p.getKey().getKey(), p.getValue());
    }

    private static ArrayList<CCGWord> constructCCGWord(AnnotatedSentence sentence) {
        ArrayList<CCGWord> words = new ArrayList<>();
        for (int i = 0; i < sentence.wordCount(); i++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (!word.isPunctuation()) {
                words.add(new CCGWord(word.getCcg()));
            }
        }
        return words;
    }

    private static int findIndex(ArrayList<CCGWord> words) throws WrongCCGException {
        for (int i = 0; i < words.size(); i++) {
            CCGWord word = words.get(i);
            if (word.size() > 1) {
                if (word.getType().equals(Type.FORWARD)) {
                    if (i + 1 < words.size()) {
                        if (words.get(i + 1).size() > 1) {
                            if (word.getCCG().equals(words.get(i + 1).getFirstCCG())) {
                                return i;
                            }
                            if (word.getCCG().equals(words.get(i + 1).toString())) {
                                return i;
                            }
                        } else {
                            if (word.getCCG().equals(words.get(i + 1).getCCG())) {
                                return i;
                            }
                        }
                    } else {
                        throw new WrongCCGException();
                    }
                } else {
                    if (i - 1 >= 0) {
                        if (words.get(i - 1).size() > 1) {
                            if (word.getCCG().equals(words.get(i - 1).getFirstCCG())) {
                                return i;
                            }
                            if (word.getCCG().equals(words.get(i - 1).toString())) {
                                return i;
                            }
                        } else {
                            if (word.getCCG().equals(words.get(i - 1).getCCG())) {
                                return i;
                            }
                        }
                    } else {
                        throw new WrongCCGException();
                    }
                }
            }
        }
        return -1;
    }

    private static Pair<Pair<Integer, Integer>, CCGWord> generateCCGTypes(ArrayList<CCGWord> words, int start, ArrayList<Type> types) throws WrongCCGException {
        int i = start, j = start;
        CCGWord word = words.get(start);
        while (word.size() > 1) {
            if (word.getType().equals(Type.FORWARD)) {
                if (j + 1 < words.size()) {
                    int startJ = j;
                    if (words.get(j + 1).size() > 1) {
                        if (word.getCCG().equals(words.get(j + 1).getFirstCCG())) {
                            // forward composition
                            if (word.composition(words.get(j + 1))) {
                                types.add(Type.FORWARD_CROSSED_COMPOSITION);
                            } else {
                                types.add(Type.FORWARD_COMPOSITION);
                            }
                            j++;
                        } else if (word.getCCG().equals(words.get(j + 1).toString())) {
                            // forward application
                            types.add(Type.FORWARD);
                            word.application();
                            j++;
                        }
                    } else if (word.getCCG().equals(words.get(j + 1).getCCG())) {
                        // forward application
                        types.add(Type.FORWARD);
                        word.application();
                        j++;
                    }
                    if (startJ == j) {
                        break;
                    }
                } else {
                    throw new WrongCCGException();
                }
            } else {
                if (i - 1 >= 0) {
                    int startI = i;
                    if (words.get(i - 1).size() > 1) {
                        if (word.getCCG().equals(words.get(i - 1).getFirstCCG())) {
                            // backward composition
                            if (word.composition(words.get(i - 1))) {
                                types.add(Type.BACKWARD_CROSSED_COMPOSITION);
                            } else {
                                types.add(Type.BACKWARD_COMPOSITION);
                            }
                            i--;
                        } else if (word.getCCG().equals(words.get(i - 1).toString())) {
                            // backward application
                            types.add(Type.BACKWARD);
                            word.application();
                            i--;
                        }
                    } else if (word.getCCG().equals(words.get(i - 1).getCCG())) {
                        // backward application
                        types.add(Type.BACKWARD);
                        word.application();
                        i--;
                    }
                    if (startI == i) {
                        break;
                    }
                } else {
                    throw new WrongCCGException();
                }
            }
        }
        return new Pair<>(new Pair<>(i, j), word);
    }

    private static boolean condition(ArrayList<CCGWord> words, int sentenceCount) {
        int count = 0;
        for (CCGWord word : words) {
            if (word.size() == 1 && word.getCCG().equals("S")) {
                count++;
            } else {
                return true;
            }
        }
        return count != sentenceCount;
    }

    private static int sentenceCount(AnnotatedSentence sentence) {
        int count = 1;
        for (int i = 0; i < sentence.wordCount(); i++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (word.getUniversalDependency().toString().equals("PARATAXIS")) {
                count++;
            }
        }
        return count;
    }

    private static void printLastCCGs(ArrayList<CCGWord> words) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            sb.append(words.get(i).getCCG());
            if (i + 1 != words.size()) {
                sb.append(",");
            }
        }
        System.out.println(sb);
    }

    public static ArrayList<Type> generate(AnnotatedSentence sentence) throws WrongCCGException {
        ArrayList<Type> types = new ArrayList<>();
        ArrayList<CCGWord> words = constructCCGWord(sentence);
        int sentenceCount = sentenceCount(sentence);
        while (condition(words, sentenceCount)) {
            int startIndex = findIndex(words);
            if (startIndex == -1) {
                if (condition(words, sentenceCount)) {
                    throw new WrongCCGException();
                }
                // last CCGs
                printLastCCGs(words);
                return types;
            }
            Pair<Pair<Integer, Integer>, CCGWord> p = generateCCGTypes(words, startIndex, types);
            deleteWords(p, words);
        }
        // last CCGs
        printLastCCGs(words);
        return types;
    }
}
