package aboutlocal.analysis.preprocessing.quotes;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONException;

import aboutlocal.analysis.confs.P;
import aboutlocal.analysis.data.dtos.QuoteDTO;

import com.aboutlocal.hypercube.io.fs.DTOHandler;
import com.aboutlocal.hypercube.io.fs.DTOHandler.CharSet;
import com.aboutlocal.hypercube.io.fs.ExcelCsvEncoder;

public class QuotesToExcel {
    
    public static void main(String[] args) {
        new QuotesToExcel().transform();
    }

    private void transform() {
        DTOHandler handler = new DTOHandler();
        handler.setEncoder(new ExcelCsvEncoder());
        handler.setOutCharsetEncoding(CharSet.UTF_8);
        
        ArrayList<File> randomFile = handler.findFiles(P.QUOTES.STRUCT, ".*", false);
        
        List<QuoteDTO> list = new ArrayList<>();
        File file = null;
        try {
            file = randomFile.get((int) (randomFile.size()*Math.random()));
            list = handler.deserializeGsonList(QuoteDTO.class, file.getPath());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        
        ArrayList<ExcelRow> rows = new ArrayList<>();
        for(QuoteDTO dto:list){
            ExcelRow row = new ExcelRow();

            row.date = format.format(new Date(dto.getTimeStamp()));
            row.value = dto.getValue().toString().replace(".", ",");
            
            rows.add(row);
        }
        
        String outPath = P.QUOTES.TEST+file.getName();
        System.out.println("serializing "+list.size()+" to "+outPath);
        handler.fileName(outPath);
        try {
            handler.serializeListToCsv(rows);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        System.out.println("DONE");
        
    }
    
    public class ExcelRow{
        public String date;
        public String value;
        
        @Override
        public String toString() {
            return date+"\t"+value;
        }
    }

}
