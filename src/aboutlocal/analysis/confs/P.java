package aboutlocal.analysis.confs;

/**
 * Static class containing Paths to used dirs
 * @author Josef Vataman <josef.vataman@about-local.com>
 *
 */
public class P {
    
    private final static String SEP = System.getProperty("file.separator");
    
    public static final String BASE = "/home/jossi/thesis/".replace("/", SEP);
//    public static final String BASE = "C:/thesis/".replace("/", SEP);
    
    public static class QUOTES{

        public static final String ROOT = BASE+"quotes"+SEP;
        public static final String STRUCT = ROOT+"struct"+SEP;
        public static final String TEST = ROOT+"test"+SEP;
        
    }
    
    public static class FEATURE_VECTORS{

        public static final String ROOT = BASE+"feature_vectors"+SEP;
        
    }
    
    public static class TWEETS{

        public static final String ROOT = BASE+"tweets"+SEP;
        public static final String DISJUNCT = ROOT+SEP+"disjunct"+SEP;
        public static final String USERS = ROOT+SEP+"users"+SEP;
        
    }
    
    public static class NEWS{

        public static final String ROOT = BASE+"news"+SEP;
        
    }
    
    public static class TICKS{

        public static final String ROOT = BASE+"ticks"+SEP;
        
    }
    
    public static class RESOURCES{

        public static final String ROOT = "resources"+SEP;
        public static final String COMPANYLIST = ROOT+"companylist.csv";
        
        public static class CORPORA{

            public static final String ROOT = RESOURCES.ROOT+"corpora"+SEP;
            public static final String REVIEW_POLARITY = ROOT+"review_polarity";
            public static final String ROTTEN_IMDB = ROOT+"rotten_imdb";
            
            public static class TWITTER_TAGGED{

                public static final String ROOT = RESOURCES.CORPORA.ROOT+"twitterTagged"+SEP;
                
                
            }
        }
        
        public static class MODELS{

            public static final String ROOT = RESOURCES.ROOT+"models"+SEP;
        }
        
    }

}
