package in.ac.iiitb.plproject.atc.generated;

import java.util.*;
import gov.nasa.jpf.symbc.Debug;

public class GeneratedATCs {

    public void appendExclamation_helper() {
        String s = Debug.makeSymbolicString("s");
        Debug.assume((s != null));
        System.out.println(("Test Input: s = " + s));
        Helper.appendExclamation(s);
        assert(s != null);
    }

    public static void main(String[] args) {
        GeneratedATCs instance = new GeneratedATCs();
        instance.appendExclamation_helper();
        instance.appendExclamation_helper();
        instance.appendExclamation_helper();
    }
}
