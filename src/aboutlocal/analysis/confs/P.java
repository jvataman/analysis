package aboutlocal.analysis.confs;

/**
 * Static class containing Paths to used dirs
 * @author Josef Vataman <josef.vataman@about-local.com>
 *
 */
public class P {
    
    private final static String SEP = System.getProperty("file.separator");
    
    public static final String BASE = "/home/jossi/thesis/";
    
    public static class QUOTES{

        public static final String ROOT = BASE+"quotes"+SEP;
        public static final String STRUCT = ROOT+"struct"+SEP;
        public static final String TEST = ROOT+"test"+SEP;
        
    }
    
    public static class TWEETS{

        public static final String ROOT = BASE+"tweets"+SEP;
        public static final String DISJUNCT = ROOT+SEP+"disjunct"+SEP;
        
    }
    
    public static class RESOURCES{

        public static final String ROOT = "resources"+SEP;
        public static final String COMPANYLIST = ROOT+"companylist.csv";
        
    }

}
