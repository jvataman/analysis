package aboutlocal.analysis.core.run;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

import com.aboutlocal.hypercube.util.data.IoUtils;
import com.aboutlocal.hypercube.util.data.IoUtils.LineParser;
import com.google.gson.Gson;

import scala.actors.threadpool.Arrays;
import aboutlocal.analysis.confs.P;
import aboutlocal.analysis.core.DataCacheCreator;
import aboutlocal.analysis.core.util.MapUtils;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.NewsDTO;
import aboutlocal.analysis.data.dtos.TweetDTO;
import aboutlocal.analysis.data.dtos.TweetDTO.HashTag;
import aboutlocal.analysis.data.dtos.TweetDTO.UrlTag;
import aboutlocal.analysis.preprocessing.lang.TextPreprocessor;
import aboutlocal.analysis.preprocessing.lang.Tokenizer;

public class CodeMapperTest {
    
    private final static Tokenizer t = new Tokenizer();
    private final static TextPreprocessor p = new TextPreprocessor();
    private final static Gson gson = new Gson();
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        new DataCacheCreator().generateCompanyNameMapping();
        
        final HashSet<String> recognized = new HashSet<>();
        final HashSet<String> unRecognized = new HashSet<>();
        
        System.out.println(IoUtils.toColumnString(DataCache.instance().companyCodeToCompanyName.entrySet()));

        IoUtils.readDocument(P.TWEETS.DISJUNCT + "tweets", new LineParser() {

            @Override
            public void parseLine(String line) {

                TweetDTO tweet = gson.fromJson(line, TweetDTO.class);

                // SEARCHTERM
                MapUtils.putIntoListMap(p.query(tweet.query), tweet, DataCache.instance().searchTermToTweet);
                String code = DataCache.instance().companyNameToCompanyCode.get(p.companyName(p.query(tweet.query)));
                if (code == null)
                    code = MapUtils.getMaxIntersectedValueFromMap(
                            Arrays.asList(p.companyName(p.query(tweet.query)).split(" ")),
                            DataCache.instance().companyNameTokenToCompanyCode);
                
                if(code!=null)
                    MapUtils.putIntoListMap(code, tweet, DataCache.instance().companyCodeToTweet);
                else{
                    Integer prevNum = DataCache.instance().unrecognizedSearchTerms.get(p.query(tweet.query));
                    DataCache.instance().unrecognizedSearchTerms.put(p.query(tweet.query),prevNum==null?1:prevNum+1);
                }

                String out = "";
                if (code == null) {
                    if (unRecognized.add((out = p.query(tweet.query) + " -> " + p.companyName(p.query(tweet.query))))){
                        System.out.println(out + " " + unRecognized.size());
                        System.out.println(DataCache.instance().unrecognizedSearchTerms);
                }
                } else if (recognized.add((out = p.query(tweet.query))))
                    System.out.println("\t\t" + out +" -> "+code+ " "+ recognized.size());
            }
        });
        
        System.out.println("rec:   "+recognized.size());
        System.out.println("unrec: "+unRecognized.size());
    }

}