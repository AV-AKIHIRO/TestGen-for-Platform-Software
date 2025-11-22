package in.ac.iiitb.plproject.symex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Utility class for mapping Java types to their appropriate initialization strategies
 * for symbolic execution with JPF/SPF.
 * 
 * This class provides intelligent type handling:
 * - Primitive types and their wrappers -> use Debug.makeSymbolic*()
 * - Java standard collections -> use concrete initialization (new HashSet(), etc.)
 * - Custom classes -> use Debug.makeSymbolicRef() with proper handling
 */
public class TypeMapper {
    
    // Set of Java standard collection types that should be initialized concretely
    private static final Set<String> COLLECTION_TYPES = new HashSet<>();
    static {
        COLLECTION_TYPES.add("Set");
        COLLECTION_TYPES.add("Map");
        COLLECTION_TYPES.add("List");
        COLLECTION_TYPES.add("Collection");
        COLLECTION_TYPES.add("Queue");
        COLLECTION_TYPES.add("Deque");
        COLLECTION_TYPES.add("java.util.Set");
        COLLECTION_TYPES.add("java.util.Map");
        COLLECTION_TYPES.add("java.util.List");
        COLLECTION_TYPES.add("java.util.Collection");
        COLLECTION_TYPES.add("java.util.Queue");
        COLLECTION_TYPES.add("java.util.Deque");
    }
    
    // Map of collection types to their concrete initialization code
    private static final Map<String, String> COLLECTION_INIT_MAP = new HashMap<>();
    static {
        COLLECTION_INIT_MAP.put("Set", "new HashSet<>()");
        COLLECTION_INIT_MAP.put("Map", "new HashMap<>()");
        COLLECTION_INIT_MAP.put("List", "new ArrayList<>()");
        COLLECTION_INIT_MAP.put("Collection", "new ArrayList<>()");
        COLLECTION_INIT_MAP.put("Queue", "new LinkedList<>()");
        COLLECTION_INIT_MAP.put("Deque", "new LinkedList<>()");
        COLLECTION_INIT_MAP.put("java.util.Set", "new java.util.HashSet<>()");
        COLLECTION_INIT_MAP.put("java.util.Map", "new java.util.HashMap<>()");
        COLLECTION_INIT_MAP.put("java.util.List", "new java.util.ArrayList<>()");
        COLLECTION_INIT_MAP.put("java.util.Collection", "new java.util.ArrayList<>()");
        COLLECTION_INIT_MAP.put("java.util.Queue", "new java.util.LinkedList<>()");
        COLLECTION_INIT_MAP.put("java.util.Deque", "new java.util.LinkedList<>()");
    }
    
    // Map of collection types to their generic type strings
    private static final Map<String, String> COLLECTION_GENERIC_MAP = new HashMap<>();
    static {
        COLLECTION_GENERIC_MAP.put("Set", "Set<?>");
        COLLECTION_GENERIC_MAP.put("Map", "Map<?,?>");
        COLLECTION_GENERIC_MAP.put("List", "List<?>");
        COLLECTION_GENERIC_MAP.put("Collection", "Collection<?>");
        COLLECTION_GENERIC_MAP.put("Queue", "Queue<?>");
        COLLECTION_GENERIC_MAP.put("Deque", "Deque<?>");
        COLLECTION_GENERIC_MAP.put("java.util.Set", "java.util.Set<?>");
        COLLECTION_GENERIC_MAP.put("java.util.Map", "java.util.Map<?,?>");
        COLLECTION_GENERIC_MAP.put("java.util.List", "java.util.List<?>");
        COLLECTION_GENERIC_MAP.put("java.util.Collection", "java.util.Collection<?>");
        COLLECTION_GENERIC_MAP.put("java.util.Queue", "java.util.Queue<?>");
        COLLECTION_GENERIC_MAP.put("java.util.Deque", "java.util.Deque<?>");
    }
    
    /**
     * Checks if a type is a Java standard collection type.
     * 
     * @param typeName The type name to check
     * @return true if it's a collection type, false otherwise
     */
    public static boolean isCollectionType(String typeName) {
        if (typeName == null) {
            return false;
        }
        // Check exact match
        if (COLLECTION_TYPES.contains(typeName)) {
            return true;
        }
        // Check if it's a generic type like "Set<?>" or "Map<?,?>"
        String baseType = typeName.split("[<>]")[0].trim();
        return COLLECTION_TYPES.contains(baseType);
    }
    
    /**
     * Checks if collections should use symbolic execution.
     * For JPF, collections can be made symbolic using makeSymbolicRef with empty instances.
     * 
     * @return true if collections should be symbolic, false for concrete initialization
     */
    public static boolean useSymbolicCollections() {
        // Return true to enable symbolic collections
        // This allows JPF to explore different collection states
        return true;
    }
    
