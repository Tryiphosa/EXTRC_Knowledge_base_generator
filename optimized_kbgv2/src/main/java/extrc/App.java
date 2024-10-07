
package extrc;
import java.io.*;

import org.tweetyproject.logics.pl.syntax.*;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.commons.ParserException;

import java.util.*;
import java.util.concurrent.*;


import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


/**
 * The App class is responsible for generating a knowledge base (KB) of defeasible implications based on user-defined parameters.
 */

public class App 
{  
   // private static Connective con = Connective.getInstance();
    private static int filenum = 1;
    private static String choice;
    /**
     * The main method for running the knowledge base (KB) generation program.
     *
     * @param args Command-line arguments.
     */
    public static void main( String[] args ){
        //Rules r = new Rules();
         Scanner in = new Scanner(System.in);
        
        do{
            Runtime.getRuntime().addShutdownHook(new Thread(() -> { }));

            System.out.println( "\nDefeasible Knowledge Base Generator:");
            
            System.out.println("\nGenerator type? [ov2 (optimised v2), r (reuse existing KB) ]:");
            String type = in.next();  // Selecting a knowledge base generator type.
            while (!typeIsValid(type)) {
                System.out.println("\nInvalid entry. Try again. Generator type? [ ov2 (optimised v2) ]:");
                type = in.next(); // Knowledge base generation using only simple defImplications
            }

            if(!type.equalsIgnoreCase("r")){
                System.out.println("\nEnter the ratio of classical to defeasible knowledge bases [classical:defeasible]:");
                String[] ratio = (in.next()).split(":");
    
                System.out.println("\nEnter the number of ranks in the KB:");
                int numRanks = in.nextInt(); // Number of ranks in the knowledgebase (including rank 0)
                while ((numRanks <= 0)){
                    System.out.println("\nEnter a non-negative number of ranks in the KB:");
                    numRanks = in.nextInt();
                }
                
                System.out.println("\nf - flat\nr - random\nn - normal\nlg - linear growth\nld - linear decline \neg - exponential growth\ned - exponential decline");
                System.out.println("\nEnter the defImplication distribution: ");
                String distribution = in.next(); // Distribution of the defImplications in the knowledge base
                while (!validDistribution(distribution) || !withValidRanks(numRanks, distribution)){
                    System.out.println("\nEnter valid defImplication distribution. Try again: ");
                    distribution = in.next();
                }

                System.out.println("\nEnter the minimum amount of statements required per rank: ");
                int minStatements = in.nextInt(); // Distribution of the defImplications in the knowledge base
                while (minStatements<0){
                    System.out.println("\nEnter valid minimum amount of statements per rank ( 0 or greater):");
                    minStatements = in.nextInt();
                }

                int min = minDefImplications(distribution, numRanks, minStatements);// + minStatements*numRanks;
                

                System.out.println("\nEnter the number of defImplications in the KB (Must be greater than or equal to " + min + "):");
                int DefStatements = in.nextInt(); // Number of defImplications in the knowledge base
                while (!(DefStatements >= min)){
                    System.out.println("\nEnter a valid number of defImplications in the KB (Must be greater than or equal to " + min + "):");
                    DefStatements = in.nextInt();
                }
                
              
                int numDefImplications=(int) (DefStatements*(((double)Integer.parseInt(ratio[1]))/(Integer.parseInt(ratio[1])+Integer.parseInt(ratio[0]))));
                int ClasStatements = DefStatements-numDefImplications;

              
                int[] defImplicationDistribution = Distribution.distributeDIs(numDefImplications, numRanks, distribution,minStatements);

                
                int minValue = 1000000;
                for (int i=0; i<defImplicationDistribution.length;i++){
                   
                    if (minValue > defImplicationDistribution[i] ){
                        minValue = defImplicationDistribution[i];
                    }
                }
                if(minValue < minStatements){ /// for defeasible statements
                    int offset = minStatements-minValue;
                    for (int i=0; i<defImplicationDistribution.length;i++){
                       
                        defImplicationDistribution[i]=defImplicationDistribution[i]+ offset; 
                    }
                    numDefImplications=numDefImplications+offset*defImplicationDistribution.length;
                }
                if(ClasStatements < minStatements){ /// for classical statements
                    ClasStatements = minStatements;
                }
                ArrayList<Integer> statements= new ArrayList<>();
                statements.add(numDefImplications);
                statements.add(ClasStatements);

                System.out.println("\nReuse Consequent [y, n]:");
                String reuseAnt = in.next(); // Reuse the rankBaseConsequent to generate ranks in the knowledge base
                boolean reuseConsequent = (reuseAnt.equalsIgnoreCase("y")) ? true : false;
                
                ArrayList<String> transitivity = new ArrayList<>();
                String transitive="";
                System.out.println("\nTransitivity [y, n]:");
                transitive = in.next();
                while(!validTransitive(transitive)){
                    System.out.println("Invalid input.Transitivity [y, n]:");
                    transitive = in.next();
                }
                transitivity.add(transitive);

                    
                ArrayList<Integer> anComplexity = new ArrayList<>();
                ArrayList<Integer> consComplexity =new ArrayList<>(); 
                System.out.println("\nEnter Antecedent and Consequent complexity in the format \"Ant|Cons\":");
                String Complexity = in.next();
                
                String[] complexityStrings = Complexity.split("|");
                if(complexityStrings.length==3){
                    anComplexity.add(Integer.parseInt(complexityStrings[0].trim()));
                    consComplexity.add(Integer.parseInt(complexityStrings[2].trim()));
                }else{
                    //invalid input
                }

                int [] connectiveLists = new int [5];
                String connectors ="";
                System.out.println("\n\n1. disjuntion [||]\n2. conjunction [&]\n3. implication [->]\n4. bi-implication [<->]\n5. All Connectives");
                System.out.println("Select Connective types to use e.g \"1,2\" :");
                
                String connectiveTypes = in.next();
                connectors=connectiveTypes;
                    
                String[] connectiveStrings = connectiveTypes.split(",");
                for (int i = 0; i < connectiveStrings.length; i++){
                    int temp = Integer.parseInt(connectiveStrings[i].trim());
                    if (temp != 1 && temp != 2 && temp != 3 && temp != 4 && temp != 5){
                        // Skip invalid numbers
                        System.out.println("Invalid entry. The default option will be used (5 = mixture).");
                    }
                    else{
                        connectiveLists[i]=temp;
                    }
                }
                
                ArrayList<String> characterSet = new ArrayList<>();
                System.out.println("\nEnter the character set for the knowledge base [lowerlatin, upperlatin, altlatin*, greek*]");
                System.out.println("*Run the 'chcp 65001' command for UTF-8 or 'chcp 1253' for Greek (Windows-1253) in the terminal before using AltLatin or Greek characters, respectively.");
                String characterSetIn = in.next(); // The character set used for the atoms
                while (!validCharacterSet(characterSetIn)){
                    System.out.println("Enter valid character set [lowerlatin, upperlatin, altlatin, greek]:");
                    characterSetIn = in.next();
                }
                characterSet.add(characterSetIn);

                do{
                    
                    
                    long startTime = System.nanoTime();
                    System.out.println("\nGenerating Knowledge Base...");

                    ArrayList<ArrayList<String>> KB = new ArrayList<>();
                    ArrayList<String> atomList = new ArrayList<>();
                    ArrayList<String> usedAtoms = new ArrayList<>();
                
                    boolean rerun = true;
                    do{
                        
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        long timeoutDuration = 2000000000; /// May change 
                        try{
                            Callable<ArrayList<ArrayList<String>>> kbGenerationTask = () -> {
                                    return KBGeneratorThreadedOPT.KBGenerate(defImplicationDistribution, anComplexity, consComplexity, connectiveLists, characterSet, transitivity, statements,reuseConsequent);
                            };
                            Future<ArrayList<ArrayList<String>>> future = executor.submit(kbGenerationTask);
                            KB = future.get(timeoutDuration, TimeUnit.MILLISECONDS);
                            
                            usedAtoms=KB.get(KB.size()-1);
                            KB.remove(usedAtoms);
                            atomList= KB.get(KB.size()-1);
                            KB.remove(atomList);
                            rerun = false;
                        
                        }catch(TimeoutException e){
                            System.out.println("Timeout occurred during KB generation. Retrying...");
                            executor.shutdownNow();
                            
                            rerun = true;
                        }catch(InterruptedException | ExecutionException e){

                        }catch (StackOverflowError  e) {
                               // e.printStackTrace();
                    
                        }finally{
                            executor.shutdownNow();
                        }
                    }while(rerun == true);
            
                    long endTime = System.nanoTime();
                    long durationInNano = endTime - startTime;
                    double durationInSeconds = (double) durationInNano / 1000000000.0;
                    System.out.println("\nKnowledge base generation time: "+durationInSeconds+"s");

                   // System.out.println("Number of created statements: "+countStatements(KB));

                    System.out.println("\nSave to text file? [y, n]:");
                    String save = in.next(); // Save the knowledge base to a text file
                    if(save.equalsIgnoreCase("y")){ kbToFile(KB);}

                   // System.out.println(Complexity);
                    
                    System.out.println("\nSave in database [y, n]:");
                    String input = in.next(); // Print knowledge base to terminal
                    if(input.equalsIgnoreCase("y")){
                        System.out.print("Saving Knowledge base ...");

                        try{
                        
                            KnowledgeDAO knowledgeDAO = new KnowledgeDAO();
                            
                            Knowledge knowledge = new Knowledge(
                                toArray(KB),
                                new Date(),  // Current date and time
                                numRanks,  // Number of ranks
                                type,  // Generator name
                                durationInSeconds,// Generation speed
                                usedAtoms,
                                atomList,
                                distribution,
                                DefStatements,
                                characterSet.get(0),
                                connectiveLists,
                                transitive,
                                reuseConsequent,
                                (ratio[0]+":"+ratio[1]),
                                minStatements,
                                Complexity
                            );
                            

                        boolean isSaved = knowledgeDAO.createKnowledgeOV2(knowledge);
                        long i =getSize(KB)+Size(usedAtoms)+Size(atomList)+ (type.length()*2)+(transitive.length()*2)+(distribution.length()*2)+(characterSet.get(0).length()*2)+ (ratio[0]+":"+ratio[1]).length()*2;
                        System.out.println("\nSize in MBs: "+String.format("%.3f", (i/ (1024.0 * 1024.0))));

                        if (isSaved) {
                            System.out.println("Document was successfully saved in the Knowledge collection of the KnowledgeDB database.");
                        } else {
                            System.out.println("Failed to save the document.");
                        }
                        // Close the MongoDB connection
                        MongoDBConnection.closeConnection();
                        System.out.println("Knowledge base saved.");
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                    
                    }

                    System.out.println("\nPrint to terminal? [y, n]:");
                    String print = in.next(); // Print knowledge base to terminal
                    if(print.equalsIgnoreCase("y")){
                        System.out.println("Knowledge base:");
                        System.out.println(readable(KB));
                    }
                    {/*
                    ArrayList<ArrayList<String>> KBs = new ArrayList<>();
                    ArrayList<String> en = new ArrayList<>();
                    en.add("ai ~> (u)");   en.add("ai ~> t");   en.add("ak ~> ai");   en.add("ai ~> af");   en.add("ai ~> ah");   en.add("ai ~> aa");
                    KBs.add(en);
                    en = new ArrayList<>();
                    en.add("g ~> !(u)");   en.add("g ~> ai");   en.add("g ~> x");   en.add("g ~> k");   en.add("l ~> g");   en.add("bp ~> g");
                    KBs.add(en);
                    en = new ArrayList<>();
                    en.add("ae ~> (u)");   en.add("ae ~> g");   en.add("ae ~> f");   en.add("s ~> ae");   en.add("ad ~> ae");   en.add("ab ~> ae");
                    KBs.add(en);
                    en = new ArrayList<>();
                    en.add("h ~> !(u)");   en.add("h ~> ae");   en.add("b ~> h");   en.add("h ~> r");   en.add("y ~> h");   en.add("h ~> bn");
                    KBs.add(en);
                    en = new ArrayList<>();
                    en.add( "a ~> (u)");   en.add("a ~> h");   en.add("a ~> w");   en.add("d ~> a");   en.add("c ~> a");
                    KBs.add(en);
                    en = new ArrayList<>();
                    en.add("bh ~> !(u)");   en.add("bh ~> a");   en.add("bh ~> bv");   en.add("bw ~> bh");   en.add("bh ~> aj");   en.add("bm ~> bh");
                    KBs.add(en);
                    en = new ArrayList<>();
                    en.add("ax ~> (u)");   en.add("ax ~> bh");   en.add("bk ~> ax");   en.add("ax ~> az");   en.add("ax ~> bl");   en.add("ax ~> bs");
                    KBs.add(en);
                    en = new ArrayList<>();
                    en.add( "by ~> !(u)");   en.add("by ~> ax");   en.add("by ~> ar");   en.add("by ~> bi");   en.add("by ~> br");   en.add("ad ~> by");
                    KBs.add(en);
                    en = new ArrayList<>();
                    en.add("av ~> (u)");   en.add("av ~> by");   en.add("al ~> av");   en.add("av ~> cc");   en.add("av ~> bc");   en.add("bg ~> av");
                    KBs.add(en);
                    en = new ArrayList<>();
                    en.add("be ~> !(u)");   en.add("be ~> av");   en.add("af ~> be");   en.add("be ~> bt");   en.add("bo ~> be");   en.add("ag ~> be");
                    KBs.add(en);*/}
                    System.out.println("\nTest using baserank algorithm? [y, n]:");
                    String baseR = in.next(); // Print knowledge base to terminal
                    if(baseR.equalsIgnoreCase("y")){
                       // System.out.println("Number of created statements: "+countStatements(KB));
                        try{                                                         
                            PlBeliefSet beliefSet = new PlBeliefSet();
                            PlBeliefSet classicalSet = new PlBeliefSet();
                            PlParser parser = new PlParser();

                            // The file is read until the end of file.
                            for(ArrayList<String> set : KB){
                                for (String element : set){
                                    if (element.isEmpty()) {
                                        continue;
                                    }
                                    if (element.contains("~>")) {
                                        // the reformatting of the defeasible queries from ~> to =>.
                                        element = reformatConnectives(reformatDefeasible(element));
                                    
                                        // All defeasible implications are added to the defeasible beliefset.
                                        beliefSet.add((PlFormula) parser.parseFormula(element));
                                    
                                    } else {
                                        // Reformatting of the classical implications of the kb if necessary.
                                        element = reformatConnectives(element);
                                        //System.out.println("Reformatted classical: " + stringFormula); // Debugging output
                                        // All classical implications are added to the classical beliefset.
                                        // Parse formula from string.
                                        classicalSet.add((PlFormula) parser.parseFormula(element));
                                    }
                                    
                                }
                            }
                            // BaseRankThreaded object instantiated to allow the base ranking algorithm to run.
                            BaseRankThreaded.setCkb(classicalSet);
                            // Ranked knowledge base returned.
                            
                            ArrayList<PlBeliefSet> rankedKB = BaseRankThreaded.rank(beliefSet, new PlBeliefSet());

                            
                        }catch(Exception e){
                           // e.printStackTrace();
                        }  
                        
                        }
                    {
                    
                    System.out.println("\nContinue generating knowledge bases [c] or Quit [q]:");
                    choice = in.next();
                    }
                    
                }while(choice.equalsIgnoreCase("r"));
    
                }
            else{

                ArrayList<String> allStoredKBs = new ArrayList<>();
                KnowledgeDAO know = new KnowledgeDAO();
                allStoredKBs = know.displayAllKnowledgeWithIds();
                
               
                if(allStoredKBs.size()>0){
                    System.out.println("_______________________________________________________");
                    System.out.println("|              Existing Knowledge Bases:               |");
                    System.out.println("|______________________________________________________|");
                    
                    for(int i=0; i< allStoredKBs.size();  i++){
                        System.out.println((i+1)+". "+allStoredKBs.get(i));
                    }
                    
                    System.out.println("\nEnter the number of ranks in the KB:");
                    int numRanks = in.nextInt(); // Number of ranks in the knowledgebase (including rank 0)
                    while ((numRanks <= 0)){
                        System.out.println("\nEnter a non-negative number of ranks in the KB:");
                        numRanks = in.nextInt();
                    }
                    
                    System.out.println("\n\nEnter existing Knowledge Base id:");
                    String id = in.next();
                    
                    Knowledge knowObj = know.readKnowledge(id);
                    String kbRatio = knowObj.getRatio();
                    int kbMin  =  knowObj.getMin();
                    String dist = knowObj.getDistribution();
                    int kbNumRanks = knowObj.getNumberOfRanks();
                    String kbComplexity = knowObj.getComplexity();
                    String trans = knowObj.getTransitivity();
                    ArrayList<String> kbTransitivity = new ArrayList<>();
                    kbTransitivity.add(trans);
                    final ArrayList<String> kbUsedAtoms = knowObj.getUsedAtoms();
                    final ArrayList<String> kbAtomList = knowObj.getAtomList();
                    int[] kbConnectors = knowObj.getConnectors();
                    boolean kbReuseConsequent = knowObj.getReuseConsequent();
                    String setCharacter = knowObj.getCharSet();           
                    ArrayList<String> kbCharSet = new ArrayList<>();
                    kbCharSet.add(setCharacter);

                    ArrayList<String> newKBUsedAtoms = new ArrayList<>();
                    ArrayList<String> newKBAtomList = new ArrayList<>();
                    ArrayList<ArrayList<String>> kbContent= to2D(knowObj.getContent());

            
                    if(numRanks <= kbNumRanks){
                        System.out.println("Knowledge base cannot be used to make a knowledge base with a smaller number of ranks");
                        choice="q";
                    }else{
                        int minDI = minDefImplications(dist, numRanks, 0);
                        System.out.println("\nPlease enter the number of statements you require ["+minDI+" or more]:");
                        int numFormula = in.nextInt();
                        while(numFormula < minDI){
                            System.out.println("Please enter the number of statements you require, they strictly should be "+minDI+" or more:");
                            numFormula = in.nextInt();
                        }
                        String[] ratio_kb = kbRatio.split(":");
                        int numDefImpl=(int) (numFormula*(((double)Integer.parseInt(ratio_kb[1]))/(Integer.parseInt(ratio_kb[1])+Integer.parseInt(ratio_kb[0]))));
                        int ClasStatements = numFormula-numDefImpl;

                        int[] defImplicationDistribution = Distribution.distributeDIs(numDefImpl, numRanks, dist, kbMin);
                        int kbNumFormula = getKBNumberofFormula(kbContent);
                        int[] oldDistribution = Distribution.distributeDIs(kbNumFormula, kbNumRanks, dist, kbMin);
                        int[] newDistribution = Distribution.getNewDistribution(defImplicationDistribution, oldDistribution);

                        ArrayList<ArrayList<String>> newKB = new ArrayList<>();
                        ArrayList<Integer> numForm = new ArrayList<>();


                        numForm.add(numDefImpl);
                        numForm.add(ClasStatements);


                        boolean rerun = true;
                        long start = System.nanoTime();
                            do{
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            long timeoutDuration = 2000000000; /// May change 
                            
                            System.out.println("\nGenerating Knowledge base...");

                            try{
                                Callable<ArrayList<ArrayList<String>>> kbGenerationTask = () -> {
                                    return KBGeneratorThreadedOPT.KBGenerateNew(newDistribution, kbContent, numForm, kbAtomList, kbUsedAtoms,kbConnectors, kbCharSet, kbTransitivity,kbReuseConsequent,kbComplexity);
                                };
                                Future<ArrayList<ArrayList<String>>> future = executor.submit(kbGenerationTask);
                                newKB = future.get(timeoutDuration, TimeUnit.MILLISECONDS);
                                // System.out.println(newKB);
                                newKBUsedAtoms=newKB.get(newKB.size()-1);
                                newKB.remove(newKBUsedAtoms);
                                newKBAtomList= newKB.get(newKB.size()-1);
                                newKB.remove(newKBAtomList);
                                rerun = false;
                            
                            }catch(TimeoutException e){
                                System.out.println("Timeout occurred during KB generation. Retrying...");
                                executor.shutdownNow();
                                                        rerun = true;
                            }catch (InterruptedException | ExecutionException e) {
                                System.out.println("Error during KB generation: " + e.getMessage());
                               // e.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("Unexpected error: " + e.getMessage());
                               // e.printStackTrace();
                            }finally{
                                executor.shutdownNow();
                            }
                        }while (rerun);
                        long end = System.nanoTime();
                        
                        // generation speed calculation
                        long durationInNano = end - start;
                        double durationInSeconds = (double) durationInNano / 1000000000.0;
                        //durationInSeconds= durationInSeconds+kbSpeed;

                        System.out.println("\nKnowledge base generation time: "+durationInSeconds);
                        System.out.println("\nSave to text file? [y, n]:");
                        String save = in.next(); // Save the knowledge base to a text file
                        if(save.equalsIgnoreCase("y")){ kbToFile(newKB);}
                            
                        System.out.println("\nSave in database? [y, n]:");

                        String input = in.next(); // Print knowledge base to terminal
                        if(input.equalsIgnoreCase("y")){
                            System.out.println("Do you want to update the existing knowledge base or create a new one? [u-update,c-create]");
                            String saveOption = in.next();
                            while (!validOption(saveOption)) {
                                System.out.println("Invalid response. Do you want to update the existing knowledge base or create a new one? [u-update,c-create]");
                                saveOption = in.next();
                            } 
                    
                            System.out.print("Saving Knowledge base ...");
                            try{
                            KnowledgeDAO knowledgeDAO = new KnowledgeDAO();
                            
                            Knowledge knowledge = new Knowledge(
                                toArray(newKB),
                                new Date(),  // Current date and time
                                numRanks,  // Number of ranks
                                type,  // Generator name
                                durationInSeconds,// Generation speed
                                newKBUsedAtoms,// Atoms used to create the knowledge base
                                newKBAtomList,// Atoms remaining from the knowledge base creation
                                dist, // knowledge base distribution
                                numFormula, // Number of statements
                                kbCharSet.get(0), // character set used
                                kbConnectors,
                                trans, // define whether the knowledge base has transitive statements in ranks
                                kbReuseConsequent, // defines whether consequents are reused or otherwise
                                kbRatio,
                                kbMin,
                                kbComplexity
                                );

                            long i =getSize(newKB)+Size(newKBUsedAtoms)+Size(newKBAtomList)+ (type.length()*2)+(trans.length()*2)+(dist.length()*2)+(kbRatio.length()*2)+(kbCharSet.get(0).length()*2);
                            System.out.println("\nSize in MBs: "+String.format("%.3f", (i/ (1024.0 * 1024.0))));
                            
                            boolean isSaved = false;
                            if(saveOption.equalsIgnoreCase("c")){
                                isSaved = knowledgeDAO.createKnowledgeOV2(knowledge);
                            }else{
                                isSaved = knowledgeDAO.updateKnowledgeOV2(id,knowledge);
                            }
                                                
                            if (isSaved) {
                                System.out.println("Document was successfully saved in the Knowledge collection of the KnowledgeDB database.");
                            } else {
                                System.out.println("Failed to save the document.");
                            }
                            // Close the MongoDB connection
                            MongoDBConnection.closeConnection();
                            System.out.println("Knowledge base saved.");
                            }catch(Exception e){
                                System.out.println(e.getMessage());
                            }
                        }

                            System.out.println("\nPrint to terminal? [y, n]:");
                            String print = in.next(); // Print knowledge base to terminal
                            if(print.equalsIgnoreCase("y")){
                                System.out.println("Knowledge base:");
                                System.out.println(readable(newKB));
                            }

                            System.out.println("\nTest using baserank algorithm? [y, n]:");
                            String baseR = in.next(); // Print knowledge base to terminal
                            if(baseR.equalsIgnoreCase("y")){
                               // System.out.println("Number of created statements: "+countStatements(newKB));
                                try{                                                         
                                    PlBeliefSet beliefSet = new PlBeliefSet();
                                    PlBeliefSet classicalSet = new PlBeliefSet();
                                    PlParser parser = new PlParser();
        
                                    // The file is read until the end of file.
                                    for(ArrayList<String> set : newKB){
                                        for (String element : set){
                                            if (element.isEmpty()) {
                                                continue;
                                            }
                                            if (element.contains("~>")) {
                                                // the reformatting of the defeasible queries from ~> to =>.
                                                element = reformatConnectives(reformatDefeasible(element));
                                            
                                                // All defeasible implications are added to the defeasible beliefset.
                                                beliefSet.add((PlFormula) parser.parseFormula(element));
                                            
                                            } else {
                                                // Reformatting of the classical implications of the kb if necessary.
                                                element = reformatConnectives(element);
                                                //System.out.println("Reformatted classical: " + stringFormula); // Debugging output
                                                // All classical implications are added to the classical beliefset.
                                                // Parse formula from string.
                                                classicalSet.add((PlFormula) parser.parseFormula(element));
                                            }
                                            
                                        }
                                    }
                                    // BaseRankThreaded object instantiated to allow the base ranking algorithm to run.
                                    BaseRankThreaded.setCkb(classicalSet);
                                    // Ranked knowledge base returned.
                                    
                                    ArrayList<PlBeliefSet> rankedKB = BaseRankThreaded.rank(beliefSet, new PlBeliefSet());
        
                                    
                                }catch(Exception e){
                                  //  e.printStackTrace();
                                }  
                                
                                }
                            {
                            
                            System.out.println("\nContinue generating knowledge bases [c] or Quit [q]:");
                            choice = in.next();
                            }
                            
                }
                
               
            }else{
                System.out.println("There are no Knowledge bases to use, please use the ov2 generator.");
                choice="c";
            }
        
            }
       
            
        }while(choice.equalsIgnoreCase("c"));
        System.out.println("***Program terminated***");
        in.close();
  
    }

    // Private helper methods:
    public static String reformatDefeasible(String formula) {
        int index = formula.indexOf("~>");
        formula = "(" + formula.substring(0, index).trim() + ") => (" + formula.substring(index + 2).trim() + ")";
        return formula;
    }

     public static String reformatConnectives(String formula) {
        formula = formula.replaceAll("¬", "!");
        formula = formula.replaceAll("~", "!");
        formula = formula.replaceAll("&", "&&");
        formula = formula.replaceAll("<->", "<=>");
        formula = formula.replaceAll("->", "=>");
        return formula;
    }

    private static int getKBNumberofFormula(ArrayList<ArrayList<String>>kbContent){
        int count = 0;
        for (int i = 0; i <kbContent.size();i++){
            for(int j = 0; j < kbContent.get(i).size();j++){
                count = count + 1;
            }
        }
        return count;
    }
   
    private static ArrayList<ArrayList<String>> to2D(ArrayList<String> arr){
        ArrayList<ArrayList<String>> array = new ArrayList<>();
        for(int i=0; i<arr.size();i++){
            String[] entry = arr.get(i).split(", ");
            ArrayList<String> val= new ArrayList<>();
            for(int j=0; j < entry.length;j++){
               val.add(entry[j]);
            }
            array.add(val);
        }
        return array;
    }

    private static boolean typeIsValid(String type){
        return type.equalsIgnoreCase("s")|| type.equalsIgnoreCase("ov1") || type.equalsIgnoreCase("ov2")||type.equalsIgnoreCase("r");
    }

    private static String readable(ArrayList<ArrayList<String>>KB){
    int i = 0;
    String output= "";
    for (ArrayList<String> set : KB){
        output=output+"Rank " + i + ": ";
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()){
            String element = iterator.next();
            output=output+element.toString();
            if(iterator.hasNext()){
                output=output+", ";
            }else{
                output=output+"\n";
            }
        }
        i++;
    }
    return output;
    }

