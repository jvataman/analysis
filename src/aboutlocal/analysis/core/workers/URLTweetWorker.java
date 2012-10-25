package aboutlocal.analysis.core.workers;

import aboutlocal.analysis.core.util.MapUtils;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.TweetDTO;
import aboutlocal.analysis.data.dtos.TweetDTO.UrlTag;

public class URLTweetWorker extends Worker<TweetDTO>{
    
    @Override
    public void executeJob(TweetDTO tweet) {
        if (tweet.entities != null && tweet.entities.urls != null)
            for (UrlTag urlTag : tweet.entities.urls) {
                String expandedUrl = urlTag.expanded_url.toLowerCase();
                MapUtils.putIntoListMap(expandedUrl, tweet, DataCache.instance().resolvedUrlToTweet);
            }
    }

}
