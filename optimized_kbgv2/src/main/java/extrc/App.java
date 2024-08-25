
package extrc;
import java.io.*;

import org.tweetyproject.logics.pl.syntax.*;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.commons.ParserException;

import java.util.*;
import java.util.concurrent.*;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
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
    private static Connective con = Connective.getInstance();
    private static AtomBuilder gen = AtomBuilder.getInstance();
    private static int filenum = 1;
    private static String choice;
    /**
     * The main method for running the knowledge base (KB) generation program.
     *
     * @param args Command-line arguments.
     */
    public static void main( String[] args ){
        Rules r = new Rules();
        Scanner in = new Scanner(System.in);
   
        do{
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutdown hook triggered. Cleaning up...");
            }));

            con.reset();
            System.out.println( "\nDefeasible Knowledge Base Generator:");
            List<Integer> complexityAntList = new ArrayList<>(); // Number of possible connectives in a defImplications antecedent
            List<Integer> complexityConList = new ArrayList<>(); // Number of possible connectives in a defImplications consequent
            List<Integer> connectiveList = new ArrayList<>(); // Number of different connectives a defImplication can have
            
            System.out.println("\nGenerator type? [s (standard), ov1 (optimised v1), ov2 (optimised v2), r (reuse existing KB) ]:");
            System.out.print("> ");
            String type = in.next(); // Knowledge base generation using only simple defImplications
            while (!typeIsValid(type)) {
                System.out.println("\nInvalid entry. Try again. Generator type? [s (standard), ov1 (optimised v1), ov2 (optimised v2) ]:");
                System.out.print("> ");
                type = in.next(); // Knowledge base generation using only simple defImplications
            }

            System.out.println("\nEnter the ratio of classical to defeasible knowledge bases [classical:defeasible]:");
            System.out.print("> ");
            String[] ratio = (in.next()).split(":");
                      
          

            System.out.println("\nEnter the number of ranks in the KB (for normal distribution enter value above 6):");
            System.out.print("> ");
            int numRanks = in.nextInt(); // Number of ranks in the knowledgebase (including rank 0)
            while ((numRanks <= 0)){
                System.out.println("\nEnter a non-negative number of ranks in the KB:");
                System.out.print("> ");
                numRanks = in.nextInt();
            }
            
            double decay = 0.0;
            if(!type.equalsIgnoreCase("r")){
                System.out.println("\nEnter the defImplication distribution [f (flat), lg (linear-growth), ld (linear-decline), r (random), n (normal- for ranks above 6), eg (exponential-growth),  ed (exponential-decline)]:");
                System.out.print("> ");
                String distribution = in.next(); // Distribution of the defImplications in the knowledge base
                while (!validDistribution(distribution) || !withValidRanks(numRanks, distribution)){
                    System.out.println("\nEnter valid defImplication distribution [f (flat), lg (linear-growth), ld (linear-decline), r (random), n (normal- for ranks above 6), eg (exponential-growth),  ed (exponential-decline)]:");
                    System.out.print("> ");
                    distribution = in.next();
                }
  

                int min = minDefImplications(distribution, numRanks);
                

                System.out.println("\nEnter the number of defImplications in the KB (Must be greater than or equal to " + min + "):");
                System.out.print("> ");
                int DefStatements = in.nextInt(); // Number of defImplications in the knowledge base
                while (!(DefStatements >= min)){
                    System.out.println("\nEnter a valid number of defImplications in the KB (Must be greater than or equal to " + min + "):");
                    System.out.print(">");
                    DefStatements = in.nextInt();
                }
                
                decay= (double)DefStatements / numRanks;
                int numDefImplications=(int) (DefStatements*(((double)Integer.parseInt(ratio[1]))/(Integer.parseInt(ratio[1])+Integer.parseInt(ratio[0]))));
                int ClasStatements = DefStatements-numDefImplications;
               // System.out.println("classic:"+ClasStatements+"  def:"+numDefImplications+"         --->"+(((double)Integer.parseInt(ratio[1]))/(Integer.parseInt(ratio[1])+Integer.parseInt(ratio[0]))));
                int[] defImplicationDistribution = Distribution.distributeDIs(numDefImplications, numRanks, distribution,decay);

                System.out.println("\nEnter the minimum amount of statements required per rank: ");
                System.out.print("> ");
                int minStatements = in.nextInt(); // Distribution of the defImplications in the knowledge base
                while (minStatements<0){
                    System.out.println("\nEnter valid minimum amount of statements per rank ( 0 or greater):");
                    System.out.print("> ");
                    minStatements = in.nextInt();
                }
                
                int minValue = 1000000;
                for (int i=0; i<defImplicationDistribution.length;i++){
                  //  System.out.println(defImplicationDistribution[i]);
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

                System.out.println("\nReuse Consequent? [y, n]:");
                System.out.print("> ");
                String reuseAnt = in.next(); // Reuse the rankBaseConsequent to generate ranks in the knowledge base
                boolean reuseConsequent = (reuseAnt.equalsIgnoreCase("y")) ? true : false;
        
                if (!type.equalsIgnoreCase("ov2")) {
                    System.out.println("\nSimple defImplications only? [y, n]:");
                    System.out.print("> ");
                    String smple = in.next(); // Knowledge base generation using only simple defImplications
                    boolean simple = (smple.equalsIgnoreCase("y")) ? true : false;

                    if((simple == false)){
                        System.out.println("Antecedent complexity [0, 1, 2]:");
                        System.out.println("Enter chosen numbers seperated by commas:");
                        System.out.print("> ");
                        String antComplexity = in.next();

                        String[] antStrings = antComplexity.split(",");
                        for (int i = 0; i < antStrings.length; i++){
                            int temp = Integer.parseInt(antStrings[i].trim());
                            if (temp != 0 && temp != 1 && temp != 2){
                                // Skip invalid numbers
                            }
                            else{
                                complexityAntList.add(temp);
                            }
                        }

                        System.out.println("Consequent complexity [0, 1, 2]:");
                        System.out.println("Enter chosen numbers separated by commas:");
                        System.out.print("> ");
                        String conComplexity = in.next();

                        String[] conStrings = conComplexity.split(",");
                        for (int i = 0; i < conStrings.length; i++){
                            int temp = Integer.parseInt(conStrings[i].trim());
                            if (temp != 0 && temp != 1 && temp != 2){
                                // Skip invalid numbers
                            }
                            else{
                                complexityConList.add(temp);
                            }
                        }

                        System.out.println("Connective types [1, 2, 3, 4, 5]:");
                        System.out.println("1 = disjuntion, 2 = conjunction, 3 = implication, 4 = bi-implication, 5 = mixture");
                        System.out.println("Enter chosen numbers separated by commas:");
                        System.out.print("> ");
                        String connectiveTypes = in.next();

                        String[] connectiveStrings = connectiveTypes.split(",");
                        for (int i = 0; i < connectiveStrings.length; i++){
                            int temp = Integer.parseInt(connectiveStrings[i].trim());
                            if (temp != 1 && temp != 2 && temp != 3 && temp != 4 && temp != 5){
                                // Skip invalid numbers
                            }
                            else{
                                connectiveList.add(temp);
                            }
                        }
                    }

                    System.out.println("Would you like to change connective symbols? [y, n]");
                    System.out.print("> ");
                    String chnge = in.next(); // Change the connective symbols used in the defImplications
                    boolean change = (chnge.equalsIgnoreCase("y")) ? true : false;
                    if(change == true){
                        System.out.println("Default Defeasible Implication symbol: |~ ['s' to skip]");
                        System.out.print("> ");
                        String defImp = in.next();
                        boolean chng = (defImp.equalsIgnoreCase("s")) ? true : false;
                        if(chng == false){con.setDISymbol(defImp);} // Sets defeasible implication symbol
                        
                        if(simple == false){
                            System.out.println("Default Conjunction symbol: & ['s' to skip]");
                            System.out.print("> ");
                            String conj = in.next();
                            chng = (conj.equalsIgnoreCase("s")) ? true : false;
                            if(chng == false){con.setConjunctionSymbol(conj);} // Sets conjunction symbol

                            System.out.println("Default Disjunction symbol: || ['s' to skip]");
                            System.out.print("> ");
                            String disj = in.next();
                            chng = (disj.equalsIgnoreCase("s")) ? true : false;
                            if(chng == false){con.setDisjunctionSymbol(disj);}  // Sets disjunction symbol

                            System.out.println("Default Implication symbol: \u001A ['s' to skip]");
                            System.out.print("> ");
                            String imp = in.next();
                            chng = (imp.equalsIgnoreCase("s")) ? true : false;
                            if(chng == false){con.setImplicationSymbol(imp);}  // Sets implication symbol

                            System.out.println("Default Bi-Implication symbol: \u001D ['s' to skip]");
                            System.out.print("> ");
                            String biimp = in.next();
                            chng = (biimp.equalsIgnoreCase("s")) ? true : false;
                            if(chng == false){con.setBiImplicationSymbol(biimp);}  // Sets bi-implication symbol

                            System.out.println("Default Negation symbol: \u00AC ['s' to skip]");
                            System.out.print("> ");
                            String negation = in.next();
                            chng = (negation.equalsIgnoreCase("s")) ? true : false;
                            if(chng == false){con.setNegationSymbol(negation);}  // Sets negation symbol
                        }
                    }
                
                    System.out.println("Enter the character set for the knowledge base [lowerlatin, upperlatin, altlatin, greek]");
                    System.out.println("Greek & altlatin character sets require code page 65001");
                    System.out.println("Can set this in the terminal using 'chcp 65001'");
                    System.out.print("> ");
                    String characterSet = in.next(); // The character set used for the atoms
                    while (!validCharacterSet(characterSet)){
                        System.out.println("Enter valid character set [lowerlatin, upperlatin, altlatin, greek]:");
                        System.out.print("> ");
                        characterSet = in.next();
                    }
                    gen.setCharacters(characterSet);

                    do{
                        int[] complexityAnt = new int[complexityAntList.size()];
                        for (int i = 0; i < complexityAntList.size(); i++){
                                complexityAnt[i] = complexityAntList.get(i);
                        }

                        int[] complexityCon = new int[complexityConList.size()];
                        for (int i = 0; i < complexityConList.size(); i++){
                                complexityCon[i] = complexityConList.get(i);
                        }

                        int[] connectiveTypes = new int[connectiveList.size()];
                        for (int i = 0; i < connectiveList.size(); i++){
                                connectiveTypes[i] = connectiveList.get(i);
                        }

                        if(simple == false){
                            if((complexityAnt.length == 1 & complexityCon.length == 1) & (complexityAnt[0] == 0 & complexityCon[0] == 0)){
                                simple = true;
                            }
                        }

                
                        long startTime = System.nanoTime();
                        System.out.println("Generating Knowledge Base...");

                        LinkedHashSet<LinkedHashSet<DefImplication>> KB = new LinkedHashSet<>();
                        boolean rerun = true;
                        if(type.equalsIgnoreCase("s")){
                            KB = KBGenerator.KBGenerate(defImplicationDistribution, simple, reuseConsequent, complexityAnt, complexityCon, connectiveTypes);
                        }
                        else if (type.equalsIgnoreCase("ov1")){
                            boolean s = simple;
                            do{
                                ExecutorService executor = Executors.newSingleThreadExecutor();
                                long timeoutDuration =2000000000;
                                try{
                                    Callable<LinkedHashSet<LinkedHashSet<DefImplication>>> kbGenerationTask = () -> {
                                        return KBGeneratorThreaded.KBGenerate(defImplicationDistribution, s, complexityAnt, complexityCon, connectiveTypes);
                                    };
                                    Future<LinkedHashSet<LinkedHashSet<DefImplication>>> future = executor.submit(kbGenerationTask);
                                    KB = future.get(timeoutDuration, TimeUnit.MILLISECONDS);
                                    rerun = false;

                                }catch(TimeoutException e){
                                    System.out.println("Timeout occurred during KB generation. Retrying...");
                                    executor.shutdownNow();
                                    gen.reset();
                                    rerun = true;
                                }catch(InterruptedException | ExecutionException e){

                                }finally{
                                    executor.shutdownNow();
                                }
                            }while(rerun == true);
                        }
                        else {

                            //Do nothing
                        }
                    
                        long endTime = System.nanoTime();
                        long durationInNano = endTime - startTime;
                        double durationInSeconds = (double) durationInNano / 1000000000.0;

                        System.out.println("Save to text file? [y, n]:");
                        System.out.print("> ");
                        String save = in.next(); // Save the knowledge base to a text file
                        if(save.equalsIgnoreCase("y")){
                        kbToFile(KB);
                        }
                        
                        System.out.println("Save in database? [y, n]:");
                        System.out.print("> ");
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
                                distribution, //Knowledge distribution
                                numDefImplications,
                                characterSet,
                                connectiveTypes                  
                            );
                            boolean isSaved = knowledgeDAO.createKnowledge(knowledge);
                        
                            
                            if (isSaved) {
                                System.out.println("Document was successfully saved in the Knowledge collection of the KnowledgeDB database.");
                            } else {
                                System.out.println("Failed to save the document.");
                            }
                            // Close the MongoDB connection
                            MongoDBConnection.closeConnection();
                            System.out.println(" Knowledge base saved.");
                            }catch(Exception e){
                                System.out.println(e.getMessage());

                            }
                        }

                        System.out.println("Print to terminal? [y, n]:");
                        System.out.print("> ");
                        String print = in.next(); // Print knowledge base to terminal
                        if(print.equalsIgnoreCase("y")){
                            System.out.println("Knowledge base:");
                            System.out.println(readable(KB));

                        }
                        
                        gen.reset();
                        System.out.println("Regenerate new knowledge base? [r]:");
                        System.out.println("Change settings? [c]:");
                        System.out.println("Quit? [q]:");
                        System.out.print("> ");
                        choice = in.next();
                        
                    }while(choice.equalsIgnoreCase("r"));
        
                }else{
                    ArrayList<Integer> anComplexity = new ArrayList<>();
                    ArrayList<Integer> consComplexity =new ArrayList<>(); 
                    ArrayList<String> transitivity = new ArrayList<>();
                    ArrayList<String> formatList = new ArrayList<>();

                    int [] connectiveLists = new int [5];
                    String connectors ="";
                    String transitive="";

                    System.out.println("\nEnter Antecedent and Consequent complexity in the format \"Ant|Cons\":");
                    System.out.print("> ");
                    String Complexity = in.next();

                    String[] complexityStrings = Complexity.split("|");
                    if(complexityStrings.length==3){
                        anComplexity.add(Integer.parseInt(complexityStrings[0].trim()));
                        consComplexity.add(Integer.parseInt(complexityStrings[2].trim()));
                    }else{
                        //invalid input
                    }

                    System.out.println("\nSelect Connective types-t or formula format-f?");
                    System.out.print("> ");
                    String form = in.next(); 

                    if(form.equalsIgnoreCase("t")){
                        System.out.println("Select Connective types [1, 2, 3, 4, 5]:");
                        System.out.println("1 = disjuntion (||), 2 = conjunction (&), 3 = implication (->), 4 = bi-implication (<->), 5 = mixture");
                        System.out.println("Enter chosen numbers separated by commas:");
                        System.out.print(">");
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
                   }else{
                        System.out.println("Enter the preferred format of the formulas, e.g A ~> B & (A || C), enter e to end the process: ");
                        String format = in.next();
                        while (!format.equalsIgnoreCase("e")) {
                            formatList.add(format);
                            format = in.next();
                        }

                   }
            

                    System.out.println("\nTransitivity [y-Yes, n-No, r-Random]:");
                    System.out.print("> ");
                    transitive = in.next();
                    while(!validTransitive(transitive)){
                        System.out.println("Invalid input.Transitivity [y-Yes, n-No, r-Random]:");
                        transitive = in.next();
                    }
                    transitivity.add(transitive);


                    ArrayList<String> characterSet = new ArrayList<>();
                    System.out.println("\nEnter the character set for the knowledge base [lowerlatin, upperlatin, altlatin, greek]");
                    System.out.println("Greek & altlatin character sets require code page 65001");
                    System.out.println("Can set this in the terminal using 'chcp 65001'");
                    System.out.print("> ");
                    String characterSetIn = in.next(); // The character set used for the atoms
                    while (!validCharacterSet(characterSetIn)){
                        System.out.println("Enter valid character set [lowerlatin, upperlatin, altlatin, greek]:");
                        System.out.print("> ");
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
                        if(form.equalsIgnoreCase("t")){
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
                                    gen.reset();
                                    rerun = true;
                                }catch(InterruptedException | ExecutionException e){

                                }catch (StackOverflowError  e) {
                                        e.printStackTrace();
                            
                                }finally{
                                    executor.shutdownNow();
                                }
                            }while(rerun == true);
                        }else{
                            // will look at it later
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
                                    gen.reset();
                                    rerun = true;
                                }catch(InterruptedException | ExecutionException e){

                                }catch (StackOverflowError  e) {
                                        e.printStackTrace();
                            
                                }finally{
                                    executor.shutdownNow();
                                }
                            }while(rerun == true);
                        
                        }
                        long endTime = System.nanoTime();
                        long durationInNano = endTime - startTime;
                        double durationInSeconds = (double) durationInNano / 1000000000.0;

                        System.out.println("\nSave to text file? [y, n]:");
                        System.out.print("> ");
                        String save = in.next(); // Save the knowledge base to a text file
                        if(save.equalsIgnoreCase("y")){ kbToFile(KB);}
                        
                        System.out.println(" Save in database? [y, n]:");
                        System.out.print("> ");
                        String input = in.next(); // Print knowledge base to terminal
                        if(input.equalsIgnoreCase("y")){
                            System.out.print("Saving Knowledge base ...");

                            try{
                            
                                // long size = Size.getObjectSize(toArray(KB));
                                // System.out.println("Size of the object: " + size + " bytes");
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
                                    numDefImplications,
                                    characterSet.get(0),
                                    connectiveLists,
                                    transitive,
                                    reuseConsequent
                                );

                            boolean isSaved = knowledgeDAO.createKnowledgeOV2(knowledge);
                        
                            if (isSaved) {
                                System.out.println("Document was successfully saved in the Knowledge collection of the KnowledgeDB database.");
                            } else {
                                System.out.println("Failed to save the document.");
                            }
                            // Close the MongoDB connection
                            MongoDBConnection.closeConnection();
                            System.out.println(" Knowledge base saved.");
                            }catch(Exception e){
                                System.out.println(e.getMessage());
                            }

                            
                        
                        }

                        System.out.println("Print to terminal? [y, n]:");
                        System.out.print("> ");
                        String print = in.next(); // Print knowledge base to terminal
                        if(print.equalsIgnoreCase("y")){
                            System.out.println("Knowledge base:");
                            System.out.println(readable(KB));

                        }
                        
                        System.out.println("Test using baserank algorithm? [y, n]:");
                        System.out.print("> ");
                        String baseR = in.next(); // Print knowledge base to terminal
                        if(baseR.equalsIgnoreCase("y")){
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
                               // System.out.println(rankedKB);
                               /* for(PlBeliefSet i: rankedKB){
                                    System.out.println(i);
                                }*/

                                
                            }catch(Exception e){
                                e.printStackTrace();
                            }  
                         }



                        // Monte Carlo Results
                       { /*String directoryPath = "MonteCarloSimulationresults";
                        File directory = new File(directoryPath);
                        // Count the number of files starting with "MCSResults_" and ending with ".txt"
                        String[] files = directory.list((dir, name) -> name.startsWith("MCSResults_") && name.endsWith(".txt"));
                        int count = (files != null) ? files.length : 0;
                        count++;

                        String fileName = "MCSResults_" + count+ ".txt";
                        String filePath = Paths.get(directoryPath, fileName).toString();
                         
                        String content = numRanks+"&"+numDefImplications+"&"+distribution+"&"+minStatements+"&"+"&"+reuseConsequent+"&"+transitivity.get(0)+"&"+anComplexity.get(0)+"&"+consComplexity.get(0)+"&"+connectors+"&"+durationInSeconds+"\\\\ \n";
                        
                        try (FileWriter fileWriter = new FileWriter(filePath, true);
                        PrintWriter printWriter = new PrintWriter(fileWriter)) {
                            // Write the content to the file
                            printWriter.println(content);
                        } catch (IOException e) {
                            e.printStackTrace();
                    }*/}

                        gen.reset();
                        System.out.println("Regenerate new knowledge base? [r]:");
                        System.out.println("Change settings? [c]:");
                        System.out.println("Quit? [q]:");
                        System.out.print("> ");
                        choice = in.next();
                        
                    }while(choice.equalsIgnoreCase("r"));
        
                }
            }
            else{

                System.out.println("Existing Knowledge Base id:");
                System.out.print("> ");
                String id = in.next();
                
                KnowledgeDAO know = new KnowledgeDAO();
                Knowledge knowObj = know.readKnowledge(id);

                String dist = knowObj.getDistribution();
                int kbNumRanks = knowObj.getNumberOfRanks();
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
                    int minDI = minDefImplications(dist, numRanks);
                    System.out.println("Please enter the number of statements you require ["+minDI+" or more]:");
                    int numFormula = in.nextInt();
                    while(numFormula < minDI){
                        System.out.println("Please enter the number of statements you require, they strictly should be "+minDI+" or more:");
                        numFormula = in.nextInt();
                    }
                  

                    int[] defImplicationDistribution = Distribution.distributeDIs(numFormula, numRanks, dist, decay);
                    int kbNumFormula = getKBNumberofFormula(kbContent);
                    int[] oldDistribution = Distribution.distributeDIs(kbNumFormula, kbNumRanks, dist, decay);
                    int[] newDistribution = Distribution.getNewDistribution(defImplicationDistribution, oldDistribution);

                    ArrayList<ArrayList<String>> newKB = new ArrayList<>();
                    ArrayList<Integer> numForm = new ArrayList<>();
                    numForm.add(numFormula);


                    boolean rerun = true;
                    long start = System.nanoTime();
                        do{
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        long timeoutDuration = 20000000; /// May change 
                        
                        System.out.println("Generating Knowledge base...");

                        try{
                            Callable<ArrayList<ArrayList<String>>> kbGenerationTask = () -> {
                                return KBGeneratorThreadedOPT.KBGenerateNew(newDistribution, kbContent, numForm, kbAtomList, kbUsedAtoms,kbConnectors, kbCharSet, kbTransitivity,kbReuseConsequent);
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
                            gen.reset();
                            rerun = true;
                        }catch (InterruptedException | ExecutionException e) {
                            System.out.println("Error during KB generation: " + e.getMessage());
                            e.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("Unexpected error: " + e.getMessage());
                            e.printStackTrace();
                        }finally{
                            executor.shutdownNow();
                        }
                    }while (rerun);
                    long end = System.nanoTime();
                    
                    // generation speed calculation
                    long durationInNano = end - start;
                    double durationInSeconds = (double) durationInNano / 1000000000.0;
                    //durationInSeconds= durationInSeconds+kbSpeed;

                    System.out.println("Save to text file? [y, n]:");
                    System.out.print("> ");
                    String save = in.next(); // Save the knowledge base to a text file
                    if(save.equalsIgnoreCase("y")){ kbToFile(newKB);}
                        
                    System.out.println("Save in database? [y, n]:");
                    System.out.print("> ");
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
                            newKBUsedAtoms,
                            newKBAtomList,
                            dist,
                            numFormula,
                            kbCharSet.get(0),
                            kbConnectors,
                            trans,
                            kbReuseConsequent
                        );
                        boolean isSaved =false;
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
                        System.out.println(" Knowledge base saved.");
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                    }

                        System.out.println("Print to terminal? [y, n]:");
                        System.out.print("> ");
                        String print = in.next(); // Print knowledge base to terminal
                        if(print.equalsIgnoreCase("y")){
                            System.out.println("Knowledge base:");
                            System.out.println(readable(newKB));

                        }
                        
                        System.out.println("Quit? [q]:");
                        System.out.print("> ");
                        choice = in.next();
                        
            }
            }
       
            
        }while(choice.equalsIgnoreCase("c"));
        System.out.println("Quitting");
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

     /**
     * converts Knowledge base to a string
     *
     * @param KB The generated knowledgebase
     * @return The knowledge base as a String type.
     */
    private static String readable(LinkedHashSet<LinkedHashSet<DefImplication>>KB){
        int i = 0;
        String output= "";
        for (LinkedHashSet<DefImplication> set : KB){
            output=output+"Rank " + i + ": ";
            Iterator<DefImplication> iterator = set.iterator();
            while (iterator.hasNext()){
                DefImplication element = iterator.next();
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

    private static ArrayList<String> toArray(LinkedHashSet<LinkedHashSet<DefImplication>>KB){
  
        ArrayList<String> result= new ArrayList<>();
        for (LinkedHashSet<DefImplication>set : KB){
            String output="";
            Iterator<DefImplication> iterator = set.iterator();
            while (iterator.hasNext()){
                DefImplication element = iterator.next();
                output=output+element.toString();
                if(iterator.hasNext()){
                    output=output+", ";
                }else{
                    output=output+"";
                }
            }
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
    private static int minDefImplications(String distribution, int numRanks){
        int min = 0;
        switch(distribution){
            case "f":
                min = (numRanks*2)-1;
                break;
            case "lg":
                min = Distribution.minDIsLinear(numRanks);
                break;
            case "ld":
                min = Distribution.minDIsLinearDecline(numRanks);
                break;
            case "r":
                min = (numRanks*2);
                break;
            case "n":
                min =Distribution.minDIsNormal(numRanks);
                break;
            case "eg":
                min = Distribution.minDIsExp(numRanks);
                break;
            case "ed":
                min = Distribution.minDIsExp(numRanks);
                break;
        }
        return min;
    }

    /**
     * Writes a knowledge base to a text file.
     *
     * @param KB The knowledge base to write to the file.
     */
    private static void kbToFile(LinkedHashSet<LinkedHashSet<DefImplication>> KB){
        String filePath = "output" + filenum + ".txt";
        filenum++;
        try{
            File file = new File(filePath);
            FileWriter fw = new FileWriter(file);

            for(LinkedHashSet<DefImplication> set : KB){
                for (DefImplication element : set){
                    fw.write(element.toString() + "\n");
                }
            }
            fw.close();
        } 
        catch(IOException e){
            e.printStackTrace();
        }
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
            e.printStackTrace();
        }
    }
}










