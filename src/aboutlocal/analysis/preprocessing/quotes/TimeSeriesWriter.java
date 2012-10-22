package aboutlocal.analysis.preprocessing.quotes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import aboutlocal.analysis.confs.P;
import aboutlocal.analysis.data.dtos.QuoteDTO;

import com.aboutlocal.hypercube.io.fs.DTOHandler;
import com.google.gson.Gson;

public class TimeSeriesWriter {

    private final HashMap<String, BufferedWriter> writers = new HashMap<>();
    private final Gson gson = new Gson();
    private int cout;

    public static void main(String[] args) {
        try {
            new TimeSeriesWriter().read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void read() throws IOException {
        DTOHandler handler = new DTOHandler();
        ArrayList<File> files = handler.findFiles(P.QUOTES.ROOT, ".*2012", false);
        BufferedReader buf;
        for (File file : files) {
            buf = new BufferedReader(new FileReader(file));
            String[] line = null;
            while (buf.ready()) {
                line = buf.readLine().replaceAll("\t", "").split("\\|");
                if (line.length < 10) // 14, actually
                    continue;

                try {
                    String name = parseName(line[2]);

                    if ("".equals(name))
                        continue;

                    Double value = stringToDouble(line[1]);
                    Double change = stringToDouble(line[6]);
                    Long timeStamp = Long.parseLong(file.getName().replaceAll("_.*", ""));

                    QuoteDTO dto = new QuoteDTO(timeStamp, name, value, change);
                    BufferedWriter writer = writers.get(name);
                    if (writer == null) {
                        writer = new BufferedWriter(new FileWriter(new File("C:/thesis/quotes/struct/" + name)));
                        writers.put(name, writer);
                    }
                    writer.write(gson.toJson(dto) + "\n");
                } catch (NumberFormatException e) {
                } catch (Exception e) {
                    e.printStackTrace();
                    // System.err.println("failed @ "+file.getName()+", line "+lineCount);
                }
            }
            System.out.println(cout++);
            // if(cout>10)
            // break;
        }
        for (BufferedWriter writer : writers.values()) {
            writer.flush();
            writer.close();
        }
    }

    private String parseName(String s) {
        String name = "";

        name = s.toLowerCase().replaceAll("\\W", " ").replaceAll("\\s+", " ").trim().replaceAll("\\s", "_");

        return name;
    }

    private Double stringToDouble(String s) {
        Double out = Double.parseDouble(s.replace(",", ".").replaceAll("[^\\d\\.-]", ""));
        return out;
    }
}
