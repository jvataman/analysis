package aboutlocal.analysis.data.dtos;

import java.util.ArrayList;

public class TweetResponseDTO {
    
    public Double completed_in;
    public Long max_id;
    public Integer page;
    public Integer results_per_page;
    public Long since_id;
    public String max_id_str;
    public String query;
    public String refresh_url;
    public String since_id_str;
    
    public ArrayList<TweetDTO> results;

}
