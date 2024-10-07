
package extrc;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.tweetyproject.arg.bipolar.examples.admissibleExample;

/**
 * The KBGeneratorThreadedOPT class provides an optimised version of defeasible knowledge base generation
 * using multithreading for parallel processing.
 */
public class KBGeneratorThreadedOPT{

    private static AtomCreation makeAtom = AtomCreation.getInstance();
    private static ArrayList<String> atomList = new ArrayList<>();
    private static Connective con = new Connective(); 
    private static Random random = new Random();
    private static ArrayList<String> usedAtoms = new ArrayList<>();
    private static int numThreads = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
    private static AtomicBoolean[] generateDIsCompletionStatus;
    private static final Object lock = new Object();
    private static AtomicInteger Count = new AtomicInteger(0);
    private static AtomicInteger inf = new AtomicInteger(0);

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
      
        int total = numStatements.get(0);

        
        // selecting character set
        makeAtom.setCharacters(characterSet.get(0));

        // preparing connectives
        ArrayList<Integer> connectiveType = new ArrayList<>();
        ArrayList<Integer> infinityConnectiveType = new ArrayList<>();

        for (int i: connectives){
            if(i == 1 || i==2){ 
                if(!connectiveType.contains(i)){connectiveType.add(i);
                if(!infinityConnectiveType.contains(i)){infinityConnectiveType.add(i);}
            }}
            else if(i == 3 || i==4){
                if(!infinityConnectiveType.contains(i)){ 
                    infinityConnectiveType.add(i);
                }
            }
            else if(i == 5){
                if(!connectiveType.contains(1)){connectiveType.add(1);}
                if(!connectiveType.contains(2)){connectiveType.add(2);}
                if(!infinityConnectiveType.contains(1)){ infinityConnectiveType.add(1);}
                if(!infinityConnectiveType.contains(2)){ infinityConnectiveType.add(2);}
                if(!infinityConnectiveType.contains(3)){ infinityConnectiveType.add(3);}
                if(!infinityConnectiveType.contains(4)){ infinityConnectiveType.add(4);}

            }
        }
        
        // Antecedent Complexity
        int anteComplexity = complexityAnt.get(0);
        int consComplexity = complexityCon.get(0);       
       
        // generating atoms
        int numAtoms = 0;
        if( anteComplexity==1 && consComplexity ==1){
            numAtoms =  (DiDistribution.length + 1) + ((numStatements.get(0) -(DiDistribution.length * 2 - 1))*2);
        }else{
            numAtoms =  ((DiDistribution.length + 1) + ((numStatements.get(0) -(DiDistribution.length * 2 - 1))*2))*anteComplexity*consComplexity;
        }
        
        int NumAtoms = numAtoms;
        atomList.clear();
       
        makeAtom.newGenerateAtom(NumAtoms, atomList);
    

        // generating statements per rank
        ArrayList<ArrayList<String>> KnowledgeB = new ArrayList<>();

     
        //Knowledge base entry
        ArrayList<String> entry;

