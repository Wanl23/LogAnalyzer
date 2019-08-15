import lombok.Getter;
import lombok.Setter;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class LogAnalyzer {

    private final Pattern pattern = Pattern.compile("\\W*((?)Exception(?-i))\\W*");

    private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

    private String folderPath;
    private String resultFilename;

    public LogAnalyzer(String folderPath, String resultFilename) {
        this.folderPath = folderPath;
        this.resultFilename = resultFilename;
    }

    public boolean startAnalise() throws IOException, InterruptedException {
        //Collecting logs paths
        File folder = new File(folderPath);
        if (!folder.exists()) return false;
        List<String> logs = listFilesForFolder(folder);

        //Main work - reading logs files and getting exceptions
        readLogs(logs);

        //Writing result file
        if (!writeStatisticToFile(resultFilename, folder)) {
            System.out.println("There are no suitable files in your directory. Check your properties or folder for logs existing");
            return false;
        } else {
            //Some information about work
            System.out.println("Result contains: " + map);
            System.out.println("Result written to: " + folder.getAbsolutePath() + "\\" + resultFilename);
            return true;
        }
    }

    private void readLogs(List<String> logs) {
        logs.forEach(l -> {
                Thread thread = new Thread(() -> parseAndGetExceptions(l));
                thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private synchronized void parseAndGetExceptions(String log) {
        BufferedReader br;
        String thisLine;
        try {
            br = new BufferedReader(new FileReader(log));
            while (true) {
                if ((thisLine = br.readLine()) != null) {          //read line by line
                    String[] words = thisLine.split("\\W"); //split line by any symbols to words

                    Arrays.stream(words).forEach(w -> {
                        Matcher m = pattern.matcher(w);
                        if (m.find()                                //check if word contains necessary word
                                && !w.equals("Exception")) { //don't count word - "Exception" because it always exist in logs near of full name exception
                            Integer count = 0;
                            if (map.get(w) != null)
                                count = map.get(w); //check if we count this exception earlie and append to it, otherwise put new
                            map.put(w, ++count);
                        }
                    });
                } else break;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean writeStatisticToFile(String resultFilename, File folder) throws IOException {
        if (map.isEmpty()) {
            return false;
        }
        FileWriter writer = new FileWriter(folder.getAbsolutePath() + "\\" + resultFilename);
        map.forEach((key, value) -> {
            try {
                writer.write(key + " = " + value + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer.flush();
        writer.close();
        return true;
    }

    private List<String> listFilesForFolder(final File folder) {
        List<String> logs = new ArrayList<>();
        Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                .filter(l -> l.isFile() && l.getName().endsWith(".log"))
                .forEach(l -> logs.add(l.getAbsolutePath()));
        return logs;
    }
}
