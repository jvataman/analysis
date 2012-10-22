package aboutlocal.analysis.run;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import aboutlocal.analysis.confs.P;

import com.aboutlocal.hypercube.domain.dto.CsvDocument;
import com.aboutlocal.hypercube.domain.dto.CsvRow;
import com.aboutlocal.hypercube.domain.dto.CustomerDataDTO;
import com.aboutlocal.hypercube.io.fs.CsvDecoder;
import com.aboutlocal.hypercube.io.fs.DTOHandler;
import com.aboutlocal.hypercube.io.fs.DTOHandler.CharSet;
import com.aboutlocal.hypercube.io.fs.ExcelCsvEncoder;

public class CsvReaderTest {
    
    public static void main(String[] args) throws IOException, JSONException {
        CsvDecoder decoder = new CsvDecoder(";", "\"", "\"\"");
        CsvDocument csv = decoder.readCsv(P.RESOURCES.ROOT+"about_local_alle.csv");
        ArrayList<CustomerDataDTO> dtos = new ArrayList<>();
        for(CsvRow row:csv.getContents()){
            dtos.add(row.toDTO(CustomerDataDTO.class,"dewezet"));
        }
        DTOHandler handler = new DTOHandler("resources/formatted");
        handler.setEncoder(new ExcelCsvEncoder());
        handler.setOutCharsetEncoding(CharSet.ISO_8859_1);
        handler.serializeListToCsv(dtos);
        
        for(CustomerDataDTO dto:dtos){
            System.out.println(dto.toEM());
        }
    }

}
