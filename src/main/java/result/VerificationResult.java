package result;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class VerificationResult {
    public static List<String> mistakes = new LinkedList<>();
    public static void writeInFile(String path){
        final String[] mistakesStr = {""};
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))){
            mistakes.forEach(x-> {
                mistakesStr[0] +=x+"\n";
            });
            writer.write(mistakesStr[0]);
            if (mistakesStr[0].equals(""))
                writer.write("Ошибок не обнаружено");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
