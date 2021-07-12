package Annotation;

import DataCollector.Sentence.SentenceAnnotatorFrame;
import DataCollector.Sentence.SentenceAnnotatorPanel;

public class SentenceCCGFrame extends SentenceAnnotatorFrame {

    @Override
    protected SentenceAnnotatorPanel generatePanel(String currentPath, String rawFileName) {
        return new SentenceCCGPanel(currentPath, rawFileName);
    }
}
