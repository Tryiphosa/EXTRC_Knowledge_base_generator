package extrc;
/**
 * The DefImplication class represents a defeasible implication and provides functions to manipulate and access its components
 */
public class DefImplication {

    private static Connective con = Connective.getInstance();
    private String antecedent;
    private String consequent;

    /**
     * Constructs a DefImplication object with the given antecedent and consequent.
     *
     * @param ant The antecedent of the defeasible implication.
     * @param cons The consequent of the defeasible implication.
     */
    public DefImplication(String ant, String cons) {
        this.antecedent = ant;
        this.consequent = cons;
    }


    /**
     * Sets the antecedent of a defeasible implication.
     *
     * @param ant An array of objects representing the antecedent.
     */
    public void setAntecedent(Object[] ant) {
        StringBuilder antecedentBuilder = new StringBuilder();
        for (int i = 0; i < ant.length; i++) {
            if (ant[i] != null) {
                antecedentBuilder.append(ant[i].toString());
            }
        }
        antecedent = antecedentBuilder.toString();
    }

    /**
     * Sets the consequent of a defeasible implication.
     *
     * @param con An array of objects representing the consequent.
     */
    public void setConsequent(Object[] con) {
        StringBuilder consequentBuilder = new StringBuilder();
        for (int i = 0; i < con.length; i++) {
            if (con[i] != null) {
                consequentBuilder.append(con[i].toString());
            }
        }
        consequent = consequentBuilder.toString();
    }

    /**
     * Gets the antecedent of the defeasible implication.
     *
     * @return The antecedent string.
     */
    public String getAntecedent() {
        return antecedent;
    }

    /**
     * Gets the consequent of the defeasible implication.
     *
     * @return The consequent string.
     */
    public String getConsequent() {
        return consequent;
    }

    /**
     * Returns a string representation of the defeasible implication.
     *
     * @return The string representation of the defeasible implication.
     */
    @Override
    public String toString() {
        return antecedent + con.getDISymbol() + consequent;
    }
}
