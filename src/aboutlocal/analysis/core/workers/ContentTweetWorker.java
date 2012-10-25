package aboutlocal.analysis.core.workers;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.aboutlocal.hypercube.util.data.IoUtils;
import com.aboutlocal.hypercube.util.data.IoUtils.CountIterator;

import aboutlocal.analysis.core.util.MapUtils;
import aboutlocal.analysis.data.DataCache;
import aboutlocal.analysis.data.dtos.TweetDTO;
import aboutlocal.analysis.preprocessing.lang.TextPreprocessor;
import aboutlocal.analysis.preprocessing.lang.Tokenizer;

public class ContentTweetWorker extends Worker<TweetDTO>{
    
    private final TextPreprocessor p = new TextPreprocessor();
    
    private final Boolean cvLock = new Boolean(true);
    private final Boolean cvtLock = new Boolean(true);
    
    private final ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(3300000);
    
    @Override
    public void run() {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(8, 8, 10, TimeUnit.SECONDS, workQueue);
        for(TweetDTO tweet:jobList){
            exec.submit(new PreprocessorWorker().addJob(tweet));
        }
        exec.shutdown();
        
        CountIterator countIterator = IoUtils.newCountIterator(this.getClass().getSimpleName()+"\t\t", 10000, exec.getQueue().size());
        int lastCount = 0;
        while(!exec.isTerminated()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while(lastCount++<=exec.getCompletedTaskCount())
                countIterator.increment();
        }
    }

    @Override
    protected void executeJob(TweetDTO tweet) {
        throw new IllegalStateException("this method must not be executed directly");
    }
    
    private class PreprocessorWorker extends Worker<TweetDTO>{
        
        private Tokenizer t = new Tokenizer();
        
        @Override
        public void run() {
            for(TweetDTO job:jobList){
                executeJob(job);
            }
        }

        @Override
        public void executeJob(TweetDTO tweet) {
            String preprocessedText = p.tweetText(tweet.text);
            String contentVector = t.getContentVector(preprocessedText);
            
            tweet.contentVector = contentVector;

            synchronized (cvLock) {
                MapUtils.putIntoListMap(contentVector, tweet, DataCache.instance().contentVectorToTweet);
            }
            
            synchronized (cvtLock) {
                for(String token:contentVector.split(" "))
                    MapUtils.putIntoListMap(token, tweet, DataCache.instance().contentVectorTokenToTweet);
            }
        }
        
    }

}