        //For Complex and Simple Statements
        String atom = "";
        String conflictAtom ="";
        String contrConsequent = "";

     
        for(int i=0; i<DiDistribution.length;i++){
            if(i == 0){
                do {    
                    synchronized(atomList){
                        //if(anteComplexity ==1 ){
                            atom = atomList.get(random.nextInt(atomList.size()));
                        //}
                        //else{atom = getFormula(anteComplexity, atomList, connectiveType);}
                    }
                } while (usedAtoms.contains(atom));
                usedAtoms.add(atom);
                synchronized(atomList){atomList.remove(atom);}
            
                do{ 
                    synchronized(atomList){
                        //if(anteComplexity ==1 ){
                            conflictAtom = atomList.get(random.nextInt(atomList.size()));
                        //}
                        //else{conflictAtom = getFormula(anteComplexity, atomList, connectiveType);}
                    } 
                } while (usedAtoms.contains(conflictAtom));
                usedAtoms.add(conflictAtom);
                synchronized(atomList){atomList.remove(conflictAtom);}
            
                do{ synchronized(atomList){
                    //if(consComplexity ==1 ){
                        contrConsequent = atomList.get(random.nextInt(atomList.size()));
                    //}
                    //else{ contrConsequent = getFormula(consComplexity, atomList, connectiveType);} 
                }
                } while (usedAtoms.contains(contrConsequent));
                usedAtoms.add(contrConsequent);
                synchronized(atomList){atomList.remove(contrConsequent);}
                

                entry = new ArrayList<>();
                entry.add(atom+" "+con.getDISymbol()+" ("+contrConsequent+")");
                DiDistribution[i]-=1;
                KnowledgeB.add(entry);
                               
                entry =new ArrayList<>();
                entry.add(conflictAtom+" "+con.getDISymbol()+" "+con.getNegationSymbol()+"("+contrConsequent+")");
                entry.add(conflictAtom+" "+con.getDISymbol()+" "+atom);
                DiDistribution[i+1]-=2; 
                KnowledgeB.add(entry);
           
                atom=conflictAtom;
                
            }else{                           
                    if(i%2==0){
                        if(i+1<DiDistribution.length){
                            entry =new ArrayList<>();
                            do{ synchronized(atomList){
                               // if(anteComplexity ==1 ){
                                    conflictAtom = atomList.get(random.nextInt(atomList.size()));
                               // }
                               //  else{  conflictAtom = getFormula(anteComplexity, atomList, connectiveType);}
                                }
                            }while (usedAtoms.contains(conflictAtom));
                            usedAtoms.add(conflictAtom);
                            synchronized(atomList){
                                atomList.remove(conflictAtom);
                            }
                            entry.add(conflictAtom+" "+con.getDISymbol()+" "+con.getNegationSymbol()+"("+contrConsequent+")");
                            entry.add(conflictAtom+" "+con.getDISymbol()+" "+atom);

                            DiDistribution[i+1]-=2;
                            KnowledgeB.add(entry);

                            atom=conflictAtom; 
                                                       
                        }
                    }else{

                        if(i+1<DiDistribution.length){
                            entry =new ArrayList<>();
                            do{ synchronized(atomList){
                                //if(anteComplexity ==1 ){
                                    conflictAtom = atomList.get(random.nextInt(atomList.size()));
                                //}
                                //else{conflictAtom = getFormula(anteComplexity, atomList, connectiveType);}
                                }
                            }while (usedAtoms.contains(conflictAtom));
                            usedAtoms.add(conflictAtom);
                            synchronized(atomList){
                                atomList.remove(conflictAtom);
                            }
                            entry.add(conflictAtom+" "+con.getDISymbol()+" ("+contrConsequent+")");
                            entry.add(conflictAtom+" "+con.getDISymbol()+" "+atom);

                            DiDistribution[i+1]-=2; 
                            KnowledgeB.add(entry);

                            atom=conflictAtom;

                        }
                    }
            } 
        }
        
      
       Future<?>[] futures = new Future[numThreads];
       int chunkSize = KnowledgeB.size() / numThreads;

