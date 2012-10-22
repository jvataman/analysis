package aboutlocal.analysis.crawl.news.bloomberg;

public class BloomBerg{
    
    private static final String baseUrl = "http://www.bloomberg.com/archive/news/2012-03-$/";
    
    public static void main(String[] args) {
        BloomBergIndexer indexer = new BloomBergIndexer();
        
        for(Integer i=1;i<31;i++)
            indexer.addSeed(baseUrl.replace("$", (i<10?"0"+i.toString():i.toString())));
        
        indexer.run();
    }

}
