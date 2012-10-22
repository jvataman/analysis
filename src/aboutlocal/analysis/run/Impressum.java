package aboutlocal.analysis.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import akka.dispatch.Await;
import akka.util.Duration;

import com.aboutlocal.hypercube.io.http.SimpleHttpService;
import com.aboutlocal.hypercube.io.http.proxy.Proxy;
import com.aboutlocal.hypercube.io.http.request.Request;

public class Impressum implements Runnable{
    
    private static final String URL_PATH = "C:/cygwin/home/Jossi/normalizedUrls";
    private final SimpleHttpService http = SimpleHttpService.instance();
    
    public static void main(String[] args) {
        new Thread(new Impressum()).start();
    }

    @Override
    public void run() {
        for(String url:new UrlIterator()){
            try {
                Document page = Jsoup.parse(Await.result(http.execute(new Request(url).proxy(Proxy.NO_PROXY)), Duration.create(10, TimeUnit.SECONDS)).getContent());
                System.out.println(url);
                Elements element = page.select("p:contains(impressum)");
                Elements element2 = page.select("p:contains(kontakt)");
                Elements element3 = page.select("[href*=.*impressum.*]");
                Elements element4 = page.select("[href*=.*kontakt.*]");
                Elements element5 = page.select("frame");
                String out = element+"\n"+element2+"\n"+element3+"\n"+element4+"\n"+element5;
                System.out.println(out);
                if(out.replace("\n", "").trim().equals(""))
                    System.err.println(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    private class UrlIterator implements Iterable<String>{

        @Override
        public Iterator<String> iterator() {
            try {
                return new Iterator<String>() {
                    
                    BufferedReader reader = new BufferedReader(new FileReader(new File(URL_PATH)));

                    @Override
                    public boolean hasNext() {
                        try {
                            return reader.ready();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    public String next() {
                        try {
                            return reader.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public void remove() {
                        // TODO Auto-generated method stub
                        
                    }
                };
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        
    }
    

}
