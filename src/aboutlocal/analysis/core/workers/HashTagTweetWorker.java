package aboutlocal.analysis.core.workers;

import aboutlocal.analysis.core.util.MapUtils;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.TweetDTO;
import aboutlocal.analysis.data.dtos.TweetDTO.HashTag;
import aboutlocal.analysis.data.dtos.TweetDTO.UrlTag;
import aboutlocal.analysis.preprocessing.lang.TextPreprocessor;

public class HashTagTweetWorker extends Worker<TweetDTO>{
    
    private final TextPreprocessor p = new TextPreprocessor();
    
    @Override
    public void executeJob(TweetDTO tweet) {
        if (tweet.entities != null && tweet.entities.hashtags != null)
            for (HashTag hashTag : tweet.entities.hashtags) {
                String hashTagText = p.hashTag(hashTag.text);
                MapUtils.putIntoListMap(hashTagText, tweet, DataCache.instance().hashTagToTweet);
            }
        MapUtils.putIntoListMap(tweet.created_at_timestamp, tweet, DataCache.instance().timeToTweet);
    }

}
