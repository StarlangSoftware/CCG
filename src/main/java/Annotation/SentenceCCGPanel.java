package Annotation;

import AnnotatedSentence.AnnotatedWord;
import AnnotatedSentence.ViewLayerType;
import DataCollector.Sentence.SentenceAnnotatorPanel;

import java.awt.*;
import java.util.ArrayList;

public class SentenceCCGPanel extends SentenceAnnotatorPanel {

    public SentenceCCGPanel(String currentPath, String rawFileName) {
        super(currentPath, rawFileName, ViewLayerType.CCG);
    }

    @Override
    protected void setWordLayer() {

    }

    @Override
    protected int getMaxLayerLength(AnnotatedWord word, Graphics g) {
        int maxSize = g.getFontMetrics().stringWidth(word.getName());
        if (word.getCcg() != null) {
            int size = g.getFontMetrics().stringWidth(word.getCcg());
            if (size > maxSize){
                maxSize = size;
            }
        }
        return maxSize;
    }

    @Override
    protected void drawLayer(AnnotatedWord word, Graphics g, int currentLeft, int lineIndex, int wordIndex, int maxSize, ArrayList<Integer> wordSize, ArrayList<Integer> wordTotal) {
        if (word.getCcg() != null){
            String correct = word.getCcg();
            g.drawString(correct, currentLeft, (lineIndex + 1) * lineSpace + 30);
        }
    }

    @Override
    protected void setBounds() {

    }
}
