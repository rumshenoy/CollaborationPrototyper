/**
 * Created by ramyashenoy on 11/8/15.
 */
public class Guard {
    String condition;
    String id;

    boolean isConsequence = false;
    boolean isAlternative = false;

    public boolean isConsequence() {
        return isConsequence;
    }

    public void setConsequence(boolean consequence) {
        isConsequence = consequence;
    }

    public boolean isAlternative() {
        return isAlternative;
    }

    public void setAlternative(boolean alternative) {
        isAlternative = alternative;
    }

    public Guard(String id){
        this.id = id;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
