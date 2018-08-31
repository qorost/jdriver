package cn.edu.nudt.secant.jdriver.scheduler;

import cn.edu.nudt.secant.jdriver.knowledgebase.HelperCodeException;
import cn.edu.nudt.secant.jdriver.preprocessor.TypeTableBuilder;
import cn.edu.nudt.secant.jdriver.tools.TypeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Set;

/**
 * Created by huang on 7/29/18.
 */
public class ClassNodeUtil {

    public static boolean isSimpleConstructor(MethodNode methodNode) {
        if(methodNode.name.contains("<init>")) {
            Type tmp = Type.getMethodType(methodNode.desc);
            if(tmp != null) {
                if(tmp.getArgumentTypes().length == 0){
                    return true;
                }
            }
        }
        return false;
    }


    public static MethodNode getSimpleConstructor(ClassNode classNode) {

        for(Object m: classNode.methods) {
            if(m instanceof MethodNode) {
                MethodNode mnode = (MethodNode) m;
                //fixme, for other input parameters, and methods
                if(isSimpleConstructor(mnode)) {
                    return  mnode;
                }
            }
        }
        return null;
    }

    /**
     * duplicate for ClassNodeAnalyzer.isClassDirectlyTestable()
     * @param classNode
     * @return
     */
    public static boolean isClassNodeBuilderable(ClassNode classNode) {
        if(!TypeUtil.isAccess(classNode.access, Opcodes.ACC_PUBLIC)) {
            //System.out.println("Skipping private class: " + classNode.name);
            return false;
        }

        //skip abstract class
        if(TypeUtil.isAccess(classNode.access, Opcodes.ACC_ABSTRACT)) {
            //System.out.println("Skipping private class: " + classNode.name);
            return false;
        }

//        if(TypeUtil.isAccess(classNode.access, Opcodes.ACC_FINAL)) {
//            //e.g., JavaConstant
//            //System.out.println("Skipping final class: " + classNode.name);
//            return false;
//        }


        //fixme, exclude exceptions classes
        if(classNode.superName != null) {
            if(classNode.superName.contains("Exception")) {
                //System.out.println("Skipping Exception class: " + classNode.name);
                return false;
            }
        }
        //skip inner class
        if(classNode.name.contains("$")) {
            //System.out.println("Skipping inside class: " + classNode.name);
            return false;
        }


        //<MethodNode> methodNodeSet = TypeTableBuilder.getConstructor(Type.getType(classNode.));

        return true;
    }
}
