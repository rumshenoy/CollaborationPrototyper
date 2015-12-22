package com.cbpro.main;

/**
 * Created by ramyashenoy on 12/10/15.
 */
public class Cbpro {
    public static void main(String[] args) throws Exception {
        if(args.length < 1 || args.length > 1 ){
            System.out.println("Usage: com.cbpro.main.Cbpro /path/inputFile");
            return;
        }
        else{
            Program program = new Program();
            program.generate(args[0]);
        }
    }
}
