package aboutlocal.analysis.core.run;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.SortedMap;

import com.aboutlocal.hypercube.util.data.IoUtils;
import com.google.gson.Gson;

import scala.actors.threadpool.Arrays;

import aboutlocal.analysis.confs.C;
import aboutlocal.analysis.confs.P;
import aboutlocal.analysis.core.DataCacheCreator;
import aboutlocal.analysis.core.TickFactory;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.FeatureVectorDTO;
import aboutlocal.analysis.data.dtos.QuoteDTO;
import aboutlocal.analysis.data.dtos.Tick;
import aboutlocal.analysis.data.dtos.TweetDTO;

public class FeatureVectorCreator {

    private final static Gson gson = new Gson();
    private final static ArrayList<Tick> ticks = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        new DataCacheCreator().fillCache();
        
        generateTicks();
        generateFeatureVectors();

    }

    private static TickFactory tickFactory() {
        TickFactory tickFactory = new TickFactory(C.TICK_SIZE_MILLIS, C.SURVEILLANCE_START, C.SURVEILLANCE_END);
        return tickFactory;
    }
    
    private static void generateFeatureVectors() {
        for(Tick tick:ticks){
            ArrayList<FeatureVectorDTO> fv = tickToVectors(tick);
        }
    }


    private static ArrayList<FeatureVectorDTO> tickToVectors(Tick tick) {
        ArrayList<FeatureVectorDTO> vectors = new ArrayList<>();
        
        //TODO
        
        return vectors;
    }

    private static void generateTicks() {
        BufferedWriter writer = IoUtils.getBufferedWriter(P.BASE + "ticks");
        int ticksTotal = 0;
        
        for(String companyCode:DataCache.instance().companyCodeToTweet.keySet()){
            int ticksForCode = 0;
            HashSet<TweetDTO> tweetsForCode = new HashSet<>();
            HashSet<QuoteDTO> quotesForCode = new HashSet<>();
            
            tweetsForCode.addAll(DataCache.instance().companyCodeToTweet.get(companyCode));
            quotesForCode.addAll(DataCache.instance().companyCodeToQuote.get(companyCode));
            
            System.out.println("merging tweet/quotes for "+companyCode+" set sizes -> "+tweetsForCode.size()+"/"+quotesForCode.size());
            
            TickFactory tickFactory = tickFactory();

            System.out.println("merging: t: " + DataCache.instance().timeToTweet.size() + " q: "
                    + DataCache.instance().timeToQuote.size());
            for (Tick tick : tickFactory) {
                SortedMap<Long, LinkedList<TweetDTO>> subMapTweets = DataCache.instance().timeToTweet.subMap(
                        tick.timeFrame.getX(), tick.timeFrame.getY());
                SortedMap<Long, QuoteDTO[]> subMapQuotes = DataCache.instance().timeToQuote.subMap(tick.timeFrame.getX()
                        + C.IMPACT_DELTA_MILLIS, tick.timeFrame.getY() + C.IMPACT_DELTA_MILLIS);
                if (subMapTweets.size() > 0 && subMapQuotes.size() > 0) {
                    ticksTotal++; ticksForCode++;
                    for (LinkedList<TweetDTO> val : subMapTweets.values())
                        tick.tweets.addAll(val);
                    for (QuoteDTO[] val : subMapQuotes.values())
                        tick.quotes.addAll(Arrays.asList(val));
                    
                    ticks.add(tick);

                    try {
                        writer.write(gson.toJson(tick) + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("ticks for this code: "+ticksForCode);
        }
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(ticksTotal);
    }

}
