package aboutlocal.analysis.run;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import aboutlocal.analysis.core.TickFactory;
import aboutlocal.analysis.data.dtos.Tick;

public class TickGenerator implements Runnable{
    
    public static void main(String[] args) {
        new Thread(new TickGenerator()).run();
    }

    @Override
    public void run() {
        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(2012, 9, 16, 0, 0, 0);
        
        long start = cal.getTimeInMillis();
        cal.add(Calendar.HOUR, 3);
        long end = cal.getTimeInMillis();
        
        TickFactory factory = new TickFactory(1000*60*10, start, end);
        
        for(Tick tick:factory){
            System.out.println(tick);
        }
    }

}
