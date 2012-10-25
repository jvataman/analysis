package aboutlocal.analysis.crawl.tweets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import aboutlocal.analysis.confs.P;
import aboutlocal.analysis.data.dtos.SentTagDTO;
import aboutlocal.analysis.data.dtos.TweetDTO;
import akka.dispatch.OnComplete;

import com.aboutlocal.crawlers.AbstractCrawler;
import com.aboutlocal.hypercube.domain.dto.CsvDocument;
import com.aboutlocal.hypercube.io.fs.CsvDecoder;
import com.aboutlocal.hypercube.io.fs.DTOHandler;
import com.aboutlocal.hypercube.io.http.SimpleHttpService;
import com.aboutlocal.hypercube.io.http.proxy.Proxy;
import com.aboutlocal.hypercube.io.http.request.HttpResponse;
import com.aboutlocal.hypercube.io.http.request.Request;
import com.aboutlocal.hypercube.system.akka.Container;
import com.aboutlocal.hypercube.util.concurrent.CountUpDownLatch;
import com.aboutlocal.hypercube.util.data.IoUtils;
import com.aboutlocal.hypercube.util.data.IoUtils.CountIterator;
import com.aboutlocal.hypercube.util.data.RandomUtils;
import com.aboutlocal.proxy.ProxySelector;

public class TweetsById {
    
    private static final BufferedWriter buffer = IoUtils.getBufferedWriter(P.RESOURCES.CORPORA.TWITTER_TAGGED.ROOT + "tweets2");

    private static final String API_URL = "http://api.twitter.com/1/statuses/show.json?id=[id]&include_entities=true";
    private static SimpleHttpService service = SimpleHttpService.instance();
    private static final CountUpDownLatch latch = new CountUpDownLatch();
    private final static CountIterator iterator = IoUtils.newCountIterator("pulled tweets: ", 10, 5514);
    
    private final static Container<List<Proxy>> container = new Container<>(null);

    public static void main(String[] args) throws JSONException, IOException {
        AbstractCrawler crawler = new TweetByIdCrawler();
        CsvDecoder decoder = new CsvDecoder(",", "\"", "\"\"");
        ArrayList<SentTagDTO> list = null;
        try {
            CsvDocument csv = decoder.readCsv(P.RESOURCES.CORPORA.TWITTER_TAGGED.ROOT + "twitterCorpus.csv");
            list = csv.toGenericObjects(SentTagDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<TweetDTO> knownTweets = (ArrayList<TweetDTO>) new DTOHandler().deserializeGsonList(TweetDTO.class, P.RESOURCES.CORPORA.TWITTER_TAGGED.ROOT + "tweets");
        ArrayList<String> knownIds = new ArrayList<>();
        for(TweetDTO knownTweet:knownTweets)
            knownIds.add(knownTweet.id_str);
            
        latch.setCount(list.size());
        
        int skipCount = 0;
        for (SentTagDTO dto : list){
            if(knownIds.contains(dto.tweetId)){
                System.out.println(++skipCount+" skipping "+dto.tweetId);
                continue;
            }
            crawl(API_URL.replace("[id]",dto.tweetId));
        }
        
        try {
            latch.await();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        try {
            buffer.close();
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
    
    private final static void crawl(final String url){
        service.execute(new Request(url).proxy(ProxySelector.getOwnProxy())).onComplete(new OnComplete<HttpResponse<String>>(){

            @Override
            public void onComplete(Throwable err, HttpResponse<String> resp) {
                if(err!=null || resp == null || resp.getContent() == null || resp.getContent().equals("") || resp.getContent().contains("error\":\"Rate limit exceeded.")){
                    crawl(url);
                }else{
                    String content = resp.getContent();
                    try {
                        synchronized (buffer) {
                            buffer.write(content+"\n");
                            buffer.flush();
                            iterator.increment();
                        }
                        latch.countDown();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            
        });
    }
    
    private static final Proxy randomProxy(){
        if(container.value()==null)
            try {
                container.value(new DTOHandler().deserializeGsonList(Proxy.class, "proxies.gsonlist"));
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        return RandomUtils.getRandom(container.value());
    }

}
