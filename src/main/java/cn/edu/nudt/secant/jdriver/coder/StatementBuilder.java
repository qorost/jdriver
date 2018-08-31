package cn.edu.nudt.secant.jdriver.coder;

import org.objectweb.asm.Type;

/**
 * Created by huang on 7/28/18.
 */
public class StatementBuilder {

    public static String makeAssignStmt(String leftExpr, String rightExpr) {
        return leftExpr + " " + rightExpr + ";";
    }


    public static String makeConstructorStmt(String className, Type methodType, String prefix) {
        return null;
    }

    public static String makeNewVoidConstructorStmt(String className, String varname) {
        return className + " " + varname + " = new " + className + "();";
    }
}
