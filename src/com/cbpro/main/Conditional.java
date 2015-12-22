package com.cbpro.main;

import java.util.ArrayList;

/**
 * Created by ramyashenoy on 10/2/15.
 */
public class Conditional extends Instruction {

    String id;
    Guard cons;

    public Conditional() {
        this.consequence = new ArrayList<Instruction>();
        this.alternative = new ArrayList<Instruction>();
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

    ArrayList<Instruction> consequence;
    ArrayList<Instruction> alternative;

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

            System.out.println("\t\t" + "if(" + cons.getCondition() + "){");
            for(Instruction instruction: this.consequence){
                MethodInvocation methodInvocation = (MethodInvocation) instruction;
                    methodInvocation.printToConsole();
            }
        System.out.println("\t\t" + "}");
        if(alt != null){
            System.out.println("\t\t" + "else{");
            for(Instruction instruction: this.alternative){
                MethodInvocation methodInvocation = (MethodInvocation) instruction;
                    methodInvocation.printToConsole();
            }
            System.out.println("\t\t}");
        }

    }

    public String print(){
        String data = "";
        data+="\t\t" + "if(" + cons.getCondition() + "){" + "\n";
        for(Instruction instruction: this.consequence){
            MethodInvocation methodInvocation = (MethodInvocation) instruction;
            data +=methodInvocation.print() +"\n";
        }
        data+="\t\t" + "}";
        if(alt != null){
            data+="\n";
            data+="\t\t" + "else{\n";
            for(Instruction instruction: this.alternative){
                MethodInvocation methodInvocation = (MethodInvocation) instruction;
                data+=methodInvocation.print()+"\n";
            }
            data+="\t\t}";
        }
        return data;
    }
}
