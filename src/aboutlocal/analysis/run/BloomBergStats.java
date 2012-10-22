package aboutlocal.analysis.run;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONException;

import aboutlocal.analysis.data.dtos.NewsDTO;

import com.aboutlocal.hypercube.io.fs.DTOHandler;

public class BloomBergStats {
    
    private static final TreeMap<String, Integer> count = new TreeMap<>();
    
    public static void main(String[] args) throws JSONException, IOException {
        DTOHandler handler = new DTOHandler();
         List<NewsDTO> list = handler.deserializeGsonList(NewsDTO.class, handler.findFiles("", "BloomBergNewsArchive", false));
         for(NewsDTO dto:list){
             String key = dto.date.toString().replaceAll("(\\s\\d\\d:.*)|(\\w{3}\\s\\w{3}\\s)", "");
             count.put(key, count.get(key)==null?1:count.get(key)+1);
         }
         
         for(Entry<String, Integer> entry:count.entrySet())
             System.out.println(entry);
         System.out.println(count.size());
    }

}