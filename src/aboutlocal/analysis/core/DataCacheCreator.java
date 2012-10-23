package aboutlocal.analysis.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scala.actors.threadpool.Arrays;
import aboutlocal.analysis.confs.P;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.QuoteDTO;
import aboutlocal.analysis.data.dtos.TweetDTO;

import com.aboutlocal.hypercube.domain.dto.CsvDocument;
import com.aboutlocal.hypercube.domain.dto.CsvRow;
import com.aboutlocal.hypercube.io.fs.CsvDecoder;
import com.aboutlocal.hypercube.io.fs.DTOHandler;
import com.aboutlocal.hypercube.util.data.IoUtils;
import com.aboutlocal.hypercube.util.data.IoUtils.CountIterator;
import com.aboutlocal.hypercube.util.data.IoUtils.LineParser;
import com.google.gson.Gson;

public class DataCacheCreator {

    private final String[] companyNameExtenttions = new String[] { "inc", "incorporated", "company", "corp",
            "corporation", "bancorp", "bancorporation", "nasd", "a", "b", "c", "ltd", "limited", "nv", "hld", "ca",
            "al", "ads", "group", "plc", "holding", "holdings", "national", "nat", "bankshares", "nj" };
    private final Gson gson = new Gson();

    private final DataCacheCreator self = this;

    public static void main(String[] args) {
        new DataCacheCreator().fillCache();
    }

    public DataCache fillCache() {
//        generateCompanyNameMapping();
//        generateQuoteMapping();
        generateTweetMapping();

        return DataCache.instance();
    }

    private void generateQuoteMapping() {
        System.out.println("generating {time,CODE} <-> quote mapping");
        ArrayList<File> files = new DTOHandler().findFiles(P.QUOTES.STRUCT, ".*", false);
        final CountIterator inc = IoUtils.newCountIterator("reading quote-files:", 100, 2594);
        final CountIterator inc2 = IoUtils.newCountIterator("reading quotes:", 1000000);
        final TreeMap<String, String> nonIndexed = new TreeMap<>();
        for (final File file : files) {
            inc.increment();
            final String companyName = normalizeCompanyName(file.getName().replace("_", " "));
            final String[] code = {DataCache.instance().companyNameToCompanyCode.get(companyName)};
            if (code[0] == null) {
                // nonIndexed.put(companyName, file.getName().replace("_",
                // " "));
                ArrayList<ArrayList<String>> candidates = new ArrayList<>();
                for (String token : companyName.split(" ")) {
                    String[] tokenResult = DataCache.instance().companyNameTokenToCompanyCode.get(token);
                    if (tokenResult != null) {
//                        System.out.println(companyName + " > " + token + " > " + Arrays.asList(tokenResult));
                        candidates.add(new ArrayList<String>(Arrays.asList(tokenResult)));
                    }
                }
                if (candidates.size() > 1) {
                    ArrayList<String> intersection = candidates.get(0);
                    for (ArrayList<String> set : candidates) {
                        intersection.retainAll(set);
                    }
                    if (intersection.size() == 1)
                        code[0] = intersection.get(0);
                }
            }
            IoUtils.readDocument(file.getPath(), new LineParser() {
                @Override
                public void parseLine(String line) {
                    // inc2.increment();
                    QuoteDTO quote = gson.fromJson(line, QuoteDTO.class);
                    Long key = quote.getTimeStamp();

                    // self.putIntoArrMap(key, quote,
                    // DataCache.instance().timeToQuote);
                    if (code != null)
                        self.putIntoListMap(code[0], quote, DataCache.instance().companyCodeToQuote);
                    else
                        nonIndexed.put(companyName, file.getName().replace("_", " "));
                }
            });
        }
        System.out.println(IoUtils.toColumnString(nonIndexed.entrySet()));
        System.out.println("indexed time  -> quote: " + DataCache.instance().timeToQuote.size());
        System.out.println("indexed CODE -> quote: " + DataCache.instance().companyCodeToQuote.size());
        System.out.println("nonIndexed cName -> quote: " + nonIndexed.size());
    }

    public void generateCompanyNameMapping() {
        System.out.println("generating {cName,Tokens} <-> CODE mapping");
        try {
            CsvDocument csv = new CsvDecoder().readCsv(P.RESOURCES.COMPANYLIST);
            System.out.println(csv.getContents().size());
            for (CsvRow row : csv.getContents()) {
                String name = normalizeCompanyName(row.get("Name")), sym = row.get("Symbol").trim();
                DataCache.instance().companyNameToCompanyCode.put(name, sym);
                DataCache.instance().companyCodeToCompanyName.put(sym, name);
                for (String token : name.split(" "))
                    putIntoArrMap(token, sym, DataCache.instance().companyNameTokenToCompanyCode);
            }
            System.out.println(IoUtils.toColumnString(DataCache.instance().companyNameToCompanyCode.entrySet()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateTweetMapping() {
        System.out.println("generating time -> tweet mapping");
        final CountIterator inc = IoUtils.newCountIterator("reading tweets:", 100000,3300000);
        IoUtils.readDocument(P.TWEETS.DISJUNCT + "tweets", new LineParser() {

            @Override
            public void parseLine(String line) {
                inc.increment();
                TweetDTO tweet = gson.fromJson(line, TweetDTO.class);
                Long tweetTimeStamp = tweet.created_at_timestamp;
                self.putIntoListMap(tweetTimeStamp, tweet, DataCache.instance().timeToTweet);
                try{
                    //tweet urls
                }catch(Exception e){
                    
                }
                self.putIntoListMap(tweetTimeStamp, tweet, DataCache.instance().timeToTweet);
            }
        });

        System.out.println("keys: " + DataCache.instance().timeToTweet.keySet().size());
        final CountIterator inc2 = IoUtils.newCountIterator("iterating through tweets:", 100000);
        for (long ts : DataCache.instance().timeToTweet.keySet()) {
            inc2.increment();
            DataCache.instance().timeToTweet.get(ts);
        }
    }

    @SuppressWarnings("unchecked")
    private <K, V> void putIntoArrMap(K key, V value, Map<K, V[]> map) {
        V[] list = map.get(key);
        if (list == null) {
            list = (V[]) Array.newInstance(value.getClass(), 1);
            list[0] = value;
        } else {
            V[] newList = (V[]) Array.newInstance(value.getClass(), list.length + 1);
            System.arraycopy(list, 0, newList, 0, list.length);
            newList[newList.length - 1] = value;
            list = newList;
        }
        map.put(key, list);
    }
    
    @SuppressWarnings("unchecked")
    private <K, V> void putIntoListMap(K key, V value, Map<K, LinkedList<V>> map) {
        LinkedList<V> list = map.get(key);
        if (list == null) {
            list = new LinkedList<V>();
        }
        list.add(value);
        map.put(key, list);
    }

    private String normalizeCompanyName(String companyName) {
        String normalized = companyName.toLowerCase().replace("\\d", "").replaceAll("[^\\w\\s]", "");
        for (String ext : companyNameExtenttions)
            normalized = normalized.replaceAll("\\b" + ext + "\\b", "");

        normalized = normalized.replaceAll("( s)\\b", "s");
        normalized = normalized.replaceAll("\\b(u s)\\b", "us");
        normalized = normalized.replaceAll("( com)\\b", "com");
        normalized = normalized.replaceAll("^(the )", "");
        Matcher mat = Pattern.compile("(.*\\d)\\s+(\\d.*)").matcher(normalized);
        if (mat.find())
            normalized = mat.replaceFirst("$1$2");

        normalized = normalized.replaceAll("\\s{2,}", " ").trim();
        return normalized;
    }

}