       for (int j = 0; j < numThreads; j++) {
            int start = j * chunkSize;
            int end = (j == numThreads - 1) ? KnowledgeB.size() : start + chunkSize;

        //Transitivity 
        
        Boolean transitive = transitivity.get(0).equalsIgnoreCase("y")? true:false;
        
   
        futures[j] = executorService.submit(() -> {
            if(notALL(DiDistribution)){
                
                String currAtom ="";
                String consequent="";
                String antecedent="";
                String ConflictAtom = "";
                String Atom = "";
                String nextAtom = "";
                Count.set(0);
                Count.addAndGet(DiDistribution.length*2-1);
                ArrayList<String> Entry = new ArrayList<>();
                ArrayList<String> reusableConsequents = new ArrayList<>();
                
                    
                for(int i = start; i <end; i++){


                    String[] formula = (KnowledgeB.get(i).get(0)).split(" ");
                    currAtom = formula[0];
                    ConflictAtom = formula[2]; 
                   
                     while(DiDistribution[i]>0){

                        // choose where the rank atom is used
                         Boolean inAntecedent = false;
                         int placement = random.nextInt(2);
                         if(placement == 0){ inAntecedent=true;}

                        if(transitive && i>0 && DiDistribution[i]> 6){
                            String[] form = (KnowledgeB.get(i-1).get(0)).split(" ");
                            String prevAtom = form[0];
                              
                            synchronized(atomList){
                                if(anteComplexity ==1 ){Atom = atomList.get(random.nextInt(atomList.size()));}
                                 else{
                                Atom = getFormula(anteComplexity, atomList, connectiveType);}
                            }
                            if(!usedAtoms.contains(Atom)){usedAtoms.add(Atom);}

                            do{synchronized(atomList){
                                if(anteComplexity ==1 ){nextAtom = atomList.get(random.nextInt(atomList.size()));}
                                 else{
                                nextAtom = getFormula(anteComplexity, atomList, connectiveType);}
                            }
                            }while(nextAtom.equals(Atom));
                            if(!usedAtoms.contains(nextAtom)){usedAtoms.add(nextAtom);}

                            
                               synchronized(atomList){
                                    atomList.remove(Atom);atomList.remove(nextAtom);
                               }
                         

                            Entry = new ArrayList<>();
                            Entry.add(currAtom+" "+con.getDISymbol()+" "+ Atom);
                            Entry.add(Atom+" "+con.getDISymbol() +" "+ prevAtom);
                            Entry.add(Atom+" "+con.getDISymbol() +" "+ ConflictAtom);
                            Entry.add(Atom+" "+con.getDISymbol()+" "+ nextAtom);
                            Entry.add(nextAtom+" "+con.getDISymbol() +" "+ prevAtom);
                            Entry.add(nextAtom+" "+con.getDISymbol() +" "+ ConflictAtom);
                            KnowledgeB.get(i).addAll(Entry);
                            DiDistribution[i] = DiDistribution[i] - 6;

                            synchronized(KBGeneratorThreadedOPT.class){
                                showProgress(Count.addAndGet(6), total,"Defeasible Implications");
                            }

                        }else{  
                             
                            boolean reuse = random.nextBoolean();                    
                            if(inAntecedent){
                                synchronized(atomList){
                                    if(anteComplexity ==1 ){antecedent = currAtom;}
                                    else{ antecedent = getFormula(anteComplexity, atomList, connectiveType,currAtom);}
                                }
                                 if(!usedAtoms.contains(antecedent)){usedAtoms.add(antecedent);}

                                synchronized(atomList){
                                    if(consComplexity ==1 ){
                                        if(reuse && reusableConsequents.size()>0){
                                            consequent = reusableConsequents.get(random.nextInt(reusableConsequents.size()));
                                        }else{
                                            consequent = atomList.get(random.nextInt(atomList.size()));
                                        }
                                    }
                                    else{ 
                                        if(reuse && reusableConsequents.size()>consComplexity){ 
                                           // System.out.println(reusableConsequents);
                                            consequent = getFormula(consComplexity, reusableConsequents, connectiveType);
                                          
                                        }else{consequent = getFormula(consComplexity, atomList, connectiveType);}
                                    }
                                }
                                if(!usedAtoms.contains(consequent)){usedAtoms.add(consequent);}

                            }else{
                                synchronized(atomList){
                                    if(anteComplexity ==1 ){antecedent = atomList.get(random.nextInt(atomList.size()));}
                                    else{antecedent = getFormula(anteComplexity, atomList, connectiveType);}
                                }
                                if(!usedAtoms.contains(antecedent)){usedAtoms.add(antecedent);}
                                
                                synchronized(atomList){
                                    if(consComplexity ==1 ){consequent = currAtom;}
                                    else{
                                        if(reuse &&  reusableConsequents.size()>consComplexity){ 
                                            consequent = getFormula(consComplexity, reusableConsequents, connectiveType,currAtom);
                                        }else{
                                            consequent = getFormula(consComplexity, atomList, connectiveType,currAtom);
                                        }
                                    }
                                }
                                if(!usedAtoms.contains(consequent)){usedAtoms.add(consequent);}
                            }

                            //Remove atoms used in the consequent if not reuse consequet is false.
                            if(!reuseConsequent){ 
                                synchronized(atomList){
                                    atomList.remove(antecedent);atomList.remove(consequent);
                                }
                            }else{
                                atomList.remove(antecedent);
                                atomList.remove(consequent); 
                                if( !reusableConsequents.contains(consequent) && !consequent.equalsIgnoreCase(currAtom) &&!consequent.contains("&")&& !consequent.contains("||")){
                                    reusableConsequents.add(consequent);
                                   // System.out.println(reusableConsequents+"       cons:"+consequent+"         -->"+consequent.contains("&"));
                                }
                            }
                            
                            Entry = new ArrayList<>();
                            String enterThis= antecedent+" "+con.getDISymbol()+" "+consequent;
                            Entry.add(enterThis);

                            if(!KnowledgeB.get(i).contains(enterThis)){
                                KnowledgeB.get(i).addAll(Entry);
                                DiDistribution[i]-=1;
                            }
                            
                            synchronized(KBGeneratorThreadedOPT.class){
                                showProgress(Count.addAndGet(1), total,"Defeasible Implications");
                            }
                         }
                     } 
                 }
               }
            });
        }
       
        
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } 
        }

        // Shutdown executor
         executorService.shutdown();
         int classicalTot = numStatements.get(1);
         ArrayList<String> infinityRank = new ArrayList<>();
         String ante = "";
         String cons = "";
         String connector = "";
         String expr = "";
         inf.set(0);
         int k = 0;
        System.out.println("");

        while( k < classicalTot){
           synchronized(atomList){
             ante = atomList.get(random.nextInt(atomList.size()));
             do{
                cons = atomList.get(random.nextInt(atomList.size()));
             }while(ante.equalsIgnoreCase(cons));
             
           }
           connector = getOperator(infinityConnectiveType);
           expr = ante+connector+cons;
           if(!infinityRank.contains(expr)){
            infinityRank.add(expr);
            k++;
            showProgress(inf.addAndGet(1), classicalTot,"Classical Statements");
           }
        }
        System.out.println("");
        //if(classicalTot>0){
            KnowledgeB.add(infinityRank);
        //}
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
    public static ArrayList<ArrayList<String>> KBGenerateNew(int[] newDistribution, ArrayList<ArrayList<String>>  kbContent, ArrayList<Integer> numForm ,ArrayList<String> kbAtomList, ArrayList<String> kbUsedAtoms,int[] connectives, ArrayList<String> characterSet, ArrayList<String> transitivity, boolean reuseConsequent,String Complexity){
      
        int total = numForm.get(0);

        
        // selecting character set
        makeAtom.setCharacters(characterSet.get(0));

        // preparing connectives
        ArrayList<Integer> connectiveType = new ArrayList<>();
        ArrayList<Integer> infinityConnectiveType = new ArrayList<>();

        for (int i: connectives){
            if(i == 1 || i==2){ 
                if(!connectiveType.contains(i)){connectiveType.add(i);
                if(!infinityConnectiveType.contains(i)){infinityConnectiveType.add(i);}
            }}
            else if(i == 3 || i==4){
                if(!infinityConnectiveType.contains(i)){ 
                    infinityConnectiveType.add(i);
                }
            }
            else if(i == 5){
                if(!connectiveType.contains(1)){connectiveType.add(1);}
                if(!connectiveType.contains(2)){connectiveType.add(2);}
                if(!infinityConnectiveType.contains(1)){ infinityConnectiveType.add(1);}
                if(!infinityConnectiveType.contains(2)){ infinityConnectiveType.add(2);}
                if(!infinityConnectiveType.contains(3)){ infinityConnectiveType.add(3);}
                if(!infinityConnectiveType.contains(4)){ infinityConnectiveType.add(4);}

            }
        }
        
        // Determine Antecedent Complexity
        String[] comp = Complexity.split("|");
        
        int anteComplexity = Integer.parseInt(comp[0]);
        int consComplexity = Integer.parseInt(comp[2]);    
       
        // generating atoms
        int numAtoms = 0;
        if( anteComplexity==1 && consComplexity == 1){
            numAtoms =  (newDistribution.length + 1) + ((numForm.get(0) + numForm.get(1) -(newDistribution.length * 2 - 1))*2);
        }else{
            numAtoms =  ((newDistribution.length + 1) + ((numForm.get(0) + numForm.get(1) -(newDistribution.length * 2 - 1))*2))*anteComplexity*consComplexity;
        }
        
        int begin = kbAtomList.size()+kbUsedAtoms.size();
        makeAtom.newGenerateAtom(begin,numAtoms, kbAtomList);
    

             
        //Knowledge base entry
        ArrayList<String> entry;

        //For Complex and Simple Statements
        String atom = "";
        String conflictAtom ="";
        String contrConsequent = "";

        System.out.println(kbContent);
        
        //getting atom
        String[] lastEntry = (kbContent.get(kbContent.size()-2).get(0)).split(con.getDISymbol());
        //for(String i: lastEntry){
       //     System.out.println(i);
        //}
        atom = lastEntry[0];
       
        //getting contradiction atom
        String[] firstEntry = (kbContent.get(0).get(0)).split(" ");
        contrConsequent = firstEntry[2].substring(1, firstEntry[2].length()-1);

     
        for(int i=0; i< newDistribution.length;i++){
            if(i+1 >= kbContent.size() ){                        
                if((i+1)%2==0){
                        entry =new ArrayList<>();
                        do{ synchronized(kbAtomList){
                            if(anteComplexity ==1 ){conflictAtom = kbAtomList.get(random.nextInt(kbAtomList.size()));}
                                else{  conflictAtom = getFormula(anteComplexity, kbAtomList, connectiveType);}
                            }
                        }while (kbUsedAtoms.contains(conflictAtom));
                        kbUsedAtoms.add(conflictAtom);
                        synchronized(kbAtomList){
                            kbAtomList.remove(conflictAtom);
                        }
                        entry.add(conflictAtom+" "+con.getDISymbol()+" "+con.getNegationSymbol()+"("+contrConsequent+")");
                        entry.add(conflictAtom+" "+con.getDISymbol()+" "+atom);

                        newDistribution[i]-=2;
                        kbContent.add(i,entry);

                        atom=conflictAtom; 
                        //System.out.println(i);
                       
                }else{
                        entry =new ArrayList<>();
                        do{ synchronized(kbAtomList){
                            if(anteComplexity ==1 ){conflictAtom = kbAtomList.get(random.nextInt(kbAtomList.size()));}
                            else{conflictAtom = getFormula(anteComplexity, kbAtomList, connectiveType);}
                            }
                        }while (kbUsedAtoms.contains(conflictAtom));
                        kbUsedAtoms.add(conflictAtom);
                        synchronized(kbAtomList){
                            kbAtomList.remove(conflictAtom);
                        }
                        entry.add(conflictAtom+" "+con.getDISymbol()+" ("+contrConsequent+")");
                        entry.add(conflictAtom+" "+con.getDISymbol()+" "+atom);

                        newDistribution[i]-=2; 
                        kbContent.add(i, entry);

                        atom=conflictAtom;
                       // System.out.println(i);
                }
            }
        }
        
        ArrayList<String> repeats = new ArrayList<>();
        for(int k = 0; k < kbAtomList.size(); k++){
            if(kbUsedAtoms.contains(kbAtomList.get(k))){
                repeats.add(kbAtomList.get(k));
            }     
         }

         for(int k = 0; k < repeats.size(); k++){
            kbAtomList.remove(repeats.get(k));
         }
        
       // System.out.println("*used atoms: "+kbUsedAtoms+"            atomlist: "+kbAtomList);

       Future<?>[] futures = new Future[numThreads];
       int chunkSize = kbContent.size() / numThreads;

       for (int j = 0; j < numThreads; j++) {
            int start = j * chunkSize;
            int end = (j == numThreads - 1) ? kbContent.size()-1 : start + chunkSize;

        //Transitivity 
        
        Boolean transitive = transitivity.get(0).equalsIgnoreCase("y")? true:false;
        
   
        futures[j] = executorService.submit(() -> {
            if(notALL(newDistribution)){
                
                String currAtom ="";
                String consequent="";
                String antecedent="";
                String ConflictAtom = "";
                String Atom = "";
                String nextAtom = "";
                Count.set(0);
                Count.addAndGet(newDistribution.length*2-1);
                ArrayList<String> Entry = new ArrayList<>();
                ArrayList<String> reusableConsequents = new ArrayList<>();

                
                  
                for(int i = start; i <end; i++){

                    String[] formula = (kbContent.get(i).get(0)).split(" ");
                    currAtom = formula[0];
                    ConflictAtom = formula[2]; 
                   
                     while(newDistribution[i]>0){

                        // choose where the rank atom is used
                         Boolean inAntecedent = false;
                         int placement = random.nextInt(2);
                         if(placement == 0){ inAntecedent=true;}

                        if(transitive && i>0 && newDistribution[i]> 6){
                            String[] form = (kbContent.get(i-1).get(0)).split(" ");
                            String prevAtom = form[0];
                              
                            synchronized(kbAtomList){
                                if(anteComplexity ==1 ){Atom = kbAtomList.get(random.nextInt(kbAtomList.size()));}
                                 else{
                                Atom = getFormula(anteComplexity, kbAtomList, connectiveType);}
                            }
                            if(!kbUsedAtoms.contains(Atom)){kbUsedAtoms.add(Atom);}

                            do{
                                synchronized(kbAtomList){
                                    if(anteComplexity ==1 ){nextAtom = kbAtomList.get(random.nextInt(kbAtomList.size()));}
                                    else{nextAtom = getFormula(anteComplexity, kbAtomList, connectiveType);}
                                }
                            }while(nextAtom.equals(Atom));
                            if(!kbUsedAtoms.contains(nextAtom)){kbUsedAtoms.add(nextAtom);}

                            synchronized(kbAtomList){
                                kbAtomList.remove(Atom);kbAtomList.remove(nextAtom);
                            }

                            Entry = new ArrayList<>();
                            Entry.add(currAtom+" "+con.getDISymbol()+" "+ Atom);
                            Entry.add(Atom+" "+con.getDISymbol() +" "+ prevAtom);
                            Entry.add(Atom+" "+con.getDISymbol() +" "+ ConflictAtom);
                            Entry.add(Atom+" "+con.getDISymbol()+" "+ nextAtom);
                            Entry.add(nextAtom+" "+con.getDISymbol() +" "+ prevAtom);
                            Entry.add(nextAtom+" "+con.getDISymbol() +" "+ ConflictAtom);
                            kbContent.get(i).addAll(Entry);
                            newDistribution[i] = newDistribution[i] - 6;

                            synchronized(KBGeneratorThreadedOPT.class){
                                showProgress(Count.addAndGet(6), total,"Defeasible Implications");
                            }

                        }else{  
                             
                            boolean reuse = random.nextBoolean();                    
                            if(inAntecedent){
                                synchronized(kbAtomList){
                                    if( anteComplexity == 1 ){antecedent = currAtom;}
                                    else{ antecedent = getFormula(anteComplexity, kbAtomList, connectiveType,currAtom);}
                                }
                                 if(!kbUsedAtoms.contains(antecedent)){kbUsedAtoms.add(antecedent);}

                                synchronized(kbAtomList){
                                    if(consComplexity ==1 ){
                                        if(reuse && reusableConsequents.size()>0){
                                            consequent = reusableConsequents.get(random.nextInt(reusableConsequents.size()));
                                        }else{
                                            consequent = kbAtomList.get(random.nextInt(kbAtomList.size()));
                                        }
                                    }
                                    else{ 
                                        if(reuse && reusableConsequents.size()>consComplexity){ 

                                            consequent = getFormula(consComplexity, reusableConsequents, connectiveType);
                                          
                                        }else{consequent = getFormula(consComplexity, kbAtomList, connectiveType);}
                                    }
                                }
                                if(!kbUsedAtoms.contains(consequent)){kbUsedAtoms.add(consequent);}

                            }else{
                                synchronized(kbAtomList){
                                    if(anteComplexity ==1 ){antecedent = kbAtomList.get(random.nextInt(kbAtomList.size()));}
                                    else{antecedent = getFormula(anteComplexity, kbAtomList, connectiveType);}
                                }
                                if(!kbUsedAtoms.contains(antecedent)){kbUsedAtoms.add(antecedent);}
                                
                                synchronized(kbAtomList){
                                    if(consComplexity ==1 ){consequent = currAtom;}
                                    else{
                                        if(reuse &&  reusableConsequents.size()>consComplexity){ 
                                            consequent = getFormula(consComplexity, reusableConsequents, connectiveType,currAtom);
                                        }else{
                                            consequent = getFormula(consComplexity, kbAtomList, connectiveType,currAtom);
                                        }
                                    }
                                }
                                if(!kbUsedAtoms.contains(consequent)){kbUsedAtoms.add(consequent);}
                            }

                            //Remove atoms used in the consequent if not reuse consequet is false.
                            if(!reuseConsequent){ 
                                synchronized(kbAtomList){
                                    kbAtomList.remove(antecedent);kbAtomList.remove(consequent);
                                }
                            }else{
                                kbAtomList.remove(antecedent);
                                kbAtomList.remove(consequent); 
                                if( !reusableConsequents.contains(consequent) && !consequent.equalsIgnoreCase(currAtom) &&!consequent.contains("&")&& !consequent.contains("||")){
                                    reusableConsequents.add(consequent);
                                   // System.out.println(reusableConsequents+"       cons:"+consequent+"         -->"+consequent.contains("&"));
                                }
                            }
                            
                            Entry = new ArrayList<>();
                            String enterThis= antecedent+" "+con.getDISymbol()+" "+consequent;
                            Entry.add(enterThis);

                            if(!kbContent.get(i).contains(enterThis)){
                               kbContent.get(i).addAll(Entry);
                                newDistribution[i]-=1;
                            }
                            
                            synchronized(KBGeneratorThreadedOPT.class){
                                showProgress(Count.addAndGet(1), total,"Defeasible Implications");
                            }
                         }
                     } 
                 
                   //  System.out.println("used atoms: "+kbUsedAtoms+"            atomlist: "+kbAtomList);
                 
                    }
               }
            });
        }
       
        
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } 
        }

        // Shutdown executor
         executorService.shutdown();
         int classicalTot = numForm.get(1);
        // ArrayList<String> infinityRank = new ArrayList<>();
         String ante = "";
         String cons = "";
         String connector = "";
         String expr = "";
         inf.set(0);
         int k = 0;
        System.out.println("");

        while( k < classicalTot){
           synchronized(atomList){
             ante = atomList.get(random.nextInt(atomList.size()));
             do{
                cons = atomList.get(random.nextInt(atomList.size()));
             }while(ante.equalsIgnoreCase(cons));
             
           }
           connector = getOperator(infinityConnectiveType);
           expr = ante+connector+cons;
           if(!kbContent.get(kbContent.size()-1).contains(expr)){
            kbContent.get(kbContent.size()-1).add(expr);
            k++;
            showProgress(inf.addAndGet(1), classicalTot,"Classical Statements");
           }
        }
        System.out.println("");
       // KnowledgeB.add(infinityRank);
         
        //adding used and non-used atoms to the knowledge base.
        kbContent.add(atomList);
        kbContent.add(usedAtoms);
        
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
           
            //formula = formula + atomList.get(random.nextInt(atomList.size()));
            int index = 0;
          
           synchronized(atomList){
                index = random.nextInt(atomList.size());
                String value = atomList.get(index);
                formula = formula + value;
           }
            
    
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
       // System.out.println(" A. getformula"+ formula);
    
            return formula;
    }
    
    public static String getOperator(ArrayList<Integer> connectives) { 
        String formula = "";
        switch (connectives.get(random.nextInt(connectives.size()))) {
            case 1:
                formula = con.getDisjunctionSymbol();
                break;
            case 2:
                formula = con.getConjunctionSymbol();
                break;
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

    public static void showProgress(int completed, int total,String type) {
        
        int completedBars = 0;
        double statementsPerBar = 0.0;
        int progress = 0;
        StringBuilder progressBar = new StringBuilder(type+": [");

        if( completed+1==total && total<4000 || completed>total){
           completedBars = 20;
           completed = total;
           progress = (int) (((completed) / (double) total) * 100);
          
        }else{
            statementsPerBar = total / 20.0; // Calculate how many statements each bar represents
            progress = (int) (((completed) / (double) total) * 100);
            completedBars = (int) ((completed)/ statementsPerBar); // Ensure progress is accurate even for small numbers of statements
        }
        for (int i = 0; i < 20; i++) {
            if (i < completedBars) {
                progressBar.append("=");
            } else {
                progressBar.append(" ");
            }
        }
        progressBar.append("] ").append(progress).append("% Complete");
        System.out.print("\r" + progressBar.toString());
    }

    public static synchronized String getFormula( int Complexity, ArrayList<String> atomList, ArrayList<Integer> connectives, String rankAtom) { 
      
        String formula = "";
        int complexity = 1 + random.nextInt(Complexity-1);
       // int rankAtomIndex = 0;
        boolean close = false;

       // if (complexity==1){rankAtomIndex = 0;}
       // else { rankAtomIndex = random.nextInt(complexity); }

        for(int i = 0; i< complexity; i++){
            if(i==0){formula = rankAtom;}
           
            //if(rankAtomIndex==i){formula = formula + rankAtom;
            //}
            else{
                int index = random.nextInt(atomList.size());
                formula = formula + atomList.get(index);
            }
            if(i<complexity-1 ){ 
                if(i==0){
                    formula = formula+con.getConjunctionSymbol()+"(";
                    close =true;
                }else{
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
        if(close){
        formula = formula +")";
        }
        return formula;
    }
      
    private static int countStatements(ArrayList<ArrayList<String>> KB){
        
        int count=0;
            for(ArrayList<String> set : KB){
                for (String element : set){
                    count++;
                }
            }
           
        return count;
    }
    
    
}

