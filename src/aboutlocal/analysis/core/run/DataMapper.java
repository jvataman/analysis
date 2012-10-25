package aboutlocal.analysis.core.run;

import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

import scala.actors.threadpool.Arrays;
import aboutlocal.analysis.core.DataCacheCreator;
import aboutlocal.analysis.core.util.MapUtils;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.NewsDTO;
import aboutlocal.analysis.data.dtos.TweetDTO;
import aboutlocal.analysis.preprocessing.lang.TextPreprocessor;
import aboutlocal.analysis.preprocessing.lang.Tokenizer;

public class DataMapper {
    
    private final static Tokenizer t = new Tokenizer();
    private final static TextPreprocessor p = new TextPreprocessor();
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        new DataCacheCreator().fillCache();

        TreeMap<String, LinkedList<NewsDTO>> cvtn = DataCache.instance().contentVectorToNews;
        
        TweetDTO val;
        for (String contentVector : cvtn.keySet()) {
            val = MapUtils.getMaxIntersectedValueFromMap(Arrays.asList(contentVector.split(" ")),
                    DataCache.instance().contentVectorToTweet, 3);
            if (val != null){
                System.out.println(contentVector);
                System.out.println(t.getContentVector(p.tweetText(val.text)));
                for(NewsDTO ndto:cvtn.get(contentVector))
                    System.out.println(ndto.title);
                System.out.println(val.text);
                System.out.println();
            }

        }

        System.out.println("DONE");
    }

}