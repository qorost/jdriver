package cn.edu.nudt.secant.jdriver.knowledgebase;

import cn.edu.nudt.secant.jdriver.coder.HelperCoder;
import org.objectweb.asm.Type;

/**
 * Created by huang on 7/18/18.
 */
public class HelperMethod {

    //public classname of the helper method
    public String classname;//java.io.String
    public String methodname;

    // for building import, default equals classname, if it is built in class, set impt to ""
    public Object impt;//could be String or HashSet

    //method description, for figuring out the argument types
    public String desc;

    //outuput of the method
    public String methodbody;

    //if this is a file classname, then set it to true
    public boolean isFileString = false;

    public HelperMethod(String classname, String desc, String methodbody) {
        init(classname, desc, methodbody);
    }

    public HelperMethod(String classname, String desc, boolean isFileString, String methodbody) {
        init(classname, desc, methodbody);
        this.isFileString = true;
    }

    private void init(String classname, String desc, String methodbody) {
        this.classname = classname;
        impt = classname;

        if(classname.endsWith("String"))//fixme support for unnecessary
            impt = null;
        this.desc = desc;
        this.methodbody = methodbody;


        String tmp = classname;
        if(classname.contains("."))
            tmp = classname.substring(classname.lastIndexOf(".") + 1);
        methodname = "get_" + tmp;
    }


    public static class StandardHelperMethod extends HelperMethod{

        public StandardHelperMethod(String name, String desc, String body){
            super(name, desc, body);
            buildMethodStmts();
        }

        public StandardHelperMethod(String name, String desc, boolean isFileString, String body){
            super(name, desc, isFileString, body);
            buildMethodStmts();
        }


        public void buildMethodStmts(){
            String tmp = methodbody;//body from the input
            methodbody = "\t";
            Type methodType = Type.getMethodType(desc);
            if(methodType != null) {
                String declaration = HelperCoder.makeDeclaration(methodType.getArgumentTypes(), classname);
                System.out.println("declaration of string is " + declaration);
                methodbody += declaration;
                methodbody += " throws Exception {\n\t\t";
                //fixme, automatically generate method methodbody
                methodbody += tmp;
                methodbody += "\n\t}\n";
            } else {
                System.err.println("Fail to get methodType");
                //throw new HelperCodeException("Fail to getMethodType for standard class: " + classname);
            }
        }
    }


}
