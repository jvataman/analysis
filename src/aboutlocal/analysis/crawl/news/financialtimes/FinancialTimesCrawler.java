package aboutlocal.analysis.crawl.news.financialtimes;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import aboutlocal.analysis.data.dtos.NewsDTO;
import akka.dispatch.Future;
import akka.dispatch.Mapper;

import com.aboutlocal.crawlers.AbstractCrawler;
import com.aboutlocal.crawlers.AbstractCrawler.ProxyLevel;
import com.aboutlocal.hypercube.domain.dto.DTO;
import com.aboutlocal.hypercube.io.http.request.HttpResponse;

public class FinancialTimesCrawler extends AbstractCrawler{

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
                
                //March 29, 2012 12:27 pm
                //data
//                Date date = new Date(Long.parseLong(doc.select("cite.byline span.datestamp").attr("epoch")));
//                dto.date = date;
                dto.title = doc.select("div.fullstory h1").text();
                String gmtPlusOneString = doc.select("p.lastUpdated span.time").text();
                DateFormat df = new SimpleDateFormat("MMMM dd, yyyy kk:mm aa");
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                try {
                    Date date =  df.parse(gmtPlusOneString);
                    dto.date = date;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                
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
