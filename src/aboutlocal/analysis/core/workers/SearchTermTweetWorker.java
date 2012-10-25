package aboutlocal.analysis.core.workers;

import scala.actors.threadpool.Arrays;
import aboutlocal.analysis.core.util.MapUtils;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.TweetDTO;
import aboutlocal.analysis.preprocessing.lang.TextPreprocessor;

public class SearchTermTweetWorker extends Worker<TweetDTO>{
    
    private final TextPreprocessor p = new TextPreprocessor();

    @Override
    public void executeJob(TweetDTO tweet) {
        MapUtils.putIntoListMap(p.query(tweet.query), tweet, DataCache.instance().searchTermToTweet);
        String code = DataCache.instance().companyNameToCompanyCode.get(p.companyName(p.query(tweet.query)));
        if (code == null)
            code = MapUtils.getMaxIntersectedValueFromMap(
                    Arrays.asList(p.companyName(p.query(tweet.query)).split(" ")),
                    DataCache.instance().companyNameTokenToCompanyCode);
    }

}
