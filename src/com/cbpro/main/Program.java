package com.cbpro.main;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;


//handle inherited methods from interface or from class
//check if its an interface then we don specify the function behaviour either
public class Program {
    public static Map<MethodInvocation, String> operationMap = new HashMap<>();

    public void generate(String inputFileName) throws Exception {
        List<MetaClass> metaClasses = new ArrayList<>();
        List<LifeLine> lifeLines = new ArrayList<>();
        List<MethodInvocation> rootMessages = new ArrayList<>();
        MethodInvocation parentMessage = new MethodInvocation();
        GsonBuilder builder = new GsonBuilder();
        List<MethodInvocation> methodInvocations = new ArrayList<>();
        Package mainPackage = new Package();
        List<Guard> listOfGuards = new ArrayList<>();
        Map<Guard, Instruction> guardToCFMap = new HashMap<>();
        List<Instruction> combinedFragments = new ArrayList<Instruction>();
        List<Operation> operationsList = new ArrayList<>();


        builder.registerTypeAdapter(RefObject.class, new RefObjectJsonDeSerializer());
        Gson gson = builder.create();

        Element myTypes = gson.fromJson(new FileReader(inputFileName), Element.class);
        if (myTypes._type.equals("Project")) {
            List<Element> umlElements = myTypes.ownedElements.stream().filter(f -> f._type.equals("UMLModel")).collect(Collectors.toList());
            if (umlElements.size() > 0) { //There has be to atleast one UMLModel package
                Element element = umlElements.get(0);
                //package that the classes are supposed to be in
                mainPackage.setName(element.name);
                List<Element> umlPackages = element.ownedElements.stream().filter(g -> g._type.equals("UMLPackage")).collect(Collectors.toList());
                if (umlPackages.size() > 1) {//There has to be two packages- one for class one for behaviour
                    Element classes = umlPackages.get(0);
                    Element behaviour = umlPackages.get(1);
                    //*--------------------------CLASSES-------------------------------*//
                    //in the first pass, get all classes that are defined in the diagram
                    //get details that can be directly inferred from the json like, fields and operations, which do not refer to other classes
                    for (Element umlClass : classes.getOwnedElements()) {
                        MetaClass metaClass = new MetaClass(umlClass.name, umlClass._id);

                        //check if class is interface or not because there is no distinction in json
                        if (umlClass._type.equals("UMLClass")) {
                            metaClass.setInterface(false);
                        } else {
                            metaClass.setInterface(true);
                        }
                        if (umlClass.operations != null) {
                            metaClass.setOperations(umlClass.operations);
                            operationsList.addAll(metaClass.operations);
                        }
                        if (umlClass.attributes != null) {
                            metaClass.setFields(umlClass.attributes);
                        }
                        metaClasses.add(metaClass);
                    }

                    //in second pass, define associations and generalizations for these classes
                    for (Element umlClass : classes.getOwnedElements()) {
                        if (umlClass.ownedElements != null) {
                            //find corresponding metaclass, then populate the secondary inferences
                            List<MetaClass> correspondingMetaClassList = metaClasses.stream().filter(f -> f._id.equals(umlClass._id)).collect(Collectors.toList());
                            MetaClass correspondingMetaClass = correspondingMetaClassList.get(0);
                            List<Element> umlAssociations = umlClass.ownedElements.stream().filter(f -> f._type.equals("UMLAssociation")).collect(Collectors.toList());

                            if (umlAssociations.size() > 0) {
                                correspondingMetaClass.setAssociations(metaClasses, umlAssociations);
                            }
                            List<Element> umlGeneralization = umlClass.ownedElements.stream().filter(f -> f._type.equals("UMLGeneralization")).collect(Collectors.toList());
                            if (umlGeneralization.size() > 0) {
                                correspondingMetaClass.setGeneralizations(metaClasses, umlGeneralization);
                            }
                            List<Element> umlRealization = umlClass.ownedElements.stream().filter(f -> f._type.equals("UMLInterfaceRealization")).collect(Collectors.toList());
                            if (umlRealization.size() > 0) {
                                correspondingMetaClass.setInterfaceRealization(metaClasses, umlRealization);
                            }
                        }
                    }


                    //*--------------------------CLASSES-------------------------------*//

                    //*-----------------------  BEHAVIOUR---------------------------------*//
                    for (Element umlCollaboration : behaviour.getOwnedElements()) {
                        //Role to Class mapping
                        ArrayList<Element> attributes = umlCollaboration.attributes;
                        HashMap<String, MetaClass> roleToClassMap = new HashMap<>();
                        if (attributes != null) {
                            for (Element attribute : attributes) {
                                List<MetaClass> roleClass = metaClasses.stream().filter(f -> f._id.equals(attribute.type.$ref)).collect(Collectors.toList());
                                roleToClassMap.put(attribute._id, roleClass.get(0));
                            }
                        }

                        for (Element umlInteraction : umlCollaboration.ownedElements) {

                            //mapping lifelines to the classes they correspond
                            ArrayList<Element> participants = umlInteraction.participants;
                            if (participants != null && participants.size() > 0) {
                                for (Element participant : participants) {
                                    MetaClass participantClass = roleToClassMap.get(participant.represent.$ref);
                                    LifeLine lifeLine = new LifeLine();
                                    lifeLine.setName(participant.name);
                                    lifeLine.setId(participant._id);
                                    lifeLine.setMetaClass(participantClass);
                                    lifeLines.add(lifeLine);
                                }
                            }
                            //first parse all the combined fragments and get ready
                            if (umlInteraction.fragments != null) {
                                for (Element fragment : umlInteraction.fragments) {                                    //depending on the fragment set the class
                                    Instruction instruction = null;
                                    if (fragment.interactionOperator.equals("loop")) {
                                        Loop loop = new Loop();
                                        loop.setId(fragment._id);
                                        loop.setWeight(0);
                                        Guard guard = new Guard(fragment.operands.get(0)._id);
                                        //loop can have only one condition--- one condition-- condition is made up of AND or OR's
                                        guard.setCondition(fragment.operands.get(0).guard);
                                        loop.setGuard(guard);
                                        instruction = loop;
                                        combinedFragments.add(loop);
                                        listOfGuards.add(guard);
                                        guardToCFMap.put(guard, loop);


                                    }

                                    if (fragment.interactionOperator.equals("alt")) {
                                        Conditional c = new Conditional();
                                        c.setId(fragment._id);
                                        c.setWeight(0);
                                        instruction = c;
                                        combinedFragments.add(c);

                                        Guard consequence = new Guard(fragment.operands.get(0)._id);
                                        consequence.setCondition(fragment.operands.get(0).guard);
                                        c.setCons(consequence);
                                        listOfGuards.add(consequence);
                                        guardToCFMap.put(consequence, c);
                                        consequence.setConsequence(true);

                                        if (fragment.operands.size() > 1) {
                                            Guard alternate = new Guard(fragment.operands.get(1)._id);
                                            alternate.setCondition(fragment.operands.get(1).guard);
                                            c.setAlt(alternate);
                                            listOfGuards.add(alternate);
                                            guardToCFMap.put(alternate, c);
                                            alternate.setAlternative(true);

                                        }
                                    }

                                    if (fragment.tags != null) {
                                        for (Element tag : fragment.tags) {
                                            if (tag.name.equals("parent")) {
                                                List<Instruction> instructionList = combinedFragments.stream().filter(e -> e.getId().equals(tag.reference.$ref)).collect(Collectors.toList());
                                                if (instructionList.size() > 0) {
                                                    instructionList.get(0).getBlock().add(instruction);
                                                    instruction.setParent(instructionList.get(0));
                                                }

                                            }
                                        }
                                    }
                                }
                            }


                            //parsing the messages and make nodes out them to later build a tree from the lifelines
                            ArrayList<Element> messages = umlInteraction.messages;
                            Element startMessage = messages.get(0);
                            String sourceRef = startMessage.source.$ref;
                            String targetRef = startMessage.target.$ref;
                            Element endMessage = null;

                            LifeLine sourceLifeLine = getLifeLine(lifeLines, sourceRef);
                            LifeLine targetLifeLine = getLifeLine(lifeLines, targetRef);

                            //First message processing
                            parentMessage = new MethodInvocation();
                            parentMessage.setAssignmentTarget(startMessage.assignmentTarget);
                            parentMessage.setMessageSort(startMessage.messageSort);
                            parentMessage.setSource(sourceLifeLine.getMetaClass());
                            parentMessage.setTarget(targetLifeLine.getMetaClass());
                            parentMessage.setName(startMessage.name);
                            parentMessage.setId(startMessage._id);
                            if(sourceLifeLine.getId().equals(targetLifeLine.getId())){
                                parentMessage.setCallerObject("this");
                            }else {
                                parentMessage.setCallerObject(targetLifeLine.getName());
                            }
                            int weight = 0;
                            parentMessage.setWeight(weight++);
                            if (startMessage.signature != null) {
                                parentMessage.setSignature(startMessage.signature.$ref);
                            }

                            if (startMessage.tags != null) {
                                for (Element tag : startMessage.tags) {
//                                    if (tag.name.equals("CF")) {
//                                        parentMessage.setInCF(true);
//                                        parentMessage.setCfID(tag.reference.$ref);
//                                    }
                                    if (tag.name.equals("operand")) {
                                        parentMessage.setOperandId(tag.reference.$ref);
                                    }
                                }
                            }


                            MethodInvocation rootMessage = parentMessage;
                            methodInvocations.add(rootMessage);
                            rootMessages.add(rootMessage);
                            Iterator<Element> iter = messages.iterator();
                            while (iter.hasNext()) {
                                if (iter.next() == endMessage) {
                                    continue;
                                }


                                iter.remove();
                                List<Element> childMessages = getChildMessages(messages, targetRef);
                                for (Element child : childMessages) {

                                    LifeLine childSource = getLifeLine(lifeLines, child.source.$ref);
                                    LifeLine childTarget = getLifeLine(lifeLines, child.target.$ref);

                                    MethodInvocation childMessage = new MethodInvocation();
                                    childMessage.setMessageSort(child.messageSort);
                                    childMessage.setSource(childSource.getMetaClass());
                                    childMessage.setTarget(childTarget.getMetaClass());
                                    childMessage.setAssignmentTarget(child.assignmentTarget);
                                    childMessage.setName(child.name);
                                    childMessage.setId(child._id);
                                    childMessage.setWeight(weight++);
                                    childMessage.setArguments(child.arguments);

                                    if(childSource.getId().equals(childTarget.getId())){
                                        childMessage.setCallerObject("this");
                                    }else {
                                        childMessage.setCallerObject(childTarget.getName());
                                    }


                                    if (child.signature != null) {
                                        childMessage.setSignature(child.signature.$ref);
                                    }

                                    if (child.tags != null) {
                                        for (Element tag : child.tags) {
//                                            if (tag.name.equals("CF")) {
//                                                childMessage.setInCF(true);
//                                                childMessage.setCfID(tag.reference.$ref);
//                                            }
                                            if (tag.name.equals("operand")) {
                                                childMessage.setOperandId(tag.reference.$ref);
                                            }
                                        }
                                    }

                                    parentMessage.childNodes.add(childMessage);
                                    methodInvocations.add(childMessage);
                                }

                                if (childMessages.size() > 0) {
                                    List<MethodInvocation> nextMessage = parentMessage.childNodes.stream().filter(f -> !f.source.equals(f.target)).collect(Collectors.toList());
                                    List<Element> startMessageNext = childMessages.stream().filter(f -> !f.source.$ref.equals(f.target.$ref)).collect(Collectors.toList());
                                    startMessage = startMessageNext.get(0);
                                    targetRef = startMessage.target.$ref;
                                    sourceRef = startMessage.source.$ref;

                                    parentMessage = nextMessage.get(0);

                                    if (childMessages.size() > 1) {
                                        endMessage = childMessages.get(childMessages.size() - 1);
                                    }
                                }

                            }
                        }

                        for (MethodInvocation methodInvocation : methodInvocations) {
                            List<Operation> matchingOperation = operationsList.stream().filter(f -> f._id.equals(methodInvocation.getSignature())).collect(Collectors.toList());
                            if (matchingOperation.size() > 0) {
                                operationMap.put(methodInvocation, matchingOperation.get(0)._id);
                                methodInvocation.setOperation(matchingOperation.get(0));
                            }
                        }

                        Stack stack = new Stack();
                        for(MethodInvocation root: methodInvocations){
                            stack.push(root);
                            while (!stack.empty()) {
                                MethodInvocation methodInvocation = (MethodInvocation) stack.pop();
                                Operation currentOperation = methodInvocation.getOperation();

                                if (currentOperation !=  null) {
                                    //all child nodes of this node make up its body
                                    List<MethodInvocation> childNodes = methodInvocation.childNodes;
                                    for (MethodInvocation child : childNodes) {
                                        stack.push(child);
                                    }
                                    for (MethodInvocation childNode : childNodes) {
                                        if (childNode.getOperandId() != null) {
                                            List<Instruction> combinedFragmentsList = combinedFragments.stream().filter(f -> f.getId().equals(childNode.getCfID())).collect(Collectors.toList());

                                            List<Guard> guardList = listOfGuards.stream().filter(f -> f.id.equals(childNode.getOperandId())).collect(Collectors.toList());

                                            if (guardList.size() > 0) {
                                                Guard currentGuard = guardList.get(0);
                                                Instruction instruction = guardToCFMap.get(guardList.get(0));
                                                //get the topmost CF if it is in a tree
                                                Instruction parent = instruction.getParent();

                                                while(instruction.getParent() != null){
                                                    instruction = instruction.getParent();
                                                }

                                                if(currentGuard.isConsequence){
                                                    Conditional conditional = (Conditional)instruction;
                                                    if(!conditional.getConsequence().contains(childNode)){
                                                        conditional.getConsequence().add(childNode);
                                                    }
                                                }
                                                if(currentGuard.isAlternative){
                                                    Conditional conditional = (Conditional)instruction;
                                                    if(!conditional.getAlternative().contains(childNode)){
                                                        conditional.getAlternative().add(childNode);
                                                    }
                                                }
                                                if(!currentGuard.isAlternative && !currentGuard.isConsequence){
                                                    Loop loop = (Loop) instruction;
                                                    loop.getBlock().add(childNode);
                                                }
                                                else{
                                                    if (!currentOperation.getBlock().contains(instruction)) {
                                                        currentOperation.getBlock().add(instruction);
                                                    }
                                                }

                                            }
                                        } else {
                                            if (!currentOperation.getBlock().contains(childNode)) {
                                                currentOperation.getBlock().add(childNode);
                                            }
                                        }
                                    }
                                }
                            }

                        }

                    }
                }
            }
        }
//
////        printAllData(metaClasses);
//        while (parentMessage.childNodes != null || parentMessage.childNodes.size() > 0) {
//            System.out.println("parent " + parentMessage.name);
//            for (com.cbpro.main.MethodInvocation child : parentMessage.childNodes) {
//                System.out.println("child " + child.name);
//            }
//            if (parentMessage.childNodes.size() > 0) {
//                parentMessage = parentMessage.childNodes.get(0);
//            } else {
//                break;
//            }
//        }

        mainPackage.print();
        File dir = new File("/home/ramyashenoy/Desktop/DemoFolder/" + mainPackage.getName());

        boolean successful = dir.mkdir();
        if (successful)
        {
            System.out.println("directory was created successfully");
            for (MetaClass metaClass : metaClasses) {
                if (metaClass.name.equals("Main")) {
                    continue;
                } else {
                    String data = metaClass.print();
                    BufferedWriter out = null;
                    try
                    {
                        FileWriter fstream = new FileWriter(dir.getPath() + "/" + metaClass.name + ".java", true); //true tells to append data.
                        out = new BufferedWriter(fstream);
                        out.write(data);
                    }
                    catch (IOException e)
                    {
                        System.err.println("Error: " + e.getMessage());
                    }
                    finally
                    {
                        if(out != null) {
                            out.close();
                        }
                    }
                }
            }
        }
        else
        {
            // creating the directory failed
            System.out.println("failed trying to create the directory");
        }

        mainPackage.setClasses(metaClasses);
    }

