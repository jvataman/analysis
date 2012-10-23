package aboutlocal.analysis.preprocessing.lang;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.porterStemmer;

import aboutlocal.analysis.confs.P;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Tokenizer {

    private static MaxentTagger tagger = null;
    private final static porterStemmer stemmer = new porterStemmer();
    private final static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
    
    private final static Pattern nounPat = Pattern.compile("(([^_\\s]+)_(N(N|P)(S|P)?))");
    
    public static void main(String[] args) {
        String s = "Peter is jumping over the lazy fox.";
        s = "Articulate launches Storyline authoring tool, outputs training modules to iPad and HTML 5: Learning profess";
        tag(s);
    }

    private static void init() {
        if (tagger == null)
            try {
                tagger = new MaxentTagger(P.RESOURCES.MODELS.ROOT + "english-left3words-distsim.tagger");
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
    }

    public static void tag(String text) {
        init();
        System.out.println(text);
        System.out.println(tagger.tagString(text));
        Matcher mat = nounPat.matcher(tagger.tagString(text));
        String nouns = "";
        while(mat.find())
            nouns += mat.group(2)+" ";
        
        TokenStream stream =  analyzer.tokenStream("sampleField", new StringReader(nouns.trim()));
        CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);

        TreeSet<String> tokens = new TreeSet<>();
        try {
            while(stream.incrementToken()){
                stemmer.setCurrent(charTermAttribute.toString());
                stemmer.stem();
                tokens.add(stemmer.getCurrent());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println(tokens);
    }
    
    public String getContentVector(String text){
        String contentVector = "";
        
        
        return contentVector;
    }

}