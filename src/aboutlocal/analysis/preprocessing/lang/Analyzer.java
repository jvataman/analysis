package aboutlocal.analysis.preprocessing.lang;

import java.io.IOException;

import aboutlocal.analysis.confs.P;

import com.aliasi.classify.JointClassification;
import com.google.gson.Gson;

public class Analyzer {

    public static PolarityBasic polarity = null;
    private final TextPreprocessor p = new TextPreprocessor();

    public static enum Mood{
        NEGATIVE(-1),NEUTRAL(0),IRRELEVANT(0),POSITIVE(1);
        int level;
        private Mood(int level){
            this.level=level;
        }
        public int getRank(){
            return level;
        }
    }

    private static final Gson gson = new Gson();

    private static String[] testSentences = {
//        "@BryanaLaChae nooo, you gotta see it on! i don't want to spoil it! ?? but i'm wearing those steven madden heels you don't like!",
//        "steven madden gym shoe, prps jeans tommy coat, some fly ass shirt versace scarf #SWAGG",
//        "My mom just bought me my dream shoes! Been looking for nude flats for almost 2 year. Thank god for Steven Madden!! @MissGabriellaR",
//        "Finally got my Steven madden boots! ????",
//        "I've just fallen in love with this bag from Steven Madden: http://t.co/ULiF0FJa! Or maybe U like one of the other bags being offered!",
//        "My steven madden combat boots make me happy sooo..",
//        "Steven Madden Boots are soon to be mine!! http://t.co/NIxlPiwO",
//        "bought one pair of steven madden boots in store last night, and another online just now!! #shoelove! oops 6 pairs of shoes in 12 hours!!",
//        "I need those Steven madden loafers like now...",
//        "RT @LaCachetona_: On the steven madden website and wanting every pair of shoes possible #iwantyou",
//        "On the steven madden website and wanting every pair of shoes possible #iwantyou",
//        "I want to get these Steven madden combat boots!!? http://t.co/a14tQpmx",
//        "Huge breakout in #32 Steven madden $SHOO today.",
//        "@DSWShoeLovers So Steven Madden is in the house!!!!! Grand Opening 34th Street Store!",
//        "@jzonazari mama need new M•A•C cosmetics and Steven madden combat boots",
//        "I just got new steven madden boots :)",
//        "@lizschoonfield_ I almost bought the same ones yesterday at Steven Madden but they didn't have my size /:??",
//        "@LoveisBarefoot I thought the Steven madden store is gone",
//        "Rush Limbaugh Loses Sponsors After Sandra Fluke Remarks Enrage http://t.co/G1VOoMkU",
//        "Sleep Number Bed Select Comfort Corporation Fax: 763-551-7826",
//        "800-438-2233 - supports #RushLimbaugh vile attack on women.",
        "Microsoft Tightens Personal Data Rules"};

    public static void main(String[] args) {
        init();

        for (String text : testSentences) {
            JointClassification klass = polarity.mClassifier.classify(text);
            System.out.println(text + " -> " + klass.bestCategory() + ", " + gson.toJson(klass));
        }
    }

    private static void init() {
        if (polarity != null)
            return;
        polarity = new PolarityBasic(new String[] { P.RESOURCES.CORPORA.TWITTER_TAGGED.ROOT });
        try {
            polarity.train();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * also preprocesses the text using {@link #p}
     * @param tweetText
     * @return
     */
    public Mood classifyMood(String tweetText) {
        init();
        tweetText = p.tweetText(tweetText);
        JointClassification klass = polarity.mClassifier.classify(tweetText);

        if (klass.bestCategory().equals("negative"))
            return Mood.NEGATIVE;
        if (klass.bestCategory().equals("positive"))
            return Mood.POSITIVE;
        if (klass.bestCategory().equals("irrelevant"))
            return Mood.IRRELEVANT;
        
        return Mood.NEUTRAL;
    }
}
