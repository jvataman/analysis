package aboutlocal.analysis.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import aboutlocal.analysis.data.dtos.NewsDTO;
import aboutlocal.analysis.data.dtos.QuoteDTO;
import aboutlocal.analysis.data.dtos.TweetDTO;
import aboutlocal.analysis.data.dtos.UserDTO;


public class DataCache {
    
    public final TreeMap<Long, LinkedList<TweetDTO>> timeToTweet = new TreeMap<>();
    public final HashMap<String, LinkedList<TweetDTO>> companyCodeToTweet = new HashMap<>();
    public final HashMap<String, LinkedList<TweetDTO>> resolvedUrlToTweet = new HashMap<>();
    public final HashMap<String, LinkedList<TweetDTO>> hashTagToTweet = new HashMap<>();
    public final HashMap<String, LinkedList<TweetDTO>> contentVectorToTweet = new HashMap<>();
    public final HashMap<String, LinkedList<TweetDTO>> contentVectorTokenToTweet = new HashMap<>();
    public final HashMap<String, LinkedList<TweetDTO>> searchTermToTweet = new HashMap<>();
    
    public final HashMap<String, Integer> unrecognizedSearchTerms = new HashMap<>();
    
    
    public final HashMap<String, UserDTO> userIdToUser = new HashMap<>();
    
    
    public final TreeMap<Long, QuoteDTO[]> timeToQuote = new TreeMap<>();
    public final HashMap<String, LinkedList<QuoteDTO>> companyCodeToQuote = new HashMap<>();
    
    
    public final TreeMap<Long, LinkedList<NewsDTO>> timeToNews = new TreeMap<>();
    public final TreeMap<String, LinkedList<NewsDTO>> contentVectorToNews = new TreeMap<>();
    public final TreeMap<String, LinkedList<NewsDTO>> contentVectorTokenToNews = new TreeMap<>();
    
    
    public final HashMap<String, String[]> companyNameTokenToCompanyCode = new HashMap<>();
    public final HashMap<String, String> companyNameToCompanyCode = new HashMap<>();
    public final HashMap<String, String> companyCodeToCompanyName = new HashMap<>();
    
    private static DataCache instance;
    
    private DataCache(){
        //singleton
    }
    
    public static DataCache instance(){
        return instance==null?(instance = new DataCache()):instance;
    }
    
}
