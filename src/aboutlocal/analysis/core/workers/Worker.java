package aboutlocal.analysis.core.workers;

import java.util.ArrayList;
import java.util.Map;

import com.aboutlocal.hypercube.util.data.IoUtils;
import com.aboutlocal.hypercube.util.data.IoUtils.CountIterator;

import aboutlocal.analysis.core.util.MapUtils;
import aboutlocal.analysis.data.DataCache;


public abstract class Worker<J> implements Runnable {
    
    ArrayList<J> jobList = new ArrayList<>();
    private boolean isRunning = false;
    protected final CountIterator iterator = IoUtils.newCountIterator(this.getClass().getSimpleName()+"\t\t", 10000, 3300000);

    @Override
    public void run() {
        for(J job:jobList){
            executeJob(job);
            iterator.increment();
        }
        System.out.println("==> Thread done: "+this.getClass().getName());
    }
    
    protected abstract void executeJob(J job);

    public Worker<J> addJob(J job){
        if(!isRunning)
            jobList.add(job);
        else
            throw new IllegalStateException("Thread already running: "+this.getClass().getName());
        return this;
    }

}
