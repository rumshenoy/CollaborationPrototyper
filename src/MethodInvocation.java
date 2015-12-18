import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramyashenoy on 9/1/15.
 */
public class MethodInvocation extends Instruction {
    String name;
    String assignmentTarget;
    MetaClass source;
    MetaClass target;
    String messageSort;
    String signature;
    String id;
    String caller;
    boolean isInCF = false;
    String cfID = null;
    Operation operation;
    String arguments;
    List<MethodInvocation> childNodes = new ArrayList<>();
    String callerObject;
    String operandId;

    public String getOperandId() {
        return operandId;
    }

    public void setOperandId(String operandId) {
        this.operandId = operandId;
    }

    public String getCallerObject() {
        return callerObject;
    }

    public void setCallerObject(String callerObject) {
        this.callerObject = callerObject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public boolean isInCF() {
        return isInCF;
    }

    public void setInCF(boolean isInCF) {
        this.isInCF = isInCF;
    }

    public String getCfID() {
        return cfID;
    }

    public void setCfID(String cfID) {
        this.cfID = cfID;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssignmentTarget() {
        return assignmentTarget;
    }

    public void setAssignmentTarget(String assignmentTarget) {
        this.assignmentTarget = assignmentTarget;
    }

    public MetaClass getSource() {
        return source;
    }

    public void setSource(MetaClass source) {
        this.source = source;
    }

    public MetaClass getTarget() {
        return target;
    }

    public void setTarget(MetaClass target) {
        this.target = target;
    }

    public String getMessageSort() {
        return messageSort;
    }

    public void setMessageSort(String messageSort) {
        this.messageSort = messageSort;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String toString() {
        String toReturn = "";
        if (assignmentTarget != null) {
            toReturn += assignmentTarget + " = ";
        }
        return toReturn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodInvocation that = (MethodInvocation) o;

        if (assignmentTarget != null ? !assignmentTarget.equals(that.assignmentTarget) : that.assignmentTarget != null)
            return false;
        if (childNodes != null ? !childNodes.equals(that.childNodes) : that.childNodes != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (messageSort != null ? !messageSort.equals(that.messageSort) : that.messageSort != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (signature != null ? !signature.equals(that.signature) : that.signature != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = signature != null ? signature.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    public void printToConsole() {
        if (operation != null) {
            if (assignmentTarget != null) {
                System.out.print("\t\t" + assignmentTarget + " = ");
            } else {
                System.out.print("\t\t");
            }
            System.out.print(callerObject + "." + operation.name + "(");
            if(arguments != null){
                System.out.print(arguments);
            }
            System.out.print(")");
            System.out.println(";");
        }else{
            if(this.messageSort.equals("reply")){
                System.out.println("\t\treturn " + this.name + ";");
            }

            if(this.messageSort.equals("synchCall")){
                if(assignmentTarget != null){
                    System.out.print("\t\t" + assignmentTarget + " = ");
                }
                System.out.println(name + ";");
            }
        }
    }

    public String print() {
        String data = "";
        if (operation != null) {
            if (assignmentTarget != null) {
                data+="\t\t" + assignmentTarget + " = ";
            } else {
                data+="\t\t";
            }
            data+=callerObject + "." + operation.name + "(";
            if(arguments != null){
                data+=arguments;
            }
            data+=");";
        }else{
            if(this.messageSort.equals("reply")){
                if(this.name.equals("return")){
                    data+="\t\treturn" + ";";

                }else{
                    data+="\t\treturn " + this.name + ";";
                }
            }

            if(this.messageSort.equals("synchCall")){
                if(assignmentTarget != null){
                    data+="\t\t" + assignmentTarget + " = ";
                }
                data+=name + ";";
            }
        }
        return data;
    }
}
