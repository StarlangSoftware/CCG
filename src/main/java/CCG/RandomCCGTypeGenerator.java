package CCG;

import AnnotatedSentence.*;
import Cookies.Tuple.Pair;

import java.util.*;

public class RandomCCGTypeGenerator extends CCGTypeGenerator {

    /**
     * Finds all possibilities for the current state.
     * @param words current words.
     * @return all possible word indexes that can merge.
     */
    private static ArrayList<Pair<Integer, String>> findIndex(ArrayList<CCGWord> words) throws WrongCCGException {
        ArrayList<Pair<Integer, String>> indexes = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            CCGWord word = words.get(i);
            if (word.size() > 1) {
                if (word.getType().equals(Type.FORWARD)) {
                    if (i + 1 < words.size()) {
                        if (words.get(i + 1).size() > 1) {
                            // checks for forward composition and forward
                            if (word.getCCG().equals(words.get(i + 1).getFirstCCG()) || word.getCCG().equals(words.get(i + 1).toString())) {
                                indexes.add(new Pair<>(i, null));
                            }
                        } else {
                            // checks for forward
                            if (word.getCCG().equals(words.get(i + 1).getCCG())) {
                                indexes.add(new Pair<>(i, null));
                            }
                        }
                    } else {
                        throw new WrongCCGException();
                    }
                } else {
                    if (i - 1 >= 0) {
                        if (words.get(i - 1).size() > 1) {
                            // checks for backward composition and backward
                            if (word.getCCG().equals(words.get(i - 1).getFirstCCG()) || word.getCCG().equals(words.get(i - 1).toString())) {
                                indexes.add(new Pair<>(i, null));
                            }
                        } else {
                            // checks for backward
                            if (word.getCCG().equals(words.get(i - 1).getCCG())) {
                                indexes.add(new Pair<>(i, null));
                            }
                        }
                    }
                }
            }
        }
        return indexes;
    }

    /**
     *  merges two {@link CCGWord}s.
     * @param words current words.
     * @param i index of the {@link CCGWord}.
     * @param types for current possibility.
     * @param isExtraPosition is the current merge done with extraposition.
     * @return the index of the word to be deleted.
     */
    private static int generateCCGTypes(ArrayList<CCGWord> words, int i, ArrayList<Type> types, boolean isExtraPosition) {
        CCGWord word = words.get(i);
        int removeIndex = i;
        if (word.getType().equals(Type.FORWARD)) {
            removeIndex++;
        } else {
            removeIndex--;
        }
        if (removeIndex > i) {
            if (words.get(i + 1).size() > 1) {
                if (word.getCCG().equals(words.get(i + 1).getFirstCCG())) {
                    // forward composition
                    if (word.composition(words.get(i + 1))) {
                        types.add(Type.FORWARD_CROSSED_COMPOSITION);
                    } else {
                        types.add(Type.FORWARD_COMPOSITION);
                    }
                    return removeIndex;
                } else if (word.getCCG().equals(words.get(i + 1).toString())) {
                    // forward application
                    types.add(Type.FORWARD);
                    word.application(words.get(i + 1).getUniversalDependency());
                    return removeIndex;
                }
            } else if (word.getCCG().equals(words.get(i + 1).getCCG())) {
                // forward application
                types.add(Type.FORWARD);
                word.application(words.get(i + 1).getUniversalDependency());
                return removeIndex;
            }
        } else {
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
                    return removeIndex;
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
                    return removeIndex;
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
                return removeIndex;
            }
        }
        return -1;
    }

    /**
     * Finds all possibilities for the current state with extrapositions.
     * @param words current words.
     * @return all possible word indexes that can merge and updates extrapositioned {@link CCGWord}s.
     */
    private static ArrayList<Pair<Integer, String>> extraPosition(ArrayList<CCGWord> words) {
        ArrayList<Pair<Integer, String>> positions = new ArrayList<>();
        for (int i = 1; i < words.size(); i++) {
            CCGWord word = words.get(i);
            if (word.getUniversalDependency().endsWith("SUBJ") || word.getUniversalDependency().endsWith("OBJ") || word.getUniversalDependency().endsWith("COMP") || word.getUniversalDependency().equals("OBL")) {
                String newCCG = "S\\(S\\" + word + ")";
                CCGWord newWord = new CCGWord(newCCG, word.getUniversalDependency());
                if (words.get(i - 1).size() > 1) {
                    if (newWord.getCCG().equals(words.get(i - 1).getFirstCCG())) {
                        positions.add(new Pair<>(i, newCCG));
                    }
                    if (newWord.getCCG().equals(words.get(i - 1).toString())) {
                        positions.add(new Pair<>(i, newCCG));
                    }
                } else {
                    if (newWord.getCCG().equals(words.get(i - 1).getCCG())) {
                        positions.add(new Pair<>(i, newCCG));
                    }
                }
            }
        }
        return positions;
    }

    /**
     *  constructs all the candidates that can merge.
     * @param words current words.
     * @return {@link ArrayList} of candidates.
     */
    private static Pair<ArrayList<Pair<Integer, String>>, Boolean> constructCandidates(ArrayList<CCGWord> words) throws WrongCCGException {
        ArrayList<Pair<Integer, String>> candidates = new ArrayList<>(findIndex(words));
        if (candidates.isEmpty()) {
            candidates.addAll(extraPosition(words));
            return new Pair<>(candidates, true);
        }
        return new Pair<>(candidates, false);
    }

    /**
     *  backtracks all the possibilities and finds {@link Type}s.
     * @param words current words for a possibility.
     * @param types current types for a possibility.
     * @param visited all visited states for all the possibilities.
     * @return {@link ArrayList} of {@link Type}s.
     */
    private static ArrayList<Type> backtrack(ArrayList<CCGWord> words, ArrayList<Type> types, HashSet<String> visited) throws WrongCCGException {
        if (visited.contains(words.toString())) {
            return null;
        }
        visited.add(words.toString());
        if (words.size() == 1 && words.get(0).getCCG().equals("S")) {
            //System.out.println("S");
            return types;
        } else {
            Pair<ArrayList<Pair<Integer, String>>, Boolean> pair = constructCandidates(words);
            for (int i = 0; i < pair.getKey().size(); i++) {
                Pair<Integer, String> current = pair.getKey().get(i);
                ArrayList<CCGWord> cloneWords = new ArrayList<>();
                for (CCGWord word : words) {
                    cloneWords.add(word.clone());
                }
                ArrayList<Type> typesClone = (ArrayList<Type>) types.clone();
                if (current.getValue() != null) {
                    words.get(current.getKey()).splitCCG(current.getValue());
                }
                int index = generateCCGTypes(words, current.getKey(), types, pair.getValue());
                words.remove(index);
                ArrayList<Type> backtrack = backtrack(words, types, visited);
                if (backtrack != null) {
                    return backtrack;
                }
                words = cloneWords;
                types = typesClone;
            }
        }
        return null;
    }

    /**
     *  generates valid {@link Type}s for an {@link AnnotatedSentence}.
     * @param sentence an {@link AnnotatedSentence}.
     * @return {@link ArrayList} of {@link Type}s.
     */
    public static ArrayList<Type> generate(AnnotatedSentence sentence) throws WrongCCGException {
        if (checkCCGs(sentence)) {
            throw new WrongCCGException();
        }
        ArrayList<Type> types = new ArrayList<>();
        // splits sentence to a sub sentence list
        ArrayList<AnnotatedSentence> sentences = splitSentence(sentence);
        for (AnnotatedSentence annotatedSentence : sentences) {
            ArrayList<CCGWord> words = constructCCGWord(annotatedSentence);
            ArrayList<Type> currentTypes = backtrack(words, new ArrayList<>(), new HashSet<>());
            if (currentTypes != null) {
                types.addAll(currentTypes);
            } else {
                throw new WrongCCGException();
            }
        }
        return types;
    }
}
