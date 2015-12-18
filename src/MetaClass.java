import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

/**
 * Created by ramyashenoy on 8/30/15.
 */
public class MetaClass {
    String name;
    String _id;
    List<Operation> operations = new ArrayList<>();
    List<Field> fields = new ArrayList<>();
    List<MetaClass> generalizations;
    List<MetaClass> interfaceRealizations;
    boolean isInterface;

    public MetaClass(String name, String id) {
        this.name = name;
        this._id = id;
        fields = new ArrayList<>();
        generalizations = new ArrayList<>();
        interfaceRealizations = new ArrayList<>();
    }

    public void setInterface(boolean isInterface) {
        this.isInterface = isInterface;
    }

    public void setOperations(List<Element> umlOperations) {
        if (umlOperations.size() > 0) {
            operations = new ArrayList<>();
            for (Element op : umlOperations) {
                Operation operation = new Operation(op.name);
                operation.setParameters(op.parameters);
                operation.setVisibility(op.visibility);
                operation.set_id(op._id);
                this.operations.add(operation);
            }
        }
    }

    public void setAssociations(List<MetaClass> classes, List<Element> umlAssociations) {
        for(Element umlAssociation: umlAssociations){
            //Associations have three cases to be considered
            //end1 is defined and navigable, current class adds it as an attribute
            //end2 is defined and navigable, current class adds it as an attribute
            List<MetaClass> end1AssociationClass = classes.stream().filter(f -> f._id.equals(umlAssociation.end1.reference.$ref)).collect(Collectors.toList());
            List<MetaClass> end2AssociationClass = classes.stream().filter(f -> f._id.equals(umlAssociation.end2.reference.$ref)).collect(Collectors.toList());
            if(umlAssociation.end2.navigable && umlAssociation.end2.navigable == true && end2AssociationClass.size() > 0){
                MetaClass end1Class = end2AssociationClass.get(0);
                Field field = new Field();
                field.setName(umlAssociation.end2.name);
                field.setMultiplicity(umlAssociation.end2.multiplicity);
                field.setVisibility(umlAssociation.visibility);
                field.setType(end1Class.name);
                field.setId(umlAssociation._id);
                this.fields.add(field);
            }
            if(umlAssociation.end1.navigable && umlAssociation.end1.navigable == true && end1AssociationClass.size() > 0){
                MetaClass end2Class = end1AssociationClass.get(0);
                Field field = new Field();
                field.setName(umlAssociation.end1.name);
                field.setMultiplicity(umlAssociation.end1.multiplicity);
                field.setVisibility(umlAssociation.visibility);
                field.setType(end2Class.name);
                field.setId(umlAssociation._id);
                end2Class.fields.add(field);
            }
        }

    }

    public void setGeneralizations(List<MetaClass> classes, List<Element> umlGeneralizations) {
        for(Element umlGeneralization: umlGeneralizations){
            List<MetaClass> targetClass = classes.stream().filter(f -> f._id.equals(umlGeneralization.target.$ref)).collect(Collectors.toList());
            this.generalizations.add(targetClass.get(0));

        }
    }

    public void setFields(ArrayList<Element> umlAttributes) {
        for(Element umlAttribute: umlAttributes){
            Field field = new Field();
            field.setName(umlAttribute.name);
            field.setVisibility(umlAttribute.visibility);
            field.setType(umlAttribute.type.value);
            field.setId(umlAttribute._id);
            this.fields.add(field);
        }
    }

    public void setInterfaceRealization(List<MetaClass> metaClasses, List<Element> umlRealizations) {
        for(Element umlRealization: umlRealizations){
            List<MetaClass> targetClass = metaClasses.stream().filter(f -> f._id.equals(umlRealization.target.$ref)).collect(Collectors.toList());
            this.interfaceRealizations.add(targetClass.get(0));

        }
    }

    public String print() throws IOException{
        String data = "";
        if(this.isInterface){
            data+="interface " +this.name + " ";
        }else{
            data+="class " + this.name + " ";
        }

        if(generalizations.size() > 0){
            MetaClass metaClass = generalizations.get(0); //there cna only be one generalization
            data+="extends" + " " + metaClass.name;
        }
        if(interfaceRealizations.size() > 0){
            data+="implements ";
            for(int i = 0; i < interfaceRealizations.size(); i++){
                data+=interfaceRealizations.get(i).name;
                if(i < interfaceRealizations.size() -1){
                    data+=", ";
                }
            }
        }
        data+="{"+ "\n";
        for(Field field: this.fields){
            data+="\t" + field.getVisibility() + " " + field.getType() + " " + field.getName() + ";" +"\n";
        }

        for(Operation operation: operations){
            if(isInterface){
                data+="\t" + operation.visibility + " ";

                data+=operation.returnType + " ";

                data+=operation.name + "(";
                if(operation.parameters.size() > 0){
                    for(Parameter parameter: operation.parameters){
                        data+=parameter.type + " " + parameter.name;
                    }
                }

                data+=");\n";

            }else{
                data+="\t" + operation.visibility + " ";

                data+=operation.returnType + " ";

                data+=operation.name + "(";
                if(operation.parameters.size() > 0){
                    for(Parameter parameter: operation.parameters){
                        data+=parameter.type + " " + parameter.name;
                    }
                }

                data+="){\n";
                for(Instruction instruction: operation.getBlock()){
                    data +=instruction.print() + "\n";
                }
                data+="\t}\n";
            }
        }
        data+="}";
        return data;

    }

    public void printToConsole() {
        System.out.print("class " + this.name + " ");
        if(generalizations.size() > 0){
            MetaClass metaClass = generalizations.get(0); //there cna only be one generalization
            System.out.print("extends" + " " + metaClass.name);
        }
        if(interfaceRealizations.size() > 0){
            System.out.print("implements ");
            for(int i = 0; i < interfaceRealizations.size(); i++){
                System.out.print(interfaceRealizations.get(i).name);
                if(i < interfaceRealizations.size() -1){
                    System.out.print(", ");
                }
            }
        }
        System.out.println("{");
        for(Field field: this.fields){
            System.out.println("\t" + field.getVisibility() + " " + field.getType() + " " + field.getName() + ";");
        }

        for(Operation operation: operations){
            System.out.print("\t" + operation.visibility + " ");

            System.out.print(operation.returnType + " ");

            System.out.print(operation.name + "(");
            if(operation.parameters.size() > 0){
                for(Parameter parameter: operation.parameters){
                    System.out.print(parameter.type + " " + parameter.name);
                }
            }

            System.out.print("){\n");
            for(Instruction instruction: operation.getBlock()){
                instruction.printToConsole();
            }
            System.out.print("\t}\n");
        }
        System.out.println("}");

    }
}