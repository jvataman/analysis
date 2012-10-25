package aboutlocal.analysis.crawl.tweets;

import java.util.List;

import akka.dispatch.Future;

import com.aboutlocal.crawlers.AbstractCrawler;
import com.aboutlocal.hypercube.domain.dto.DTO;

public class TweetByIdCrawler extends AbstractCrawler{

    @Override
    public Future<List<DTO>> crawl(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ProxyLevel getStandardProxyLevel() {
        // TODO Auto-generated method stub
        return null;
    }
    
    

}
