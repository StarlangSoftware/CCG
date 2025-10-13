import AnnotatedSentence.*;
import CCG.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TestCCGTypeGenerator {

    public static void main(String[] args) throws IOException {
        String dataSetName = "KeNet";
        int k = 1;
        BufferedWriter outfile, o;
        OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(Paths.get(dataSetName + "-Table-1- " + k + ".txt")), StandardCharsets.UTF_8);
        outfile = new BufferedWriter(writer);
        OutputStreamWriter w = new OutputStreamWriter(Files.newOutputStream(Paths.get(dataSetName + "-results-" + k)), StandardCharsets.UTF_8);
        o = new BufferedWriter(w);
        outfile.write("File Id\tTotal Operation Count");
        outfile.newLine();
        AnnotatedCorpus annotatedCorpus = new AnnotatedCorpus(new File("Turkish-Phrase-" + dataSetName));
        int count = 0;
        int total = 0;
        for (int i = 0; i < annotatedCorpus.sentenceCount(); i++) {
            total++;
            AnnotatedSentence sentence = (AnnotatedSentence) annotatedCorpus.getSentence(i);
            try {
                System.out.println(sentence.getFileName());
                ArrayList<Type> types = KIntervalCCGTypeGenerator.generate(sentence, k);
                o.write(types.toString());
                o.newLine();
                o.write(sentence.getFileName() + " done.");
                o.newLine();
                count++;
                outfile.write(sentence.getFileName() + "\t" + types.size());
                outfile.newLine();
            } catch (OutOfMemoryError e) {
                System.out.println(sentence.getFileName() + " Out of memory.");
            } catch (WrongCCGException e) {
                System.out.println(sentence.getFileName() + " not done.");
            }
        }
        o.write(Integer.toString(count));
        o.newLine();
        o.write(Integer.toString(total));
        o.newLine();
        o.write(Double.toString((count + 0.0) / total));
        o.close();
        outfile.close();
        writer = new OutputStreamWriter(Files.newOutputStream(Paths.get(dataSetName + "-Table-2-" + k + ".txt")), StandardCharsets.UTF_8);
        outfile = new BufferedWriter(writer);
        outfile.write("File Id\tWord\tCCG\tTag");
        outfile.newLine();
        for (int i = 0; i < annotatedCorpus.sentenceCount(); i++) {
            AnnotatedSentence sentence = (AnnotatedSentence) annotatedCorpus.getSentence(i);
            for (int j = 0; j < sentence.wordCount(); j++) {
                AnnotatedWord word = (AnnotatedWord) sentence.getWord(j);
                if (!word.isPunctuation()) {
                    outfile.write(sentence.getFileName() + "\t" + word.getName() + "\t" + word.getCcg() + "\t" + word.getUniversalDependencyPos());
                    outfile.newLine();
                }
            }
        }
        outfile.close();
    }
}
