import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramyashenoy on 10/8/15.
 */
public class Loop  extends Instruction{
    String guard;

    public Loop() {
        this.block = new ArrayList<>();
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

    String id;

    public List<Instruction> getBlock() {
        return block;
    }

    public void setBlock(ArrayList<Instruction> block) {
        this.block = block;
    }

    public String print(){

        System.out.println("\t\twhile("+ guard + "){");
        if(block != null){
            for(Instruction instruction: block){
                instruction.print();
            }

        }

        System.out.println("\t\t}");
        return null;
    }
}