    private static LifeLine getLifeLine(List<LifeLine> lifeLines, String $ref) {
        List<LifeLine> list = lifeLines.stream().filter(f -> f.getId().equals($ref)).collect(Collectors.toList());
        return list.get(0);
    }


    private static List<Element> getChildMessages(List<Element> messages, String $ref) {
        List<Element> childMessages = messages.stream().filter(f -> (f.source.$ref.equals($ref) && !f.name.contains("result")) || (f.target.$ref.equals($ref) && f.name.contains("result"))).collect(Collectors.toList());
        return childMessages;
    }

    private static List<Element> getNextParent(List<Element> messages, String $ref) {
        List<Element> nextMessage = messages.stream().filter(f -> (f.source.$ref.equals($ref) && !f.name.contains("result")) || (f.target.$ref.equals($ref) && f.name.contains("result")) && !(f.source.equals(f.target))).collect(Collectors.toList());
        return nextMessage;
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}

class Element {
    String _id;
    String _type;
    String name;
    RefObject _parent;
    ArrayList<Element> ownedElements;
    String visibility;
    boolean navigable;
    String aggregation;
    String isReadOnly;
    String isOrdered;
    String isUnique;
    String isID;
    String isDerived;
    ArrayList<Element> parameters;
    String isStatic;
    String direction;
    String isLeaf;
    ArrayList<Element> participants;
    String value;
    String isReentrant;
    ArrayList<Element> messages;
    ArrayList<Element> tags;
    RefObject source;
    RefObject target;
    RefObject reference;
    ArrayList<Element> operations;
    ArrayList<Element> attributes;
    Element end1;
    Element end2;
    String multiplicity;
    RefObject type;
    RefObject represent;
    RefObject signature;
    String assignmentTarget;
    String messageSort;
    RefObject connector;
    ArrayList<Element> fragments;
    String interactionOperator;
    String arguments;
    String guard;
    ArrayList<Element> operands;

    public String get_type() {
        return _type;
    }

    public Iterable<Element> getOwnedElements() {
        return new Iterable<Element>() {
            public Iterator<Element> iterator() {
                return ownedElements.iterator();
            }
        };
    }
}

class RefObjectJsonDeSerializer implements JsonDeserializer<RefObject> {
    @Override
    public RefObject deserialize(JsonElement jsonElement, Type type,
                                 JsonDeserializationContext context) throws JsonParseException {
        if (jsonElement.isJsonPrimitive()) {
            RefObject refObject = new RefObject();
            refObject.setValue(jsonElement.getAsString());
            return refObject;
        }

        return context.deserialize(jsonElement, JsonRefObject.class);
    }
}


class RefObject {
    String $ref;
    String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

class JsonRefObject extends RefObject {

}



