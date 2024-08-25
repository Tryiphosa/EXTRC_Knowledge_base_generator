    
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
        /*String directoryPath = "MonteCarloSimulationresults\\"; // Change this to your directory path
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
        }*/
        
        String inputFilePath = "MonteCarloCombinedResults.txt";  // Path to the input file
        String outputFilePath = "Youtput.txt"; // Path to the output file

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Split the line by '&'
                String[] elements = line.split("&");

                // Remove the 5th element (index 4)
                if (elements.length > 4) {
                    elements[4] = null;
                }

                // Rebuild the line without the 5th element
                StringBuilder newLine = new StringBuilder();
                for (int i = 0; i < elements.length; i++) {
                    if (elements[i] != null) {
                        newLine.append(elements[i]);
                        if (i < elements.length - 1) {
                            newLine.append("&");
                        }
                    }
                }

                // Write the modified line to the output file
                writer.write(newLine.toString());
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
