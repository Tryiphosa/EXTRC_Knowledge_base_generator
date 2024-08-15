package extrc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The AtomBuilder class provides functions for generating and keeping track of atoms in a knowledge base.
 */
public class AtomCreation {
    private static AtomCreation gen;
    private char startChar;
    private char endChar;
    private Random random;
    private int maxChar;
    private ArrayList<Character> characterList;


    /**
     * Constructs an AtomBuilder with default settings.
     */
    public AtomCreation() {

        random = new Random();
        startChar = '\u0041';
        endChar = '\u005A';
        maxChar = 26;

        characterList = new ArrayList<>();
        for(int i=0; i<maxChar;i++){
            characterList.add((char)(startChar+i));
        }
    }

    /**
     * Gets an instance of AtomBuilder.
     *
     * @return The AtomBuilder instance.
     */
    public static AtomCreation getInstance() {
        if (gen == null) {
            gen = new AtomCreation();
        }
        return gen;
    }

    /**
     * Sets the character set to be used for atom generation.
     *
     * @param characterSet The character set to be used ("upperlatin", "lowerlatin", "greek", or "altlatin").
     */
    public void setCharacters(String characterSet) {
        switch (characterSet) {
            case "upperlatin":
                startChar = '\u0041';
                endChar = '\u005A';
                maxChar = 26;
                characterList.clear();
                for(int i=0; i<maxChar;i++){
                    characterList.add((char)(startChar+i));
                }
                break;
            case "lowerlatin":
                startChar = '\u0061';
                endChar = '\u007A';
                maxChar = 26;
                characterList.clear();
                for(int i=0; i<maxChar;i++){
                    characterList.add((char)(startChar+i));
                }
                break;
            case "greek":
                startChar = '\u03B1';
                endChar = '\u03C9';
                maxChar= 24;
                characterList.clear();
                for(int i=0; i<maxChar;i++){
                    characterList.add((char)(startChar+i));
                }
                break;
            case "altlatin":
                startChar = '\u0250';
                endChar = '\u0267';
                maxChar = 228;
                characterList.clear();
                for(int i=0; i<maxChar;i++){
                    characterList.add((char)(startChar+i));
                }
                break;
        }
    }

     /**
     * The function returns the starting character for the selected character set.
     *
     * @return The starting character for the selected character set, of type char.
     */
    public char getStart(){
        return startChar;
    }

    /**
     * The function returns the ending character for the selected character set.
     *
     * @return The ending character for the selected character set, of type char.
     */
    public char getEnd(){
        return endChar;
    }

    /*
    * Repopulates the character list
    */
    private void repopulate(){
        characterList.clear();
        for(int i=0; i<maxChar;i++){
            characterList.add((char)(startChar+i));
        }
    }
    
    /**
     * Generates a unique set of atoms.
     *
     */
    public void generateAtom(int atomNum, ArrayList<String> atomList) {    
        int length = 1;
        int count=0;
        for(int i = 0; i < atomNum; i++){
            StringBuilder atom = new StringBuilder();
           
            
            if (i >= this.maxChar ){ // At some point the atom length will have to change during generation
              
                if((double)(i+1)/(double)(maxChar)> 1.0){length=2;}
                else if((double)(i+1)/(double)(maxChar*maxChar + maxChar)> 1.0){length=3;}
                else if((double)(i+1)/(double)(maxChar*maxChar*maxChar+maxChar*maxChar + maxChar)> 1.0){length=4;}
                else if((double)(i+1)/(double)(maxChar*maxChar*maxChar*maxChar*maxChar+maxChar*maxChar*maxChar+maxChar*maxChar + maxChar)> 1.0){length=5;}
                else{ //Do nothing...this would be too many atoms
                }
               
                
                do {
                    atom.setLength(0); // clear atom
                    for (int j = 0; j < length; j++) {
                        char randomChar =  characterList.get(random.nextInt(characterList.size())); //(char) (startChar + random.nextInt(endChar - startChar + 1));
                        atom.append(randomChar);
                        characterList.remove((Character)randomChar);

                        if(characterList.size()==0){
                            repopulate();
                       }
                    }
                    try {Thread.sleep(100);} catch (InterruptedException e) { e.printStackTrace();}
                    System.out.println("length"+ length+"   atomlist"+atomList.size()+"  i:"+i+ "   count: "+count+ " *"+atomList.contains(atom.toString())); 
                    count++;

                } while (atomList.contains(atom.toString()));
                synchronized (atomList) {
                    atomList.add(atom.toString());
                }
               
            }else{
             
                do {
                    atom.setLength(0); // clear atom
                    for (int j = 0; j < length; j++) {
                        char randomChar =  characterList.get(random.nextInt(characterList.size()));
                        atom.append(randomChar);
                        characterList.remove((Character)randomChar);
                                             
                    }
                } while (atomList.contains(atom.toString()));
                synchronized (atomList) {
                    atomList.add(atom.toString());
                   if (characterList.size()==0) {
                       repopulate();
                    }
                }
            }  
       }
        System.out.println(atomList);   
    }

