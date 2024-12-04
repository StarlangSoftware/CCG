import AnnotatedSentence.*;
import CCG.CCGWordPair;
import Cookies.Set.DisjointSet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SentenceInfoMain {

    private static String calculateWordOrder(AnnotatedSentence sentence) {
        StringBuilder wordOrder = new StringBuilder();
        for (int j = 0; j < sentence.wordCount(); j++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(j);
            if (word.getUniversalDependency().toString().equals("ROOT")) {
                wordOrder.append("V");
            } else {
                CCGWordPair ccgWordPair = new CCGWordPair(word, (AnnotatedWord) sentence.getWord(word.getUniversalDependency().to() - 1), j);
                if (ccgWordPair.isToRoot() && (ccgWordPair.getUniversalDependency().equals("NSUBJ") || ccgWordPair.getUniversalDependency().equals("CSUBJ"))) {
                    wordOrder.append("S");
                } else if (ccgWordPair.isToRoot() && (ccgWordPair.getUniversalDependency().equals("CCOMP") || ccgWordPair.getUniversalDependency().equals("OBJ") || ccgWordPair.getUniversalDependency().equals("XCOMP"))) {
                    wordOrder.append("O");
                }
            }
        }
        String wordOrderString = wordOrder.toString();
        if (wordOrderString.contains("V") && !wordOrderString.equals("V")) {
            return wordOrderString;
        }
        return "-";
    }

    private static int dependencyLength(int i, int j, AnnotatedSentence sentence) {
        int count = 1;
        if (i > j) {
            for (int k = j + 1; k < i; k++) {
                AnnotatedWord word = (AnnotatedWord) sentence.getWord(k);
                if (!word.isPunctuation()) {
                    count++;
                }
            }
        } else {
            for (int k = i + 1; k < j; k++) {
                AnnotatedWord word = (AnnotatedWord) sentence.getWord(k);
                if (!word.isPunctuation()) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int calculateDependencyLength(AnnotatedSentence sentence) {
        int count = 0;
        for (int j = 0; j < sentence.wordCount(); j++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(j);
            if (!word.getUniversalDependency().toString().equals("ROOT") && !word.isPunctuation()) {
                count += dependencyLength(word.getUniversalDependency().to() - 1, j, sentence);
            }
        }
        return count;
    }

    private static String getCase(AnnotatedWord word) {
        ArrayList<String> features = word.getUniversalDependencyFeatures();
        for (String feature : features) {
            if (feature.startsWith("Case=")) {
                return feature.substring("Case=".length());
            }
        }
        return null;
    }

    private static HashMap<AnnotatedWord, HashSet<AnnotatedWord>> generateWordMap(AnnotatedSentence sentence) {
        HashMap<AnnotatedWord, HashSet<AnnotatedWord>> wordMap = new HashMap<>();
        for (int i = 0; i < sentence.wordCount(); i++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(i);
            if (!word.getUniversalDependency().toString().equals("ROOT") && !word.isPunctuation()) {
                AnnotatedWord toWord = (AnnotatedWord) sentence.getWord(word.getUniversalDependency().to() - 1);
                if (!wordMap.containsKey(toWord)) {
                    wordMap.put(toWord, new HashSet<>());
                }
                wordMap.get(toWord).add(word);
            }
        }
        return wordMap;
    }

    private static String findState(HashMap<AnnotatedWord, HashSet<AnnotatedWord>> wordMap, AnnotatedWord word) {
        String wordCase = getCase(word);
        if (word.getUniversalDependencyPos().equals("NOUN") && wordCase != null && wordCase.equals("Nom") && (wordMap.get(word) == null || checkForCompatability(wordMap.get(word)))) {
            return "1";
        }
        return "0";
    }

    private static boolean checkForCompatability(HashSet<AnnotatedWord> words) {
        for (AnnotatedWord word : words) {
            ArrayList<String> features = word.getUniversalDependencyFeatures();
            if (!(features.contains("Definite=Ind") || word.getUniversalDependencyPos().equals("NUM"))) {
                return false;
            }
        }
        return true;
    }

    private static String calculateArgumentLength(AnnotatedSentence sentence) {
        HashMap<AnnotatedWord, HashSet<AnnotatedWord>> wordMap = generateWordMap(sentence);
        int subjectCount = 0;
        int objectCount = 0;
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int j = 0; j < sentence.wordCount(); j++) {
            indexes.add(j);
        }
        String isSubjectNew = "-";
        String isObjectNew = "-";
        int totalCaseCount = 0;
        int subjIndex = Integer.MAX_VALUE;
        int objectIndex = Integer.MAX_VALUE;
        DisjointSet<Integer> set = new DisjointSet<>(indexes);
        AnnotatedWord last = null;
        String subjDependencyLength = "-";
        String objectDependencyLength = "-";
        for (int j = 0; j < sentence.wordCount(); j++) {
            AnnotatedWord word = (AnnotatedWord) sentence.getWord(j);
            if (!word.getUniversalDependency().toString().equals("PUNCT")) {
                last = word;
            }
            String wordCase = getCase(word);
            if (wordCase != null && !wordCase.equals("Nom")) {
                totalCaseCount++;
            }
            if (!word.getUniversalDependency().toString().equals("ROOT")) {
                AnnotatedWord toWord = (AnnotatedWord) sentence.getWord(word.getUniversalDependency().to() - 1);
                if (!toWord.getUniversalDependency().toString().equals("ROOT")) {
                    set.union(word.getUniversalDependency().to() - 1, j);
                } else {
                    if (word.getUniversalDependency().toString().equals("NSUBJ") || word.getUniversalDependency().toString().equals("CSUBJ")) {
                        isSubjectNew = findState(wordMap, word);
                        subjIndex = j;
                        subjDependencyLength = Integer.toString(dependencyLength(word.getUniversalDependency().to() - 1, subjIndex, sentence));
                    } else if (word.getUniversalDependency().toString().equals("CCOMP") || word.getUniversalDependency().toString().equals("OBJ") || word.getUniversalDependency().toString().equals("XCOMP")) {
                        isObjectNew = findState(wordMap, word);
                        objectIndex = j;
                        objectDependencyLength = Integer.toString(dependencyLength(word.getUniversalDependency().to() - 1, objectIndex, sentence));
                    }
                }
            }
        }
        int subjCaseCount = 0;
        if (subjIndex != Integer.MAX_VALUE) {
            int s = set.findSet(subjIndex);
            for (int i = 0; i < sentence.wordCount(); i++) {
                if (!sentence.getWord(i).isPunctuation()) {
                    if (set.findSet(i) == s) {
                        String wordCase = getCase((AnnotatedWord) sentence.getWord(i));
                        if (wordCase != null && !wordCase.equals("Nom")) {
                            subjCaseCount++;
                        }
                        subjectCount++;
                    }
                }
            }
        }
        int objectCaseCount = 0;
        if (objectIndex != Integer.MAX_VALUE) {
            int s = set.findSet(objectIndex);
            for (int i = 0; i < sentence.wordCount(); i++) {
                if (!sentence.getWord(i).isPunctuation()) {
                    if (set.findSet(i) == s) {
                        String wordCase = getCase((AnnotatedWord) sentence.getWord(i));
                        if (wordCase != null && !wordCase.equals("Nom")) {
                            objectCaseCount++;
                        }
                        objectCount++;
                    }
                }
            }
        }
        String s = "0";
        if (last.getUniversalDependencyPos().equals("VERB")) {
            s = "1";
        }
        return subjectCount + "\t" + objectCount + "\t" + totalCaseCount + "\t" + s + "\t" + subjCaseCount + "\t" + objectCaseCount + "\t" + isSubjectNew + "\t" + isObjectNew + "\t" + subjDependencyLength + "\t" + objectDependencyLength;
    }

    public static void main(String[] args) throws IOException {
        AnnotatedCorpus keNet = new AnnotatedCorpus(new File("Turkish-Phrase-KeNet"));
        BufferedWriter outfile;
        OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(Paths.get("KeNet.txt")), StandardCharsets.UTF_8);
        outfile = new BufferedWriter(writer);
        outfile.write("File Id\tWord Order\tDependency Length\tSubject Count\tObject Count\tN of Case\tVerb Last\tN of Case(subj)\tN of Case(obj)\tIs New(subj)\tIs New(obj)\tSubject Dependency Length\tObject Dependency Length\tWord Count");
        outfile.newLine();
        for (int i = 0; i < keNet.sentenceCount(); i++) {
            AnnotatedSentence sentence = (AnnotatedSentence) keNet.getSentence(i);
            int count = 0;
            for (int j = 0; j < sentence.wordCount(); j++) {
                if (!sentence.getWord(j).isPunctuation()) {
                    count++;
                }
            }
            String wordOrder = calculateWordOrder(sentence);
            int dependencyLength = calculateDependencyLength(sentence);
            outfile.write(sentence.getFileName() + "\t" + wordOrder + "\t" + dependencyLength + "\t" + calculateArgumentLength(sentence) + "\t" + count);
            outfile.newLine();
        }
        outfile.close();
        /*AnnotatedCorpus ets = new AnnotatedCorpus(new File("Turkish-Phrase-Ets"));
        writer = new OutputStreamWriter(Files.newOutputStream(Paths.get("Ets.txt")), StandardCharsets.UTF_8);
        outfile = new BufferedWriter(writer);
        outfile.write("File Id\tWord Order\tDependency Length\tSubject Count\tObject Count");
        outfile.newLine();
        for (int i = 0; i < ets.sentenceCount(); i++) {
            AnnotatedSentence sentence = (AnnotatedSentence) ets.getSentence(i);
            String wordOrder = calculateWordOrder(sentence);
            int dependencyLength = calculateDependencyLength(sentence);
            outfile.write(sentence.getFileName() + "\t" + wordOrder + "\t" + dependencyLength + "\t" + calculateArgumentLength(sentence));
            outfile.newLine();
        }
        outfile.close();
        AnnotatedCorpus frameNet = new AnnotatedCorpus();
        File file = new File("Turkish-Phrase-FrameNet");
        File[] files = file.listFiles();
        for (int j = 0; j < Objects.requireNonNull(files).length; j++) {
            File f = files[j];
            if (!f.getName().equals(".git") && !f.getPath().equals("Turkish-Phrase-FrameNet/.DS_Store")) {
                AnnotatedCorpus c = new AnnotatedCorpus(f);
                for (int i = 0; i < c.sentenceCount(); i++) {
                    frameNet.addSentence(c.getSentence(i));
                }
            }
        }
        writer = new OutputStreamWriter(Files.newOutputStream(Paths.get("FrameNet.txt")), StandardCharsets.UTF_8);
        outfile = new BufferedWriter(writer);
        outfile.write("File Id\tWord Order\tDependency Length\tSubject Count\tObject Count");
        outfile.newLine();
        for (int i = 0; i < frameNet.sentenceCount(); i++) {
            AnnotatedSentence sentence = (AnnotatedSentence) frameNet.getSentence(i);
            String wordOrder = calculateWordOrder(sentence);
            int dependencyLength = calculateDependencyLength(sentence);
            outfile.write(sentence.getFileName() + "\t" + wordOrder + "\t" + dependencyLength + "\t" + calculateArgumentLength(sentence));
            outfile.newLine();
        }
        outfile.close();*/
    }
}
