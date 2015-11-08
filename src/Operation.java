import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramyashenoy on 8/30/15.
 */
public class Operation {
    String name;
    String visibility;
    boolean isStatic;
    List<Parameter> parameters;
    String _id;
    String returnType;
    ArrayList<Instruction> block;

    public Operation() {
        this.block = new ArrayList<>();
    }

    public ArrayList<Instruction> getBlock() {
        return block;
    }

    public void setBlock(ArrayList<Instruction> block) {
        this.block = block;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Operation(String name) {
        this.name = name;
        block = new ArrayList<Instruction>();
    }

    public void setParameters(List<Element> umlParameters) {
        parameters = new ArrayList<>();
        if(umlParameters.size() > 0 && umlParameters != null){
            for(Element umlParameter: umlParameters){
                if(umlParameter.direction.equals("return")){
                    returnType = umlParameter.type.value;
                }else{
                    Parameter parameter = new Parameter(umlParameter.name, umlParameter.type.value, umlParameter.direction, umlParameter.visibility);
                    parameters.add(parameter);
                }
            }
        }

    }

    public String toString(){
        String toReturn = "\t" + this.visibility + " "  + this.returnType + " " +  this.name + "(";
        for(Parameter p: parameters){
            toReturn += p.type + " " + p.name;
        }
        toReturn += ")";

        return toReturn;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String print(){

        String result = this.name + "(";
        for(Parameter p: parameters){
            result += p.type + " " + p.name;
        }
        result+=")";
        return result;
    }
}
