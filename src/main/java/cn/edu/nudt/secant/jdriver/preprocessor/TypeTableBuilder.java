package cn.edu.nudt.secant.jdriver.preprocessor;

import cn.edu.nudt.secant.jdriver.tools.ASMReader;
import cn.edu.nudt.secant.jdriver.tools.Options;
import cn.edu.nudt.secant.jdriver.tools.TypeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.InputStream;
import java.util.*;

/**
 * Created by huang on 4/9/18.
 */
public class TypeTableBuilder {


    public static Hashtable<Type, ClassNode> classTable = new Hashtable<>();
    public static Hashtable<Type, HashSet<MethodNode>> typeTable = new Hashtable<>();

    //some instances are created statically, e.g.,public static final BinaryConstant XMP_IDENTIFIER
    public static Hashtable<Type, HashSet<FieldNode>> typeFieldTable = new Hashtable<>();
    public static GlobalCallGraph globalCallGraph = GlobalCallGraph.v();
    public static HashMap<MethodNode, ClassNode> methodNodeClassNodeHashMap = new HashMap<>();

    public static HashMap<FieldNode, ClassNode> fieldNodeClassNodeHashMap = new HashMap<>();


    /**
     * add a methodnode to the type key
     * @param returnType the key of the typeTable
     * @param m methodnode that return the inputtype
     */
    public static void addType(Type returnType, MethodNode m) {
        if(returnType.getSort() == Type.OBJECT) {
            //this is a class
            if(typeTable.containsKey(returnType)) {
                typeTable.get(returnType).add(m);
            } else {
                HashSet<MethodNode> methodNodeSet = new HashSet<>();
                methodNodeSet.add(m);
                typeTable.put(returnType,methodNodeSet);
            }
        }
        //fixme, array types
    }

    public static int typefieldSize = 0;

    public static void addTypeField(Type returnType, FieldNode fieldNode) {

        int t = 0;
        if(returnType.getClassName().contains("JpegConstant"))
            t = 1;

        int tmp = typefieldSize;
        if(typeFieldTable.containsKey(returnType)) {
            typeFieldTable.get(returnType).add(fieldNode);
        } else {
            HashSet<FieldNode> fieldNodes = new HashSet<>();
            fieldNodes.add(fieldNode);
            typeFieldTable.put(returnType,fieldNodes);

            typefieldSize += 1;
        }

        if(tmp > typefieldSize)
            System.out.println("hello error");





    }

    /**
     * iterate over all the method in the given classnode, parse the method type information and add it to the type
     * table
     * @param classNode
     */
    public static void processClassNode(ClassNode classNode) {
        Type classType = Type.getObjectType(classNode.name);
        if(!classTable.containsKey(classType))
            classTable.put(classType, classNode);

        Iterator miterator = classNode.methods.iterator();
        while (miterator.hasNext()) {
            MethodNode m = (MethodNode) miterator.next();
            methodNodeClassNodeHashMap.put(m, classNode);
            Type mType = Type.getMethodType(m.desc);
            if(m.name.contains("<init>")) {
                addType(classType, m);
            } else {
                Type returnType = mType.getReturnType();
                addType(returnType, m);
            }
        }



        Iterator fieldIterator = classNode.fields.iterator();
        while(fieldIterator.hasNext()) {
            //
            FieldNode fnode = (FieldNode) fieldIterator.next();
            fieldNodeClassNodeHashMap.put(fnode, classNode);
            if(TypeUtil.isAccess(fnode.access, Opcodes.ACC_STATIC) && TypeUtil.isAccess(fnode.access, Opcodes.ACC_PUBLIC)) {
                Type fnodeType = Type.getType(fnode.desc);
                //if(TypeUtil.isSameType(classType, fnodeType))
                addTypeField(fnodeType, fnode);
                    //System.out.println("This is the same type");
                //}
            }

        }
    }

