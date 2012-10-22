package aboutlocal.analysis.crawl.news.bloomberg;

import com.aboutlocal.indexer.AbstractIndexerNAI;
import com.aboutlocal.indexer.branchenbuchmeinestadt.IndexerConfig;


public class BloomBergIndexer extends AbstractIndexerNAI {
    
    String visitRegex = ".*/news/2012-03-\\d\\d/";
    String crawlRegex = ".*/news/2012-03-\\d\\d/.+";

    @Override
    protected IndexerConfig getConfig() {
        return IndexerConfig.standardConfig(BloomBergCrawler.class, 200, "BloomBergNewsArchive");
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
