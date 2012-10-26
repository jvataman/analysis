package aboutlocal.analysis.crawl.tweets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import org.json.JSONException;

import aboutlocal.analysis.confs.P;
import aboutlocal.analysis.data.dtos.SentTagDTO;
import aboutlocal.analysis.data.dtos.TweetDTO;
import aboutlocal.analysis.data.dtos.UserDTO;
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
import com.aboutlocal.hypercube.util.data.IoUtils.LineParser;
import com.aboutlocal.hypercube.util.data.RandomUtils;
import com.aboutlocal.proxy.ProxySelector;
import com.google.gson.Gson;

public class UsersById {
    
    private static final BufferedWriter buffer = IoUtils.getBufferedWriter(P.TWEETS.USERS + "users2");

    private static final String API_URL = "http://api.twitter.com/1/users/show.json?user_id=[id]&include_entities=true";
    private static SimpleHttpService service = SimpleHttpService.instance();
    private static final CountUpDownLatch latch = new CountUpDownLatch(0);
    private final static CountIterator iterator = IoUtils.newCountIterator("pulled users: ", 10, 60000);
    private final static CountIterator readIterator = IoUtils.newCountIterator("read tweets: ", 100000);
    
    private static final Gson gson = new Gson();
    
    public static void main(String[] args) throws JSONException, IOException {
        final HashSet<String> knownIds = new HashSet<>();
        for(UserDTO user:new DTOHandler().deserializeGsonList(UserDTO.class, P.TWEETS.USERS+"users")){
            knownIds.add(user.id_str);
        }
        
        final TreeMap<String,Integer> userIdCounts = new TreeMap<>();
        IoUtils.readDocument(P.TWEETS.USERS+"topUsersSorted", new LineParser() {
            
            @Override
            protected void parseLine(String line) {
                if(knownIds.contains(line))
                    return;
                String url = API_URL.replace("[id]", line);
                latch.countUp();
                crawl(url);
            }
            
        });
        
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
        service.execute(new Request(url).proxy(ProxySelector.instance().getGaussianProxy())).onComplete(new OnComplete<HttpResponse<String>>(){

            @Override
            public void onComplete(Throwable err, HttpResponse<String> resp) {
                if(err!=null || resp == null || resp.getContent() == null || resp.getContent().equals("") || !resp.getContent().startsWith("{") ||resp.getContent().contains("error\":\"Rate limit exceeded.")){
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
    
}
