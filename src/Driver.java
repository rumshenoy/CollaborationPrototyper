import com.google.gson.*;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.stream.Collectors;


//handle inherited methods from interface or from class
//check if its an interface then we don specify the function behaviour either
public class Driver {
    public static Map<MessageNode, String> operationMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        List<MetaClass> metaClasses = new ArrayList<>();
        List<LifeLine> lifeLines = new ArrayList<>();

        MessageNode parentMessage = new MessageNode();
        MessageNode rootMessage = new MessageNode();
        GsonBuilder builder = new GsonBuilder();
        List<MessageNode> messageNodes = new ArrayList<>();

        List<Instruction> combinedFragments = new ArrayList<Instruction>();
        List<Operation> operationsList = new ArrayList<>();

        builder.registerTypeAdapter(RefObject.class, new RefObjectJsonDeSerializer());
        Gson gson = builder.create();

        Element myTypes = gson.fromJson(new FileReader("src/basic.json"), Element.class);
        if (myTypes._type.equals("Project")) {
            List<Element> umlElements = myTypes.ownedElements.stream().filter(f -> f._type.equals("UMLModel")).collect(Collectors.toList());
            if (umlElements.size() > 0) { //There has be to atleast one UMLModel package
                Element element = umlElements.get(0);
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

                    //*-----------------------BEHAVIOUR---------------------------------*//
                    for (Element umlCollaboration : behaviour.getOwnedElements()) {
                        //Role to Class mapping
                        ArrayList<Element> attributes = umlCollaboration.attributes;
                        HashMap<String, MetaClass> roleToClassMap = new HashMap<>();
                        if(attributes != null){
                            for (Element attribute : attributes) {
                                List<MetaClass> roleClass = metaClasses.stream().filter(f -> f._id.equals(attribute.type.$ref)).collect(Collectors.toList());
                                roleToClassMap.put(attribute._id, roleClass.get(0));
                            }
                        }

                        for (Element umlInteraction : umlCollaboration.ownedElements) {

                            //mapping lifelines to the classes they correspond
                            ArrayList<Element> participants = umlInteraction.participants;
                            for (Element participant : participants) {
                                MetaClass participantClass = roleToClassMap.get(participant.represent.$ref);
                                LifeLine lifeLine = new LifeLine();
                                lifeLine.setName(participant.name);
                                lifeLine.setId(participant._id);
                                lifeLine.setMetaClass(participantClass);
                                lifeLines.add(lifeLine);
                            }

                            //first parse all the combined fragments and get ready
                            if(umlInteraction.fragments != null){
                                for(Element fragment: umlInteraction.fragments){                                    //depending on the fragment set the class
                                    Instruction instruction = null;
                                    if(fragment.interactionOperator.equals("loop")){
                                        Loop loop = new Loop();
                                        loop.setId(fragment._id);
                                        loop.setWeight(0);
                                        Operand opewith basirand = new Operand(fragment.operands.get(0)._id);
                                        //loop can have only one operand--- one condition-- guard is made up of AND or OR's
                                        operand.setGuard(fragment.operands.get(0).guard);
                                        loop.setOperand(operand);
                                        instruction = loop;
                                        combinedFragments.add(loop);
                                    }

                                    if(fragment.interactionOperator.equals("alt")){
                                        Conditional c = new Conditional();
                                        c.setId(fragment._id);
                                        c.setWeight(0);
                                        instruction = c;
                                        combinedFragments.add(c);

                                        Operand consequence = new Operand(fragment.operands.get(0)._id);
                                        consequence.setGuard(fragment.operands.get(0).guard);
                                        c.setConsequence(consequence);
                                        if(fragment.operands.size() > 0){
                                            Operand alternate = new Operand(fragment.operands.get(1)._id);
                                            consequence.setGuard(fragment.operands.get(1).guard);
                                            c.setAlternative(alternate);
                                        }
                                    }

                                    if(fragment.tags != null){
                                        for(Element tag: fragment.tags){
                                            if(tag.name.equals("parent")){
                                                List<Instruction> instructionList = combinedFragments.stream().filter(e -> e.getId().equals(tag.reference.$ref)).collect(Collectors.toList());
                                                if(instructionList.size() > 0){
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
                            parentMessage = new MessageNode();
                            parentMessage.setAssignmentTarget(startMessage.assignmentTarget);
                            parentMessage.setMessageSort(startMessage.messageSort);
                            parentMessage.setSource(sourceLifeLine.getMetaClass());
                            parentMessage.setTarget(targetLifeLine.getMetaClass());
                            parentMessage.setName(startMessage.name);
                            parentMessage.setId(startMessage._id);
                            parentMessage.setCallerObject(targetLifeLine.getName());
                            int weight = 0;
                            parentMessage.setWeight(weight++);
                            if(startMessage.signature != null){
                                parentMessage.setSignature(startMessage.signature.$ref);
                            }

                            if (startMessage.tags != null) {
                                for (Element tag : startMessage.tags) {
                                    if (tag.name.equals("Ref")) {
                                        parentMessage.setCaller(tag.value);
                                    }

                                    if(tag.name.equals("CF")){
                                        parentMessage.setInCF(true);
                                        parentMessage.setCfID(tag.reference.$ref);
                                    }
                                }
                            }


                            rootMessage = parentMessage;
                            messageNodes.add(rootMessage);

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

                                    MessageNode childMessage = new MessageNode();
                                    childMessage.setMessageSort(child.messageSort);
                                    childMessage.setSource(childSource.getMetaClass());
                                    childMessage.setTarget(childTarget.getMetaClass());
                                    childMessage.setAssignmentTarget(child.assignmentTarget);
                                    childMessage.setName(child.name);
                                    childMessage.setId(child._id);
                                    childMessage.setWeight(weight++);
                                    childMessage.setArguments(child.arguments);
                                    childMessage.setCallerObject(childTarget.getName());
                                    if (child.signature != null) {
                                        childMessage.setSignature(child.signature.$ref);
                                    }

                                    if (child.tags != null) {
                                        for (Element tag : child.tags) {
                                            if (tag.name.equals("Ref")) {
                                                childMessage.setCaller(tag.value);
                                            }
                                            if(tag.name.equals("CF")){
                                                childMessage.setInCF(true);
                                                childMessage.setCfID(tag.reference.$ref);
                                            }
                                            if(tag.name.equals("operand")){
                                                childMessage.setOperandId(tag.reference.$ref);
                                            }

                                        }
                                    }

                                    parentMessage.childNodes.add(childMessage);
                                    messageNodes.add(childMessage);
                                }

                                if (childMessages.size() > 0) {
                                    startMessage = childMessages.get(0);
                                    targetRef = startMessage.target.$ref;
                                    sourceRef = startMessage.source.$ref;
                                    parentMessage = parentMessage.childNodes.get(0);
                                    if (childMessages.size() > 1) {
                                        endMessage = childMessages.get(childMessages.size() - 1);
                                    }
                                }

                            }
                        }

                        for (MessageNode messageNode : messageNodes) {
                            List<Operation> matchingOperation = operationsList.stream().filter(f -> f._id.equals(messageNode.getSignature())).collect(Collectors.toList());
                            if (matchingOperation.size() > 0) {
                                operationMap.put(messageNode, matchingOperation.get(0)._id);
                                messageNode.setOperation(matchingOperation.get(0));
                            }
                        }

                        Stack stack = new Stack();
                        stack.push(messageNodes.get(0));
                        while(!stack.empty()){
                            MessageNode messageNode = (MessageNode)stack.pop();
                            Operation currentOperation = messageNode.getOperation();
                            if(currentOperation != null){
                                //all child nodes of this node make up its body
                                List<MessageNode> childNodes = messageNode.childNodes;
                                for(MessageNode child: childNodes){
                                    stack.push(child);
                                }
                                for(MessageNode childNode: childNodes){
                                    if(childNode.isInCF()){
                                        List<Instruction> combinedFragmentsList = combinedFragments.stream().filter(f -> f.getId().equals(childNode.getCfID())).collect(Collectors.toList());

                                        if(combinedFragmentsList.size() > 0){
                                            Instruction instruction = combinedFragmentsList.get(0);
                                            //get the topmost CF if it is in a tree
                                            Instruction parent = instruction.getParent();
                                            if(parent != null){
                                                while(true){
                                                    if(parent.getParent() == null){
                                                        break;
                                                    }else{
                                                        parent = parent.getParent();
                                                    }
                                                }
                                                if(!instruction.getBlock().contains(childNode)){
                                                    instruction.getBlock().add(childNode);
                                                }
                                                if(!currentOperation.getBlock().contains(parent)){
                                                    currentOperation.getBlock().add(parent);
                                                }
                                            }

                                        }
                                    }else{
                                        if(!currentOperation.getBlock().contains(childNode)){
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

        //printAllData(metaClasses);
//        while (rootMessage.childNodes != null || rootMessage.childNodes.size() > 0) {
//            System.out.println("parent " + rootMessage.name);
//            for (MessageNode child : rootMessage.childNodes) {
//                System.out.println("child " + child.name);
//            }
//            if (rootMessage.childNodes.size() > 0) {
//                rootMessage = rootMessage.childNodes.get(0);
//            } else {
//                break;
//            }
//        }



        for(MetaClass metaClass :metaClasses ){
            if(metaClass.name.equals("Main")){
                continue;
            }else{
                metaClass.printToConsole();
            }
        }

    }

    private static LifeLine getLifeLine(List<LifeLine> lifeLines, String $ref) {
        List<LifeLine> list = lifeLines.stream().filter(f -> f.getId().equals($ref)).collect(Collectors.toList());
        return list.get(0);
    }

    private static List<Element> getChildMessages(List<Element> messages, String $ref) {
        List<Element> childMessages = messages.stream().filter(f -> (f.source.$ref.equals($ref) && !f.name.contains("result")) || (f.target.$ref.equals($ref) && f.name.contains("result"))).collect(Collectors.toList());
        return childMessages;
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



