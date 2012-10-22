package aboutlocal.analysis.crawl.news.financialtimes;

import com.aboutlocal.indexer.AbstractIndexerNAI;
import com.aboutlocal.indexer.branchenbuchmeinestadt.IndexerConfig;


public class FinantialTimesIndexer extends AbstractIndexerNAI {
    
    String visitRegex = "http://search.ft.com/.*&p=\\d+";
    String crawlRegex = "http://www.ft.com/cms/s/0/[\\w-]+.html";

    @Override
    protected IndexerConfig getConfig() {
        return IndexerConfig.standardConfig(FinancialTimesCrawler.class, 200, "FinancialTimesArchive");
    }

    @Override
    protected void preStart() {
    }

    @Override
    protected boolean shouldVisit(String url) {
        return url.matches(visitRegex);
    }

    @Override
    protected boolean shouldCrawl(String url) {
        return url.matches(crawlRegex);
    }

}
