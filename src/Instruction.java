import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramyashenoy on 10/2/15.
 */
public abstract class Instruction {
    String id;
    List<Instruction> block;
    int weight;
    Instruction parent;

    public Instruction getParent() {
        return parent;
    }

    public void setParent(Instruction parent) {
        this.parent = parent;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public List<Instruction> getBlock() {
        return block;
    }

    public void setBlock(List<Instruction> block) {
        this.block = block;
    }

    public String getId() {
        return id;

    }

    public void setId(String id) {
        this.id = id;
    }


    public void printToConsole(){

    }
    public String print(){
        return ""   ;
    }
}
