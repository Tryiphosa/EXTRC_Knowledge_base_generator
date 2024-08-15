    
package extrc;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;



public class MonteCarloSimulation {
    private static final int NUM_SIMULATIONS = 1000; // Number of simulations to run
    

    public static void main(String[] args) {
        Random random = new Random();
    
        for (int i = 0; i < NUM_SIMULATIONS; i++) {

            // Generate random inputs for the knowledge base generator
            String generatorType = getRandomGeneratorType(random);
            int numRanks = getRandomNumRanks(random);
            String defImplicationDistribution = getRandomDefImplicationDistribution(random);
            int min =minDefImplications(defImplicationDistribution, numRanks);
            int numDefImplications = getRandomNumDefImplications(random, min);
            int minStatementsPerRank = getRandomMinStatementsPerRank(random);
            String simpleDefImplications = getRandomAnswer(random);
            String reuseConsequent = getRandomAnswer(random);
            String complexity = getRandomComplexity(random)+"|"+getRandomComplexity(random);
            String transitivity = getRandomTransitivity(random);
            String connectiveTypes = getRandomConnectiveTypes(random);
            String characterSet = getRandomCharacterSet(random);

            // Generate text files.
            String OUTPUT_FILE = "MonteCarloSimulationOutput/"+generatorType+"_"+(i+1)+".txt"; // File to store results

            try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(OUTPUT_FILE))) { 
            // Save the generated data to a text file
                fileWriter.write(generatorType);
                fileWriter.newLine();
                fileWriter.write(Integer.toString(numRanks));
                fileWriter.newLine();
                fileWriter.write(defImplicationDistribution);
                fileWriter.newLine();
                fileWriter.write(Integer.toString(numDefImplications));
                fileWriter.newLine();
                fileWriter.write(Integer.toString(minStatementsPerRank));
                fileWriter.newLine();
                fileWriter.write(simpleDefImplications);
                fileWriter.newLine();
                fileWriter.write(reuseConsequent);
                fileWriter.newLine();
                if(simpleDefImplications.equalsIgnoreCase("n")){
                    fileWriter.write(complexity);
                    fileWriter.newLine();
                    fileWriter.write(connectiveTypes);
                    fileWriter.newLine();
                }
                fileWriter.write(transitivity);
                fileWriter.newLine();
                fileWriter.write(characterSet);
                fileWriter.newLine();
                fileWriter.write("n");
                fileWriter.newLine();
                fileWriter.write("n");
                fileWriter.newLine();
                fileWriter.write("n");
                fileWriter.newLine();
                fileWriter.write("q");
                fileWriter.newLine(); // Add an empty line between simulations
                fileWriter.flush(); // Ensure data is written to the file
                
                } catch (IOException e) {
                System.out.println("Error writing to file: " + e.getMessage());
            }

        }

   }
    private static String getRandomGeneratorType(Random random) { return "ov2";   }

    private static String getRandomAnswer(Random random) {
        String[] distributions = {"y", "n"};
        return distributions[random.nextInt(distributions.length)];
    }

    private static int getRandomNumRanks(Random random) { return 6 + random.nextInt(300);}

    private static String getRandomDefImplicationDistribution(Random random) {
        String[] distributions = {"f", "lg", "ld", "r", "n"};
        return distributions[random.nextInt(distributions.length)];
    }


    private static int getRandomNumDefImplications(Random random, int min) {
        return min + random.nextInt(50000); 
    }

    private static int getRandomMinStatementsPerRank(Random random) {
        return random.nextInt(1000);
    }

    private static int getRandomComplexity(Random random) {
        return 1 + random.nextInt(9);
    }

    private static String getRandomTransitivity(Random random) {
        String[] transitivityOptions = {"y", "n", "r"};
        return transitivityOptions[random.nextInt(transitivityOptions.length)];
    }

    private static String getRandomConnectiveTypes(Random random) {
        String connectives = ""; //{"1", "2", "3", "4", "5"};
        int choice = 1 + random.nextInt(15);
        switch (choice) {
            case 1:
                connectives="1";
                break;
            case 2:
                connectives="2";
                break;
            case 3:
                connectives="3";
                break;
            case 4:
                connectives="4";
                break;
            case 5:
                connectives="1,2,3,4";
                break;
            case 6:
                connectives="1,2";
                break;
            case 7:
                connectives="1,3";
                break;
            case 8:
                connectives="1,4";
                break;
            case 9:
                connectives="1,2,3";
                break;
            case 10:
                connectives="1,3,4";
                break;
            case 11:
                connectives="2,3";
                break;
            case 12:
                connectives="2,4";
                break;
            case 13:
                connectives="2,3,4";
                break;
            case 14:
                connectives="1,2,4";
                break;
        
            default:
                break;
        }
        return connectives;
    }

    private static String getRandomCharacterSet(Random random) {
        return "lowerlatin";
    }
 
    
   private static int minDefImplications(String distribution, int numRanks){
        int min = 0;
        switch(distribution){
            case "f":
                min = (numRanks*2)-1;
                break;
            case "lg":
                min = minDIsLinear(numRanks);
                break;
            case "ld":
                min = minDIsLinearDecline(numRanks);
                break;
            case "r":
                min = (numRanks*2);
                break;
            case "n":
                min =minDIsNormal(numRanks);
                break;
        }
        return min;
    }
    
    /**
     * Calculates the minimum number of DIs needed for a normal distribution.
     *
     * @param numRanks The number of ranks over which DIs are distributed.
     * @return The minimum number of DIs needed for a normal distribution.
     */
    public static int minDIsNormal(int numRanks) {
        int sum = 0;
        boolean odd = numRanks % 2 != 0; 
        int center;
    
        // Calculate the center index
        if (odd) {
            center = numRanks / 2 + 1;
        } else {
            center = numRanks / 2;
        }
    
        // Calculate one-third of the center rank
        int oneThirdRank = center / 3;
    
        // This function should be defined elsewhere in your code
        int oneThirdDIs = oneThirdRank*2;
    
        // Assuming minDIsLinear returns a value in the range needed, and applying the scaling
        sum = (oneThirdDIs * 100) / 5; 
    
        return sum + 2*numRanks;
    }

    /**
     * Calculates the minimum number of DIs needed for a linear-growth distribution.
     *
     * @param numRanks The number of ranks over which DIs are distributed.
     * @return The minimum number of DIs needed for a linear-growth distribution.
     */
    public static int minDIsLinear(int numRanks){
        int sum = numRanks * (numRanks + 1) / 2;
        return sum;
    }

    /**
     * Calculates the minimum number of DIs needed for a linear-decline distribution.
     *
     * @param numRanks The number of ranks over which DIs are distributed.
     * @return The minimum number of DIs needed for a linear-decline distribution.
     */
    public static int minDIsLinearDecline(int numRanks){
        int sum = 0;
        int x = 2;
        for(int i = 0; i < numRanks; i++){
            sum += (x);
            x++;
        }
        return sum;
    }

}
