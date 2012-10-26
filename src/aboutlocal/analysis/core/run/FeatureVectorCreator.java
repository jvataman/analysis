package aboutlocal.analysis.core.run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONException;

import aboutlocal.analysis.confs.C;
import aboutlocal.analysis.confs.P;
import aboutlocal.analysis.core.DataCacheCreatorPara;
import aboutlocal.analysis.core.TickFactory;
import aboutlocal.analysis.core.util.MathUtils;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.FeatureVectorDTO;
import aboutlocal.analysis.data.dtos.QuoteDTO;
import aboutlocal.analysis.data.dtos.Tick;
import aboutlocal.analysis.data.dtos.TweetDTO;
import aboutlocal.analysis.data.dtos.TweetDTO.HashTag;
import aboutlocal.analysis.data.dtos.TweetDTO.UrlTag;
import aboutlocal.analysis.data.dtos.UserDTO;
import aboutlocal.analysis.preprocessing.lang.Analyzer;
import aboutlocal.analysis.preprocessing.lang.TextPreprocessor;

import com.aboutlocal.hypercube.io.fs.CsvEncoder;
import com.aboutlocal.hypercube.io.fs.DTOHandler;
import com.aboutlocal.hypercube.io.fs.ExcelCsvEncoder;
import com.aboutlocal.hypercube.util.data.IoUtils;
import com.google.gson.Gson;

public class FeatureVectorCreator {

    private final static Gson gson = new Gson();
    private final static ArrayList<Tick> ticks = new ArrayList<>();
    private final static Analyzer analyzer = new Analyzer();

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        new DataCacheCreatorPara().fillCache();
        
        generateTicks();
        ArrayList<FeatureVectorDTO> vectors = generateFeatureVectors(ticks);
        
