package aboutlocal.analysis.data.dtos;

import java.util.ArrayList;
import java.util.Date;

import com.aboutlocal.hypercube.util.data.Tuple;

/**
 * A timeslot containing all events in its time scope.
 * @author Josef Vataman <josef.vataman@about-local.com>
 *
 */
public class Tick {
    
    public String companyCode = "ALL_COMPANIES";
    
    public final Tuple<Long, Long> timeFrame = new Tuple<>();
    
    public final ArrayList<TweetDTO> tweets = new ArrayList<>();
    
    public final ArrayList<QuoteDTO> quotes = new ArrayList<>();
    
    @Override
    public String toString() {
        if(timeFrame.getX()==null || timeFrame.getY()==null)
            return "not initialized";
        return "<"+new Date(timeFrame.getX()).toString()+","+new Date(timeFrame.getY()).toString()+">"+" #t: "+tweets.size()+", #q: "+quotes.size();
    }

}
