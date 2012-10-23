package aboutlocal.analysis.preprocessing.lang;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextPreprocessor {
    
    public static void main(String[] args) {
        TextPreprocessor p = new TextPreprocessor();
        System.out.println(p.tweetText("Apple launching #iPhone5, way to late sux!, see http://bit.ly/A8U4ZA"));
    }
    
    private final String[] companyNameExtentions = new String[] { "inc", "incorporated", "company", "corp",
            "corporation", "bancorp", "bancorporation", "nasd", "a", "b", "c", "ltd", "limited", "nv", "hld", "ca",
            "al", "ads", "group", "plc", "holding", "holdings", "national", "nat", "bankshares", "nj" };
    
    public String companyName(String companyName){
        String normalized = companyName.toLowerCase().replace("\\d", "").replaceAll("[^\\w\\s]", "");
        for (String ext : companyNameExtentions)
            normalized = normalized.replaceAll("\\b" + ext + "\\b", "");

        normalized = normalized.replaceAll("( s)\\b", "s");
        normalized = normalized.replaceAll("\\b(u s)\\b", "us");
        normalized = normalized.replaceAll("( com)\\b", "com");
        normalized = normalized.replaceAll("^(the )", "");
        Matcher mat = Pattern.compile("(.*\\d)\\s+(\\d.*)").matcher(normalized);
        if (mat.find())
            normalized = mat.replaceFirst("$1$2");

        normalized = normalized.replaceAll("\\s{2,}", " ").trim();
        return normalized;
    }
    
    public String hashTag(String hashTag){
        return hashTag.trim().toLowerCase().replace("_", "");
    }
    
    public String tweetText(String tweetText){
        String text = "";
        //remove hashtags and urls
        text = tweetText.toLowerCase().replaceAll("(#[^\\s]+)|(http://[^\\s]+)", "").replaceAll("\\s{2,}", " ");
        return text;
    }

}
