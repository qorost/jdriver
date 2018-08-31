package cn.edu.nudt.secant.jdriver.assembler.statement;

import cn.edu.nudt.secant.jdriver.tools.Debug;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Created by huang on 4/13/18.
 */
public class ExpressionBuilder {

    public static String makeCallExpr(String methodName, int paralength) {
        String expr =  methodName + "(";
        String tmp = "";
        if(paralength == 0) {
            tmp = ")";
        } else {
            int i = 0;
            for (; i < paralength - 1; i++) {
                tmp += methodName + "_" + i + ", ";
            }
            if (i >= 0)
                tmp += methodName + "_" + i;
            tmp += ")";
        }
        return expr + tmp;
    }

    public static String makeMethodCallExpr(String instanceName, String methodName, int paralength) {
        return instanceName + "." + makeCallExpr(methodName, paralength);
    }


    /**
    * Naming convention:
    * The cut instance is named "cut"
    * Other class instance is classname with camel convention,
    * The variable for the method, is named with classname and index
    * */
    public static String makeClassInstanceName(String name) {
        return name.toLowerCase().charAt(0)+name.substring(1,name.length());
    }
    public static String makeConstructorCallExpr(String methondName, int paralength) {
        if(methondName.contains("/"))
            methondName = methondName.replace("/",".");
        if(methondName.contains("."))
            methondName = methondName.substring(methondName.lastIndexOf(".") +1);
        return "new " + makeCallExpr(methondName, paralength);
    }

    /**
     * make an expr for class instance, e.g AClass cut
     * @param classname classname of the class, usually
     * @param isCut
     * @return
     */
    public static String makeClassDeclaration(String classname, boolean isCut) {
        if(classname.contains("/")) {
            classname = classname.replace("/", ".");
        }
        if(classname.contains(".")) {
            int index = classname.lastIndexOf('.') + 1;
            //classname = classname.split(".")[index];
            classname = classname.substring(index);

        }
        if(isCut)
            return classname + " cut";
        else
            return classname + " " + makeClassInstanceName(classname);
    }

    public static String makeStandLibraryClassInitialization(String classname) {
        return "";
    }
}