    /**
     * Iterate over the methods in the classNode, for each method, iterate over all the instructions, and retrieves
     * all the method calls, add nodes to the golbalcallgraph
     * @param classNode
     */
    public static void processClassNodeCg(ClassNode classNode) {
        for(Object o: classNode.methods ) {
            MethodNode methodNode = (MethodNode) o;
            Object methodNodeinCg;
            if(Options.v().isVerbose())
                System.out.println("Processing classNode " + classNode.name + " method: " + methodNode.name);
            if(!globalCallGraph.contains(classNode, methodNode)) {
                globalCallGraph.add(classNode, methodNode);
            }

            methodNodeinCg = globalCallGraph.get(classNode,methodNode);
            int fileOperations = 0;
            InsnList instrs = methodNode.instructions;
            Iterator i = instrs.iterator();

            boolean debug = true;

            while(i.hasNext()) {
                AbstractInsnNode inode = (AbstractInsnNode) i.next();
                int type = inode.getType();

                //fixme, find a file operation
                if (type == AbstractInsnNode.METHOD_INSN) {
                    MethodInsnNode minode = (MethodInsnNode) inode;
                    if(false)
                        System.out.println("Information for minode, owner: " + minode.owner + " desc: " + minode.desc + " classname " + minode.name);

                    if(TypeUtil.isFileOperations(minode.owner))
                        fileOperations += 1;

                    Object calleenode = null;
                    String key = TypeUtil.calcHashSum(minode.owner + minode.desc + minode.name);
                    if (globalCallGraph.contains(key)) {
                        calleenode = globalCallGraph.get(key);
                        globalCallGraph.addEdge(methodNodeinCg, calleenode);
                        //System.err.println("The methodNode " + methodNode.classname + " doesn't exist in the cfg");
                    } else {
                        //load the class
                        try {
                            ClassNode calleeClassNode = ASMReader.getClassNode(minode.owner);
                            calleenode = ASMReader.getMethodNode(calleeClassNode, minode.desc, minode.name);
                            //calleenode = (Object) ASMReader.getMethodNode(minode);
                            globalCallGraph.add(calleeClassNode,(MethodNode) calleenode);
                            globalCallGraph.addEdge(methodNodeinCg, calleenode);
                        } catch (Exception e) {
                            //System.err.println("Failed to load class " + minode.owner);
                            String data = minode.owner + minode.desc + minode.name;
                            globalCallGraph.add(key, data);
                            globalCallGraph.addEdge(methodNodeinCg,  data);
                            //e.printStackTrace();
                        }
                    }
                }
            }

            if(methodNodeinCg instanceof MethodItem) {
                ((MethodItem) methodNodeinCg).setFileoperations(fileOperations);
            }
        }
    }


    /**
     * build type table and global call graph
     */
    public static void build() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Set<String> inputClasses = Options.v().getInput();

