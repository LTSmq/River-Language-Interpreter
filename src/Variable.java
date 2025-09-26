import java.util.HashMap;


public class Variable {
    private static final int DEFAULT_INDEX = 0;
    static HashMap<String, HashMap<Integer, Measure>> registry = new HashMap<>();
    public static void set(String variableName, Measure value) {
        set(variableName, DEFAULT_INDEX, value);
    }

    public static void set(String variableName, int index, Measure value) {
        if (!registry.containsKey(variableName)) {
            registry.put(variableName, new HashMap<>());
        }
        registry.get(variableName).put(index, value);
    }
    
    public static Measure get(String variableName) {
        return get(variableName, DEFAULT_INDEX);
    }

    public static Measure get(String variableName, int index) {
        if (registry.containsKey(variableName)) {
            HashMap<Integer, Measure> variableArray = registry.get(variableName);
            if (variableArray.containsKey(index)) {
                return variableArray.get(index);
            }
        }
        
        return Measure.falseValue;
    }
}
