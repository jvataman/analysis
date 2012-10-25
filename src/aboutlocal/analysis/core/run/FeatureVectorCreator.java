package aboutlocal.analysis.core.run;

import java.util.LinkedList;
import java.util.SortedMap;

import aboutlocal.analysis.confs.C;
import aboutlocal.analysis.core.DataCacheCreator;
import aboutlocal.analysis.core.TickFactory;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.QuoteDTO;
import aboutlocal.analysis.data.dtos.Tick;
import aboutlocal.analysis.data.dtos.TweetDTO;

public class FeatureVectorCreator {
    
    public static void main(String[] args) {
        TickFactory tickFactory = tickFactory();
        new DataCacheCreator().fillCache();
        
        int ticksTotal = 0;
        System.out.println("merging: t: "+DataCache.instance().timeToTweet.size()+" q: "+DataCache.instance().timeToQuote.size());
        for(Tick tick:tickFactory){
            SortedMap<Long, LinkedList<TweetDTO>> subMapTweets = DataCache.instance().timeToTweet.subMap(tick.timeFrame.getX(), tick.timeFrame.getY());
            SortedMap<Long, QuoteDTO[]> subMapQuotes = DataCache.instance().timeToQuote.subMap(tick.timeFrame.getX()+C.IMPACT_DELTA_MILLIS, tick.timeFrame.getY()+C.IMPACT_DELTA_MILLIS);
            if(subMapTweets.size()>0 && subMapQuotes.size()>0){
                ticksTotal++;
                int valueCountTweets = 0;
                for(LinkedList<TweetDTO> val :subMapTweets.values())
                    valueCountTweets+=val.size();
                System.out.println("t: "+valueCountTweets+" -> "+subMapTweets.keySet());
                int valueCountQuotes = 0;
                for(QuoteDTO[] val :subMapQuotes.values())
                    valueCountQuotes+=val.length;
                System.out.println("q: "+valueCountQuotes+" -> "+subMapTweets.keySet());
            }
        }
        System.out.println(ticksTotal);
    }
    
    private static TickFactory tickFactory(){
        TickFactory tickFactory = new TickFactory(C.TICK_SIZE_MILLIS, C.SURVEILLANCE_START, C.SURVEILLANCE_END);
        return tickFactory;
    }

}
