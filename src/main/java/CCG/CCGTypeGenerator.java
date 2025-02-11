package CCG;

import AnnotatedSentence.*;
import Cookies.Set.DisjointSet;
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
                words.add(new CCGWord(word.getCcg(), word.getUniversalDependency().toString()));
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
                    }
                }
            }
        }
        return -1;
    }

    private static Pair<Pair<Integer, Integer>, CCGWord> generateCCGTypes(ArrayList<CCGWord> words, int start, ArrayList<Type> types, boolean isExtraPosition) throws WrongCCGException {
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
                            word.application(words.get(j + 1).getUniversalDependency());
                            j++;
                        }
                    } else if (word.getCCG().equals(words.get(j + 1).getCCG())) {
                        // forward application
                        types.add(Type.FORWARD);
                        word.application(words.get(j + 1).getUniversalDependency());
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
                                if (isExtraPosition) {
                                    types.add(Type.EXTRA_POSITION_BACKWARD_CROSSED_COMPOSITION);
                                } else {
                                    types.add(Type.BACKWARD_CROSSED_COMPOSITION);
                                }
                            } else {
                                if (isExtraPosition) {
                                    types.add(Type.EXTRA_POSITION_BACKWARD_COMPOSITION);
                                } else {
                                    types.add(Type.BACKWARD_COMPOSITION);
                                }
                            }
                            i--;
                        } else if (word.getCCG().equals(words.get(i - 1).toString())) {
                            // backward application
                            if (isExtraPosition) {
                                types.add(Type.EXTRA_POSITION_BACKWARD);
                            } else {
                                if (words.get(i - 1).getUniversalDependency().endsWith("SUBJ")) {
                                    types.add(Type.TYPE_RAISING_FORWARD);
                                } else {
                                    types.add(Type.BACKWARD);
                                }
                            }
                            word.application(words.get(i - 1).getUniversalDependency());
                            i--;
                        }
                    } else if (word.getCCG().equals(words.get(i - 1).getCCG())) {
                        // backward application
                        if (isExtraPosition) {
                            types.add(Type.EXTRA_POSITION_BACKWARD);
                        } else {
                            if (words.get(i - 1).getUniversalDependency().endsWith("SUBJ")) {
                                types.add(Type.TYPE_RAISING_FORWARD);
                            } else {
                                types.add(Type.BACKWARD);
                            }
                        }
                        word.application(words.get(i - 1).getUniversalDependency());
                        i--;
                    }
                    if (startI == i) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return new Pair<>(new Pair<>(i, j), word);
    }

    private static ArrayList<AnnotatedSentence> splitSentence(AnnotatedSentence sentence) {
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

    private static int extraPosition(ArrayList<CCGWord> words) {
        for (int i = 1; i < words.size(); i++) {
            CCGWord word = words.get(i);
            if (word.getUniversalDependency().endsWith("SUBJ") || word.getUniversalDependency().endsWith("OBJ") || word.getUniversalDependency().endsWith("COMP") || word.getUniversalDependency().equals("OBL")) {
                String newCCG = "S\\(S\\" + word + ")";
                CCGWord newWord = new CCGWord(newCCG, word.getUniversalDependency());
                if (words.get(i - 1).size() > 1) {
                    if (newWord.getCCG().equals(words.get(i - 1).getFirstCCG())) {
                        word.splitCCG(newCCG);
                        return i;
                    }
                    if (newWord.getCCG().equals(words.get(i - 1).toString())) {
                        word.splitCCG(newCCG);
                        return i;
                    }
                } else {
                    if (newWord.getCCG().equals(words.get(i - 1).getCCG())) {
                        word.splitCCG(newCCG);
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public static ArrayList<Type> generate(AnnotatedSentence sentence) throws WrongCCGException {
        ArrayList<Type> types = new ArrayList<>();
        ArrayList<AnnotatedSentence> sentences = splitSentence(sentence);
        StringBuilder sb = new StringBuilder();
        for (AnnotatedSentence annotatedSentence : sentences) {
            boolean isExtraPosition = false;
            ArrayList<CCGWord> words = constructCCGWord(annotatedSentence);
            while (words.size() > 1) {
                int startIndex = findIndex(words);
                if (startIndex == -1) {
                    startIndex = extraPosition(words);
                    if (startIndex == -1) {
                        if (words.size() > 1 || !words.get(0).getCCG().equals("S")) {
                            throw new WrongCCGException();
                        }
                        break;
                    }
                    isExtraPosition = true;
                }
                Pair<Pair<Integer, Integer>, CCGWord> p = generateCCGTypes(words, startIndex, types, isExtraPosition);
                deleteWords(p, words);
            }
            // last CCGs
            if (words.get(0).getCCG().equals("S")) {
                sb.append(words.get(0).getCCG()).append(" ");
            } else {
                throw new WrongCCGException();
            }
        }
        System.out.println(sb);
        return types;
    }
}
