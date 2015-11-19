import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramyashenoy on 11/14/15.
 */
public class Package extends Instruction {
    String name;
    List<MetaClass> classes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MetaClass> getClasses() {
        return classes;
    }

    public void setClasses(List<MetaClass> classes) {
        this.classes = classes;
    }
}