        for(String cls: inputClasses) {
            InputStream bytecode = classLoader.getResourceAsStream(cls);
            try {
                ClassNode classNode = ASMReader.getClassNode(bytecode);
                processClassNode(classNode);
                ClassNodesAnalyzer.addClass(classNode);
                //MethodArgumentAnalyzer.addClass(classNode);
                //ClassNodesAnalyzer.addClassv2(classNode);
                if(Options.v().isRanking()) {
                    processClassNodeCg(classNode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //update methodItem with degree values
        GlobalCallGraph.v().updateValue();
    }



    public static void buildHelperCode() {
        //fixme


    }

    public static void printSummary() {
        int size = typeTable.size();
        System.out.println("There are " + size + " items.");
        Set<Type> keySet = typeTable.keySet();
        Iterator iterator = keySet.iterator();
        while(iterator.hasNext()) {
            Type t = (Type) iterator.next();
            HashSet<MethodNode> methodNodeHashSet = typeTable.get(t);
            String methods = "";
            for(MethodNode m: methodNodeHashSet) {
                methods += m.name;
                methods += "(";
                methods += m.desc;
                methods += ");; ";
            }
            System.out.println("\tType: " + t.getClassName() + " : " + methods);
        }
    }


    public static ClassNode getClassNode(Type t) {
        if(classTable.containsKey(t))
            return TypeTableBuilder.classTable.get(t);
        return null;
    }

    /**
     * find out the proper constructor to instantiate instance
     * @param type input a class type
     * @return a hashset containing all the methods that can generate the target instances
     */
    public static HashSet<MethodNode> getConstructor(Type type) {
        HashSet<MethodNode> methods = new HashSet<>();
        if(typeTable.containsKey(type)) {
            //fixme, subclassing, implementation
            //classTable.get(type);
            ClassNode classNode = classTable.get(type);
            if(classNode == null) {
                //System.err.println("no class constructor found for " + type.getClassName());
                return null;
            }
            if( classNode.superName != null) {
                //System.out.println("looking for superclass: " + classNode.superName);
                Type superclass = Type.getObjectType(classNode.superName);
                if(superclass != null) {
                    //System.out.println("Found Type superclass " + superclass.toString());
                } else {
                    //System.out.println("Super class not found");
                }
            }
            methods.addAll(typeTable.get(type));
            return methods;
        }
        else {
            //System.err.println("Fail to find proper constructor");
            return null;
        }
    }

    /**
     * return classnode the given methodnode belongs to
     * @param methodNode
     * @return
     */
    public static ClassNode getMethodClassNode(MethodNode methodNode) {
        if(methodNodeClassNodeHashMap.containsKey(methodNode))
            return methodNodeClassNodeHashMap.get(methodNode);
        else
            return null;
    }

    /**
     * return classnode the given fieldnode belongs to
     * @param fieldNode
     * @return
     */
    public static ClassNode getFieldClassNode(FieldNode fieldNode) {
        if(fieldNodeClassNodeHashMap.containsKey(fieldNode))
            return fieldNodeClassNodeHashMap.get(fieldNode);
        else
            return null;
    }


    public static void showClassFieldStatics() {
        for(Type t: typeFieldTable.keySet()) {
            System.out.println("Type: " + t.getClassName());
            if(t.getClassName().contains("JpegConstant"))
                System.out.println("hello debug");
            HashSet<FieldNode> fieldNodes = typeFieldTable.get(t);
            for(FieldNode f: fieldNodes)
                System.out.println("\tField " + f.name);
        }

        int totalsize = typeFieldTable.keySet().size();
        System.out.println("There are " + totalsize + " items in Type Field Table");

    }


    public static void showClassTypeStatics() {
        Set<Type> types = classTable.keySet();

        int number_of_able_to_generate_class = 0;
        int total_methods = 0;

        int total_able_methods = 0;
        int total_only_primitive_methods = 0;

        System.out.println("The summary for sut: "+ Options.v().getRawInput());
        String msg;
        int numberofmethods = 0;
        for(Type t: types) {
            msg = "\tClass( " + t.getClassName() + "): ";
            HashSet<MethodNode> methodNodes =typeTable.get(t);
            numberofmethods = 0;
            if(methodNodes == null) {
                System.err.println("null for class: " + t.getClassName());
                continue;
            }
            for(MethodNode m: methodNodes) {
                Type methodType = Type.getMethodType(m.desc);
                if(TypeUtil.isAbletoGenerate(methodType))
                    numberofmethods += 1;
                if(TypeUtil.getObjectNumberInInputParameters(methodType) == 0)
                    total_only_primitive_methods += 1;
            }

            total_methods += methodNodes.size();
            total_able_methods += numberofmethods;
            if(numberofmethods > 0) {
                number_of_able_to_generate_class += 1;
                msg += "can be instantiated by " + numberofmethods + " methods!";
            } else {
                msg += "can't be instantiated!";
            }
            System.out.println(msg);
        }

        String summary = String.format("\n%d out of %d are able to generate instances, %d out of %d are pure " +
                        "primitive(total methods: %d)",
                number_of_able_to_generate_class, types.size(), total_only_primitive_methods, total_able_methods, total_methods);
        System.out.println(summary);
    }


    public static void showClassTable() {
        Set<Type> types = classTable.keySet();
        for(Type t: types) {
            ClassNode node = classTable.get(t);
            String msg = String.format("Type: %s", t.toString());
            System.out.println(msg);
        }
    }

    public static void showClass(String classname) {
        Set<Type> types = classTable.keySet();
        for(Type t: types) {
            if(t.toString().contains(classname)) {
                System.out.println("Found class " + t.toString());
                ClassNode c = classTable.get(t);
            }
        }
    }
}
