package aboutlocal.analysis.data.dtos;

import java.util.ArrayList;

public class TweetDTO {
    
    public String query; //addition
    
    public String created_at;
    public Long created_at_timestamp; //addition
    public Long information_origination_timestamp;
    public String from_user;
    public String from_user_id_str;
    public String from_user_name;
    public String id_str;
    public String iso_language_code;
    public String profile_image_url;
    public String profile_image_url_https;
    public String source;
    public String text;
    
    public TweetEntityDTO entities;

    public Long from_user_id;
    public Long id;
    
    public MetaData metadata;
    
    public class TweetEntityDTO{
        public ArrayList<HashTag> hashtags;
        public ArrayList<UrlTag> urls;
        public ArrayList<UserMentionsTag> user_mentions;
    }
    
    public class MetaData{
        public String result_type;
    }
    
    public class HashTag{
        public String text;
        public ArrayList<Integer> indices;
    }
    
    public class UrlTag{
        public String url;
        public String expanded_url;
        public String display_url;
        public ArrayList<Integer> indices;
    }
    
    public class UserMentionsTag{
        public String screen_name;
        public String name;
        public String id_str;
        public Long id;
        public ArrayList<Integer> indices;
    }
    
}
