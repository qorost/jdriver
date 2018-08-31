package cn.edu.nudt.secant.jdriver.preprocessor;

import cn.edu.nudt.secant.jdriver.scheduler.ClassNodeUtil;
import com.sun.xml.internal.bind.v2.util.QNameMap;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;

/**
 * This is to analyze the input arguments in the method, decide which classess are involved!
 * Created by huang on 7/18/18.
 */
public class MethodArgumentAnalyzer {

    //The data structure, a tree/graph/map?
    public static HashSet<MethodNode> methodNodeHashSet = new HashSet<>();

    //public static HashSet<Type> failedTypes = new HashSet<>();
    public static HashMap<Type, Integer> failedTypes = new HashMap();


    public static void analyzeMethod(MethodNode m, String classname) {
        Type mtype = Type.getMethodType(m.desc);
        Type[] inputTypes = mtype.getArgumentTypes();

        String msg = "method: " + m.name + " (" + classname + "):";
        int failedArg = 0;
        for(Type t: inputTypes) {
            int sort = t.getSort();
            if(sort < Type.ARRAY)
                continue;
            else {
                if(TypeTableBuilder.getConstructor(t) == null) {
                    System.err.println("\tThe method " + m.name + " can not be instantiated for argument: " + t
                            .getClassName());
                    failedArg += 1;
                    //failedTypes.add(t);
                    if(failedTypes.containsKey(t)) {
                        failedTypes.put(t, failedTypes.get(t) +1);
                    } else
                        failedTypes.put(t, 1);
                }
            }
        }
        msg += failedArg == 0? "success." : "failed with " + failedArg + " args";
        System.out.println(msg);
    }



    public static ArrayList<Type> sortFailedTypes() {
        ArrayList<Type> sortedTypeList = new ArrayList<>();
        for(Type tt: failedTypes.keySet()) {
            int times = failedTypes.get(tt);
            if(sortedTypeList.size() == 0) {
                sortedTypeList.add(tt);
                continue;
            }

            int len = sortedTypeList.size();
            for(int i=0; i < sortedTypeList.size(); i++) {
                int dst = failedTypes.get(sortedTypeList.get(i));
                if(times < dst) {
                    sortedTypeList.add(i,tt);
                    break;
                }
            }
            if(len == sortedTypeList.size())
                sortedTypeList.add(tt);
        }
        return sortedTypeList;
    }


    public static void saveFailedTypestoFile(String filename) {


    }


    public static void showFailedTypes() {
        System.out.println("Failed Types: ");
        for(Type t: sortFailedTypes()) {
            int times = failedTypes.get(t);
            System.out.println("\t" + t.getClassName() + " : " + times);
        }

        Type t = Type.getObjectType("java.nio.ByteOrder");
        for(Type tt: failedTypes.keySet()) {
            if(tt == t)
                System.out.println("The self made type contains in failedtypes");
            if(tt.getClassName().equals(t.getClassName())) {
                System.out.println("The self made type contains in failedtypes");
                break;
            }
            else
                continue;
        }
        //System.out.println("The self made type doesn't contain in failedtypes");

    }

    public static void analyze() {
        //analyze while the addMethod finished!
        if(ClassNodesAnalyzer.classNodeHashTable != null) {
            System.out.println("The size of classNodeHashTable:" + ClassNodesAnalyzer.classNodeHashTable.keySet().size
                    ());
            for(String s: ClassNodesAnalyzer.classNodeHashTable.keySet()) {
                Object o = ClassNodesAnalyzer.classNodeHashTable.get(s);
                if(o instanceof ClassNode) {
                    for(Object m: ((ClassNode) o).methods) {
                        if(m instanceof MethodNode) {
                            analyzeMethod((MethodNode) m,((ClassNode) o).name);
                        }
                    }
                }
            }
            showFailedTypes();
        }
    }




}
