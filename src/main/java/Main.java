import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
        long start = System.currentTimeMillis();

        //Initializing properties
        InputStream inputStream = new FileInputStream("src\\main\\resources\\application.properties");
        Properties prop = new Properties();
        prop.load(inputStream);

        String folderPath = prop.getProperty("logfile.folder_path");
        String resultFilename = prop.getProperty("result.file_name");

        LogAnalyzer logAnalyzer = new LogAnalyzer(folderPath, resultFilename);
        logAnalyzer.startAnalise();

        System.out.println("logs reading lasted for " + (System.currentTimeMillis() - start) + " ms");
    }



}
