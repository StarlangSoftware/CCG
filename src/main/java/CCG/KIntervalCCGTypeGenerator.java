package CCG;

import AnnotatedSentence.*;
import Cookies.Tuple.Pair;

import java.util.*;

public class KIntervalCCGTypeGenerator extends CCGTypeGenerator {

    private static Pair<Integer, Boolean> findIndex(ArrayList<CCGWord> words, int k, int current) {
        int totalIndex = current, i = current;
        while (totalIndex < current + k + 1) {
            if (i == words.size()) {
                i = 0;
            }
            CCGWord word = words.get(i);
            if (i + 1 < words.size()) {
                CCGWord nextWord = words.get(i + 1);
                if (word.getType() != null && word.getType().equals(Type.FORWARD)) {
                    if (nextWord.size() > 1) {
                        if (word.getCCG().equals(nextWord.getFirstCCG())) {
                            return new Pair<>(i, false);
                        }
                        if (word.getCCG().equals(nextWord.toString())) {
                            return new Pair<>(i, false);
                        }
                    } else {
                        if (word.getCCG().equals(nextWord.getCCG())) {
                            return new Pair<>(i, false);
                        }
                    }
                }
                if (nextWord.getType() != null && nextWord.getType().equals(Type.BACKWARD)) {
                    if (word.size() > 1) {
                        if (nextWord.getCCG().equals(word.getFirstCCG())) {
                            return new Pair<>(i + 1, false);
                        }
                        if (nextWord.getCCG().equals(word.toString())) {
                            return new Pair<>(i + 1, false);
                        }
                    } else {
                        if (nextWord.getCCG().equals(word.getCCG())) {
                            return new Pair<>(i + 1, false);
                        }
                    }
                }
            }
            i++;
            totalIndex++;
        }
        return extraposition(words, k, current);
    }

    private static Pair<Integer, Boolean> extraposition(ArrayList<CCGWord> words, int k, int current) {
        int totalIndex = current, i = current;
        while (totalIndex < current + k + 1) {
            if (i == words.size()) {
                i = 0;
            }
            CCGWord word = words.get(i);
            if (word.getUniversalDependency().endsWith("SUBJ") || word.getUniversalDependency().endsWith("OBJ") || word.getUniversalDependency().endsWith("COMP") || word.getUniversalDependency().equals("OBL")) {
                CCGWord newWord = new CCGWord("S\\(S\\" + word + ")", word.getUniversalDependency());
                if (i > 0) {
                    if (words.get(i - 1).size() > 1) {
                        if (newWord.getCCG().equals(words.get(i - 1).getFirstCCG())) {
                            words.set(i, newWord);
                            return new Pair<>(i, true);
                        }
                        if (newWord.getCCG().equals(words.get(i - 1).toString())) {
                            words.set(i, newWord);
                            return new Pair<>(i, true);
                        }
                    } else {
                        if (newWord.getCCG().equals(words.get(i - 1).getCCG())) {
                            words.set(i, newWord);
                            return new Pair<>(i, true);
                        }
                    }
                }
            }
            i++;
            totalIndex++;
        }
        return new Pair<>(-1, false);
    }

    private static void merge(ArrayList<CCGWord> words, Pair<Integer, Boolean> pair, ArrayList<Type> types) {
        int index = pair.getKey();
        boolean isExtraPosition = pair.getValue();
        CCGWord word = words.get(index);
        int removeIndex = index;
        if (word.getType().equals(Type.FORWARD)) {
            removeIndex++;
        } else {
            removeIndex--;
        }
        if (removeIndex > index) {
            if (words.get(index + 1).size() > 1) {
                if (word.getCCG().equals(words.get(index + 1).getFirstCCG())) {
                    // forward composition
                    if (word.composition(words.get(index + 1))) {
                        types.add(Type.FORWARD_CROSSED_COMPOSITION);
                    } else {
                        types.add(Type.FORWARD_COMPOSITION);
                    }
                } else if (word.getCCG().equals(words.get(index + 1).toString())) {
                    // forward application
                    types.add(Type.FORWARD);
                    word.application(words.get(index + 1).getUniversalDependency());
                }
            } else if (word.getCCG().equals(words.get(index + 1).getCCG())) {
                // forward application
                types.add(Type.FORWARD);
                word.application(words.get(index + 1).getUniversalDependency());
            }
        } else {
            if (words.get(index - 1).size() > 1) {
                if (word.getCCG().equals(words.get(index - 1).getFirstCCG())) {
                    // backward composition
                    if (word.composition(words.get(index - 1))) {
                        if (isExtraPosition) {
                            types.add(Type.EXTRA_POSITION);
                            types.add(Type.BACKWARD_CROSSED_COMPOSITION);
                        } else {
                            types.add(Type.BACKWARD_CROSSED_COMPOSITION);
                        }
                    } else {
                        if (isExtraPosition) {
                            types.add(Type.EXTRA_POSITION);
                            types.add(Type.BACKWARD_COMPOSITION);
                        } else {
                            types.add(Type.BACKWARD_COMPOSITION);
                        }
                    }
                } else if (word.getCCG().equals(words.get(index - 1).toString())) {
                    // backward application
                    if (isExtraPosition) {
                        types.add(Type.EXTRA_POSITION);
                        types.add(Type.BACKWARD);
                    } else {
                        if (words.get(index - 1).getUniversalDependency().endsWith("SUBJ")) {
                            types.add(Type.TYPE_RAISING);
                            types.add(Type.FORWARD);
                        } else {
                            types.add(Type.BACKWARD);
                        }
                    }
                    word.application(words.get(index - 1).getUniversalDependency());
                }
            } else if (word.getCCG().equals(words.get(index - 1).getCCG())) {
                // backward application
                if (isExtraPosition) {
                    types.add(Type.EXTRA_POSITION);
                    types.add(Type.BACKWARD);
                } else {
                    if (words.get(index - 1).getUniversalDependency().endsWith("SUBJ")) {
                        types.add(Type.TYPE_RAISING);
                        types.add(Type.FORWARD);
                    } else {
                        types.add(Type.BACKWARD);
                    }
                }
                word.application(words.get(index - 1).getUniversalDependency());
            }
        }
        words.remove(removeIndex);
    }

    private static ArrayList<Type> generateTypes(ArrayList<CCGWord> words, int k) {
        ArrayList<Type> types = new ArrayList<>();
        int current = 0;
        while (words.size() > 1) {
            Pair<Integer, Boolean> pair = findIndex(words, k, current);
            if (pair.getKey() == -1) {
                return null;
            }
            Type type = words.get(pair.getKey()).getType();
            merge(words, pair, types);
            current = pair.getKey();
            if (type.equals(Type.BACKWARD)) {
                current--;
            }
            if (current >= words.size()) {
                current = 0;
            }
        }
        if (!words.get(0).getCCG().equals("S")) {
            return null;
        }
        return types;
    }

    public static ArrayList<Type> generate(AnnotatedSentence sentence, int k) throws WrongCCGException {
        if (checkCCGs(sentence)) {
            throw new WrongCCGException();
        }
        ArrayList<Type> allTypes = new ArrayList<>();
        ArrayList<AnnotatedSentence> sentences = splitSentence(sentence);
        for (AnnotatedSentence annotatedSentence : sentences) {
            ArrayList<CCGWord> words = constructCCGWord(annotatedSentence);
            if (!words.isEmpty()) {
                ArrayList<Type> types = generateTypes(words, k);
                if (types != null) {
                    allTypes.addAll(types);
                } else {
                    throw new WrongCCGException();
                }
            }
        }
        return allTypes;
    }
}
