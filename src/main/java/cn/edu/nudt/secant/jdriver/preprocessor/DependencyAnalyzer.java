package cn.edu.nudt.secant.jdriver.preprocessor;

import cn.edu.nudt.secant.jdriver.tools.Debug;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Dependency Analyzer takes in a CUT, and analyze how fields are accessed and modified.
 * Created by huang on 4/10/18.
 */
public class DependencyAnalyzer {

    public DirectedGraph<Object, DefaultEdge> accessGraph;
    public DirectedGraph<Object, DefaultEdge> modifyGraph;
    public ClassNode classnode;
    private HashMap<String, Object> map;

    public static int superclass = 0;
    public static int faildsuperclass = 0;
    public GlobalCallGraph gcfg;


    public DependencyAnalyzer(ClassNode classNode) {
        classnode = classNode;
        accessGraph = new DefaultDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);
        modifyGraph = new DefaultDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);

        map = new HashMap<>();
        classnode = classNode;

        for(Object o: classnode.methods) {
            MethodNode m = (MethodNode) o;
            String key = m.desc+m.name;
            if(map.containsKey(key))
                System.err.println("same key exists in constructing dependencyanalyzer for classnode: " + classnode
                        .name);
            map.put(key, m);
        }

        for(Object o: classnode.fields) {
            FieldNode f = (FieldNode) o;
            String key = f.name;
            if(map.containsKey(key))
                System.err.println("same key exists in constructing dependencyanalyzer for classnode: " + classnode
                        .name);
            map.put(key, f);
        }
    }

    public HashSet<FieldNode> getAccessSet(MethodNode methodNode) {
        return getAccessSet(methodNode, null);
    }

    public HashSet<FieldNode> getAccessSet(MethodNode methodNode, Set<MethodNode> transversedMethodNodeSet) {
        //fixme, there exist loop
        HashSet<FieldNode> fieldNodeSet = new HashSet<>();
        if(transversedMethodNodeSet == null)
            transversedMethodNodeSet = new HashSet<>();
        if(accessGraph.containsVertex(methodNode)) {
            Set<DefaultEdge> outgoingEdgeSet = accessGraph.outgoingEdgesOf(methodNode);
            for(DefaultEdge e : outgoingEdgeSet) {
                Object o = accessGraph.getEdgeTarget(e);
                if(o instanceof FieldNode)
                    fieldNodeSet.add((FieldNode) o);
                else if(o instanceof MethodNode){
                    MethodNode callee = (MethodNode) o;
                    if(transversedMethodNodeSet.contains(callee))
                        continue;
                    //System.out.println("The methodNode of callee is " + callee.classname);
                    transversedMethodNodeSet.add(callee);
                    if(callee != methodNode) {
                        fieldNodeSet.addAll(getAccessSet(callee, transversedMethodNodeSet));
                    } else
                        System.out.println("the callee is the same as the input methodNode (" + methodNode.name + ");");
                }
            }
        }
        return fieldNodeSet;
    }

    /**
    * given a methodNode, and a fieldNode,
    * return true, if the method modify the field inside its body.
    * otherwise, return false
    * */
    public boolean isMethodDirectModifier(MethodNode methodNode, FieldNode fieldNode) {
        return modifyGraph.getEdge(methodNode, fieldNode) != null;
    }


    /**
    * given a fieldnode, return the set that can directly modify it.
    * */
    public HashSet<MethodNode> getDirectModifySet(FieldNode fieldNode) {
        HashSet<MethodNode> methodNodes = new HashSet<>();
        if(modifyGraph.containsVertex(fieldNode)) {
            Set<DefaultEdge> outgoingEdgeSet = modifyGraph.outgoingEdgesOf(fieldNode);
            for(DefaultEdge e: outgoingEdgeSet) {
                Object o = modifyGraph.getEdgeTarget(e);
                methodNodes.add((MethodNode) o );
            }
        }
        return methodNodes;
    }

    /**
    * given an fieldnode, return the set of method nodes that modify its value
    * */
    public HashSet<MethodNode> getModifySet(FieldNode fieldNode) {
        HashSet<MethodNode> methodNodes = new HashSet<>();
        if(modifyGraph.containsVertex(fieldNode)) {
            Set<MethodNode> directModifySet = getDirectModifySet(fieldNode);
            methodNodes.addAll(directModifySet);
            for(MethodNode methodNode: directModifySet) {
                methodNodes.addAll(findCaller(methodNode));
            }
        }
        return methodNodes;
    }

    /**
    * given a methodNode, find the set of methods that calls the given method
    * */
    public HashSet<MethodNode> findCaller(MethodNode methodNode) {
        return findCaller(methodNode, null);
    }

    /**
    * given a methodnode, find all the callers from the modifying graph recursively
    * */
    public HashSet<MethodNode> findCaller(MethodNode methodNode, Set<MethodNode> transversedMethodNodeSet) {
        HashSet<MethodNode> methodNodes = new HashSet<>();
        if(transversedMethodNodeSet == null)
            transversedMethodNodeSet = new HashSet<>();
        if(modifyGraph.containsVertex(methodNode)) {
            Set<DefaultEdge> outgoingEdgeSet = modifyGraph.outgoingEdgesOf(methodNode);
            for(DefaultEdge e: outgoingEdgeSet) {
                Object o = modifyGraph.getEdgeTarget(e);
                if( o == methodNode)
                    continue;
                if(o instanceof MethodNode) {
                    MethodNode m = (MethodNode) o;
                    if(transversedMethodNodeSet.contains(o))
                        continue;
                    transversedMethodNodeSet.add(m);
                    methodNodes.addAll(findCaller(m, transversedMethodNodeSet));
                }
            }
        }
        return methodNodes;
    }


    public void build() {
        Iterator i = classnode.methods.iterator();
        while(i.hasNext()) {
            MethodNode mnode = (MethodNode) i.next();
            //processMethod(mnode);
            accessGraph.addVertex(mnode);
            modifyGraph.addVertex(mnode);
        }

        i = classnode.methods.iterator();
        while(i.hasNext()) {
            MethodNode mnode = (MethodNode) i.next();
            processMethod(mnode);
        }
    }

    public void processMethod(MethodNode mnode) {
        InsnList instrs = mnode.instructions;
        Iterator i = instrs.iterator();
        Debug.debug("Processing Method to build dependency Analyzer: " + mnode.desc + " " + mnode.name);
//        accessGraph.addVertex(mnode);
//        modifyGraph.addVertex(mnode);
        while(i.hasNext()) {
            AbstractInsnNode inode = (AbstractInsnNode) i.next();
            int type = inode.getType();
            int opcode = inode.getOpcode();
            switch (type) {
                case AbstractInsnNode.FIELD_INSN:
                    FieldInsnNode finode = (FieldInsnNode) inode;
                    //System.out.println("\tfield insn: " + finode.classname);
                    FieldNode fnode = null;
                    if(map.containsKey(finode.name)) {
                        fnode = (FieldNode) map.get(finode.name);
                    }
                    else {
                        if(finode.desc.contains(classnode.name)) {
                            System.err.println("\tError, fieldnode " + finode.name + " not found in the graph");
                        }
//                        System.err.println("\tThere is error in finding FieldNode: " + finode.classname
//                                + " desc: " + finode.desc + " this class is " + classnode.classname);
                        continue;
                    }

                    switch (opcode) {
                        case Opcodes.GETFIELD:
                        case Opcodes.GETSTATIC:
                            //this is access, method-field edge
                            //System.out.println("\taccessing " + fnode.classname + " in " + mnode.classname);
                            accessGraph.addVertex(fnode);
                            accessGraph.addEdge(mnode, fnode);
                            break;
                        case Opcodes.PUTFIELD:
                        case Opcodes.PUTSTATIC:
                            //this is modification, field-method edge
                            //System.out.println("\tmodifying " + fnode.classname + " in " + mnode.classname);
                            modifyGraph.addVertex(fnode);
                            modifyGraph.addEdge(fnode, mnode);
                            break;
                    }
                    break;
                case AbstractInsnNode.METHOD_INSN:
                    MethodInsnNode minode = (MethodInsnNode) inode;
                    Object calleenode = null;
                    String key = minode.desc + minode.name;
                    if(map.containsKey(key)) {
                        calleenode = map.get(key);
                        //Debug.debug("\tcalleenode = " + calleenode.desc + " " + calleenode.classname);
                        if(calleenode == mnode) {
                            Debug.debug("\t"+ minode.name +" : The caller is same as callee");
                            continue;
                        }
                        accessGraph.addEdge(mnode, calleenode);
                        modifyGraph.addEdge(calleenode, mnode);
                    }
                    else {
                        String callee = mnode.name;
                        map.put(key, callee);
                        accessGraph.addVertex(callee);
                        modifyGraph.addVertex(callee);
                        accessGraph.addEdge(mnode, callee);
                        modifyGraph.addEdge(callee, mnode);
                        //fixme, what is this is a foreign, subclassing
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
    * print summary for method
    * */
    public void printMethodSummary(MethodNode methodNode) {
        Set<FieldNode> fieldNodeSet = getAccessSet(methodNode);
        String msg = "Summary for ";
        msg += methodNode.name;
        msg += " : \n";
        if(fieldNodeSet.size() == 0) {
            msg += "\tIt doesn't access fields.";
        } else {
            msg += "\tAccessed Fields: ";
            Iterator fsiterator = fieldNodeSet.iterator();
            while (fsiterator.hasNext()) {
                FieldNode f = (FieldNode) fsiterator.next();
                msg += f.name;
                msg += ";\t";
            }
        }
        msg += "\n";
        System.out.println(msg);
    }

    /**
    * print summary for fields
    * */
    public void printFieldSummary(FieldNode fieldNode) {
        Set<MethodNode> methodNodeSet = getModifySet(fieldNode);

        String msg = "Summary for ";
        msg += fieldNode.name;
        msg += " : \n";
        if(methodNodeSet.size() == 0) {
            msg += "\tNo methods found to modify this field;";
        } else {
            msg += "\tModify Methods: ";
            for (MethodNode methodNode : methodNodeSet) {
                msg += methodNode.name;
                msg += ";\t";
            }
        }
        msg += "\n";
        System.out.println(msg);
    }

    public void printSummary() {
        System.out.println("The summary of classnode: " + classnode.name);
        for(Object methodNode: classnode.methods) {
            printMethodSummary((MethodNode) methodNode);
        }
        for(Object fieldNode: classnode.fields) {
            printFieldSummary((FieldNode) fieldNode);
        }
    }

    public static DependencyAnalyzer build(ClassNode classNode) {
        DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(classNode);
        dependencyAnalyzer.build();
        //dependencyAnalyzer.printSummary();
        return dependencyAnalyzer;
    }
}
