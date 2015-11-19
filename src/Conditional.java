import java.util.ArrayList;

/**
 * Created by ramyashenoy on 10/2/15.
 */
public class Conditional extends Instruction {

    String id;
    Guard cons;
    ArrayList<Instruction> consequence;
    ArrayList<Instruction> alternative;

    public Conditional() {
        this.block = new ArrayList<>();
    }

    public Guard getCons() {
        return cons;
    }

    public void setCons(Guard cons) {
        this.cons = cons;
    }

    public Guard getAlt() {
        return alt;
    }

    public void setAlt(Guard alt) {
        this.alt = alt;
    }

    Guard alt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void printToConsole(){

            System.out.println("\t\t" + "if(" + cons.getCondition() + " ){");
            for(Instruction instruction: this.block){
                MethodInvocation methodInvocation = (MethodInvocation) instruction;
                if(methodInvocation.operandId.equals(cons.id)){
                    methodInvocation.printToConsole();
                }
            }
        System.out.println("\t\t" + "}");
        if(alt != null){
            System.out.println("\t\t" + "else{");
            for(Instruction instruction: this.block){
                MethodInvocation methodInvocation = (MethodInvocation) instruction;
                if(methodInvocation.operandId.equals(alt.id)){
                    methodInvocation.printToConsole();
                }
            }
        }

            System.out.println("\t\t}");

        System.out.println("\t\t}");
    }

    public String print(){
        String data = "";
        data+="\t\t" + "if(" + cons.getCondition() + " ){";
        for(Instruction instruction: this.block){
            MethodInvocation methodInvocation = (MethodInvocation) instruction;
            if(methodInvocation.operandId.equals(cons.id)){
                methodInvocation.print();
            }
        }
        data+="\t\t" + "}";
        data+="\t\t" + "else{";
        for(Instruction instruction: this.block){
            MethodInvocation methodInvocation = (MethodInvocation) instruction;
            if(methodInvocation.operandId.equals(alt.id)){
                methodInvocation.print();
            }
        }
        data+="\t\t}";
        return data;
    }
}
