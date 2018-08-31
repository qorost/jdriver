package cn.edu.nudt.secant.jdriver.preprocessor;

import cn.edu.nudt.secant.jdriver.scheduler.ClassNodeUtil;
import cn.edu.nudt.secant.jdriver.tools.ClassUtil;
import cn.edu.nudt.secant.jdriver.tools.Options;
import cn.edu.nudt.secant.jdriver.tools.TypeUtil;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import javax.swing.text.html.Option;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 * This class analyze the relationships (subclassing/implement) between classnodes
 * Created by huang on 6/8/18.
 */
public class ClassNodesAnalyzer {

    public static HashSet<ClassNode> classNodes;

    public static boolean DEBUG = false;

    //hash table, key: classnode classname, Object, classNode or String
    public static Hashtable<String,Object> classNodeHashTable = new Hashtable<>();
    public static HashSet<String> abstractclass = new HashSet<>();
    //classgraph, is to store how classes are related to each other in subclassing relationships
    public static DirectedGraph<Object, DefaultEdge> classgraph = new DefaultDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);


    /**
     * add class to the hashtable and classgraph
     * @param classNode
     */
    public static void addClass(ClassNode classNode) {
        String key = classNode.name;

        if (classNodeHashTable.containsKey(key)) {
            Object o = classNodeHashTable.get(key);
            if(o instanceof String) {
                //System.out.println("same string key exist, update it for " + key);
                //classgraph.removeVertex(o);
                classNodeHashTable.put(key, classNode);
            }
        } else {
            classNodeHashTable.put(key, classNode);
            classgraph.addVertex(classNode);
            if(classNode.superName != null) {
                Object o = classNodeHashTable.get(classNode.superName);
                if(o == null) {
                    //add temporary to the classgraph
                    classNodeHashTable.put(classNode.superName, classNode.superName);
                }
            }
        }
    }


    /**
     * build the call graph, according to the hashtable
     */
    public static void analyze() {
        Set<String> keys = classNodeHashTable.keySet();
        for(String key: keys) {
            Object object = classNodeHashTable.get(key);
            classgraph.addVertex(object);
        }

        for(Object v: classgraph.vertexSet()) {
            if(v instanceof ClassNode) {
                ClassNode classnode = (ClassNode) v;
                if(classnode.superName != null) {
                    Object supernode = classNodeHashTable.get(classnode.superName);
                    if(supernode != null) {
                        if(classgraph.containsVertex(supernode)) {
                            classgraph.addEdge(classnode, supernode);
                            continue;
                        } else
                            System.err.println(classnode.superName + " doesn't exist in the classgraph");
                    }
                    System.err.println("unable to add edge for classnode " + classnode.name + " and its superclass "
                            + classnode.superName);
                }
            }
        }
    }


    /**
     * test if the two given nodes has a subclassing relationship
     * @param classNode the son node
     * @param superclass the super node, the classname of the class
     * @return tree if superclass is super class of classNode
     */
    public static boolean isSuperClass(ClassNode classNode, String superclass) {
        ConnectivityInspector<Object, DefaultEdge> inspector = new ConnectivityInspector<>(classgraph);
        if(classgraph.vertexSet().contains(classNode)) {
            if(classNodeHashTable.containsKey(superclass)) {
                Object supernode = classNodeHashTable.get(superclass);
                return inspector.pathExists(classNode, supernode);
            }
        }
        return false;
    }

    /**
     * decide if inputclass is a subclass of superclass
     * @param inputclass the classname of the class
     * @param superclass the classname of the superclass
     * @return true if superclass is a super class of inputclass
     */
    public static boolean isSuperClass(String inputclass, String superclass) {
        if(classNodeHashTable.containsKey(inputclass)) {
            Object o = classNodeHashTable.get(inputclass);
            if(o instanceof ClassNode)
                return isSuperClass((ClassNode) o, superclass);
        }
        return false;
    }

    /**
     * get the ClassNode by classname
     * @param classname the classname of the class
     * @return ClassNode if found, else return null;
     */
    public static ClassNode getClassNode(String classname) {
        Object o = classNodeHashTable.get(classname);
        if(o instanceof ClassNode)
            return (ClassNode) o;
        return null;
    }


    /**
     * for debugging, show standard libraries in classgraph
     */
    public static void showStandardLibraries() {
        System.out.println("The standard classnode in classgraph: ");
        for(Object o : classgraph.vertexSet()) {
            if(o instanceof String)
                System.out.println("\t" + (String) o);
        }
    }


    /**
     * print the class graph.
     */
    public static void showClassGraph() {
        //HashSet<DefaultEdge> edges = classgraph.get
        for(Object o: classgraph.vertexSet()) {
            int outNum = classgraph.outDegreeOf(o);
            int inNum = classgraph.inDegreeOf(o);
            if(inNum ==0 && outNum == 0) {
                //none found for commons-io
                System.out.println("This is a stand alone class!");
                continue;
            }

            String msg = "The class ";
            if(o instanceof ClassNode) {
                ClassNode classNode = (ClassNode) o;
                msg += classNode.name;
            } else
                msg += o.toString();

            if (inNum > 0) {
                msg += " has " + inNum + " subclasses";
                if (outNum > 0)
                    msg += " and " + outNum + " superclassess";
            } else {
                if (outNum > 0)
                    msg += " has " + outNum + " superclassess";
            }
            if(Options.v().isVerbose())
                System.out.println(msg);
        }
    }
    //public static void showabstracts()

    /**
     * test if the given classnode is testable, abstract classes, non-public class, interface, inner classes are
     * considered not directly testable
     * @param classNode the given class
     * @return
     */
    public static boolean isClassDirectlyTestable(ClassNode classNode) {
        if((classNode.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) return false;
        if((classNode.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE) return false;
        if((classNode.access & Opcodes.ACC_PUBLIC) != Opcodes.ACC_PUBLIC) return false;


        //fixme
        //if the classnode is defined inside pass it!
        if(classNode.name.contains("$")) return false;

        if(classNode.superName.contains("Exception")) {
            //System.err.println("The class has a super class of Exception");
            return false;
        }
        return true;
    }


    /**
     * test if the file processes file
     * @param methodNode
     * @return
     */
    public static boolean isMethodProcessingFile(MethodNode methodNode) {

        String msg = "Input Parameters for " + methodNode.name + "\n";
        boolean containsBuiltIn = false;
        Type methodType = Type.getMethodType(methodNode.desc);
        for(Type t: methodType.getArgumentTypes()) {
            if(t.getSort() == Type.OBJECT) {
                String className = t.getClassName();
                if(className.startsWith("java")) {
                    containsBuiltIn = true;
                    //System.out.println("Method: ")
                    msg += "\t" + className + "\n";
                }
            }
        }
        if(containsBuiltIn)
            System.out.println(msg);
        return false;
    }

    /**
     * test if the method is directly tesetable
     * @param methodNode methodnode
     * @return
     */
    public static boolean isMethodDirectlyTestable(MethodNode methodNode) {
        //fixme
        if(methodNode.name.contains("<init>"))
            return false;

        if(TypeUtil.isAccess(methodNode.access, Opcodes.ACC_PUBLIC))
            return true;
        return false;
    }

    public static int getTestableMethodsNum(ClassNode classNode) {
        int num = 0;
        for(Object m: classNode.methods) {
            if(m instanceof MethodNode) {
                if(isMethodDirectlyTestable((MethodNode) m))
                    num += 1;
            }
        }
        return num;
    }

    public static void showStatics() {
        int methodNum = 0;
        int direclty = 0;
        int innerclass = 0;

        int testableMethodNum = 0;
        int innermethods = 0;


        for(Object o: classgraph.vertexSet()) {
            if(o instanceof  ClassNode) {
                ClassNode classNode = (ClassNode) o;
                methodNum += classNode.methods.size();
                if (isClassDirectlyTestable(classNode)) {
                    direclty += 1;
                    testableMethodNum += getTestableMethodsNum(classNode);
                }
                if (classNode.name.contains("$")) {
                    innerclass += 1;
                    innermethods += classNode.methods.size();
                    //System.out.println("The class " + classNode.classname + " is an inner class from " + classNode.sourceFile);
                }

                if (true)
                    continue;


                if ((classNode.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) {
                    //System.out.println("The class " + classNode.classname + " is abastract");
                    continue;
                }
                if ((classNode.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE) {
                    //System.out.println("The class " + classNode.classname + " is an interface");
                    continue;
                }
            }
            //if(classNode.access != Opcodes.ACC_ABSTRACT || classNode.access!= Opcodes.ACC_INTERFACE)
        }

        int totalclassfile = Options.v().getInput().size();

        String msg = String.format("Input classfile: %d, total vertex in set: %d, Directly Testable: %d, " +
                        "Inner classess: %d",
                totalclassfile,
                classgraph.vertexSet().size(),
                direclty,innerclass);
        System.out.println(msg);

        System.out.println(classgraph.vertexSet().size() + " classes, " + methodNum + " methods detected(no access " +
                "class), "+ innermethods + " are inner methods, " +  + testableMethodNum+ " are testable");
        showClassGraph();
    }


    public static void showMethods() {
        int  numofclasses = 0;
        HashSet<String> standard = new HashSet<>();
        try {
            PrintWriter out = new PrintWriter("./tests/methods.log");
            String classSummary;
            for (Object o : classgraph.vertexSet()) {
                if (o instanceof ClassNode) {
                    numofclasses += 1;
                    ClassNode classNode = (ClassNode) o;
                    classSummary = "\nSummary for Class " + classNode.name;
                    for (Object m : ((ClassNode) o).methods) {
                        if (m instanceof MethodNode) {
                            MethodNode methodNode = (MethodNode) m;
                            classSummary += "\n\t" + methodNode.name + "(";
                            Type t = TypeUtil.getMethodType(methodNode);

                            Type[] types = t.getArgumentTypes();
                            if(types.length == 0) {
                                classSummary += ");";
                                continue;
                            }
                            for (Type p: types) {
                                //parameterNode.
                                classSummary += p.getClassName() + ",";
                                if(p.getClassName().startsWith("java")) {
                                    standard.add(p.getClassName());
                                }
                            }
                            classSummary += ",";
                            classSummary = classSummary.replace(",,", ");");
                            classSummary.replace("<init>",classNode.name);
                        }
                    }
                    out.write(classSummary);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("The libraries:");
        for(String s: standard) {
            System.out.println("\t\"" + s +"\",");
        }
    }


    /**
     * show all testable classes/methods
     */
    public static void showTestable() {

        int totalClassNum = 0;
        int testableClassNum = 0;

        int totalMethodsNum = 0;
        int totalPublicTestable = 0;


        int totalGetterMethods = 0;
        int totalsetterMethods = 0;
        int totalSingleBranchMethods = 0;
        int totalInsnFewMethods = 0;
        int totalInsn20FewMethods = 0;

        HashSet<MethodNode> bigMethodsSet = new HashSet<>();//set of methods bigger than 100

        for(Object o: classgraph.vertexSet()) {
            if(o instanceof ClassNode) {
                totalClassNum += 1;

                ClassNode classNode = (ClassNode) o;
                if(ClassNodeUtil.isClassNodeBuilderable(classNode)) {
                //if(isClassDirectlyTestable(classNode)) {
                    testableClassNum += 1;
                    for (Object m : classNode.methods) {

                        if (m instanceof MethodNode) {
                            totalMethodsNum += 1;
                            if (isMethodDirectlyTestable((MethodNode) m)) {
                                totalPublicTestable += 1;

                                MethodNode methodNode = (MethodNode) m;
                                if(methodNode.instructions.size()<10)
                                    totalInsnFewMethods += 1;
                                if(methodNode.instructions.size()<20)
                                    totalInsn20FewMethods += 1;

                                if(methodNode.instructions.size() > 100)
                                    bigMethodsSet.add(methodNode);

                                if(methodNode.name.startsWith("get"))
                                    totalGetterMethods += 1;

                                if(methodNode.name.startsWith("set"))
                                    totalsetterMethods += 1;

                                isMethodProcessingFile(methodNode);

                            }
                        }
                    }
                }

            }
        }


        System.out.println("There are " + totalClassNum + " classes, and "  + testableClassNum + " are testable");
        System.out.println("There are " + totalMethodsNum + " methods, and "  + totalPublicTestable + " are public testable");

        System.out.println("There are " + totalGetterMethods + " getter methods and" + totalsetterMethods + " setter");

        System.out.println("There are " + totalInsnFewMethods + " methods are less than 10 insns");
        System.out.println("There are " + totalInsn20FewMethods + " methods are less than 20 insns");
        System.out.println("\nThere are " + bigMethodsSet.size() + " methods are bigger than 100 insns");

        for(MethodNode m: bigMethodsSet)
            System.out.println("\t" + m.name);
    }


    public static HashSet<String> standardClasses = new HashSet<>();

    public static void analyzeMethodParameter(MethodNode methodNode) {
        Type methodType = Type.getMethodType(methodNode.desc);
        for(Type t: methodType.getArgumentTypes()) {
            if (t.getSort() == Type.OBJECT) {
                String className = t.getClassName();
                if (className.startsWith("java")) {
                    //System.out.println("Method: ")
                    standardClasses.add(className);
                }
            }
        }
    }

    /**
     * explore all the method parameters, and show all the involved standard libraries
     */
    public static void showInvolvedStandardLibary() {
        for(Object o: classgraph.vertexSet()) {
            if (o instanceof ClassNode) {
                ClassNode classNode = (ClassNode) o;
                if (ClassNodeUtil.isClassNodeBuilderable(classNode)) {
                    for (Object m : classNode.methods) {
                        if (m instanceof MethodNode) {
                            analyzeMethodParameter((MethodNode) m);
                        }
                    }
                }
            }
        }

        System.out.println("The standard library classes: ");
        for(String s: standardClasses)
            System.out.println("\t" + s);
    }
}
