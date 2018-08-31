package cn.edu.nudt.secant.jdriver.knowledgebase;

import cn.edu.nudt.secant.jdriver.preprocessor.ClassNodesAnalyzer;
import cn.edu.nudt.secant.jdriver.scheduler.ClassNodeUtil;
import cn.edu.nudt.secant.jdriver.preprocessor.TypeTableBuilder;
import cn.edu.nudt.secant.jdriver.tools.JCompiler;
import cn.edu.nudt.secant.jdriver.tools.Options;
import cn.edu.nudt.secant.jdriver.tools.TypeUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import sun.java2d.pipe.ShapeSpanIterator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * This is to build class instance according to type table
 * Created by huang on 7/17/18.
 */
public class InstanceHelperBuilder extends HelperBuilder {

    public static HashSet<Type> unsupportedTypeSet = new HashSet<>();

    public static void addunsuported(Type type) {
        unsupportedTypeSet.add(type);
    }


    public static void showUnsupported() {
        System.out.println("Unsupported Types: ");
        for(Type t: unsupportedTypeSet)
            System.out.println("\t" + t.getClassName());
    }

    public void build() {
        int size = TypeTableBuilder.classTable.keySet().size();
        System.out.println("Starting building " + size + " types.");

        Set<Type> typeSet = new HashSet<>();
        for(Type t: TypeTableBuilder.classTable.keySet()) {
            ClassNode classNode = TypeTableBuilder.classTable.get(t);
            //skip non-public class
            if(ClassNodeUtil.isClassNodeBuilderable(classNode))
                typeSet.add(t);
        }

        System.out.println(typeSet.size() + " types are buildable.");

        Set<Type> unprocessedSet = buildTypeHelper(typeSet, 100);

        System.out.println("\nFinsh building " + size + " types, " + typeSet.size() + " of them are buildable");
        System.out.println( unprocessedSet.size() + " types remains unprocessed");

        for(Type t: unprocessedSet) {
            ClassNode classNode = TypeTableBuilder.getClassNode(t);
            if(classNode != null)
                System.out.println("\t" + t.getClassName() + "/(" + classNode.name + ") unprocessed, defined in " + classNode.sourceFile);
        }

    }


    public Set<Type> buildTypeHelper(Set<Type> typeSet, int maxAllowed) {

        Set<Type> unprocessedTypeSet = new HashSet<>();//unprocessed
        unprocessedTypeSet.addAll(typeSet);

        for(Type t: typeSet) {
            int result = buildType(t);
            if(result > 0)
                unprocessedTypeSet.remove(t);
            else {

            }
        }

        System.out.println(unprocessedTypeSet.size() + " types remains unable to initialize");


        if(unprocessedTypeSet.size() != typeSet.size() && unprocessedTypeSet.size() > maxAllowed)
            buildTypeHelper(unprocessedTypeSet, maxAllowed);


        return unprocessedTypeSet;
    }

    /**
     * build instance helper for given type t
     * @param t the type for building
     * @return -1 for unable to build, 0 for no success, bigger than 1, indicates the number of generated InstanceHepler
     */
    public int buildType(Type t) {
        int result = 0;
        ClassNode classNode = TypeTableBuilder.classTable.get(t);

        //skip non-public class
        if(!ClassNodeUtil.isClassNodeBuilderable(classNode))
            return -1;

        //fixme, debug
        if(classNode.name.contains("ByteSourceFile"))
            System.out.println("building ByteSourceFile");

        Set<MethodNode> methodNodeSet = TypeTableBuilder.getConstructor(t);
        if(methodNodeSet == null) {
            System.err.println("Nothing found for class: " + t.getClassName());

        } else {
            //int index = 0;
            for (MethodNode m : methodNodeSet) {
                try {
                    InstanceHelperMethod helperMethod = new InstanceHelperMethod(t, m, result);
                    InstanceHelperClasses.add(t, helperMethod);
                    //unprocessedTypeSet.remove(t);//remove t out of types
                    result += 1;
                } catch (Exception e) {
                    System.err.println("Failed to build instances helper for method: " + m.name);
                }
            }
        }
        return result;
    }

    /**
     * build TypeHelper for all the types in the type table
     * depreciated!
     */
    public void buildTypeHelper(int size) {
        //iterate over all types in

        Set<Type> unprocessedTypeSet = new HashSet<>();//unprocessed
        unprocessedTypeSet.addAll(TypeTableBuilder.classTable.keySet());

        for(Type t: TypeTableBuilder.classTable.keySet()) {
            //skip processed types
            if(!unprocessedTypeSet.contains(t))
                continue;

            int result = buildType(t);
            if(result < 0) {
                //unable to build
                //continue;
            } else if(result == 0) {
                //unable to build
            } else {
                //more than one is built
                unprocessedTypeSet.remove(t);
            }
            //some types can't be instantiated

        }
        if(unprocessedTypeSet.size() < size)
            buildTypeHelper(unprocessedTypeSet.size());
        System.out.println(unprocessedTypeSet.size() + " types remains unable to initialize");
    }

    /**
     * call this to build type table, and assemble them into helper classes
     */
    public void assemble() {

//        Hashtable<Type, ClassNode> testableClassTable = new Hashtable<>();
//        buildTypeHelper(size);

        build();


        HashSet<HelperMethod> methods = new HashSet<>();
        boolean isSameExist;
        for(InstanceHelperMethod m: InstanceHelperClasses.getAllInstanceHelpers()) {
            //prevent duplicate
            //fixme,


            isSameExist = false;
            for(HelperMethod hm: methods){
                if(hm.methodname.equals(m.methodname)) {
                //&& hm.desc.equals(m.desc))//two method with same name can't exist in static
                    isSameExist = true;
                }
            }

            if(isSameExist)
                continue;
            methods.add(m);
        }
        super.assemble(methods);
        System.out.println("Number of items in InstanceHelper: " + methods.size());

    }


    public void write(String filename) throws Exception {
        super.write(filename, "InstanceHelper");
    }


    public boolean compile(String filename) {
        return JCompiler.compileHelperFile(filename);
    }


    public static void build(String filename) throws Exception {
        InstanceHelperBuilder builder = new InstanceHelperBuilder();
        builder.assemble();
        if(filename == null) {
            filename = Options.v().getOutput() + "/InstanceHelper.java";
            filename = filename.replace("//","/");
        }
        builder.write(filename);
        builder.compile(filename);


        showUnsupported();
    }

}
