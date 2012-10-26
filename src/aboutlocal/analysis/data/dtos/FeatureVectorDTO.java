package aboutlocal.analysis.data.dtos;

public class FeatureVectorDTO {

    public String companyCode = "";
    // sentiment average(-1:neg,0:{neutral, irrelevant},1:pos)
    public double sentiment = 0;
    // TODO
    public double subjectivity = 0;
    // sum of all tweets in tick timeframe
    public int tweetVolume = 0;
    // average oldest mapping via url or contentVector to tweet or news article
    public double informationAge = 0;
    // count hashtag references to all tweets in corpus for current
    // timframe(*+-x)?
    public int topicVolume = 0;
    // average combination of followers/listed
    public double authorExpertise = 0;

    // (newest value - oldest value)%
    public double valStart = 0;
    public double valEnd = 0;
    public String change = "";

}