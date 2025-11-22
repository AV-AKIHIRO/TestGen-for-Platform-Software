package in.ac.iiitb.plproject.atc.generated;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

public class Helper {
    public static void increment(int[] x) {
        if (x != null && x.length > 0) {
            x[0] = x[0] + 1;
        }
    }

    public static void process(Set<Integer> data, Map<Integer, Integer> result) {
        if (data != null && result != null) {
            for (Integer item : data) {
                result.put(item, item * 2);
            }
        }
    }

    public static Map<?,?> update(Map<Integer, Integer> result, Set<Integer> data) {
        if (result == null) {
            return new HashMap<>();
        }
        Map<Integer, Integer> updated = new HashMap<>(result);
        if (data != null) {
            for (Integer item : data) {
                updated.put(item, item * 2);
            }
        }
        return updated;
    }
}
