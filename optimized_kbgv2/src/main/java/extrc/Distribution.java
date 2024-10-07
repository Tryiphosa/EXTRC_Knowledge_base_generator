package extrc;

import java.util.Arrays;
import java.util.Random;
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
    public static int[] distributeDIs(int numDIs, int numRanks, String distribution, int min){
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
                distributeRandom(numDIs, numRanks, ranks, min);
                break;
            case "n":
                distributeNormal(numDIs, numRanks, ranks, min);
                break;
            case "eg":
                distributeDIsExpDecline(numDIs, numRanks, ranks);  
                break;
            case "ed":
                distributeDIsExpIncline(numDIs, numRanks, ranks);
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
    private static void distributeRandom(int numDIs, int numRanks, int[] ranks, int min){
        int remainingDIs = numDIs - numRanks * 2;
        if(min <2){Arrays.fill(ranks, 2);}
        else{Arrays.fill(ranks, min);}

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
    private static void distributeNormal(int numDIs, int numRanks, int[] ranks, int min) {
        int start=0;
        int end=numRanks;
        int mul = 1;

        while (start<= end){
            for (int i = start; i < end;i++){
                ranks[i]=ranks[i]+mul;
                numDIs=numDIs-mul;
            }
            start++;
            end--;
            mul++;
        }
        while (numDIs> min*numRanks) {
            int newStart=0;
            int newEnd=numRanks;
            int count=0;
            mul = 1;
            while (newStart<= newEnd){
                for (int i = newStart; i < newEnd;i++){
                    if(numDIs> min*numRanks && numDIs>=mul){
                        ranks[i]=ranks[i]+mul;
                        numDIs=numDIs-mul;
                    }
                }
                count++;
                newStart++;
                newEnd--;
                if(count==2){
                    mul++;
                    count=0;
                }
            }
        }
        for(int i = 0; i<ranks.length; i++){
            ranks[i] = ranks[i]+min;
        }
    }
    
  
    /**
     * Calculates the minimum number of DIs needed for a normal distribution.
     *
     * @param numRanks The number of ranks over which DIs are distributed.
     * @return The minimum number of DIs needed for a normal distribution.
     */
    public static int minDIsNormal(int numRanks, int min) {
        int sum = 0;
        int subtractor = 0;
        int mul = 1;

        while(numRanks-subtractor >= 1){
            //System.out.println(sum+":"+mul+"::"+subtractor);
            sum = sum + (numRanks-subtractor)*mul;
            subtractor=subtractor+2;
            mul++;
        }

        return  sum + min*numRanks;
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
    public static int minDIsLinear(int numRanks, int min){
       // int sum = numRanks * (numRanks + 1) / 2;
        int sum = numRanks * (2 * (min + 1) + (numRanks - 1)) / 2;
        return sum;
    }

    /**
     * Calculates the minimum number of DIs needed for a linear-decline distribution.
     *
     * @param numRanks The number of ranks over which DIs are distributed.
     * @return The minimum number of DIs needed for a linear-decline distribution.
     */
    public static int minDIsLinearDecline(int numRanks,int min){
        int sum = 0;
        int x = 2;
        for(int i = 0; i < numRanks; i++){
            if(x<min){x=min;}
            sum += (x);
            x++;
        }
        return sum;
    }

    /**
     * Calculates the minimum number of DIs needed for a exp-decline distribution.
     *
     * @param numRanks The number of ranks over which DIs are distributed.
     * @return The minimum number of DIs needed for a exp-decline distribution.
     */
    public static int minDIsExp(int numRanks,int min){
        Random random = new Random();
        int sum = 0;
        double decayFactor = 1.2;
        //setDecayFactor(decayFactor);
        for (int i = 0; i < numRanks; i++) {
            int DIsAtRank = (int) Math.round( Math.pow(decayFactor, i));
            if(DIsAtRank<min){DIsAtRank=min;}
            sum += DIsAtRank;
        }
      //  System.out.println(sum);
        return sum;
    }

    public static void distributeDIsExpDecline(int numDIs, int numRanks, int[] ranks ){

        int assignedDIs = 0;
        for (int i = 0; i < numRanks; i++) {
            int DIsAtRank = (int) Math.round(Math.pow(numDIs, (i-1)/(numRanks-1)));
            //(decayFactor, (i-1)/(numRanks-1)
            ranks[i] = DIsAtRank;
            assignedDIs = assignedDIs + DIsAtRank;
        }
        
        // Check if all numDIs are assigned and assign if not
        int index = 0;
        while(assignedDIs<numDIs){
            ranks[index]= ranks[index]+1;
            assignedDIs++;
            index++;
            if(index==ranks.length){
                index = 0;
            }
        }

    }

    public static void distributeDIsExpIncline(int numDIs, int numRanks, int[] ranks){
 
        distributeDIsExpDecline(numDIs, numRanks, ranks);
        int[] tempRank = new int[ranks.length];
        for(int i = 0; i < tempRank.length ; i++ ){
            tempRank[i] = ranks[(tempRank.length-1)-i];
        }

        for (int j = 0; j < tempRank.length ; j++ ){
            ranks[j] = tempRank[j];
        }


    }

}
