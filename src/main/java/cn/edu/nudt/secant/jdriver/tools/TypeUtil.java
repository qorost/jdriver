package cn.edu.nudt.secant.jdriver.tools;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.Type;

import java.security.MessageDigest;

/**
 * Created by huang on 4/1/18.
 */
public class TypeUtil {


    public static String getMethodNodeName(ClassNode classNode, MethodNode methodNode) {
        if(methodNode.name.contains("<init>"))
            return getClassNodeName(classNode);
        return methodNode.name;
    }

    public static String getClassNodeName(ClassNode classNode) {
        String name = classNode.name;
        if(name.contains("/"))
            name = name.replace("/", ".");
        if(name.contains("."))
            name = name.substring(name.lastIndexOf(".") + 1);
        return name;
    }


    public static String getMethodName(Type type) {
        return type.toString();
    }

    public static boolean isFieldModifiable(FieldNode f) {
        if(f.access == Opcodes.ACC_PUBLIC) {
            //not const
            return true;
        }
        return  false;
    }

    public static boolean isAccess(int access, int attribute) {
        return (access & attribute) == attribute;
    }

    public static boolean isAccessAbstract(int access) {
        return isAccess(access, Opcodes.ACC_ABSTRACT);
    }

    public static boolean isFileOperations(String owner) {
        boolean result =  owner.startsWith("java/io/") || owner.contains("/nio/");
        return result;
    }

    /**
     * detect if this method is related to file operations
     * @param owner
     * @param name
     * @return
     */
    public static boolean isFileOperations(String owner, String name) {
        boolean result =  owner.startsWith("java/io/") || owner.contains("/nio/");
        return result;
    }

    public static boolean isPrimitiveType(Type type) {
        switch (type.getSort()) {
            case Type.ARRAY:
            case Type.METHOD:
            case Type.OBJECT:
            case Type.VOID:
                return false;
            default:
                return true;
        }
    }

    public static String getTypeStr(Type type) {
        switch (type.getSort()) {
            case Type.OBJECT:
                return "Class: " + type.getClassName().toString();
            case Type.METHOD:
                return "Method: " + type.getDescriptor();
            case Type.ARRAY:
                return "Array: " + type.toString();
            case Type.VOID:
                return "VOID: " ;
            default:
                return "Primitive: " + type.getClassName();
        }
    }


    public static String getTypeClassName(Type t) {
        if(t.getSort() == Type.OBJECT) {
            String className = t.getClassName();
            return className.substring(className.lastIndexOf(".") + 1);
        }
        return null;
    }


    public static boolean isSameType(Type atype, Type btype) {
        return atype == btype;

//        String aname = atype.getClassName();
//        String bname = btype.getClassName();
//        if(aname.equals(bname))
//            return true;
//        return false;
    }

    /**
     * decide if the method is able to generate by the judging the input type
     * @param mtype
     * @return
     */
    public static boolean isAbletoGenerate(Type mtype) {
        Type[] inputs = mtype.getArgumentTypes();
        for (Type t: inputs) {
            if(!TypeUtil.isPrimitiveType(t)) {
                //fixme
                if(t.getClassName().endsWith("String"))
                    continue;
                if(t.getClassName().endsWith("File"))
                    continue;
                else
                    return false;
            }
        }
        return true;
    }

    public static int getObjectNumberInInputParameters(Type mtype) {
        Type[] inputs = mtype.getArgumentTypes();
        int tmpNum = 0;
        for (Type t: inputs) {
            if(!TypeUtil.isPrimitiveType(t))
                tmpNum += 1;
        }
        return tmpNum;
    }

    public static int getPrimitiveNumberInInputParameters(Type mtype) {
        return mtype.getArgumentTypes().length - getObjectNumberInInputParameters(mtype);
    }

    public static Type getClassType(ClassNode classNode) {
        return Type.getObjectType(classNode.name);
    }

    /**
     * get class Type by string
     * @param desc string, e.g. "org/apache/commons/imaging/formats/jpeg/JpegImageMetadata"
     * @return
     */
    public static Type getClassType(String desc) {
        return Type.getObjectType(desc);
    }


    /**
     * get class Type by string, with forName method
     * @param desc
     * @return
     * @throws Exception
     */
    public static Type getClassTypewithForName(String desc) throws  Exception{
        String classpath = desc.replace("/",".");
        Class c = Class.forName(classpath);
        return Type.getType(c);
    }

    public static Type getMethodType(MethodNode methodNode) {
        return Type.getMethodType(methodNode.desc);
    }



    public static boolean isConstructor(MethodNode m) {
        return m.name.contains("<init>");
    }

    public static boolean isStandardLibrary(Type t) {
        return isStandardLibrary(t.getClassName());
    }

    public static boolean isStandardLibrary(String s) {
        if(s == null)
            return false;
        if(s.contains("."))
            s = s.replace(".","/");
        return s.contains("java/");
    }

    public static String calcHashSum(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(input.getBytes());
            return new String(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
