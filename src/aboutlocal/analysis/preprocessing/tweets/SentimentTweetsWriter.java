package aboutlocal.analysis.preprocessing.tweets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONException;

import aboutlocal.analysis.confs.P;
import aboutlocal.analysis.core.util.MapUtils;
import aboutlocal.analysis.data.dtos.SentTagDTO;
import aboutlocal.analysis.data.dtos.TweetDTO;

import com.aboutlocal.hypercube.domain.dto.CsvDocument;
import com.aboutlocal.hypercube.domain.dto.CsvRow;
import com.aboutlocal.hypercube.io.fs.CsvDecoder;
import com.aboutlocal.hypercube.io.fs.DTOHandler;
import com.aboutlocal.hypercube.util.data.IoUtils;

public class SentimentTweetsWriter {
    
    private static final HashMap<String, LinkedList<String>> sentimentTweets = new HashMap<>();
    private static final HashMap<String, String> idToSentiment = new HashMap<>();
    private static final HashMap<String, String> numToSentiment = new HashMap<>();
    
    public static void main(String[] args) throws IOException {
        numToSentiment.put("0", "negative");
        numToSentiment.put("4", "positive");
        
        
        //read labels
        CsvDecoder decoder = new CsvDecoder(",", "\"", "\"\"");
        ArrayList<SentTagDTO> list = null;
        try {
            CsvDocument csv = decoder.readCsv(P.RESOURCES.CORPORA.TWITTER_TAGGED.ROOT + "twitterCorpus.csv");
            list = csv.toGenericObjects(SentTagDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(SentTagDTO dto:list)
            idToSentiment.put(dto.tweetId, dto.sentiment);
        
        //read tweets
        List<TweetDTO> tweets = null;
        try {
            tweets = new DTOHandler().deserializeGsonList(TweetDTO.class, P.RESOURCES.CORPORA.TWITTER_TAGGED.ROOT + "tweets");
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        
        for(TweetDTO tweet:tweets)
            MapUtils.putIntoListMap(idToSentiment.get(tweet.id.toString()), tweet.text.replaceAll("\n", " "), sentimentTweets);
        
        //read stanfordsentiment tweet corpus tweets
        CsvDocument csv = new CsvDecoder("§", "\"", "\"\"").readCsv(P.RESOURCES.CORPORA.TWITTER_TAGGED.ROOT+"sts_train.csv");
        for(CsvRow row:csv.getContents()){
            MapUtils.putIntoListMap(numToSentiment.get(row.get("sentiment")), row.get("text"), sentimentTweets);
        }
            
        for(Entry<String, LinkedList<String>> e:sentimentTweets.entrySet()){
            BufferedWriter writer;
            for(String text:e.getValue()){
                writer = IoUtils.getBufferedWriter(P.RESOURCES.CORPORA.TWITTER_TAGGED.ROOT+"/txt_sentoken/"+e.getKey()+"/"+System.currentTimeMillis());
                writer.write(text);
                writer.flush();
                writer.close();
            }
        }
        
    }

}
