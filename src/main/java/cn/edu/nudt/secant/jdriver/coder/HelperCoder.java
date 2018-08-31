package cn.edu.nudt.secant.jdriver.coder;

import org.objectweb.asm.Type;

/**
 * Created by huang on 7/18/18.
 */
public class HelperCoder {


    public static String makeClassName(String className) {
        if(className.contains("."))
            className = className.substring(className.lastIndexOf(".") + 1);
        if(className.contains("/"))
            className = className.substring(className.lastIndexOf("/") + 1);
        return className;
    }

    /**
     * make method declaration: public static String getString(String s)
     * @param types
     * @param returnTypeStr
     * @return
     */
    public static String makeDeclaration(Type[] types, String returnTypeStr) {
        returnTypeStr = makeClassName(returnTypeStr);
        String methodName = "get_" + returnTypeStr;

        String declaration = "public static " + returnTypeStr + " " + methodName + "(";
        for(int i=0; i < types.length; i++) {
            Type mt = types[i];
            declaration += makeClassName(mt.getClassName()) + " " + "arg" + i + ",";
        }
        if(declaration.endsWith(",")) {
            declaration += "))";
            declaration = declaration.replace(",))", ")");
        }
        else
            declaration += ")";
        //fixme
        //declaration += " throws Exception ";
        return declaration;
    }


    public static String makeDeclaration(Type[] types, Type returnType) {
        String returnTypeStr = returnType.getClassName();
        return makeDeclaration(types,returnTypeStr);
    }


    public static String makeNewInstanceStmt(Type[] types, String prefix) {
        String stmt = "";
        return stmt;
    }
}