     /**
     * Generates a unique set of atoms.
     *
     */
    public void newGenerateAtom(int atomNum, ArrayList<String> atomList) {    
        int count=0, count_1=0, count_2=0, count_3=0, count_4=0;
           
        for(int i = 0; i < atomNum; i++){
            StringBuilder atom = new StringBuilder();
            if (i >= this.maxChar ){ // At some point the atom length will have to change during generation
              
                if((double)(i+1)/(double)(maxChar)> 1.0 && (double)(i+1)/(double)(maxChar*maxChar + maxChar)<1 ){
                    
                    do {
                        atom.setLength(0); // clear atom 
                        char randomChar =  characterList.get(count);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_1);
                        count_1++;
                        atom.append(randomChar);
                        if(count_1 == characterList.size()){ count_1=0;
                        count++;}
                        if(count == characterList.size()){ count = 0; count_1=0;}
            
                    } while (atomList.contains(atom.toString()));
                    synchronized (atomList) {
                        atomList.add(atom.toString());
                    }
                }
                else if((double)(i+1)/(double)(maxChar*maxChar + maxChar)>= 1.0 && (double)(i+1)/(double)(maxChar*maxChar*maxChar+maxChar*maxChar + maxChar)<1.0){
                    do {
                        atom.setLength(0); // clear atom 
                        char randomChar =  characterList.get(count);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_1);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_2);
                        atom.append(randomChar);

                        count_2++;
                        if(count_2 == characterList.size()){ count_2=0;
                            count_1++;}
                        if(count_1 == characterList.size()){ count_1=0;
                        count++;}
                        if(count == characterList.size()){ count = 0; count_1=0;}
            
                    } while (atomList.contains(atom.toString()));
                    synchronized (atomList) {
                        atomList.add(atom.toString());
                    }
                   // System.out.println("end of the line");
               }
                else if((double)(i+1)/(double)(maxChar*maxChar*maxChar+maxChar*maxChar + maxChar)>= 1.0 && (double)(i+1)/(double)(maxChar*maxChar*maxChar*maxChar*maxChar+maxChar*maxChar*maxChar+maxChar*maxChar + maxChar)<1.0){
                    do {
                        atom.setLength(0); // clear atom 
                        char randomChar =  characterList.get(count);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_1);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_2);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_3);
                        atom.append(randomChar);

                        count_3++;
                        if(count_3 == characterList.size()){ count_3=0;
                            count_2++;}
                        if(count_2 == characterList.size()){ count_2=0;
                            count_1++;}
                        if(count_1 == characterList.size()){ count_1=0;
                        count++;}
                        if(count == characterList.size()){ count = 0; count_1=0;}
            
                    } while (atomList.contains(atom.toString()));
                    synchronized (atomList) {
                        atomList.add(atom.toString());
                    }
                }
                else if((double)(i+1)/(double)(maxChar*maxChar*maxChar*maxChar*maxChar+maxChar*maxChar*maxChar+maxChar*maxChar + maxChar)>= 1.0){
                    do {
                        atom.setLength(0); // clear atom 
                        char randomChar =  characterList.get(count);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_1);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_2);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_3);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_4);
                        atom.append(randomChar);

                        count_4++;
                        if(count_4 == characterList.size()){ count_4=0;
                            count_3++;}
                        if(count_3 == characterList.size()){ count_3=0;
                            count_2++;}
                        if(count_2 == characterList.size()){ count_2=0;
                            count_1++;}
                        if(count_1 == characterList.size()){ count_1=0;
                        count++;}
                        if(count == characterList.size()){ count = 0; count_1=0;}
            
                    } while (atomList.contains(atom.toString()));
                    synchronized (atomList) {
                        atomList.add(atom.toString());
                    }    
                }
                else{ //Do nothing...this would be too many atoms
                   // System.out.println("end of the line");
                }
  
            }else{

                do {
                    atom.setLength(0); // clear atom
                    char randomChar =  characterList.get(count);
                    atom.append(randomChar);
                    count++;                          
                    if(count==characterList.size()){count=0;}
                } while (atomList.contains(atom.toString()));
                synchronized (atomList) {
                    atomList.add(atom.toString());
                }
            }  
            
       } 
    }

    /**
     * Generates a unique set of atoms.
     *
     */
    public void newGenerateAtom(int start, int atomNum, ArrayList<String> atomList) {    
        int count=0,count_1=0, count_2=0, count_3=0, count_4=0;
        
        for(int i = start; i < atomNum; i++){
            StringBuilder atom = new StringBuilder();
            if (i >= this.maxChar ){ // At some point the atom length will have to change during generation
              
                if((double)(i+1)/(double)(maxChar)> 1.0 && (double)(i+1)/(double)(maxChar*maxChar + maxChar)<1 ){
                    do {
                        atom.setLength(0); // clear atom 
                        char randomChar =  characterList.get(count);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_1);
                        count_1++;
                        atom.append(randomChar);
                        if(count_1 == characterList.size()){ count_1=0;
                        count++;}
                        if(count == characterList.size()){ count = 0; count_1=0;}
            
                    } while (atomList.contains(atom.toString()));
                    synchronized (atomList) {
                        atomList.add(atom.toString());
                    }
                }
                else if((double)(i+1)/(double)(maxChar*maxChar + maxChar)>= 1.0 && (double)(i+1)/(double)(maxChar*maxChar*maxChar+maxChar*maxChar + maxChar)<1.0){
                    do {
                        atom.setLength(0); // clear atom 
                        char randomChar =  characterList.get(count);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_1);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_2);
                        atom.append(randomChar);

                        count_2++;
                        if(count_2 == characterList.size()){ count_2=0;
                            count_1++;}
                        if(count_1 == characterList.size()){ count_1=0;
                        count++;}
                        if(count == characterList.size()){ count = 0; count_1=0;}
            
                    } while (atomList.contains(atom.toString()));
                    synchronized (atomList) {
                        atomList.add(atom.toString());
                    }
               }
                else if((double)(i+1)/(double)(maxChar*maxChar*maxChar+maxChar*maxChar + maxChar)>= 1.0 && (double)(i+1)/(double)(maxChar*maxChar*maxChar*maxChar*maxChar+maxChar*maxChar*maxChar+maxChar*maxChar + maxChar)<1.0){
                    do {
                        atom.setLength(0); // clear atom 
                        char randomChar =  characterList.get(count);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_1);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_2);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_3);
                        atom.append(randomChar);

                        count_3++;
                        if(count_3 == characterList.size()){ count_3=0;
                            count_2++;}
                        if(count_2 == characterList.size()){ count_2=0;
                            count_1++;}
                        if(count_1 == characterList.size()){ count_1=0;
                        count++;}
                        if(count == characterList.size()){ count = 0; count_1=0;}
            
                    } while (atomList.contains(atom.toString()));
                    synchronized (atomList) {
                        atomList.add(atom.toString());
                    }
                }
                else if((double)(i+1)/(double)(maxChar*maxChar*maxChar*maxChar*maxChar+maxChar*maxChar*maxChar+maxChar*maxChar + maxChar)>= 1.0){
                    do {
                        atom.setLength(0); // clear atom 
                        char randomChar =  characterList.get(count);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_1);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_2);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_3);
                        atom.append(randomChar);
                        randomChar =  characterList.get(count_4);
                        atom.append(randomChar);

                        count_4++;
                        if(count_4 == characterList.size()){ count_4=0;
                            count_3++;}
                        if(count_3 == characterList.size()){ count_3=0;
                            count_2++;}
                        if(count_2 == characterList.size()){ count_2=0;
                            count_1++;}
                        if(count_1 == characterList.size()){ count_1=0;
                        count++;}
                        if(count == characterList.size()){ count = 0; count_1=0;}
            
                    } while (atomList.contains(atom.toString()));
                    synchronized (atomList) {
                        atomList.add(atom.toString());
                    }    
                }
                else{ //Do nothing...this would be too many atoms
                }
  
            }else{

                do {
                    atom.setLength(0); // clear atom
                    char randomChar =  characterList.get(count);
                    atom.append(randomChar);
                    count++;                          
                    if(count==characterList.size()){count=0;}
                } while (atomList.contains(atom.toString()));
                synchronized (atomList) {
                    atomList.add(atom.toString());
                }
            }  
            
       }
    
    }

    /**
     * Generates a unique set of atoms and adds them to an existing set.
     *
     */
    public void generateAtom(int diff, ArrayList<String> atomList,ArrayList<String> usedAtoms, int atomLength,int baseChar){

        int length = atomLength;
        for(int i = 0; i < diff; i++){

            StringBuilder atom = new StringBuilder();
            // At some point the atom length will have to change during generation
            int alreadyCreated = atomList.size() + usedAtoms.size();
            if(alreadyCreated>=(baseChar) && alreadyCreated <= (baseChar + baseChar*baseChar)){
                atomLength = 2;
            }else if(alreadyCreated>(baseChar + baseChar*baseChar) && alreadyCreated <= (baseChar + baseChar*baseChar+ baseChar*baseChar*baseChar)){
                atomLength = 3;
            }else if(alreadyCreated>(baseChar + baseChar*baseChar+ baseChar*baseChar*baseChar) && alreadyCreated<=(baseChar + baseChar*baseChar+ baseChar*baseChar*baseChar + baseChar*baseChar*baseChar*baseChar)){
                atomLength = 4;
            }else{//Do nothing...this would be too many atoms
            }

            do {
                atom.setLength(0); // clear atom
                for (int j = 0; j < length; j++) {
                    char randomChar =  characterList.get(random.nextInt(characterList.size())); 
                    characterList.remove((Character)randomChar);

                    if(characterList.size()==0){ repopulate(); } 
                }
            } while (atomList.contains(atom.toString()) || usedAtoms.contains(atom.toString()));

            synchronized (atomList) {
                atomList.add(atom.toString());
            }
            
           
       }
    
    }
}
