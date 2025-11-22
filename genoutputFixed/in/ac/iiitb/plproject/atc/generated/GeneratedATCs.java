package in.ac.iiitb.plproject.atc.generated;

import gov.nasa.jpf.symbc.Debug;

public class GeneratedATCs {
    public void increment_helper() {
        int x = Debug.makeSymbolicInteger("x");
        Debug.assume((x > 0));
        System.out.println("Test Input: x = " + x);
        int x_old = x;
        int[] xRef = new int[]{x};
        Helper.increment(xRef);
        x = xRef[0];
        assert((x > x_old)) : "Postcondition violated: incremented value should be greater than original";
    }

    public static void main(String[] args) {
        GeneratedATCs instance = new GeneratedATCs();
        instance.increment_helper();
        instance.increment_helper();
        instance.increment_helper();
    }
}
