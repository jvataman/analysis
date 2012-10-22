package aboutlocal.analysis.preprocessing.quotes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import aboutlocal.analysis.data.dtos.QuoteDTO;

import com.aboutlocal.hypercube.io.fs.DTOHandler;

public class TimeSeriesCreator {

    private final HashMap<String, ArrayList<QuoteDTO>> companyTimeSeries = new HashMap<>();
    private int cout;

    public static void main(String[] args) {
        try {
            new TimeSeriesCreator().read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void read() throws IOException {
        DTOHandler handler = new DTOHandler();
        ArrayList<File> files = handler.findFiles("C:\\thesis\\quotes", ".*2012", false);
        BufferedReader buf;
        for (File file : files) {
            buf = new BufferedReader(new FileReader(file));
            String[] line = null;
            int lineCount = 0;
            while (buf.ready()) {
                lineCount++;
                line = buf.readLine().replaceAll("\t", "").split("\\|");
                if (line.length < 10) // 14, actually
                    continue;
                ArrayList<QuoteDTO> tsDTOList = companyTimeSeries.get(line[2]);
                if (tsDTOList == null)
                    companyTimeSeries.put(line[2], (tsDTOList = new ArrayList<>()));
                try {
                    tsDTOList.add(new QuoteDTO(Long.parseLong(file.getName().replaceAll("_.*", "")), line[2], Double
                            .parseDouble(line[1].replace(",", ".")), Double.parseDouble(line[6])));
                } catch (Exception e) {
//                    System.err.println("failed @ "+file.getName()+", line "+lineCount);
                }
            }
            System.out.println(cout++);
//            System.out.println(companyTimeSeries.keySet().size());
//            for(String key:companyTimeSeries.keySet()){
//                System.out.println(companyTimeSeries.get(key).size());
//                break;
//            }
        }
    }
}
