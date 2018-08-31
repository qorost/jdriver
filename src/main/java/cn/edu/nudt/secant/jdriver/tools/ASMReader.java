package cn.edu.nudt.secant.jdriver.tools;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * Created by huang on 3/30/18.
 */
public class ASMReader {
    public static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();


    /**
     * given a class InputStream, return a ClassNode
     * @param src
     * @return
     * @throws Exception
     */
    public static ClassNode getClassNode (InputStream src) throws Exception {
        ClassNode classNode=new ClassNode();
        ClassReader cr = new ClassReader(src);
        cr.accept(classNode, 0);
        return classNode;
    }


    /**
     * get classnode
     * @param cls the classname of the class , e.g. “AClass.class”
     * @return
     * @throws Exception
     */
    public static ClassNode getClassNode(String cls) throws Exception {
        if(!cls.endsWith(".class"))
            cls = cls + ".class";
        InputStream bytecode = classLoader.getResourceAsStream(cls);
        Debug.debug("\tget class node " + cls + ", cls has a length of : " + cls.length());
        return getClassNode(bytecode);
    }


    public static MethodNode getMethodNode(ClassNode classNode, String desc, String name) {
        if(classNode != null) {
            for(Object o: classNode.methods) {
                MethodNode m = (MethodNode) o;
                //fixme
                if(m.desc.equals(desc) && m.name.equals(name))
                    return m;
            }
        }
        return  null;
    }

    /**
     * get method node set by method classname
     * @param classNode ClassNode of the class
     * @param name classname of the method
     * @return a set of methods of with the classname
     */
    public static Set<MethodNode> getMethodNodes(ClassNode classNode, String name) {
        HashSet<MethodNode> methodNodeHashSet = new HashSet<>();
        if(classNode != null) {
            for(Object o: classNode.methods) {
                MethodNode m = (MethodNode) o;
                if(m.name.equals(name))
                    methodNodeHashSet.add(m);
            }
        }
        return  methodNodeHashSet;
    }

    public static MethodNode getMethodNode(String cls, String desc, String name) throws Exception {
        ClassNode classNode = getClassNode(cls);
        return getMethodNode(classNode, desc, name);
    }

    public static MethodNode getMethodNode(MethodInsnNode methodInsnNode) throws Exception{
        return getMethodNode(methodInsnNode.owner, methodInsnNode.desc, methodInsnNode.name);
    }

    public static ClassNode getSuperClassNode(ClassNode classNode) throws Exception {
        String cls = classNode.superName;
        Debug.debug("\tget super class: " + cls);
        if(TypeUtil.isStandardLibrary(cls))
            return null;
        else
            return getClassNode(classNode.superName + ".class");
    }

    public static int[] getNumberofAccessedFields(ClassNode classNode, MethodNode methodNode) {
        HashSet<String> fset = new HashSet<>();
        HashSet<String> aset = new HashSet<>();
        HashSet<String> mset = new HashSet<>();
        InsnList instrs = methodNode.instructions;
        Iterator i = instrs.iterator();
        while(i.hasNext()) {
            AbstractInsnNode inode = (AbstractInsnNode) i.next();
            int opcode = inode.getOpcode();
            int type = inode.getType();
            if(type == AbstractInsnNode.FIELD_INSN) {
                FieldInsnNode finode = (FieldInsnNode) inode;
                if(finode.owner.equals(classNode.name)) {
                    //
                    fset.add(finode.name);
                    switch (opcode) {
                        case Opcodes.GETFIELD:
                        case Opcodes.GETSTATIC:
                            aset.add(finode.name);
                            break;
                        case Opcodes.PUTFIELD:
                        case Opcodes.PUTSTATIC:
                            mset.add(finode.name);
                            break;
                    }
                }

            }
        }
        return new int[]{fset.size(),aset.size(),mset.size()};
        //return fset.size();
    }

    /**
     * calculate the number of branches in methodnode, currently, it is measured by the number of jmp instructions
     * @param classNode class node
     * @param methodNode method node
     * @return number of branches
     */
    public static int getNumberofBranches(ClassNode classNode, MethodNode methodNode) {
        int counter = 0;
        InsnList instrs = methodNode.instructions;
        Iterator i = instrs.iterator();
        while(i.hasNext()) {
            AbstractInsnNode inode = (AbstractInsnNode) i.next();
            int opcode = inode.getOpcode();
            int type = inode.getType();
            if(type == AbstractInsnNode.JUMP_INSN || type == AbstractInsnNode.TABLESWITCH_INSN) {
                counter += 1;
            }
        }
        return counter;
    }

}
