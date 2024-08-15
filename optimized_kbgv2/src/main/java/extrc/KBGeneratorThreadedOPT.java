
package extrc;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.tweetyproject.arg.bipolar.examples.admissibleExample;

/**
 * The KBGeneratorThreadedOPT class provides an optimised version of defeasible knowledge base generation
 * using multithreading for parallel processing.
 */
public class KBGeneratorThreadedOPT{

    private static AtomCreation makeAtom = AtomCreation.getInstance();
    //private static int numThreads = Runtime.getRuntime().availableProcessors();
    private static ArrayList<String> atomList = new ArrayList<>();
    private static Connective con = new Connective(); 
    private static Random random = new Random();
    private static ArrayList<String> usedAtoms = new ArrayList<>();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private static AtomicBoolean[] generateDIsCompletionStatus;
    private static final Object lock = new Object();

    /**
     * Generates a knowledge base (KB) consisting of a collection of ranks, each containing defeasible implications (DIs).
     *
     * @param DiDistribution The distribution of DIs for each rank.
     * @param simpleOnly     If true, only simple DIs are generated; if false, complex DIs are also generated.
     * @param complexityAnt  The complexity of antecedents for complex DIs.
     * @param complexityCon  The complexity of consequents for complex DIs.
     * @param connectiveType The type of connectives used in complex DIs.
     * @param characterSet  The type of characters used as atoms to generaate the defeasible knowledge base. 
     * @param transitivity  The degree of transitivity within the statements.
     * @return A ArrayList<ArrayList<String>> representing the generated KB.
     */
    public static ArrayList<ArrayList<String>> KBGenerate(int[] DiDistribution, boolean simpleOnly, ArrayList<Integer> complexityAnt, ArrayList<Integer> complexityCon, int[] connectives, ArrayList<String> characterSet,  ArrayList<String> transitivity, ArrayList<Integer> numStatements, boolean reuseConsequent){      
        // selecting character set
        makeAtom.setCharacters(characterSet.get(0));

        //preparing connectives
        ArrayList<Integer> connectiveType = new ArrayList<>();
        ArrayList<Integer> infinityConnectiveType = new ArrayList<>();

        for (int i: connectives){
            if(i == 1 || i==2){ if(!connectiveType.contains(i)){connectiveType.add(i);}}
            else if(i == 3 || i==4){if(!infinityConnectiveType.contains(i)){ infinityConnectiveType.add(i);}}
            else if(i == 5){
                if(!connectiveType.contains(1)){connectiveType.add(1);}
                if(!connectiveType.contains(2)){connectiveType.add(2);}
                if(!infinityConnectiveType.contains(3)){ infinityConnectiveType.add(3);}
                if(!infinityConnectiveType.contains(4)){ infinityConnectiveType.add(4);}

            }
        }
      //  System.out.println(connectiveType);
       // System.out.println(infinityConnectiveType);
        
        
        
        //Antecedent Complexity
        int anteComplexity = complexityAnt.get(0);
        int consComplexity = complexityCon.get(0);
        System.out.println(anteComplexity+"|"+consComplexity);
       
       
        // generating atoms
        int numAtoms = 0;
        if( anteComplexity==1 && consComplexity ==1){
            numAtoms =  (DiDistribution.length + 1) + ((numStatements.get(0) -(DiDistribution.length * 2 - 1)));
        }else{
            numAtoms = (DiDistribution.length + 1) + ((numStatements.get(0) -(DiDistribution.length * 2 - 1))/3);
        }
        int NumAtoms = numAtoms;
        atomList.clear();
      
        Callable<Void> generateAtoms = () -> {
            makeAtom.newGenerateAtom(NumAtoms, atomList);
            return null;
        };


        // generating statements per rank
        ArrayList<ArrayList<String>> KnowledgeB = new ArrayList<>();

        // Initialize completion status array
        generateDIsCompletionStatus = new AtomicBoolean[DiDistribution.length];
        for (int i = 0; i < generateDIsCompletionStatus.length; i++) {
            generateDIsCompletionStatus[i] = new AtomicBoolean(false);
        }

        Callable<Void> generateDIs = () -> {
            //Knowledge base entry
            ArrayList<String> entry;

            //For Complex and Simple Statements
            String atom = "";
            String conflictAtom ="";
            String contrConsequent = "";
            synchronized (lock) {
                int defRanks = 0;
                if(infinityConnectiveType.size()==0){defRanks=DiDistribution.length;}
                else{defRanks=DiDistribution.length-1;}

                for(int i=0; i<defRanks;i++){
                    while (atomList.size() < anteComplexity || atomList.size() <consComplexity) { 
                        // Wait for creation of atoms to complete index i
                    }
                
                    if(i == 0){
                        do {    
                            atom = getFormula(anteComplexity, atomList, connectiveType);
                        } while (usedAtoms.contains(atom));
                        usedAtoms.add(atom);
                    
                        do{
                            conflictAtom = getFormula(anteComplexity, atomList, connectiveType); 
                        } while (usedAtoms.contains(conflictAtom));
                        usedAtoms.add(conflictAtom);
                    
                        do{
                            contrConsequent = getFormula(consComplexity, atomList, connectiveType); 
                        } while (usedAtoms.contains(contrConsequent));
                        usedAtoms.add(contrConsequent);
                        

                        entry = new ArrayList<>();
                        entry.add(atom+" "+con.getDISymbol()+" "+contrConsequent);
                        DiDistribution[i]-=1;
                        KnowledgeB.add(entry);
                        generateDIsCompletionStatus[i].set(true); ///

                        entry =new ArrayList<>();
                        entry.add(conflictAtom+" "+con.getDISymbol()+" "+con.getNegationSymbol()+contrConsequent);
                        entry.add(conflictAtom+" "+con.getDISymbol()+" "+atom);
                        DiDistribution[i+1]-=2; 
                        KnowledgeB.add(entry);
                        generateDIsCompletionStatus[i+1].set(true); ///

                        atom=conflictAtom;
                        
                    }else{                           
                            if(i%2==0){
                                if(i+1<DiDistribution.length){
                                    entry =new ArrayList<>();
                                    do{
                                        conflictAtom = getFormula(anteComplexity, atomList, connectiveType);
                                    }while (usedAtoms.contains(conflictAtom));
                                    usedAtoms.add(conflictAtom);

                                    entry.add(conflictAtom+" "+con.getDISymbol()+" "+con.getNegationSymbol()+contrConsequent);
                                    entry.add(conflictAtom+" "+con.getDISymbol()+" "+atom);
                                
                                    DiDistribution[i+1]-=2;
                                    KnowledgeB.add(entry);

                                    atom=conflictAtom; 
                                    generateDIsCompletionStatus[i+1].set(true); ///
                                }
                            }else{

                                if(i+1<DiDistribution.length){
                                    entry =new ArrayList<>();
                                    do{
                                        conflictAtom = getFormula(anteComplexity, atomList, connectiveType);
                                    }while (usedAtoms.contains(conflictAtom));
                                    usedAtoms.add(conflictAtom);

                                    entry.add(conflictAtom+" "+con.getDISymbol()+" "+contrConsequent);
                                    entry.add(conflictAtom+" "+con.getDISymbol()+" "+atom);
                                    
                                    DiDistribution[i+1]-=2; 
                                    KnowledgeB.add(entry);

                                    atom=conflictAtom;
                                    generateDIsCompletionStatus[i+1].set(true); ///
                                }
                            }
                            
                    }
                        
                }
               
            }
            return null;
        };
        //Transitivity 
        String transitiveStatements = transitivity.get(0);

        Callable<Void> generateOtherformulas = () -> {
            if(notALL(DiDistribution)){
                String currAtom;
                String consequent="";
                int ran = 0;
                synchronized (lock) {
                    int defRanks = 0;
                    if(infinityConnectiveType.size()==0){defRanks=DiDistribution.length;}
                    else{defRanks=DiDistribution.length-1;}
                    for(int i=0; i<defRanks;i++){
                        
                        while (!generateDIsCompletionStatus[i].get()) {
                            //Wait
                        }
                    
                        String[] formula = (KnowledgeB.get(i).get(0)).split(" ");
                        currAtom = formula[0];
                        int index =0;

                        while(DiDistribution[i]>0){
                            index++;
                            if (transitiveStatements.equalsIgnoreCase("n")){
                                consequent = getFormula(consComplexity, atomList, connectiveType);
                                if(!reuseConsequent){atomList.remove((String)consequent);}
                                usedAtoms.add(consequent);
                                KnowledgeB.get(i).add(currAtom+" "+con.getDISymbol()+" "+consequent);
                                DiDistribution[i]--;
                            }else if(transitiveStatements.equalsIgnoreCase("y")){
                                if(index>1){
                                    currAtom = consequent;
                                }
                                consequent = getFormula(consComplexity, atomList, connectiveType); 
                                if(!reuseConsequent){atomList.remove((String)consequent);}
                                usedAtoms.add(consequent);
                                KnowledgeB.get(i).add(currAtom+" "+con.getDISymbol()+" "+consequent);
                                DiDistribution[i]--;
                                
                            }else{
                                if(index>1){
                                    ran  = random.nextInt(2);
                                    if (ran == 1){
                                            currAtom = consequent;
                                    }else{
                                            currAtom = formula[0];
                                    }
                                }
                               consequent = getFormula(consComplexity, atomList, connectiveType); 
                               if(!reuseConsequent){atomList.remove((String)consequent);}
                                usedAtoms.add(consequent);
                                KnowledgeB.get(i).add(currAtom+" "+con.getDISymbol()+" "+consequent);
                                DiDistribution[i]--;
                            }
                        
                        }   
                        
                    }
                    if(infinityConnectiveType.size()>0){
                        currAtom = getFormula(anteComplexity, atomList, connectiveType);
                        int index = 0;
                        ArrayList<String> entry = new ArrayList<>();

                        while(DiDistribution[defRanks]>0){
                            index++;
                            if (transitiveStatements.equalsIgnoreCase("n")){
                                consequent = getFormula(consComplexity, atomList, connectiveType);
                                if(!reuseConsequent){atomList.remove((String)consequent);}
                                usedAtoms.add(consequent);
                                entry.add(currAtom+" "+getOperator(infinityConnectiveType)+" "+consequent);
                                DiDistribution[defRanks]--;
                            }else if(transitiveStatements.equalsIgnoreCase("y")){
                                if(index>1){
                                    currAtom = consequent;
                                }
                                consequent = getFormula(consComplexity, atomList, connectiveType); 
                                if(!reuseConsequent){atomList.remove((String)consequent);}
                                usedAtoms.add(consequent);
                                entry.add(currAtom+" "+getOperator(infinityConnectiveType)+" "+consequent);
                                DiDistribution[defRanks]--;
                                
                            }else{
                                if(index>1){
                                    ran  = random.nextInt(2);
                                    if (ran == 1){
                                            currAtom = consequent;
                                    }else{
                                        currAtom = getFormula(anteComplexity, atomList, connectiveType);
                                    }
                                }
                               consequent = getFormula(consComplexity, atomList, connectiveType); 
                               if(!reuseConsequent){atomList.remove((String)consequent);}
                                usedAtoms.add(consequent);
                                entry.add(currAtom+" "+getOperator(infinityConnectiveType)+" "+consequent);
                                DiDistribution[defRanks]--;
                            }
                        
                        }   
                        KnowledgeB.add(entry);
                        
                    }
                } 
            }
            return null;
        };
    
        Future<Void> future1 = executorService.submit(generateAtoms);
        Future<Void> future2 = executorService.submit(generateDIs);
        Future<Void> future3 = executorService.submit(generateOtherformulas);

        try {
            future1.get(); // Wait for generation of atoms to complete
            future2.get(); // Wait for creation of defeasible implication to complete
            future3.get(); // Wait for generation of other formulas to complete
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Shutdown executor
        executorService.shutdown();
    
        /* try {Thread.sleep(1700);} catch (InterruptedException e) { e.printStackTrace();} */
        //adding used and non-used atoms to the knowledge base.
        KnowledgeB.add(atomList);
        KnowledgeB.add(usedAtoms);
        
        return KnowledgeB;
    }
     
    /**
     * Generates a knowledge base (KB) consisting of a collection of ranks, each containing defeasible implications (DIs) from an existing knowledgwbase.
     *
     * @param newDistribution The distribution of DIs for each rank.
     * @param kbContent  The previously generated defeasible knowledge base.
     * @param numForm  The number of statements required in the distribution.
     * @param kbAtomList  The existing atom list from the previous defeasible knowledge base generation.
     * @param kbUsedList The list of used atoms from the previous defeasible knowledge base generation.
     * @param connectiveType The type of connectives used in complex DIs.
     * @param characterSet  The type of characters used as atoms to generaate the defeasible knowledge base. 
     * @return A ArrayList<ArrayList<String>> representing the generated KB.
     */
    public static ArrayList<ArrayList<String>> KBGenerateNew(int[] newDistribution, ArrayList<ArrayList<String>>  kbContent, ArrayList<Integer> numForm ,ArrayList<String> kbAtomList, ArrayList<String> kbUsedAtoms,int[] connectives, ArrayList<String> characterSet, ArrayList<String> transitivity, boolean reuseConsequent){
        //Determine complexities
        String[] formula = kbContent.get(0).get(0).split("["+con.getBiImplicationSymbol()+con.getDISymbol()+"]");
        int anteComplexity = getComplexity(formula[0]);
        int consComplexity = getComplexity(formula[2].trim());

        //preparing connectives
        ArrayList<Integer> connectiveType = new ArrayList<>();
        ArrayList<Integer> infinityConnectiveType = new ArrayList<>();

        for (int i: connectives){
            if(i == 1 || i==2){ if(!connectiveType.contains(i)){connectiveType.add(i);}}
            else if(i == 3 || i==4){if(!infinityConnectiveType.contains(i)){ infinityConnectiveType.add(i);}}
            else if(i == 5){
                if(!connectiveType.contains(1)){connectiveType.add(1);}
                if(!connectiveType.contains(2)){connectiveType.add(2);}
                if(!infinityConnectiveType.contains(3)){ infinityConnectiveType.add(3);}
                if(!infinityConnectiveType.contains(4)){ infinityConnectiveType.add(4);}
            }
        }
      
        //ensuring theres enough atoms
        int atomLength = 1;

        //selecting character set
        makeAtom.setCharacters(characterSet.get(0));
        int startChar= (int) makeAtom.getStart();
        int endChar = (int) makeAtom.getEnd();
        int baseChar = (endChar - startChar)+1;
        boolean notEnoughAtoms = false;
    
        
        int required =  0;
        if(anteComplexity==1 && consComplexity==1){
            required = (newDistribution.length + 1) + ((numForm.get(0) -(newDistribution.length * 2 - 1)));
        }else{
            required = (newDistribution.length + 1) + ((numForm.get(0) -(newDistribution.length * 2 - 1))/3);
        }
        int requiredAtoms = required;
        int alreadyCreated = kbAtomList.size() + kbUsedAtoms.size();
        int diff = 0;
        if(alreadyCreated< requiredAtoms){
            diff= requiredAtoms - alreadyCreated;
            notEnoughAtoms = true;
            if(alreadyCreated>=(baseChar) && alreadyCreated <= (baseChar + baseChar*baseChar)){
                atomLength = 2;
            }else if(alreadyCreated>(baseChar + baseChar*baseChar) && alreadyCreated <= (baseChar + baseChar*baseChar+ baseChar*baseChar*baseChar)){
                atomLength = 3;
            }
            // Many more conditions maybe added.
        }
        
        int difference = diff;
        boolean NotEnoughAtoms=notEnoughAtoms;
        
        Callable<Void> generateAtoms = () -> {
            if(NotEnoughAtoms){
                // makeAtom.generateAtom(diff, kbAtomList,usedAtoms, atomLength, baseChar);
                makeAtom.newGenerateAtom(alreadyCreated,difference, kbAtomList);
            }
            return null;
        };

        //starting point
        int start = kbContent.size();

         // Initialize completion status array
        generateDIsCompletionStatus = new AtomicBoolean[newDistribution.length];
        for (int i = 0; i < generateDIsCompletionStatus.length; i++) {
            generateDIsCompletionStatus[i] = new AtomicBoolean(false);
        }
        
        Callable<Void> generateDIs = () -> {
            synchronized (lock) {
            //For Complex and simple Statements
            String[] lastRankFormula = kbContent.get(kbContent.size()-1).get(0).split("["+con.getBiImplicationSymbol()+con.getDISymbol()+"]");
            String atom=lastRankFormula[0].trim();
            String conflictAtom="";
            String contrConsequent=formula[2].trim();

            //for generating statements per rank
            ArrayList<String> entry; 
                for(int i=0; i<newDistribution.length;i++){  
                    
                    if(i>= start) {  
                        while (kbAtomList.size() < anteComplexity || kbAtomList.size() <consComplexity) { 
                           // Wait for creation of atoms to complete index i
                           
                        }  
                        
                        if((i+1)%2==0){
                            if(i<newDistribution.length){
                                entry =new ArrayList<>();
                                do{
                                    conflictAtom = getFormula(anteComplexity, kbAtomList, connectiveType);
                                }while (kbUsedAtoms.contains(conflictAtom));
                                kbUsedAtoms.add(conflictAtom);

                                entry.add(conflictAtom+" "+con.getDISymbol()+" "+con.getNegationSymbol()+contrConsequent);
                                entry.add(conflictAtom+" "+con.getDISymbol()+" "+atom);
                            
                                newDistribution[i]-=2;
                                kbContent.add(entry);

                                atom=conflictAtom; 
                                generateDIsCompletionStatus[i].set(true); ///
                            }
                        }else{
                            if(i<newDistribution.length){
                                entry = new ArrayList<>();
                                do{
                                    conflictAtom = getFormula(anteComplexity, kbAtomList, connectiveType);
                                }while (kbUsedAtoms.contains(conflictAtom));
                                kbUsedAtoms.add(conflictAtom);

                                entry.add(conflictAtom+" "+con.getDISymbol()+" "+contrConsequent);
                                entry.add(conflictAtom+" "+con.getDISymbol()+" "+atom);
                                
                                newDistribution[i]-=2; 
                                kbContent.add(entry);

                                atom=conflictAtom;
                                generateDIsCompletionStatus[i].set(true); ///
                            }
                        }
                    }else{generateDIsCompletionStatus[i].set(true);}
                }
            }
            return null;
        }; 
        
        //Transitivity 
        String transitiveStatements = transitivity.get(0);

       //filling in the missing statements
       Callable<Void> generateOtherformulas = () -> {
            if(notALL(newDistribution)){
                String currAtom;
                String consequent="";
                int ran = 0;
                synchronized (lock) {
                    for(int i=0; i<newDistribution.length;i++){
                        while (!generateDIsCompletionStatus[i].get()) {
                        
                        }
                        String[] formulaEntry = (kbContent.get(i).get(0)).split(" ");
                        currAtom = formulaEntry[0];
                        int index =0;

                        while(newDistribution[i]>0){
                            if (transitiveStatements.equalsIgnoreCase("n")){
                                consequent = getFormula(consComplexity, kbAtomList, connectiveType); 
                                if(!reuseConsequent){kbAtomList.remove((String)consequent);}
                                kbUsedAtoms.add(consequent);
                                kbContent.get(i).add(currAtom+" "+con.getDISymbol()+" "+consequent);
                                newDistribution[i]--;
                            }else if(transitiveStatements.equalsIgnoreCase("y")){
                                if(index>1){
                                    currAtom = consequent;
                                }
                                consequent = getFormula(consComplexity, kbAtomList, connectiveType); 
                                if(!reuseConsequent){kbAtomList.remove((String)consequent);}
                                kbUsedAtoms.add(consequent);
                                kbContent.get(i).add(currAtom+" "+con.getDISymbol()+" "+consequent);
                                newDistribution[i]--;
                            }else{
                                if(index>1){
                                    ran  = random.nextInt(2);
                                    if (ran == 1){
                                            currAtom = consequent;
                                    }else{
                                            currAtom = formulaEntry[0];
                                    }
                                }
                                consequent = getFormula(consComplexity, kbAtomList, connectiveType); 
                                if(!reuseConsequent){kbAtomList.remove((String)consequent);}
                                kbUsedAtoms.add(consequent);
                                kbContent.get(i).add(currAtom+" "+con.getDISymbol()+" "+consequent);
                                newDistribution[i]--;
                            }
                        }
                    }
                }
            }
            return null;
        };
    
        Future<Void> future1 = executorService.submit(generateAtoms);
        Future<Void> future2 = executorService.submit(generateDIs);
        Future<Void> future3 = executorService.submit(generateOtherformulas);

        try {
            future1.get(); // Wait for generation of atoms to complete
            future2.get(); // Wait for creation of defeasible implication to complete
            future3.get(); // Wait for generation of other formulas to complete
        } catch (Exception e) {e.printStackTrace();}

         // Shutdown executor
        executorService.shutdown();

        /* try {Thread.sleep(1700);} catch (InterruptedException e) { e.printStackTrace();} */
        //adding used and non-used atoms to the knowledge base.

        kbContent.add(kbAtomList);
        kbContent.add(kbUsedAtoms);

        return kbContent;
    }
  
    /**
     * Determines the complexity of a formula.
     *
     * @param formula The formula in String format.
     * @return An integer representing the level of complexity.
     */
    public static int getComplexity(String formula ){
        String[] complexity = formula.split("["+con.getConjunctionSymbol()+con.getDisjunctionSymbol()+con.getNegationSymbol()+"]");
        int complexityValue = 0;
        for (String entry : complexity) {
            if (!entry.isEmpty()) {
                complexityValue++;   
            }
        }
        return complexityValue;
    }

    /**
     * Creates a formula based of its complexity.
     *
     * @param anteComplexity The formula in String format.
     * @param atomList The list of atoms used to create the formula.
     * @param connectives The list of connectives used to create the formula.
     * @return A String type formula.
     */
    public static String getFormula( int anteComplexity, ArrayList<String> atomList, ArrayList<Integer> connectives) { 
        String formula = "";

        for(int i = 0; i< anteComplexity; i++){
            if(anteComplexity==1){try {Thread.sleep(1);} catch (InterruptedException e) { e.printStackTrace();}}
            formula = formula + atomList.get(1+random.nextInt(atomList.size()-1));
    
            if(i<anteComplexity-1)
            {  
                switch (connectives.get(random.nextInt(connectives.size()))) {
                    case 1:
                        formula = formula + con.getDisjunctionSymbol();
                        break;
                    case 2:
                        formula = formula+con.getConjunctionSymbol();
                        break;

                }
            }
        } 
    
            return formula;
    }
    
    public static String getOperator(ArrayList<Integer> connectives) { 
        String formula = "";
        switch (connectives.get(random.nextInt(connectives.size()))) {
            case 3:
                formula = con.getImplicationSymbol();
                break;
            case 4:
                formula = con.getBiImplicationSymbol();
                break;

        }
        return formula;
    }
    
     /**
     * Checks if all formulas a generated according to the distribution.
     *
     * @param distArr The array with the required distribution.
     * @return A boolean response to whether all formulas are generated as per the distribution.
     */
    public static boolean notALL(int[] distrArr){
      boolean allFormulasCreated =false;
      int counter =0;
      for(int i=0; i<distrArr.length; i++){
        counter=counter+distrArr[i];
        if(counter>0){
            allFormulasCreated=true;
        }
      }
      return allFormulasCreated;
    }

}

