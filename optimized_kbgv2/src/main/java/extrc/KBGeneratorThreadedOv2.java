
package extrc;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.tweetyproject.arg.dung.causal.syntax.KnowledgeBase;


/**
 * The KBGeneratorThreadedOPT class provides an optimised version of defeasible knowledge base generation
 * using multithreading for parallel processing.
 */
public class KBGeneratorThreadedOv2{

    private static AtomCreation makeAtom = AtomCreation.getInstance();
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
       public static ArrayList<ArrayList<String>> KBGenerate(int[] DiDistribution, ArrayList<Integer> complexityAnt, ArrayList<Integer> complexityCon, int[] connectives, ArrayList<String> characterSet,  ArrayList<String> transitivity, ArrayList<Integer> numStatements, boolean reuseConsequent){      
        // selecting character set
        makeAtom.setCharacters(characterSet.get(0));

        // Antecedent Complexity
        int anteComplexity = complexityAnt.get(0);
        int consComplexity = complexityCon.get(0);       
         
        // generating atoms
        int numAtoms = numStatements.get(0)*anteComplexity+numStatements.get(0)*consComplexity;
        Callable<Void> generateAtoms = () -> { makeAtom.newGenerateAtom(numAtoms, atomList); return null;};

        //preparing connectives
        ArrayList<Integer> rankCons= rankConnectives(connectives, "defeasible");
        ArrayList<Integer> infinityCons = rankConnectives(connectives, "infinity");
       

        // initialize final knowledge base
        ArrayList<ArrayList<String>> KB = new ArrayList<>();
        ArrayList<ArrayList<String>> KBHold = new ArrayList<>();
        ArrayList<String> usedAtoms = new ArrayList<>();
       

        // Initialize completion status array
        generateDIsCompletionStatus = new AtomicBoolean[DiDistribution.length];
        for (int i = 0; i < generateDIsCompletionStatus.length; i++) {
            generateDIsCompletionStatus[i] = new AtomicBoolean(false);
        }
 
       
        Callable<Void> generateDIs = () -> {
       
            //Knowledge base entry
            ArrayList<String> entry = new ArrayList<>();
            
            //For Complex and Simple Statements
            String antecedent = getFormula(1, atomList, rankCons);
            String conflictAtom = getFormula(1, atomList, rankCons);
            String prevAntecedent = "";
            synchronized (lock) {
                synchronized(atomList){
                    antecedent = getFormula(1, atomList, rankCons);
                    conflictAtom = getFormula(1, atomList, rankCons);
                    prevAntecedent = "";
                }
                
                for(int i=0; i<DiDistribution.length; i++){
                    while(atomList.size()< anteComplexity+consComplexity){/*wait*/}

                    if(i==0){
                        entry.add(statement(antecedent,conflictAtom));
                        synchronized(KB){
                            KB.add(entry);
                        }
                        DiDistribution[i]--;
                        generateDIsCompletionStatus[i].set(true);///
                        
                        usedAtoms.add(antecedent);
                        usedAtoms.add(conflictAtom);
                        
                        if(!usedAtoms.contains(antecedent)){usedAtoms.add(antecedent);}
                        if(!usedAtoms.contains(conflictAtom)){usedAtoms.add(conflictAtom);}
                        if(atomList.contains(antecedent)){synchronized(atomList){atomList.remove(antecedent);}}
                        if(atomList.contains(conflictAtom)){synchronized(atomList){ atomList.remove(conflictAtom);}}
                        entry= new ArrayList<>();
                    
                    }else{
                        prevAntecedent = antecedent;
                        synchronized(atomList){
                            antecedent = getFormula(1, atomList, rankCons);
                        }
                        while(usedAtoms.contains(antecedent)){
                            synchronized(atomList){
                                antecedent = getFormula(1, atomList, rankCons);
                            }
                        }
                        entry.add(statement(antecedent, prevAntecedent));
                        conflictAtom= negation(conflictAtom);
                        entry.add(statement(antecedent,conflictAtom));
                        synchronized(KB){
                            KB.add(entry);
                        }
                        DiDistribution[i] = DiDistribution[i]-2;
                        generateDIsCompletionStatus[i].set(true);///

                        if(!usedAtoms.contains(antecedent)){usedAtoms.add(antecedent);}
                        if(!usedAtoms.contains(conflictAtom)){usedAtoms.add(conflictAtom);}
                        if(atomList.contains(antecedent)){synchronized(atomList){atomList.remove(antecedent);}}
                        if(atomList.contains(conflictAtom)){synchronized(atomList){ atomList.remove(conflictAtom);}}
                  
                        entry= new ArrayList<>();
                        
                    }
                    if (i==DiDistribution.length-1){
                        KBHold.addAll(KB);
                    }
                   showProgress(i*2+1, numStatements.get(0));
                }
            }   
            return null;
        };

        //Transitivity 
        Boolean transitive = transitivity.get(0) == "y"? true:false;
        // Reuse Consequents
        

        Callable<Void> generateOtherformulas = () -> {
            if(notALL(DiDistribution) - (DiDistribution.length*2-1)>0)   {     
                //For Complex and Simple Statements
                //Knowledge base entry
                ArrayList<String> entry = new ArrayList<>();
                String antecedent ="";
                String consequent = "";
                synchronized (lock) {
              
                    for(int i = 0; i <DiDistribution.length; i++){
                       // System.out.println("KB:"+KBHold.size());
                       synchronized(KB){
                        System.out.println("KB:"+KB.size());
                       }
                        while (i>=KB.size()){
                        }

                        String rankAtom = getRankAtom(KB.get(i).get(0));

                        while(DiDistribution[i]>0){
                            int rankAtomInConsequent = random.nextInt(2);
                            int proceed = random.nextInt(2);
                            if(rankAtomInConsequent==0){
                                if(transitive && proceed==1 && i>0 && DiDistribution[i]>3){
                                    String formula = KB.get(i-1).get(2+random.nextInt(KB.get(i).size()-1));
                                    consequent = negation(getConsequent(formula));
                                    String prevRankAntecedent = getAntecedent(formula);
                                    synchronized(atomList){
                                        antecedent = getFormula(anteComplexity, atomList, rankCons);
                                    }
                                    entry.add(statement(rankAtom, antecedent));
                                    while(KB.get(i).contains(entry.get(0))){
                                        synchronized(atomList){
                                            antecedent = getFormula(anteComplexity, atomList, rankCons);
                                        }
                                        entry = new ArrayList<>();
                                        entry.add(statement(rankAtom, antecedent));
                                    } 
                                    entry.add(statement(antecedent,consequent));
                                    entry.add(statement(antecedent,prevRankAntecedent));

                                    synchronized(atomList){
                                        consequent = getFormula(consComplexity, atomList, rankCons);
                                    }
                                    entry.add(statement(antecedent,consequent));
                                    DiDistribution[i] = DiDistribution[i] - 3;

                                }else{
                                    synchronized(atomList){
                                        antecedent = getFormula(anteComplexity, atomList, rankCons, rankAtom);
                                        consequent = getFormula(consComplexity, atomList, rankCons);
                                    }
                                    while(usedAtoms.contains(antecedent)){
                                        synchronized(atomList){
                                            antecedent = getFormula(anteComplexity, atomList, rankCons, rankAtom);
                                        }
                                    }
                                    while(!reuseConsequent && usedAtoms.contains(consequent)){
                                        synchronized(atomList){
                                            consequent = getFormula(consComplexity, atomList, rankCons);
                                        }
                                    }
                                }
                                
                            }else{
                                synchronized(atomList){
                                    antecedent = getFormula(anteComplexity, atomList, rankCons);
                                    consequent = getFormula(consComplexity, atomList, rankCons,rankAtom);
                                }
                                while(usedAtoms.contains(antecedent)){
                                    synchronized(atomList){
                                        antecedent = getFormula(anteComplexity, atomList, rankCons);
                                    }
                                }
                                while(!reuseConsequent && usedAtoms.contains(consequent)){
                                    synchronized(atomList){
                                        consequent = getFormula(consComplexity, atomList, rankCons, rankAtom);
                                    }
                                }
                               
                            }                       
                            if(!transitive){
                                entry.add(statement(antecedent,consequent));
                            }
                           
                            KB.get(i).addAll(entry);
                            DiDistribution[i]--;
                     
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Shutdown executor
        executorService.shutdown();
        
        KB.add(atomList);
        KB.add(usedAtoms);
        
        return KB;
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
                                    synchronized(kbAtomList){
                                    conflictAtom = getFormula(anteComplexity, kbAtomList, connectiveType);
                                    }
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
            if(notALL(newDistribution)-(newDistribution.length*2-1)>0){
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
    public static String getFormula( int Complexity, ArrayList<String> atomList, ArrayList<Integer> connectives) { 
        String formula = "";
        int complexity = 1+ random.nextInt(Complexity);

        synchronized(atomList){ 
            for(int i = 0; i< complexity; i++){
                    while (atomList.size()<Complexity) { /*wait*/ }
                    int index = random.nextInt(atomList.size());
                    formula = formula + atomList.get(index);
                
                if(i<complexity-1)
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
        } 
      //System.out.println(formula);
    
        return formula;
    }

    public static String getFormula( int Complexity, ArrayList<String> atomList, ArrayList<Integer> connectives, String rankAtom) { 
      
        String formula = "";
        int complexity = random.nextInt(Complexity+1);
        int rankAtomIndex = random.nextInt(complexity);
        
        synchronized(atomList){
            for(int i = 0; i< complexity; i++){
                while (atomList.size()<Complexity) { /*wait*/ }
                if(rankAtomIndex==i){formula = formula + rankAtom;}
                else{
                    int index = random.nextInt(atomList.size());
                    formula = formula + atomList.get(index);
                }
                if(i<complexity-1)
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
    public static int notALL(int[] distrArr){
     // boolean allFormulasCreated =false;
      int counter =0;
      for(int i=0; i<distrArr.length; i++){
        counter=counter+distrArr[i];
       // if(counter>0){
        //    allFormulasCreated=true;
       // }
      }
      return counter;
    }

    public static void showProgress(int completed, int total) {
        int progress = (int) ((completed / (double) total) * 100);
        StringBuilder progressBar = new StringBuilder("[");
        int completedBars = progress / 5; // Assuming the progress bar is 20 characters wide
        for (int i = 0; i < 20; i++) {
            if (i < completedBars) {
                progressBar.append("=");
            } else {
                progressBar.append("");
            }
        }
        progressBar.append("] ").append(progress).append("% Complete");
        System.out.print("\r" + progressBar.toString());
    }
     
    public static ArrayList<Integer> rankConnectives(int[] connectives, String type){
        ArrayList<Integer> connectiveType = new ArrayList<>();
        if (type=="defeasible"){
            for (int i: connectives){
                if(i == 1 || i==2){ if(!connectiveType.contains(i)){connectiveType.add(i);}}
                else if(i == 5){
                    if(!connectiveType.contains(1)){connectiveType.add(1);}
                    if(!connectiveType.contains(2)){connectiveType.add(2);}
                }
            }
        }else{
            for (int i: connectives){
                if(i == 3 || i==4){if(connectiveType.contains(i)){ connectiveType.add(i);}}
                else if(i == 5){
                    if(!connectiveType.contains(3)){ connectiveType.add(3);}
                    if(!connectiveType.contains(4)){ connectiveType.add(4);}

                }
            }
        }
        return connectiveType;
    }
  
    public static String statement(String antecedent,String conflictAtom){
        return antecedent+con.getDISymbol()+conflictAtom;
    }
  
    public static String negation(String consequent){
        String conflictAtom = "";
        if (consequent.contains("!(")){
            conflictAtom = consequent.substring(2, consequent.length()-1);
        }else{
            conflictAtom = con.getNegationSymbol()+"("+consequent+")";
        }
        return conflictAtom;
    }

    public static String getRankAtom(String rank){
        String[] formula = (rank).split(con.getDISymbol());
        return formula[0]; 
    }

    public static String getAntecedent (String formula){
        String[] atoms = formula.split(con.getDISymbol());
        return atoms[0];
    }
  
    public static String getConsequent (String formula){
        String[] atoms = formula.split(con.getDISymbol());
        return atoms[1];
    }


}

