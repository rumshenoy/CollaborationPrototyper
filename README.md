# Collaboration Prototyper

### Description and Background:

This project is my Master's thesis. The main idea of Collaboration Prototyper is to generate near complete prototypes from UML Collaborations (defined next). In the software development process, the 60% of the time, key stakeholders find that the product differs from their initial specification and requirements. The requirements are lost due to a long pipeline of procedures and people in between. If we could take the requirement specification consisting of analysis models and usecases directly, and generate a prototype from it, we could try and reduce this gap between the expectation and delivery of the product. 

![Software Development Process](https://cloud.githubusercontent.com/assets/1961478/11947061/9656c14c-a816-11e5-9a0a-231673fee252.png)

### What are Collaborations?

Class diagrams by themselves lack method implementations. Hence, we combine sequence diagrams with class diagrams to include information about method implementations. Collaborations are nothing but the structure (class diagrams) and behaviour (sequence diagrams) of an application. A collaboration is a collection of classes that work together to implement the services specified in a component's provided interfaces. We use [StarUML] (http://staruml.io/) to draw UML diagrams. 

### How it works?

The software architect or modeler prepares a UML collaboration with necessary detail filled in. The Collaboration Prototyper specifies a set of rules the user has to follow so that it can generate code from the collaboration. The UML Collaboration reads in the .mdj file generated from StarUML and generates a prototype. 

![Process Pipeline](https://cloud.githubusercontent.com/assets/1961478/11947105/29cd5e22-a817-11e5-863b-e2c70878a07d.png)

### Usage:

On importing the project, you have to build the artifact and  the cbpro.jar file will be generated. It takes in the input file as the argument. 

```
java -jar cbpro.jar input file
```
