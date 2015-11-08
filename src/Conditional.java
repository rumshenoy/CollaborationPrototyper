import java.util.ArrayList;

/**
 * Created by ramyashenoy on 10/2/15.
 */
public class Conditional extends Instruction {
    ArrayList<Instruction> consequence;
    ArrayList<Instruction> alternative;
    String id;
    String guard;

    public Conditional() {
        this.consequence = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGuard() {
        return guard;
    }

    public void setGuard(String guard) {
        this.guard = guard;
    }

    public ArrayList<Instruction> getConsequence() {
        return consequence;
    }

    public void setConsequence(ArrayList<Instruction> consequence) {
        this.consequence = consequence;
    }

    public ArrayList<Instruction> getAlternative() {
        return alternative;
    }

    public void setAlternative(ArrayList<Instruction> alternative) {
        this.alternative = alternative;
    }
}
