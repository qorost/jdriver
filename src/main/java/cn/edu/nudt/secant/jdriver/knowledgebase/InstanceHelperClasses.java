package cn.edu.nudt.secant.jdriver.knowledgebase;

import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * save instance helper classess
 * Created by huang on 7/21/18.
 */
public class InstanceHelperClasses {

    public static HashMap<Type, HashSet<InstanceHelperMethod>> instanceHelperHashMap = new HashMap<>();

    public static void add(Type t, InstanceHelperMethod instanceHelperMethod) {

        if(instanceHelperHashMap.containsKey(t)) {
            for(InstanceHelperMethod m: instanceHelperHashMap.get(t)) {
                if(m.desc.equals(instanceHelperMethod.desc))//fixme
                    return;
            }
            instanceHelperHashMap.get(t).add(instanceHelperMethod);
        } else {
            HashSet<InstanceHelperMethod> instanceHelperMethodHashSet = new HashSet<>();
            instanceHelperMethodHashSet.add(instanceHelperMethod);
            instanceHelperHashMap.put(t, instanceHelperMethodHashSet);
        }
    }

    /**
     * get an instanceHelperMethod to make type t;
     * @param t
     * @return
     */
    public static InstanceHelperMethod getInstanceHelper(Type t) {
        if(instanceHelperHashMap.containsKey(t)) {
            HashSet<InstanceHelperMethod> results = instanceHelperHashMap.get(t);
            Iterator iterator = results.iterator();
            while(iterator.hasNext())
                return (InstanceHelperMethod) iterator.next();
        }
        return null;
    }

    public static HashSet<InstanceHelperMethod> getInstanceHelpers(Type t) {
        return instanceHelperHashMap.get(t);
    }


    public static HashSet<InstanceHelperMethod> getAllInstanceHelpers() {
        HashSet<InstanceHelperMethod> allHelperMethod = new HashSet<>();
        for(Type t: instanceHelperHashMap.keySet()) {
            allHelperMethod.addAll(instanceHelperHashMap.get(t));
        }
        return allHelperMethod;
    }


}
