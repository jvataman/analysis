package aboutlocal.analysis.preprocessing.lang;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class TextPreprocessor {

    public static void main(String[] args) {
        TextPreprocessor p = new TextPreprocessor();
        System.out.println(p.tweetText("Apple ♥ launching #iPhone5, way to late sux!, see http://bit.ly/A8U4ZA"));
        System.out.println(p.companyName("JA Solar Holdings, Co., Ltd."));
    }

    private final String[] companyNameExtentions = new String[] { "inc", "incorporated", "co", "company", "corp",
            "corporation", "bancorp","bankshares", "bancorporation", "nasd", "a", "b", "c", "ltd", "lls", "limited", "nv", "hld", "ca",
            "al", "ads", "group", "plc", "holding", "holdings", "nat", "national", "international", "pharmaceuticals",
            "nj", "techn", "technologies" };

    public String companyName(String companyName) {
        String normalized = companyName.toLowerCase().replace("\\d", "").replaceAll("[^\\w\\s]", "");
        for (String ext : companyNameExtentions)
            normalized = normalized.replaceAll("\\b" + ext + "\\b", "");

        normalized = normalized.replaceAll("( s)\\b", "s");
        normalized = normalized.replaceAll("\\b(u s)\\b", "us");
        normalized = normalized.replaceAll("(.*\\w+)com(\\b.*)", "$1$2");
        normalized = normalized.replaceAll("(.*\\d)\\s+(\\d.*)", "$1$2");
        normalized = normalized.replaceAll("^(the )", "");

        normalized = normalized.replaceAll("\\s{2,}", " ").trim();
        return normalized;
    }

    public String hashTag(String hashTag) {
        return hashTag.trim().toLowerCase().replace("_", "");
    }

    public String tweetText(String tweetText) {
        String text = "";
        // remove RT, repiles/mentions, hashtags and urls
        text = tweetText.toLowerCase().replaceAll("(rt)|#|(@[^\\s]+)|(http://[^\\s]+)", "")
                .replaceAll("[^\\p{ASCII}]", "").replaceAll("\\.+", " ").replaceAll("\\s{2,}", " ").trim();
        return text;
    }

    public String query(String query) {
        try {
            return URLDecoder.decode(query, "UTF-8").replace("\"", "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
