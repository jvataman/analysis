package aboutlocal.analysis.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scala.actors.threadpool.Arrays;
import aboutlocal.analysis.confs.P;
import aboutlocal.analysis.core.util.MapUtils;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.QuoteDTO;
import aboutlocal.analysis.data.dtos.TweetDTO;
import aboutlocal.analysis.data.dtos.TweetDTO.HashTag;
import aboutlocal.analysis.data.dtos.TweetDTO.UrlTag;
import aboutlocal.analysis.preprocessing.lang.TextPreprocessor;

import com.aboutlocal.hypercube.domain.dto.CsvDocument;
import com.aboutlocal.hypercube.domain.dto.CsvRow;
import com.aboutlocal.hypercube.io.fs.CsvDecoder;
import com.aboutlocal.hypercube.io.fs.DTOHandler;
import com.aboutlocal.hypercube.util.data.IoUtils;
import com.aboutlocal.hypercube.util.data.IoUtils.CountIterator;
import com.aboutlocal.hypercube.util.data.IoUtils.LineParser;
import com.google.gson.Gson;

public class DataCacheCreator {

    private final Gson gson = new Gson();

    private final DataCacheCreator self = this;
    private final TextPreprocessor p = new TextPreprocessor();

    public static void main(String[] args) {
        new DataCacheCreator().fillCache();
    }

    public DataCache fillCache() {
//        generateCompanyNameMapping();
//        generateQuoteMapping();
        generateTweetMapping();

        return DataCache.instance();
    }

    private void generateQuoteMapping() {
        System.out.println("generating {time,CODE} <-> quote mapping");
        ArrayList<File> files = new DTOHandler().findFiles(P.QUOTES.STRUCT, ".*", false);
        final CountIterator inc = IoUtils.newCountIterator("reading quote-files:", 100, 2594);
        final CountIterator inc2 = IoUtils.newCountIterator("reading quotes:", 1000000);
        final TreeMap<String, String> nonIndexed = new TreeMap<>();
        for (final File file : files) {
            inc.increment();
            final String companyName = p.companyName(file.getName().replace("_", " "));
            final String[] code = {DataCache.instance().companyNameToCompanyCode.get(companyName)};
            if (code[0] == null) {
                // nonIndexed.put(companyName, file.getName().replace("_",
                // " "));
                ArrayList<ArrayList<String>> candidates = new ArrayList<>();
                for (String token : companyName.split(" ")) {
                    String[] tokenResult = DataCache.instance().companyNameTokenToCompanyCode.get(token);
                    if (tokenResult != null) {
//                        System.out.println(companyName + " > " + token + " > " + Arrays.asList(tokenResult));
                        candidates.add(new ArrayList<String>(Arrays.asList(tokenResult)));
                    }
                }
                if (candidates.size() > 1) {
                    ArrayList<String> intersection = candidates.get(0);
                    for (ArrayList<String> set : candidates) {
                        intersection.retainAll(set);
                    }
                    if (intersection.size() == 1)
                        code[0] = intersection.get(0);
                }
            }
            IoUtils.readDocument(file.getPath(), new LineParser() {
                @Override
                public void parseLine(String line) {
                    // inc2.increment();
                    QuoteDTO quote = gson.fromJson(line, QuoteDTO.class);
                    Long key = quote.getTimeStamp();

                    // self.putIntoArrMap(key, quote,
                    // DataCache.instance().timeToQuote);
                    if (code != null)
                        MapUtils.putIntoListMap(code[0], quote, DataCache.instance().companyCodeToQuote);
                    else
                        nonIndexed.put(companyName, file.getName().replace("_", " "));
                }
            });
        }
        System.out.println(IoUtils.toColumnString(nonIndexed.entrySet()));
        System.out.println("indexed time  -> quote: " + DataCache.instance().timeToQuote.size());
        System.out.println("indexed CODE -> quote: " + DataCache.instance().companyCodeToQuote.size());
        System.out.println("nonIndexed cName -> quote: " + nonIndexed.size());
    }

