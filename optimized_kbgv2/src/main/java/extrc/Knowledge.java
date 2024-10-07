package extrc;
import java.util.ArrayList;
import java.util.Date;


public class Knowledge {
    private ArrayList<String> usedAtoms;
    private ArrayList<String> content;
    private ArrayList<String> AtomList;
    private Date creationDate;
    private int numberOfRanks;
    private String generatorName;
    private double generationSpeed;
    private int num;
    private String distribution; 
    private int[] connectType;
    private String charSet;
    private String transitivity;
    private boolean reuseConsequent;
    private String ratio;
    private int min;
    private String complexity;

    // Constructors...
   /* public Knowledge( ArrayList<String>  content, 
                     Date creationDate, int numberOfRanks, String generatorName, double generationSpeed,String distribution, int num, String charSet,int[] connectType) {
        this.content = content;  // Defeasible knowledge base
        this.creationDate = creationDate;
        this.numberOfRanks = numberOfRanks;
        this.generatorName = generatorName;
        this.generationSpeed = generationSpeed;
        this.distribution=distribution;
        this.num=num;
        this.connectType=connectType;
        this.charSet=charSet;
       
    }
*/
    public Knowledge(ArrayList<String> content, Date creationDate, int numberOfRanks, String generatorName, double speed,ArrayList<String> usedAtoms, ArrayList<String> AtomList, String distribution, int num, String charSet,int[] connectType, String transitivity,boolean reuseConsequent, String ratio, int min,String complexity) {
        this.content = content;  // Defeasible knowledge base
        this.creationDate = creationDate;
        this.numberOfRanks = numberOfRanks;
        this.generatorName = generatorName;
        this.generationSpeed = speed;
        this.AtomList=AtomList;
        this.usedAtoms=usedAtoms;
        this.distribution=distribution;
        this.num=num;
        this.connectType=connectType;
        this.charSet=charSet;
        this.transitivity=transitivity;
        this.reuseConsequent=reuseConsequent;
        this.ratio=ratio;
        this.min=min;
        this.complexity=complexity;
    }

    // Getters and setters
    public String getComplexity() {
        return complexity;
    }
    public void setComplexity(String complexity) {
        this.complexity=complexity;
    }
    public int getMin() {
        return min;
    }
    public void setMin(int min) {
        this.min=min;
    }
    public String getRatio() {
        return ratio;
    }
    public void setRatio(String ratio) {
        this.ratio=ratio;
    }
    public boolean getReuseConsequent() {
        return reuseConsequent;
    }
    public void setReuseConsequent(boolean reuseConsequent) {
        this.reuseConsequent=reuseConsequent;
    }
    public String getTransitivity() {
        return transitivity;
    }
    public void setTransitivity(String transitivity) {
        this.transitivity=transitivity;
    }
    public String getCharSet() {
        return charSet;
    }
    public void setCharSet(String charSet) {
        this.charSet=charSet;
    }
    public int[] getConnectors() {
        return connectType;
    }
    public void setConnectors(int[] connectType) {
        this.connectType=connectType;
    }
    public int getNum() {
        return num;
    }
    public void setNum(int num) {
        this.num=num;
    }
    public String getDistribution() {
        return distribution;
    }
    public void setDistribution(String distribution) {
        this.distribution=distribution;
    }
    
    public ArrayList<String> getAtomList() {
        return AtomList;
    }

    public void setAtomList(ArrayList<String> AtomList) {
        this.AtomList=AtomList;
    }

    public ArrayList<String> getUsedAtoms() {
        return usedAtoms;
    }

    public void setUsedAtom(ArrayList<String> usedAtoms) {
        this.usedAtoms=usedAtoms;
    }

    public ArrayList<String> getContent() {
        return content;
    }

    public void setContent(ArrayList<String> content) {
        this.content = content;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public int getNumberOfRanks() {
        return numberOfRanks;
    }

    public void setNumberOfRanks(int numberOfRanks) {
        this.numberOfRanks = numberOfRanks;
    }

    public String getGeneratorName() {
        return generatorName;
    }

    public void setGeneratorName(String generatorName) {
        this.generatorName = generatorName;
    }

    public double getGenerationSpeed() {
        return generationSpeed;
    }

    public void setGenerationSpeed(double generationSpeed) {
        this.generationSpeed = generationSpeed;
    }

}
