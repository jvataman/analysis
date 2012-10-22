package aboutlocal.analysis.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        Matcher mat = Pattern.compile("(\\d)\\s+(\\d)").matcher("11 21");
        if(mat.find())
            System.out.println(mat.replaceFirst("$1$2"));
        
    }
}
