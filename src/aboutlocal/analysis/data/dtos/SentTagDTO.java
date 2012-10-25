package aboutlocal.analysis.data.dtos;

public class SentTagDTO {
    
    public String topic,sentiment,tweetId;
    
    @Override
    public String toString() {
        return topic+"\t"+sentiment+"\t"+tweetId;
    }

}
