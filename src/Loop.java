import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramyashenoy on 10/8/15.
 */
public class Loop  extends Instruction{
    Operand operand;

    public Loop() {
        this.block = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Operand getOperand() {
        return operand;
    }

    public void setOperand(Operand guard) {
        this.operand = guard;
    }

    String id;

    public List<Instruction> getBlock() {
        return block;
    }

    public void setBlock(ArrayList<Instruction> block) {
        this.block = block;
    }

    public void printToConsole(){

        System.out.println("\t\twhile("+ this.operand.getGuard() + "){");
        if(block != null){
            for(Instruction instruction: block){
                instruction.printToConsole();
            }

        }

        System.out.println("\t\t}");
    }
}