    private static ArrayList<String> toArray(ArrayList<ArrayList<String>>KB){
  
        ArrayList<String> result= new ArrayList<>();
        for (ArrayList<String> set : KB){
            String output="";
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()){
                String element = iterator.next();
                output=output+element.toString();
                if(iterator.hasNext()){
                    output=output+", ";
                }else{
                    output=output+"";
                }
            }
            //i++;
            result.add(output);
        }
        return result;
    }
 
    /**
     * Checks if the input string is a valid distribution type.
     *
     * @param input The input string to check.
     * @return True if the input is a valid distribution type, otherwise false.
     */
    private static boolean validDistribution(String input){
        return input.equalsIgnoreCase("f") || input.equalsIgnoreCase("lg") || input.equalsIgnoreCase("eg")|| input.equalsIgnoreCase("ed")||
               input.equalsIgnoreCase("ld") || input.equalsIgnoreCase("r") || input.equalsIgnoreCase("n");
    }
    
    /**
     * Checks if the input string is a valid number of ranks for said distribution.
     *
     * @param input The input string to check.
     * @return True if the input is a valid number of ranks for said distribution, otherwise false.
     */
    private static boolean withValidRanks(int numRanks, String input){
        return input.equalsIgnoreCase("n")&& (numRanks > 3) ||input.equalsIgnoreCase("f") || input.equalsIgnoreCase("lg") || input.equalsIgnoreCase("ed") ||
        input.equalsIgnoreCase("ld") || input.equalsIgnoreCase("r") || input.equalsIgnoreCase("eg");
    }
   
    /**
     * Checks if the input string is a valid character set.
     *
     * @param input The input string to check.
     * @return True if the input is a valid character set, otherwise false.
     */
    private static boolean validCharacterSet(String input){
        return input.equalsIgnoreCase("lowerlatin") || input.equalsIgnoreCase("upperlatin") ||
               input.equalsIgnoreCase("altlatin") || input.equalsIgnoreCase("greek");
    }

     /**
     * Checks if the transitive string is a valid transitivity option.
     *
     * @param transitive The input string to check.
     * @return True if the transitive string is a valid transitivity option, otherwise false.
     */
    private static boolean validTransitive(String transitive){
        return transitive.equalsIgnoreCase("y") || transitive.equalsIgnoreCase("n") || transitive.equalsIgnoreCase("r");
    }

    /**
     * Checks if the input string is a valid save option.
     *
     * @param saveOption The input string to check.
     * @return True if the input is a valid save option, otherwise false.
     */
    private static boolean validOption(String saveOption){
        return saveOption.equalsIgnoreCase("u") || saveOption.equalsIgnoreCase("c");
    }

    /**
     * Calculates the minimum number of defImplications required based on the distribution type and number of ranks.
     *
     * @param distribution The distribution type.
     * @param numRanks     The number of ranks in the knowledge base.
     * @return The minimum number of defImplications required.
     */
    private static int minDefImplications(String distribution, int numRanks, int minstatements){
        int min = 0;
        switch(distribution){
            case "f":
                if(minstatements>1){min=minstatements*numRanks;}else{min = (numRanks*2)-1;}//
                break;
            case "lg":
                min = Distribution.minDIsLinear(numRanks,minstatements);//

                break;
            case "ld":
                min = Distribution.minDIsLinearDecline(numRanks, minstatements);//
                break;
            case "r":
                if(minstatements>2){min=minstatements*numRanks;}else{min = (numRanks*2);}//
                break;
            case "n":
                min =Distribution.minDIsNormal(numRanks,minstatements);
                break;
            case "eg":
                min = Distribution.minDIsExp(numRanks,minstatements);//
                break;
            case "ed":
                min = Distribution.minDIsExp(numRanks,minstatements);//
                break;
        }
        return min;
    }

    /**
     * Writes a knowledge base to a text file.
     *
     * @param KB The knowledge base to write to the file.
     */
    private static void kbToFile(ArrayList<ArrayList<String>> KB){
        String filePath = "output" + filenum + ".txt";
        filenum++;
        try{
            File file = new File(filePath);
            FileWriter fw = new FileWriter(file);

            for(ArrayList<String> set : KB){
                for (String element : set){
                    fw.write(element.toString() + "\n");
                }
            }
            fw.close();
        } 
        catch(IOException e){
           // e.printStackTrace();
        }
    }

    private static int countStatements(ArrayList<ArrayList<String>> KB){
        
        int count=0;
            for(ArrayList<String> set : KB){
               
                if(!set.isEmpty()){
                        for (String element : set){
                        count++;
                    }
                }
            }
           
        return count;
    }
    
    public static long getSize(ArrayList<ArrayList<String>> list) {
        long totalSize = 0;

        // Estimate size of outer ArrayList
        totalSize += 24 + list.size() * 8;

        for (ArrayList<String> innerList : list) {
            // Estimate size of inner ArrayLists
            totalSize += 24 + innerList.size() * 8;

            for (String str : innerList) {
                // Estimate size of each String
                totalSize += 40 + str.length() * 2;
            }
        }

        return totalSize;
    }
 
    public static long Size(ArrayList<String> list) {
        long totalSize = 0;

        
            totalSize += 24 + list.size() * 8;

            for (String str : list) {
                // Estimate size of each String
                if(str!=null){
                    totalSize += 40 + str.length() * 2;
                }
            }
        

        return totalSize;
    }

    public static void generatorIntro() {
             
        String asciiArt =
            "     _      __             _ _    _       _  __                _        _            ___                 ___                       _           \n"
        + "  __| |___ / _|___ __ _ __(_) |__| |___  | |/ /_ _  _____ __ _| |___ __| |__ _ ___  | _ ) __ _ ______   / __|___ _ _  ___ _ _ __ _| |_ ___ _ _ \n"
        + " / _` / -_)  _/ -_) _` (_-< | '_ \\ / -_) | ' <| ' \\/ _ \\ V  V / / -_) _` / _` / -_) | _ \\/ _` (_-< -_) | (_ / -_) ' \\/ -_) '_/ _` |  _/ _ \\ '_|\n"
        + " \\__,_\\___|_| \\___\\__,_/__/_|_.__/_\\___| |_\\_\\_||_\\___/\\_/\\_/|_\\___\\__,_\\__, \\___| |___/\\__,_/__|___|  \\___\\___|_||_\\___|_| \\__,_|\\__\\___/_|  \n"
        + "                                                                         |___/                                   \n";
    
        System.out.println(asciiArt);
          
    }

   
}











