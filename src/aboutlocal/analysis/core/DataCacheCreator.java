package aboutlocal.analysis.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scala.actors.threadpool.Arrays;
import aboutlocal.analysis.confs.P;
import aboutlocal.analysis.core.util.MapUtils;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.NewsDTO;
import aboutlocal.analysis.data.dtos.QuoteDTO;
import aboutlocal.analysis.data.dtos.TweetDTO;
import aboutlocal.analysis.data.dtos.UserDTO;
import aboutlocal.analysis.data.dtos.TweetDTO.HashTag;
import aboutlocal.analysis.data.dtos.TweetDTO.UrlTag;
import aboutlocal.analysis.preprocessing.lang.TextPreprocessor;
import aboutlocal.analysis.preprocessing.lang.Tokenizer;

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

    private static final Tokenizer t = new Tokenizer();
    private static final TextPreprocessor p = new TextPreprocessor();

    public static void main(String[] args) {
        new DataCacheCreator().fillCache();
    }

    public DataCache fillCache() {
        System.out.println("FILLING CACHE");
        long start = System.currentTimeMillis();
        generateCompanyNameMapping();
        generateNewsMapping();
        generateUserMapping();
        generateQuoteMapping();
        generateTweetMapping();
        System.out.println("CAHING DONE IN "+(System.currentTimeMillis()-start)/1000+" seconds");

        return DataCache.instance();
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
//            System.out.println(IoUtils.toColumnString(DataCache.instance().companyNameToCompanyCode.entrySet()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void generateUserMapping(){
        System.out.println("generating {uId} -> User mapping");
        IoUtils.readDocument(P.TWEETS.USERS+"users",new LineParser(){
            @Override
            protected void parseLine(String line) {
                UserDTO user = gson.fromJson(line, UserDTO.class);
                DataCache.instance().userIdToUser.put(user.id_str, user);
            }
        });
        System.out.println("cached users: "+DataCache.instance().userIdToUser.size());
    }

    public void generateQuoteMapping() {
        System.out.println("generating {time,CODE} <-> quote mapping");
        ArrayList<File> files = new DTOHandler().findFiles(P.QUOTES.STRUCT, ".*", false);
        final CountIterator inc = IoUtils.newCountIterator("reading quote-files:", 100, 2594);
        final CountIterator inc2 = IoUtils.newCountIterator("reading quotes:", 1000000);
        final TreeMap<String, String> nonIndexed = new TreeMap<>();
        for (final File file : files) {
            inc.increment();
            final String companyName = p.companyName(file.getName().replace("_", " "));
            final String[] code = { DataCache.instance().companyNameToCompanyCode.get(companyName) };
            if (code[0] == null) {
                code[0] = MapUtils.getMaxIntersectedValueFromMap(Arrays.asList(companyName.split(" ")),
                        DataCache.instance().companyNameTokenToCompanyCode);
            }
            IoUtils.readDocument(file.getPath(), new LineParser() {
                @Override
                public void parseLine(String line) {
                    // inc2.increment();
                    QuoteDTO quote = gson.fromJson(line, QuoteDTO.class);
                    Long key = quote.getTimeStamp();

                    MapUtils.putIntoArrMap(key, quote, DataCache.instance().timeToQuote);
                    
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

    public void generateNewsMapping(){
        IoUtils.readDocument(P.NEWS.ROOT+"BloomBergNewsArchive", new NewsParser("BloomBerg"));
        IoUtils.readDocument(P.NEWS.ROOT+"FinancialTimesArchive", new NewsParser("FinancialTimes"));
    }
    
    private class NewsParser extends IoUtils.LineParser{
        String name;
        public NewsParser(String sourceName){
            name = sourceName;
        }
        @Override
        protected void parseLine(String line) {
            NewsDTO dto = gson.fromJson(line, NewsDTO.class);
            if(dto.date==null || dto.title==null)
                return;
            MapUtils.putIntoListMap(dto.date.getTime(), dto, DataCache.instance().timeToNews);
            String contentVector = t.getContentVector(p.tweetText(dto.title));
            MapUtils.putIntoListMap(contentVector, dto, DataCache.instance().contentVectorToNews);
            for(String token:contentVector.split(" "))
                MapUtils.putIntoListMap(token, dto, DataCache.instance().contentVectorTokenToNews);
        }
    }
    
    public void generateTweetMapping() {
        System.out.println("generating {time,url,hashtag,contentVector} -> tweet mapping");
        final CountIterator inc = IoUtils.newCountIterator("reading tweets:", 100000, 3300000);
        // XXX max filter
        final int[] max = { 100000 }, current = { 0 };
        final HashSet<String> expandedUrlsDisjunct = new HashSet<>();
        final LinkedList<String> expandedUrls = new LinkedList<>();
        final HashSet<String> hashTagsDisjunct = new HashSet<>();
        final LinkedList<String> hashTags = new LinkedList<>();

        final HashSet<String> recognized = new HashSet<>();
        final HashSet<String> unRecognized = new HashSet<>();

        IoUtils.readDocument(P.TWEETS.DISJUNCT + "tweets", new LineParser() {

            @Override
            public void parseLine(String line) {

                inc.increment();

                if ((current[0] = current[0] + 1) > max[0])
                    stopFlag = true;

                TweetDTO tweet = gson.fromJson(line, TweetDTO.class);
                Long tweetTimeStamp = tweet.created_at_timestamp;

                // TIME
                MapUtils.putIntoListMap(tweetTimeStamp, tweet, DataCache.instance().timeToTweet);

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

                // String out = "";
                // if (code == null) {
                // if (unRecognized.add((out = p.query(tweet.query) + " -> " +
                // p.companyName(p.query(tweet.query)))))
                // System.out.println(out);
                // } else if (recognized.add((out = "\t\t" +
                // p.query(tweet.query))))
                // System.out.println(out);

                // URL
                if (tweet.entities != null && tweet.entities.urls != null)
                    for (UrlTag urlTag : tweet.entities.urls) {
                        String expandedUrl = urlTag.expanded_url.toLowerCase();
                        expandedUrls.add(expandedUrl);
                        expandedUrlsDisjunct.add(expandedUrl);
                        MapUtils.putIntoListMap(expandedUrl, tweet, DataCache.instance().resolvedUrlToTweet);
                    }

                // HASHTAG
                if (tweet.entities != null && tweet.entities.hashtags != null)
                    for (HashTag hashTag : tweet.entities.hashtags) {
                        String hashTagText = p.hashTag(hashTag.text);
                        hashTags.add(hashTagText);
                        hashTagsDisjunct.add(hashTagText);
                        MapUtils.putIntoListMap(hashTagText, tweet, DataCache.instance().hashTagToTweet);
                    }
                MapUtils.putIntoListMap(tweetTimeStamp, tweet, DataCache.instance().timeToTweet);

                // CONTENT
                String preprocessedText = p.tweetText(tweet.text);
                String contentVector = t.getContentVector(preprocessedText);
                
                tweet.contentVector = contentVector;

                MapUtils.putIntoListMap(contentVector, tweet, DataCache.instance().contentVectorToTweet);
                
                for(String token:contentVector.split(" "))
                    MapUtils.putIntoListMap(token, tweet, DataCache.instance().contentVectorTokenToTweet);

                // System.out.println(tweet.text);
                // System.out.println(preprocessedText);
                // System.out.println(contentVector);
                // System.out.println();
            }
        });

        // System.out.println(IoUtils.toColumnString(hashTagsDisjunct));
        System.out.println("timeStamp keys:         " + DataCache.instance().timeToTweet.keySet().size());
        System.out.println("searchTerm keys:        " + DataCache.instance().searchTermToTweet.keySet().size());
        System.out.println("expanded urls:          " + expandedUrls.size());
        System.out.println("expanded urls disjunct: " + expandedUrlsDisjunct.size());
        System.out.println("hashTags:               " + hashTags.size());
        System.out.println("hashTags disjunct:      " + hashTagsDisjunct.size());
//        System.out.println("search terms:");
//        System.out.println(IoUtils.toColumnString(DataCache.instance().searchTermToTweet.keySet()));
    }

    public static void printHashTagMappings() {
        int numMultiMappings = 0;
        System.out.println("hashTag mappings:");
        for (Entry<String, LinkedList<TweetDTO>> entry : DataCache.instance().hashTagToTweet.entrySet())
            if (entry.getValue().size() > 1) {
                HashSet<String> contents = new HashSet<>();
                for (TweetDTO tweet : entry.getValue())
                    contents.add(p.tweetText(tweet.text));
                if (contents.size() > 1) {
                    System.out.println((numMultiMappings++) + " " + entry.getValue().size() + " -> " + entry.getKey()
                            + " " + contents);
                    System.out.println(entry.getValue());
                    System.out.println();
                }
            }
    }

    private static void printContentVectorMappings() {
        int numMultiMappings = 0;
        System.out.println("content vector mappings:");
        for (Entry<String, LinkedList<TweetDTO>> entry : DataCache.instance().contentVectorToTweet.entrySet())
            if (entry.getValue().size() > 1) {
                HashSet<String> contents = new HashSet<>();
                for (TweetDTO tweet : entry.getValue())
                    contents.add(p.tweetText(tweet.text));
                if (contents.size() > 1) {
                    System.out.println((numMultiMappings++) + " " + entry.getValue().size() + " -> " + entry.getKey()
                            + " " + contents);
                    System.out.println(entry.getValue());
                    System.out.println();
                }
            }
    }
}