    /**
     * Gets the generic type string for a collection type.
     * Preserves existing generic type information if present.
     * 
     * @param typeName The type name (e.g., "Set", "Map", "Set<Integer>", "Map<Integer, Integer>")
     * @return The generic type string (e.g., "Set<?>", "Map<?,?>", "Set<Integer>", "Map<Integer, Integer>")
     */
    public static String getGenericType(String typeName) {
        if (typeName == null) {
            return "Object";
        }
        // If the type already has generic parameters, preserve them
        if (typeName.contains("<") && typeName.contains(">")) {
            return typeName;
        }
        // Extract base type if it's already generic
        String baseType = typeName.split("[<>]")[0].trim();
        return COLLECTION_GENERIC_MAP.getOrDefault(baseType, typeName);
    }
    
    /**
     * Gets the initialization code for a collection type.
     * Can return either concrete initialization or symbolic initialization.
     * 
     * @param typeName The type name
     * @param varName The variable name (for symbolic initialization)
     * @return The initialization code
     */
    public static String getCollectionInitCode(String typeName, String varName) {
        if (typeName == null) {
            typeName = "Set";
        }
        // Extract base type if it's generic
        String baseType = typeName.split("[<>]")[0].trim();
        String concreteInit = COLLECTION_INIT_MAP.getOrDefault(baseType, "new java.util.HashSet<>()");
        
        if (useSymbolicCollections() && varName != null) {
            // Use makeSymbolicRef with empty collection as default value
            // This allows JPF to treat the collection as symbolic while avoiding null issues
            String genericType = getGenericType(typeName);
            return "(" + genericType + ") Debug.makeSymbolicRef(\"" + varName + "\", " + concreteInit + ")";
        } else {
            // Use concrete initialization
            return concreteInit;
        }
    }
    
    /**
     * Gets the concrete initialization code for a collection type (legacy method).
     * 
     * @param typeName The type name
     * @return The initialization code (e.g., "new HashSet<>()")
     */
    public static String getCollectionInitCode(String typeName) {
        return getCollectionInitCode(typeName, null);
    }
    
    /**
     * Checks if a type is a primitive or primitive wrapper.
     * 
     * @param typeName The type name to check
     * @return true if it's a primitive type, false otherwise
     */
    public static boolean isPrimitiveType(String typeName) {
        if (typeName == null) {
            return false;
        }
        String lower = typeName.toLowerCase();
        return lower.equals("int") || lower.equals("integer") ||
               lower.equals("double") || lower.equals("float") ||
               lower.equals("long") || lower.equals("short") ||
               lower.equals("byte") || lower.equals("char") ||
               lower.equals("boolean") || lower.equals("string");
    }
    
    /**
     * Checks if a type should use Debug.makeSymbolicRef().
     * This is for custom classes that are not collections or primitives.
     * 
     * @param typeName The type name to check
     * @return true if it should use makeSymbolicRef, false otherwise
     */
    public static boolean shouldUseSymbolicRef(String typeName) {
        return !isPrimitiveType(typeName) && !isCollectionType(typeName);
    }
    
    /**
     * Gets the appropriate initialization code for a type.
     * 
     * @param typeName The type name
     * @param varName The variable name (for symbolic types)
     * @return The initialization code
     */
    public static String getInitializationCode(String typeName, String varName) {
        if (isCollectionType(typeName)) {
            return getCollectionInitCode(typeName);
        } else if (shouldUseSymbolicRef(typeName)) {
            // For custom classes, use makeSymbolicRef with null
            // Note: Some types may not work with null, but this is the standard approach
            // Collections are handled separately above
            String genericType = getGenericType(typeName);
            return "(" + genericType + ") Debug.makeSymbolicRef(\"" + varName + "\", null)";
        } else {
            // Primitive types - this shouldn't be called for primitives in this context
            // as they're handled separately
            return "Debug.makeSymbolicInteger(\"" + varName + "\")";
        }
    }
    
    /**
     * Gets the makeSymbolicRef call for custom classes.
     * For collections, this should not be called - use getCollectionInitCode instead.
     * 
     * @param typeName The type name
     * @param varName The variable name
     * @param useNewInstance If true, try to use new instance; if false, use null
     * @return The makeSymbolicRef call
     */
    public static String getMakeSymbolicRefCall(String typeName, String varName, boolean useNewInstance) {
        String genericType = getGenericType(typeName);
        if (useNewInstance) {
            return "(" + genericType + ") Debug.makeSymbolicRef(\"" + varName + "\", new " + typeName + "())";
        } else {
            return "(" + genericType + ") Debug.makeSymbolicRef(\"" + varName + "\", null)";
        }
    }
}

