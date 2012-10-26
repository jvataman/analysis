package aboutlocal.analysis.core.util;

import java.util.Collection;

public class MathUtils {
    
    /**
     * @param numbers
     * @return avg, 0 if numbers.isEmpty
     */
    public static double average(Collection<? extends Number> numbers){
        if(numbers.isEmpty())
            return 0.0d;
        double sum = 0;
        for(Number num:numbers)
            sum += num.doubleValue();
        return sum/numbers.size();
    }
    
    public static int sum(Collection<? extends Number> numbers){
        if(numbers.isEmpty())
            return 0;
        int sum = 0;
        for(Number num:numbers)
            sum += num.doubleValue();
        return sum;
    }
    
}
