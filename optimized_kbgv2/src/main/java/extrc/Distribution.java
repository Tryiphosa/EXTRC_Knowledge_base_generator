package extrc;

import java.util.Arrays;
import java.lang.Math;

/**
 * The Distribution class provides methods for calculating the distribution of defeasible implications (DIs) over ranks.
 */
public class Distribution{

    /**
     * Controls the distribution calculation for DIs over the ranks based on the specified distribution type.
     *
     * @param numDIs      The total number of DIs to distribute.
     * @param numRanks    The number of ranks over which DIs are distributed.
     * @param distribution The type of distribution to calculate (f: flat, lg: linear growth, ld: linear decline, r: random).
     * @return An array representing the calculated distribution of DIs over the ranks.
     */
    public static int[] distributeDIs(int numDIs, int numRanks, String distribution){
        int[] ranks = new int[numRanks];

        switch (distribution){
            case "f":
                distributeFlat(numDIs, numRanks, ranks);
                break;
            case "lg":
                distributeLinearGrowth(numDIs, numRanks, ranks);
                break;
            case "ld":
                distributeLinearDecline(numDIs, numRanks, ranks);
                break;
            case "r":
                distributeRandom(numDIs, numRanks, ranks);
                break;
            case "n":
                distributeNormal(numDIs, numRanks, ranks);
                break;
        }
        return ranks;
    }

    /**
     * Calculates a flat distribution of DIs over the ranks.
     *
     * @param numDIs   The total number of DIs to distribute.
     * @param numRanks The number of ranks over which DIs are distributed.
     * @param ranks    An array to store the calculated distribution of DIs.
     */
    private static void distributeFlat(int numDIs, int numRanks, int[] ranks){
        int defImplicationsPerRank = numDIs / numRanks;
        int remainder = numDIs % numRanks;

        for (int i = 0; i < numRanks; i++){
            ranks[i] = defImplicationsPerRank;
        }

        int i = numRanks-1;
        while (remainder > 0){
            ranks[i]++;
            remainder--;
            i--;
        }
    }

    /**
     * Calculates a linear-growth distribution of DIs over the ranks.
     *
     * @param numDIs   The total number of DIs to distribute.
     * @param numRanks The number of ranks over which DIs are distributed.
     * @param ranks    An array to store the calculated distribution of DIs.
     */
    private static void distributeLinearGrowth(int numDIs, int numRanks, int[] ranks){
        int remainingDIs = numDIs;
        
        for (int i = 0; i < numRanks; i++){
            int defImplicationsToAdd = Math.min(remainingDIs, i + 1);
            ranks[i] = defImplicationsToAdd;
            remainingDIs -= defImplicationsToAdd;
        }
    
        int currentRank = numRanks - 1;
        while (remainingDIs > 0){
            if(currentRank < 0){
                currentRank = numRanks - 1;
            }
            int defImplicationsToAdd = Math.min(remainingDIs, 1);
            ranks[currentRank] += defImplicationsToAdd;
            remainingDIs -= defImplicationsToAdd;
            currentRank--;
        }
    }
    
    /**
     * Calculates a linear-decline distribution of DIs over the ranks.
     *
     * @param numDIs   The total number of DIs to distribute.
     * @param numRanks The number of ranks over which DIs are distributed.
     * @param ranks    An array to store the calculated distribution of DIs.
     */
    private static void distributeLinearDecline(int numDIs, int numRanks, int[] ranks){
        int remainingDIs = numDIs;
    
        for (int i = 0; i < numRanks; i++){
            int defImplicationsToAdd = Math.min(remainingDIs, i + 1);
            ranks[i] = defImplicationsToAdd;
            remainingDIs -= defImplicationsToAdd;
        }
    
        int currentRank = numRanks - 1;
        while (remainingDIs > 0){
            if(currentRank < 0){
                currentRank = numRanks - 1;
            }
            int defImplicationsToAdd = Math.min(remainingDIs, 1);
            ranks[currentRank] += defImplicationsToAdd;
            remainingDIs -= defImplicationsToAdd;
            currentRank--;
        }
    
        for (int i = 0; i < numRanks / 2; i++){
            int temp = ranks[i];
            ranks[i] = ranks[numRanks - i - 1];
            ranks[numRanks - i - 1] = temp;
        }
    }

    /**
     * Calculates a random distribution of DIs over the ranks.
     *
     * @param numDIs   The total number of DIs to distribute.
     * @param numRanks The number of ranks over which DIs are distributed.
     * @param ranks    An array to store the calculated distribution of DIs.
     */
    private static void distributeRandom(int numDIs, int numRanks, int[] ranks){
        int remainingDIs = numDIs - numRanks * 2;
        Arrays.fill(ranks, 2);

        while(remainingDIs > 0){
            int i = (int)(Math.random() * ranks.length);
            ranks[i]++;
            remainingDIs--;
        }
    }
     
    /**
     * Calculates a normal distribution of DIs over the ranks.
     *
     * @param numDIs   The total number of DIs to distribute.
     * @param numRanks The number of ranks over which DIs are distributed.
     * @param ranks    An array to store the calculated distribution of DIs.
     */
    private static void distributeNormal(int numDIs, int numRanks, int[] ranks) {
        int numberOfIntervals = numRanks;
        double[] intervals = new double[numberOfIntervals];
        
        double mean = (numberOfIntervals - 1) / 2.0;
        double stdDev = numberOfIntervals / 6.0; // Approximation for covering 99.7% in 6 standard deviations
        
        // Generate values using the normal distribution
        double sum = 0.0;
        for (int i = 0; i < numberOfIntervals; i++) {
            double z = (i - mean) / stdDev;
            intervals[i] = Math.exp(-0.5 * z * z); // Normal distribution formula
            sum += intervals[i];
        }
    
        // Normalize the values so that their total sum matches the given total sum
        double totalIntervals = 0.0;
        for (int i = 0; i < numberOfIntervals; i++) {
            intervals[i] = (intervals[i] / sum) * numDIs;
            totalIntervals += intervals[i];
        }
    
        // Adjust rounding to ensure total sum is exact
        int totalSum = 0;
        for (int i = 0; i < numberOfIntervals; i++) {
            ranks[i] = (int) Math.round(intervals[i]);
            totalSum += ranks[i];
        }
    
        // Adjust the last element to ensure the total sum is correct
        if (totalSum != numDIs) {
            ranks[numberOfIntervals - 1] += (numDIs - totalSum);
        }
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
     * Calculates the minimum number of DIs needed for a normal distribution.
     *
     * @param numRanks The number of ranks over which DIs are distributed.
     * @return The minimum number of DIs needed for a normal distribution.
     */
    public static int[] getNewDistribution( int[] defImplicationDistribution, int[] oldDistribution ){
        for (int i = 0; i < oldDistribution.length; i++){
            if(defImplicationDistribution[i]-oldDistribution[i]>=0){
                defImplicationDistribution[i]=defImplicationDistribution[i]-oldDistribution[i];
            }else{
                defImplicationDistribution[i]=0;
            }
        }
        for (int j = 0; j < defImplicationDistribution.length; j++){
            if(defImplicationDistribution[j]<0){
                defImplicationDistribution[j]=1;
            }
        }
        return defImplicationDistribution;
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
