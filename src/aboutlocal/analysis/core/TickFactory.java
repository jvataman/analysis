package aboutlocal.analysis.core;

import java.util.Iterator;

import aboutlocal.analysis.data.dtos.Tick;

public class TickFactory implements Iterable<Tick>{
    
    private long tickDuration;
    private long startTime;
    private long currentTime;
    private long endTime;
    
    private final TickFactory self = this;
    
    private final Iterator<Tick> iterator = new Iterator<Tick>() {
        @Override
        public void remove() {
            self.remove();
        }
        @Override
        public Tick next() {
            return self.next();
        }
        @Override
        public boolean hasNext() {
            return self.hasNext();
        }
    };
    
    public TickFactory(long tickDuration, long startTime, long endTime) {
        super();
        this.tickDuration = tickDuration;
        this.startTime = this.currentTime = startTime;
        this.endTime = endTime;
    }

    private final Tick createNext(){
        if(!hasNext())
            return null;
        Tick tick = new Tick();
        tick.timeFrame.setX(currentTime).setY(currentTime+tickDuration);
        currentTime += tickDuration;
        return tick;
    }
    
    public void reset(){
        this.currentTime = this.startTime;
    }
    
    public boolean hasNext() {
        return endTime - currentTime >= tickDuration;
    }

    public Tick next() {
        return createNext();
    }

    public void remove() {
        // not implemented
    }
    
    
    
    public long getTickDuration() {
        return tickDuration;
    }

    public void setTickDuration(long tickDuration) {
        this.tickDuration = tickDuration;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public Iterator<Tick> iterator() {
        return iterator;
    }

}
