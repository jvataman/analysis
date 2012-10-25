package aboutlocal.analysis.preprocessing.lang;

import static com.aboutlocal.hypercube.logging.SuperLogger.debug;
import static com.aboutlocal.hypercube.logging.SuperLogger.setClassLogLevel;

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

import scala.actors.threadpool.Arrays;

import com.aboutlocal.hypercube.logging.SuperLogger.LogLevel;

import aboutlocal.analysis.confs.P;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Tokenizer {

    private static MaxentTagger tagger = null;
    private final porterStemmer stemmer = new porterStemmer();
    private final static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
    
    private final static Pattern nounPat = Pattern.compile("(([^_\\s]+)_(N(N|P)(S|P)?))");
    
    public static void main(String[] args) {
        String s = "Peter is jumping over the lazy fox.";
        s = "Articulate launches Storyline authoring tool, outputs training modules to iPad and HTML 5: Learning professionally";
        setClassLogLevel(Tokenizer.class, LogLevel.DEBUG);
        
        new Tokenizer().getContentVector(s);
    }

    private static void init() {
        if (tagger == null)
            try {
                System.out.println("initalizing maxenttagger...");
                tagger = new MaxentTagger(P.RESOURCES.MODELS.ROOT + "english-left3words-distsim.tagger");
                System.out.println("init maxenttagger done");
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
    }

    @SuppressWarnings("unchecked")
    public String getContentVector(String preprocessedText){
        StringBuilder contentVector = new StringBuilder();
        
        debug("input:                 "+preprocessedText);
        
        init();
        String tagged = tagger.tagString(preprocessedText);
        
        debug("tagged:                "+tagged);
        
        Matcher mat = nounPat.matcher(tagged);
        String nouns = "";
        while(mat.find())
            nouns += mat.group(2)+" ";
        
        debug("nouns:                 "+nouns);
        
        TreeSet<String> tokens = new TreeSet<>();
        for(String noun: new ArrayList<String>(Arrays.asList(nouns.trim().split(" ")))){
            stemmer.setCurrent(noun);
            stemmer.stem();
            tokens.add(stemmer.getCurrent().toLowerCase());
        }
        
        for(String t:tokens)
            contentVector.append(t).append(" ");
        
        debug("tokenized and stemmed: "+contentVector);
        
        return contentVector.toString().trim();
    }
    
    /**
     * not used, because StanfordPOS already tokenizes, stemming done separately
     * @return
     */
    @Deprecated
    private String tokenizeAndStem(String nouns){
        StringBuilder contentVector = new StringBuilder();
        
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
        
        for(String t:tokens)
            contentVector.append(t).append(" ");
        
        return contentVector.toString();
    }

}