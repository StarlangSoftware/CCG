package Annotation;

import AnnotatedSentence.ViewLayerType;
import DataCollector.Sentence.SentenceAnnotatorPanel;

public class SentenceCCGPanel extends SentenceAnnotatorPanel {

    public SentenceCCGPanel(String currentPath, String rawFileName) {
        super(currentPath, rawFileName, ViewLayerType.CCG);
    }
}
