import AnnotatedSentence.*;
import Cookies.Sort.QuickSort;
import DataStructure.CounterHashMap;

import java.io.File;
import java.util.*;
import java.util.AbstractMap.*;

public class Results {

    private static void printMatrix(CounterHashMap<String> map, int n) {
        ArrayList<SimpleEntry<String, Integer>> list = new ArrayList<>();
        for (String key : map.keySet()) {
            if (list.size() < n) {
                list.add(new SimpleEntry<>(key, map.get(key)));
            } else {
                int minValue = Integer.MAX_VALUE;
                int minIndex = -1;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getValue() < minValue) {
                        minValue = list.get(i).getValue();
                        minIndex = i;
                    }
                }
                if (map.get(key) > minValue) {
                    list.remove(minIndex);
                    list.add(new SimpleEntry<>(key, map.get(key)));
                }
            }
        }
        Comparator<SimpleEntry<String, Integer>> comparator = new Comparator<SimpleEntry<String, Integer>>() {
            @Override
            public int compare(SimpleEntry<String, Integer> o1, SimpleEntry<String, Integer> o2) {
                if (o1.getValue() > o2.getValue()) {
                    return -1;
                } else if (o1.getValue() < o2.getValue()) {
                    return 1;
                }
                return 0;
            }
        };
        QuickSort<SimpleEntry<String, Integer>> quickSort = new QuickSort<>(comparator);
        quickSort.sort(list, 0, list.size() - 1);
        System.out.println("Table #4");
        System.out.println();
        System.out.print("|category                      |All                           |");
        for (int i = 0; i < n; i++) {
            SimpleEntry<String, Integer> entry = list.get(i);
            System.out.println();
            System.out.print("|" + entry.getKey());
            for (int j = 0; j < 30 - entry.getKey().length(); j++) {
                System.out.print(" ");
            }
            System.out.print("|" + entry.getValue());
            for (int j = 0; j < 30 - Integer.toString(entry.getValue()).length(); j++) {
                System.out.print(" ");
            }
            System.out.print("|");
        }
        System.out.println();
    }

    private static void printMatrix(HashMap<String, CounterHashMap<String>> mostFrequentMap, int n) {
        ArrayList<SimpleEntry<String, CounterHashMap<String>>> list = new ArrayList<>(n);
        for (String key : mostFrequentMap.keySet()) {
            if (list.size() < n) {
                list.add(new SimpleEntry<>(key, mostFrequentMap.get(key)));
            } else {
                int count = mostFrequentMap.get(key).sumOfCounts();
                int minValue = Integer.MAX_VALUE;
                int index = -1;
                for (int i = 0; i < list.size(); i++) {
                    int currentCount = list.get(i).getValue().sumOfCounts();
                    if (currentCount < minValue) {
                        minValue = currentCount;
                        index = i;
                    }
                }
                if (count > minValue) {
                    list.remove(index);
                    list.add(new SimpleEntry<>(key, mostFrequentMap.get(key)));
                }
            }
        }
        Comparator<SimpleEntry<String, CounterHashMap<String>>> comparator = new Comparator<SimpleEntry<String, CounterHashMap<String>>>() {
            @Override
            public int compare(SimpleEntry<String, CounterHashMap<String>> o1, SimpleEntry<String, CounterHashMap<String>> o2) {
                int firstCount = o1.getValue().sumOfCounts();
                int secondCount = o2.getValue().sumOfCounts();
                if (firstCount > secondCount) {
                    return -1;
                } else if (secondCount > firstCount) {
                    return 1;
                }
                return 0;
            }
        };
        QuickSort<SimpleEntry<String, CounterHashMap<String>>> quickSort = new QuickSort<>(comparator);
        quickSort.sort(list, 0, list.size() - 1);
        System.out.println("Table #10");
        System.out.println();
        System.out.print("|token                                                            |freq.                                                            |most freq.                                                       |fwe                                                              |");
        for (int i = 0; i < n; i++) {
            SimpleEntry<String, CounterHashMap<String>> entry = list.get(i);
            System.out.println();
            System.out.print("|" + entry.getKey());
            for (int j = 0; j < 65 - entry.getKey().length(); j++) {
                System.out.print(" ");
            }
            String count = Integer.toString(entry.getValue().sumOfCounts());
            System.out.print("|" + count);
            for (int j = 0; j < 65 - count.length(); j++) {
                System.out.print(" ");
            }
            String mostFreq = entry.getValue().max();
            System.out.print("|" + mostFreq);
            for (int j = 0; j < 65 - mostFreq.length(); j++) {
                System.out.print(" ");
            }
            String mostFreqCount = Integer.toString(entry.getValue().get(mostFreq));
            System.out.print("|" + mostFreqCount);
            for (int j = 0; j < 65 - mostFreqCount.length(); j++) {
                System.out.print(" ");
            }
            System.out.print("|");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        AnnotatedCorpus corpus = new AnnotatedCorpus();
        File file = new File("Turkish-Phrase-FrameNet");
        File[] files = file.listFiles();
        for (int j = 0; j < files.length; j++) {
            File f = files[j];
            if (!f.getPath().equals("Turkish-Phrase-FrameNet/.DS_Store")) {
                AnnotatedCorpus c = new AnnotatedCorpus(f);
                for (int i = 0; i < c.sentenceCount(); i++) {
                    corpus.addSentence(c.getSentence(i));
                }
            }
        }
        AnnotatedCorpus atis = new AnnotatedCorpus(new File("Turkish-Phrase-Atis"));
        for (int i = 0; i < atis.sentenceCount(); i++) {
            corpus.addSentence(atis.getSentence(i));
        }
        AnnotatedCorpus ets = new AnnotatedCorpus(new File("Turkish-Phrase-Ets"));
        for (int i = 0; i < ets.sentenceCount(); i++) {
            corpus.addSentence(ets.getSentence(i));
        }
        AnnotatedCorpus penn = new AnnotatedCorpus(new File("Turkish-Phrase"));
        for (int i = 0; i < penn.sentenceCount(); i++) {
            corpus.addSentence(penn.getSentence(i));
        }
        AnnotatedCorpus keNet = new AnnotatedCorpus(new File("Turkish-Phrase-KeNet"));
        for (int i = 0; i < keNet.sentenceCount(); i++) {
            corpus.addSentence(keNet.getSentence(i));
        }
        HashMap<String, CounterHashMap<String>> mostFrequentMap = new HashMap<>();
        CounterHashMap<String> categories = new CounterHashMap<>();
        int n = 200;
        for (int i = 0; i < corpus.sentenceCount(); i++) {
            if ((i + 1) % n == 0) {
                System.out.println((i + 1) + "->" + categories.size());
            }
            AnnotatedSentence sentence = (AnnotatedSentence) corpus.getSentence(i);
            for (int j = 0; j < sentence.wordCount(); j++) {
                AnnotatedWord word = (AnnotatedWord) sentence.getWord(j);
                String name = word.getName().toLowerCase(new Locale("tr"));
                switch (name) {
                    case "de":
                    case "da":
                        name = "de/da";
                        break;
                    default:
                        break;
                }
                if (word.getCcg() != null) {
                    categories.put(word.getCcg());
                    if (!mostFrequentMap.containsKey(name)) {
                        mostFrequentMap.put(name, new CounterHashMap<>());
                    }
                    mostFrequentMap.get(name).put(word.getCcg());
                }
            }
        }
        printMatrix(categories, 15);
        printMatrix(mostFrequentMap, 15);
    }
}
