import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogAnalyzer {

    private final Pattern pattern = Pattern.compile("\\W*((?)Exception(?-i))\\W*");

    private HashMap<String, Integer> map = new HashMap<>();

    private String folderPath;
    private String resultFilename;

    public LogAnalyzer(String folderPath, String resultFilename) {
        this.folderPath = folderPath;
        this.resultFilename = resultFilename;
    }

    public void startAnalise() throws IOException, InterruptedException {
        //Collecting logs paths
        File folder = new File(folderPath);
        List<String> logs = listFilesForFolder(folder);

        //Main work - reading logs files and getting exceptions
        readLogs(logs);

        //Writing result file
        writeStatisticToFile(resultFilename, folder, map);

        //Some information about work
        System.out.println("Result contains: " + map);
        System.out.println("Result written to: " + folder.getAbsolutePath() + "\\" + resultFilename);
    }

    private void readLogs(List<String> logs) throws InterruptedException {
        for (int i = 0; i < logs.size(); i++) {
            final String log = logs.get(i);
            Thread thread = new Thread(() -> parseAndGetExceptions(log));
            thread.start();
            thread.join();
        }
    }

    private void parseAndGetExceptions(String log) {
        BufferedReader br;
        String thisLine;
        try {
            br = new BufferedReader(new FileReader(log));
            while (true) {
                if ((thisLine = br.readLine()) != null) {          //read line by line
                    String[] words = thisLine.split("\\W"); //split line by any symbols to words

                    for (int j = 0; j < words.length; j++) {
                        Matcher m = pattern.matcher(words[j]);
                        if (m.find()                                //check if word contains necessary word
                                && !words[j].equals("Exception")) { //don't count word - "Exception" because it always exist in logs near of full name exception
                            Integer count = 0;
                            if (map.get(words[j]) != null) count = map.get(words[j]); //check if we count this exception earlie and append to it, otherwise put new
                            map.put(words[j], ++count);
                        }
                    }
                } else break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeStatisticToFile(String resultFilename, File folder, Map<String, Integer> map) throws IOException {
        FileWriter writer = new FileWriter(folder.getAbsolutePath() + "\\" + resultFilename);
        map.entrySet().forEach(log -> {
            try {
                writer.write(log.getKey() + " = " + log.getValue() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer.flush();
    }

    private List listFilesForFolder(final File folder) {
        List logs = new ArrayList();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                /**If you need, you can remove check for log's type
                 * just use only logs.add(fileEntry.getAbsolutePath())
                 */
                if (fileEntry.getName().endsWith(".log")) {
                    logs.add(fileEntry.getAbsolutePath());
                }
            }
        }
        return logs;
    }
}
