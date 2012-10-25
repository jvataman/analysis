package aboutlocal.analysis.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
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
import aboutlocal.analysis.core.workers.ContentTweetWorker;
import aboutlocal.analysis.core.workers.HashTagTweetWorker;
import aboutlocal.analysis.core.workers.SearchTermTweetWorker;
import aboutlocal.analysis.core.workers.TimeStampTweetWorker;
import aboutlocal.analysis.core.workers.URLTweetWorker;
import aboutlocal.analysis.core.workers.Worker;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.NewsDTO;
import aboutlocal.analysis.data.dtos.QuoteDTO;
import aboutlocal.analysis.data.dtos.TweetDTO;
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

public class DataCacheCreatorPara {

    private final Gson gson = new Gson();

    private final DataCacheCreatorPara self = this;

    private static final Tokenizer t = new Tokenizer();
    private static final TextPreprocessor p = new TextPreprocessor();

    public static void main(String[] args) {
        new DataCacheCreatorPara().fillCache();
    }

    public DataCache fillCache() {
        System.out.println("FILLING CACHE");
        long start = System.currentTimeMillis();
        generateCompanyNameMapping();
        generateNewsMapping();
//        generateQuoteMapping();
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

    private void generateQuoteMapping() {
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

    private void generateNewsMapping(){
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
    
    private void generateTweetMapping() {
        System.out.println("generating {time,url,hashtag,contentVector} -> tweet mapping");
        final CountIterator inc = IoUtils.newCountIterator("reading tweets:", 1000000, 3300000);
        // XXX max filter
        final int[] max = { 100000 }, current = { 0 };
        final HashSet<String> expandedUrlsDisjunct = new HashSet<>();
        final LinkedList<String> expandedUrls = new LinkedList<>();
        final HashSet<String> hashTagsDisjunct = new HashSet<>();
        final LinkedList<String> hashTags = new LinkedList<>();

        @SuppressWarnings("unchecked")
        final ArrayList<Worker<TweetDTO>> workers = new ArrayList(Arrays.asList(new Worker[]{
                new ContentTweetWorker(),
                new HashTagTweetWorker(),
                new SearchTermTweetWorker(),
                new TimeStampTweetWorker(),
                new URLTweetWorker(),
        }));
        

        IoUtils.readDocument(P.TWEETS.DISJUNCT + "tweets", new LineParser() {

            @Override
            public void parseLine(String line) {

                inc.increment();

                if ((current[0] = current[0] + 1) > max[0])
                    stopFlag = true;

                TweetDTO tweet = gson.fromJson(line, TweetDTO.class);
                
                for(Worker<TweetDTO> worker:workers)
                    worker.addJob(tweet);
            }
        });
        
        ArrayList<Thread> threads = new ArrayList<>();
        for(Worker<TweetDTO> worker:workers){
            Thread workerThread = new Thread(worker);
            workerThread.start();
            threads.add(workerThread);
        }
        
        for(Thread t:threads)
            try {
                t.join();
            } catch (Exception e) {
                e.printStackTrace();
            }

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

    private static void printHashTagMappings() {
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
