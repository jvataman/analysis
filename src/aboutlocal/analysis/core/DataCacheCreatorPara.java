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
    private final DataCacheCreator sync = new DataCacheCreator();

    private static final Tokenizer t = new Tokenizer();
    private static final TextPreprocessor p = new TextPreprocessor();

    public static void main(String[] args) {
        new DataCacheCreatorPara().fillCache();
    }

    public DataCache fillCache() {
        System.out.println("FILLING CACHE");
        long start = System.currentTimeMillis();
        sync.generateCompanyNameMapping();
        sync.generateNewsMapping();
        sync.generateQuoteMapping();
        sync.generateCompanyNameMapping();
        sync.generateUserMapping();
        generateTweetMapping();
        System.out.println("CAHING DONE IN "+(System.currentTimeMillis()-start)/1000+" seconds");

        return DataCache.instance();
    }

    private void generateTweetMapping() {
        System.out.println("generating {time,url,hashtag,contentVector} -> tweet mapping");
        // XXX max filter
        final int[] max = { 20000 }, current = { 0 };
        final CountIterator inc = IoUtils.newCountIterator("reading tweets:", 1000000, max[0]);
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
}
