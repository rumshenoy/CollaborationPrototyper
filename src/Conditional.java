import java.util.ArrayList;

/**
 * Created by ramyashenoy on 10/2/15.
 */
public class Conditional extends Instruction {

    String id;
    Operand consequence;

    public Conditional() {
        this.block = new ArrayList<>();
    }

    public Operand getConsequence() {
        return consequence;
    }

    public void setConsequence(Operand consequence) {
        this.consequence = consequence;
    }

    public Operand getAlternative() {
        return alternative;
    }

    public void setAlternative(Operand alternative) {
        this.alternative = alternative;
    }

    Operand alternative;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void printToConsole(){

            System.out.print("if(" + consequence.getGuard() + " ){");
            for(Instruction instruction: this.block){
                MessageNode messageNode = (MessageNode) instruction;
                if(messageNode.operandId == consequence.id){
                    messageNode.printToConsole();
                }
            }
            System.out.println("}");

        System.out.println("\t\t}");
    }

}