    public void generateCompanyNameMapping() {
        System.out.println("generating {cName,Tokens} <-> CODE mapping");
        try {
            CsvDocument csv = new CsvDecoder().readCsv(P.RESOURCES.COMPANYLIST);
            System.out.println(csv.getContents().size());
            for (CsvRow row : csv.getContents()) {
                String name = p.companyName(row.get("Name")), sym = row.get("Symbol").trim();
                DataCache.instance().companyNameToCompanyCode.put(name, sym);
                DataCache.instance().companyCodeToCompanyName.put(sym, name);
                for (String token : name.split(" "))
                    MapUtils.putIntoArrMap(token, sym, DataCache.instance().companyNameTokenToCompanyCode);
            }
            System.out.println(IoUtils.toColumnString(DataCache.instance().companyNameToCompanyCode.entrySet()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateTweetMapping() {
        System.out.println("generating {time,url,hashtag,contentVector} -> tweet mapping");
        final CountIterator inc = IoUtils.newCountIterator("reading tweets:", 100000,3300000);
        //XXX max filter
        final int[] max = {100000}, current = {0};
        final HashSet<String> expandedUrlsDisjunct = new HashSet<>();
        final LinkedList<String> expandedUrls = new LinkedList<>();
        final HashSet<String> hashTagsDisjunct = new HashSet<>();
        final LinkedList<String> hashTags = new LinkedList<>();
        IoUtils.readDocument(P.TWEETS.DISJUNCT + "tweets", new LineParser() {

            @Override
            public void parseLine(String line) {
                
                inc.increment();
                
                if((current[0] = current[0]+1) > max[0])
                    return;
                
                TweetDTO tweet = gson.fromJson(line, TweetDTO.class);
                Long tweetTimeStamp = tweet.created_at_timestamp;
                
                //TIME
                MapUtils.putIntoListMap(tweetTimeStamp, tweet, DataCache.instance().timeToTweet);
                
                //URL
                if(tweet.entities!=null && tweet.entities.urls!=null)
                    for(UrlTag urlTag:tweet.entities.urls){
                        String expandedUrl = urlTag.expanded_url.toLowerCase();
                        expandedUrls.add(expandedUrl);
                        expandedUrlsDisjunct.add(expandedUrl);
                        MapUtils.putIntoListMap(expandedUrl, tweet, DataCache.instance().resolvedUrlToTweet);
                    }
                
                //HASHTAG
                if(tweet.entities!=null && tweet.entities.hashtags!=null)
                    for(HashTag hashTag:tweet.entities.hashtags){
                        String hashTagText = p.hashTag(hashTag.text);
                        hashTags.add(hashTagText);
                        hashTagsDisjunct.add(hashTagText);
                        MapUtils.putIntoListMap(hashTagText, tweet, DataCache.instance().hashTagToTweet);
                    }
                MapUtils.putIntoListMap(tweetTimeStamp, tweet, DataCache.instance().timeToTweet);
                
                //CONTENT
                String preprocessedText = p.tweetText(tweet.text);
                String tokenizedText = 
                
            }
        });

        System.out.println(IoUtils.toColumnString(hashTagsDisjunct));
        System.out.println("keys: " + DataCache.instance().timeToTweet.keySet().size());
        System.out.println("expanded urls:          "+expandedUrls.size());
        System.out.println("expanded urls disjunct: "+expandedUrlsDisjunct.size());
        System.out.println("expanded hashTags:          "+hashTags.size());
        System.out.println("expanded HashTags disjunct: "+hashTagsDisjunct.size());
        
        final CountIterator inc2 = IoUtils.newCountIterator("iterating through tweets:", 100000);
        for (long ts : DataCache.instance().timeToTweet.keySet()) {
            inc2.increment();
            DataCache.instance().timeToTweet.get(ts);
        }
    }
}
