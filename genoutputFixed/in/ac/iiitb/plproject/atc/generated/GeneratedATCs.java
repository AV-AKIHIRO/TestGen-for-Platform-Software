package in.ac.iiitb.plproject.atc.generated;

import gov.nasa.jpf.symbc.Debug;

import org.junit.Test;
import java.util.*;

public class GeneratedATCs {

    @Test
    public void increment_helper() {
        int x = Debug.makeSymbolicInteger("x");
        Debug.assume((x > 0));
        int x_old = x;
        System.out.println(("Test Input: x = " + x));
        int[] xRef = new int[]{x};
        Helper.increment(xRef);
        x = xRef[0];
        assert((x > x_old));
    }

    public void process_helper() {
        Set<Integer> data = (Set<Integer>) Debug.makeSymbolicRef("data", new HashSet<>());
        Map<Integer, Integer> result = (Map<Integer, Integer>) Debug.makeSymbolicRef("result", new HashMap<>());
        if (data == null || result == null) {
            return;
        }
        Debug.assume(data != null);
        Debug.assume(result != null);
        Debug.assume(new HashSet<>(Arrays.asList(1, 2, 3)).contains(2));
        System.out.println("Test Input: data = " + data);
        if (data != null && result != null) {
            Helper.process(data, result);
        }
        System.out.println("Test Input: Helper.process completed");
        assert(result != null);
    }

    public static void main(String[] args) {
        GeneratedATCs instance = new GeneratedATCs();
        instance.increment_helper();
        instance.increment_helper();
        instance.process_helper();
        instance.increment_helper();
    }
}
