import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class LogAnalyzerTest {

    File file;
    LogAnalyzer logAnalyzer;

    @BeforeEach
    void setUp() throws IOException {
        System.out.println();
        file = new File("src\\temp\\test.log");
        FileWriter writer = new FileWriter(file.getAbsolutePath());
        try {
            writer.write("NullPointerException" + "\n");
            writer.write("FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.flush();
        logAnalyzer = new LogAnalyzer("src\\temp\\", "Statistic.txt");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void whenParsingLogsItAddInfoToResultMap() throws IOException, InterruptedException {
        logAnalyzer.startAnalise();
        Map<String, Integer> map = new HashMap<>();
        map.put("NullPointerException", 1);
        map.put("FileNotFoundException", 1);
        Assert.assertEquals(logAnalyzer.getMap(), map);
    }

    @Test
    public void whenTryToParseEmptyFolderItShowMessageAboutMistake() throws IOException, InterruptedException {
        logAnalyzer.setFolderPath("1");
        Assert.assertFalse(logAnalyzer.startAnalise());
    }

    @Test
    public void whenParsingLogsItCreatesStatisticFile() throws IOException, InterruptedException {
        Arrays.stream(new File("src/temp").listFiles()).forEach(File::delete);
        logAnalyzer.startAnalise();
        File directory = new File(logAnalyzer.getFolderPath() + logAnalyzer.getResultFilename());
        Assert.assertTrue(directory.exists());
    }
}