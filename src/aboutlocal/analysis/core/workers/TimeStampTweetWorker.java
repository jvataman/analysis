package aboutlocal.analysis.core.workers;

import com.google.gson.Gson;

import aboutlocal.analysis.core.util.MapUtils;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.TweetDTO;

public class TimeStampTweetWorker extends Worker<TweetDTO>{

    @Override
    public void executeJob(TweetDTO tweet) {
        try {
            MapUtils.putIntoListMap(tweet.created_at_timestamp, tweet, DataCache.instance().timeToTweet);
        } catch (Exception e) {
            System.err.println(new Gson().toJson(tweet));
            e.printStackTrace();
        }
    }

}
