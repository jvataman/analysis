package aboutlocal.analysis.crawl.news.financialtimes;


public class FinantialTimes {
    
    private static final String baseUrl = "http://search.ft.com/?f=format[%22^Articles%24%22][%22Articles%22]&f=gadatetimearticle[2012-03-01T00%3A00%3A00%2C2012-04-01T23%3A59%3A59]&p=$";
    
    public static void main(String[] args) {
        FinantialTimesIndexer indexer = new FinantialTimesIndexer();
        
        for(Integer i=2;i<400;i++)
            indexer.addSeed(baseUrl.replace("$", i.toString()));
        
        indexer.run();
    }

}