        try {
            DTOHandler handler = new DTOHandler(P.FEATURE_VECTORS.ROOT+"featurevectors");
            handler.setEncoder(new ExcelCsvEncoder());
            handler.serializeListToCsv(vectors);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static TickFactory tickFactory() {
        TickFactory tickFactory = new TickFactory(C.TICK_SIZE_MILLIS, C.SURVEILLANCE_START, C.SURVEILLANCE_END);
        return tickFactory;
    }
    
    private static ArrayList<FeatureVectorDTO> generateFeatureVectors(ArrayList<Tick> ticks) {
        ArrayList<FeatureVectorDTO> fvs = new ArrayList<>();
        for(Tick tick:ticks){
            FeatureVectorDTO fv = tickToVector(tick);
            fvs.add(fv);
        }
        return fvs;
    }


    private static FeatureVectorDTO tickToVector(Tick tick) {
        FeatureVectorDTO vector = new FeatureVectorDTO();
        
        ArrayList<Integer> sentiments = new ArrayList<>();
        ArrayList<Integer> subjectivities = new ArrayList<>();
        ArrayList<Long> informationAges = new ArrayList<>();
        ArrayList<Integer> authorExpertises = new ArrayList<>();
        ArrayList<Integer> topicCounts = new ArrayList<>();
        
        TreeMap<Long, Double> timeToValue = new TreeMap<>();
        
        for(TweetDTO tweet:tick.tweets){
            //sentiment
            sentiments.add(analyzer.classifyMood(tweet.text).getRank());
            
            //TODO subjectivities
            
            //info age
            if(tweet.entities!=null && tweet.entities.urls!=null)
            for(UrlTag urlTag:tweet.entities.urls){
                LinkedList<TweetDTO> list = DataCache.instance().resolvedUrlToTweet.get(urlTag.expanded_url);
                if(list!=null)
                for(TweetDTO linkedTweet:list){
                    long age = tweet.created_at_timestamp-linkedTweet.created_at_timestamp;
                    informationAges.add(age>0?age:0);
                }
                else
                    informationAges.add(0L);
                
                //TODO also chek for news and content vectors
            }
            
            //topicVolume
            if(tweet.entities!=null && tweet.entities.hashtags!=null)
                for(HashTag hashTag:tweet.entities.hashtags){
                    LinkedList<TweetDTO> list = DataCache.instance().hashTagToTweet.get(hashTag.text);
                    if(list!=null)
                        topicCounts.add(list.size());
                }
            
            //authorExpertises
            UserDTO user = DataCache.instance().userIdToUser.get(tweet.from_user_id_str);
            if(user!=null)
                authorExpertises.add(user.followers_count+2*user.listed_count);
            else
                //TODO find out real average
                authorExpertises.add(126);
        }
        for(QuoteDTO quote:tick.quotes){
            timeToValue.put(quote.getTimeStamp(), quote.getValue());
        }
        
        vector.sentiment = MathUtils.average(sentiments);
        vector.subjectivity = MathUtils.average(subjectivities);
        vector.tweetVolume = tick.tweets.size();
        vector.informationAge = MathUtils.average(informationAges);
        vector.topicVolume = MathUtils.sum(topicCounts);
        vector.authorExpertise = MathUtils.average(authorExpertises);
        
        double startVal = timeToValue.firstEntry().getValue(), endVal = timeToValue.lastEntry().getValue();
        vector.valStart = startVal;
        vector.valEnd = endVal;
        if(startVal<endVal)
            vector.change = "up";
        if(startVal>endVal)
            vector.change = "dn";
        if(startVal==endVal)
            vector.change = "nn";
        
        vector.companyCode = tick.companyCode;
        
        return vector;
    }

    private static void generateTicks() {
        int ticksTotal = 0;
        
        System.out.println("unrecognized tweetSearchTerms:");
        System.out.println(IoUtils.toColumnString(DataCache.instance().unrecognizedSearchTerms.entrySet()));;
        
        final String PATH = P.TICKS.ROOT + "TS"+C.TICK_SIZE_MILLIS/1000+"_ID"+C.IMPACT_DELTA_MILLIS/1000+"/";
        
        System.out.println("creating ticks, persisting to: "+PATH);
        File file = new File(PATH);
        file.mkdirs();
        
        BufferedWriter allWriter = IoUtils.getBufferedWriter(PATH+"ALL_COMPANIES_COUNTS");
        
        for(String companyCode:DataCache.instance().companyCodeToTweet.keySet()){
            int ticksForCode = 0;
            HashSet<TweetDTO> tweetsForCode = new HashSet<>();
            HashSet<QuoteDTO> quotesForCode = new HashSet<>();
            
            tweetsForCode.addAll(DataCache.instance().companyCodeToTweet.get(companyCode));
            LinkedList<QuoteDTO> quotes = DataCache.instance().companyCodeToQuote.get(companyCode);
            if(quotes!=null)
                quotesForCode.addAll(quotes);
            
            System.out.println("merging tweet/quotes for "+companyCode+" set sizes -> "+tweetsForCode.size()+"/"+quotesForCode.size());
            
            TickFactory tickFactory = tickFactory();

            System.out.println("merging: t: " + DataCache.instance().timeToTweet.size() + " q: "
                    + DataCache.instance().timeToQuote.size());
            
            BufferedWriter writer = IoUtils.getBufferedWriter(PATH+companyCode);
            for (Tick tick : tickFactory) {
                
                tick.companyCode = companyCode;
                
                SortedMap<Long, LinkedList<TweetDTO>> subMapTweets = DataCache.instance().timeToTweet.subMap(
                        tick.timeFrame.getX(), tick.timeFrame.getY());
                SortedMap<Long, QuoteDTO[]> subMapQuotes = DataCache.instance().timeToQuote.subMap(tick.timeFrame.getX(), tick.timeFrame.getY() + C.IMPACT_DELTA_MILLIS);
                if (subMapTweets.size() > 0 && subMapQuotes.size() > 0) {
                    ticksTotal++; ticksForCode++;
                    for (LinkedList<TweetDTO> val : subMapTweets.values())
                        for(TweetDTO tweet:val)
                            if(tweetsForCode.contains(tweet))
                                tick.tweets.add(tweet);
                    for (QuoteDTO[] val : subMapQuotes.values())
                        for(QuoteDTO quote:val)
                            if(quotesForCode.contains(quote))
                                 tick.quotes.add(quote);
                    
                    //XXX empty tweets are also a statement!!, so well save those too
                    if(!tick.quotes.isEmpty() && !tick.tweets.isEmpty()){
//                        XXX
                        ticks.add(tick);

                        try {
                            writer.write(gson.toJson(tick) + "\n");
                            allWriter.write(tick + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("ticks for this code: "+ticksForCode);
        }
        try {
            allWriter.flush();
            allWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ticks total: "+ticksTotal);
    }

}
