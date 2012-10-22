package aboutlocal.analysis.preprocessing.tweets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.JSONException;

import aboutlocal.analysis.confs.P;
import aboutlocal.analysis.data.dtos.TweetDTO;
import aboutlocal.analysis.data.dtos.TweetResponseDTO;

import com.aboutlocal.hypercube.io.fs.DTOHandler;
import com.google.gson.Gson;

public class DisjunctTweetsWriter {

    public static void main(String[] args) throws IOException, ParseException {
        new DisjunctTweetsWriter().writeDiscnuctTweets();
    }

    private void writeDiscnuctTweets() throws IOException, ParseException {
        HashMap<String, Integer> dateCounter = new HashMap<>();
        DTOHandler handler = new DTOHandler();
        ArrayList<File> files = handler.findFiles(P.TWEETS.ROOT, ".*", false);
        DateFormat inFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss ZZZZZ");
        DateFormat outFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(P.TWEETS.DISJUNCT+"tweets")));
        Gson gson = new Gson();
        HashSet<Long> uniquesIDs = new HashSet<>();
        int fileCounter = 0;
        for (File file : files) {
            System.out.println("reading "+file.getName());
            try {
                List<TweetResponseDTO> responses = handler.deserializeGsonList(TweetResponseDTO.class, file.getPath());
                for (TweetResponseDTO response : responses)
                    for (TweetDTO tweet : response.results) {
                        tweet.query = response.query;
                        Date createdAt = inFormat.parse(tweet.created_at);
                        tweet.created_at_timestamp = createdAt.getTime();
                        
                        String key = outFormat.format(createdAt);
                        Integer value = dateCounter.get(key);
                        dateCounter.put(key, value==null?1:value+1);
                        
                        
                        if (uniquesIDs.add(tweet.id))
                            writer.write(gson.toJson(tweet)+"\n");
                    }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            System.out.println(++fileCounter+" files, "+uniquesIDs.size()+" tweets, "+dateCounter.keySet().size()+" disjunct dates");
//            System.out.println(dateCounter);
        }
        writer.flush();
        writer.close();
        
        BufferedWriter statsWriter = new BufferedWriter(new FileWriter(new File(P.TWEETS.DISJUNCT+"dateCounts")));
        for(String date:dateCounter.keySet()){
            statsWriter.write(date+"\t"+dateCounter.get(date)+"\n");
        }
        statsWriter.flush();
        statsWriter.close();
        
        System.out.println("DONE");
    }
}
