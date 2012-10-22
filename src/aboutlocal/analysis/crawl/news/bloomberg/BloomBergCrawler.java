package aboutlocal.analysis.crawl.news.bloomberg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import aboutlocal.analysis.data.dtos.NewsDTO;
import akka.dispatch.Future;
import akka.dispatch.Mapper;

import com.aboutlocal.crawlers.AbstractCrawler;
import com.aboutlocal.hypercube.domain.dto.DTO;
import com.aboutlocal.hypercube.io.http.request.HttpResponse;

public class BloomBergCrawler extends AbstractCrawler{

    @Override
    public Future<List<DTO>> crawl(String url) {
        System.out.println("received "+url);
        return getHtml(url).map(new Mapper<HttpResponse<String>, List<DTO>>() {

            @Override
            public List<DTO> apply(HttpResponse<String> response) {
                ArrayList<DTO> dtos = new ArrayList<>();
                NewsDTO dto = new NewsDTO();
                dtos.add(dto);
                
                Document doc = Jsoup.parse(response.getContent());
                
                //data
                Date date = new Date(Long.parseLong(doc.select("cite.byline span.datestamp").attr("epoch")));
                dto.date = date;
                dto.title = doc.select("div#disqus_title").text();
                dto.story = doc.select("div#story_display").text();
                
                System.err.println(dto.title);
                System.err.println(dto.date);
                
                return dtos;
            }
            
        });
    }

    @Override
    protected ProxyLevel getStandardProxyLevel() {
        return ProxyLevel.DIRECT;
    }

}
