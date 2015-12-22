package com.cbpro.main;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramyashenoy on 10/8/15.
 */
public class Loop  extends Instruction {
    Guard guard;

    public Loop() {
        this.block = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Guard getGuard() {
        return guard;
    }

    public void setGuard(Guard guard) {
        this.guard = guard;
    }

    String id;

    public List<Instruction> getBlock() {
        return block;
    }

    public void setBlock(ArrayList<Instruction> block) {
        this.block = block;
    }

    public void printToConsole(){

        System.out.println("\t\twhile("+ this.guard.getCondition() + "){");
        if(block != null){
            for(Instruction instruction: block){
                instruction.printToConsole();
            }

        }

        System.out.println("\t\t}");
    }

    public String print(){
        String data = "";
        data+="\t\twhile("+ this.guard.getCondition() + "){";
        if(block != null){
            for(Instruction instruction: block){
                instruction.print();
            }

        }

        data+="\t\t}";
        return data;
    }
}
