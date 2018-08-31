package cn.edu.nudt.secant.jdriver.coder;

import org.objectweb.asm.Type;

/**
 * Created by huang on 7/28/18.
 */
public class ExpressionCoder {

    public static String makeVariableDeclarationExpr(String typeName, String varName) {
        return typeName + " " + varName;
    }

    public static String makeMethodCallExpr(Type methodType, String methodName, String prefix) {
        String output = methodName + "(";
        int argNum = methodType.getArgumentTypes().length;
        if(argNum == 0)
            output += ");";
        else {
            for(int i = 0; i < argNum; i++)
                output += prefix + i + ", ";
            output += ")";
            output = output.replace(", )" , ");");
        }
        return output;
    }


    public static String makeConstructorCallExpr(Type methodType, String className, String prefix) {
        return "new " + makeMethodCallExpr(methodType, className, prefix);
    }

    public static String makeInstanceMethodCallExpr(Type methodType, String instanceName, String methodName, String
            prefix) {
        return instanceName + makeMethodCallExpr(methodType, methodName, prefix);  
    }



}
