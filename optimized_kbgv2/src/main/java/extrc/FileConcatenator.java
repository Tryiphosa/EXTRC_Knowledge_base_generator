    
package extrc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileConcatenator{

    public static void main(String[] args) {
        String directoryPath = "MonteCarloSimulationresults\\"; // Change this to your directory path
        String outputFilePath = "MonteCarloCombinedResults.txt"; // Change this to your desired output file path

        try {
            File directory = new File(directoryPath);
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt")); // Filter text files

            if (files != null) {
                try (FileWriter writer = new FileWriter(outputFilePath)) {
                    for (File file : files) {
                        Files.lines(file.toPath()).forEach(line -> {
                            try {
                                writer.write(line + "\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            } else {
                System.out.println("No files found in the directory.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
    }
}
