package aboutlocal.analysis.confs;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class C {
    
    static{
        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(2012, 2, 1, 0, 0, 0);
        
        SURVEILLANCE_START = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        SURVEILLANCE_END = cal.getTimeInMillis();
    }

    private static final Integer SECOND = 1000, MINUTE = 60 * SECOND, HOUR = 60 * MINUTE, DAY = 24 * HOUR;
    
    public static final Integer TICK_SIZE_MILLIS = 10 * MINUTE;
    public static final Integer IMPACT_DELTA_MILLIS = 3 * HOUR;
    
    public static final Long SURVEILLANCE_START;
    public static final Long SURVEILLANCE_END;

}
