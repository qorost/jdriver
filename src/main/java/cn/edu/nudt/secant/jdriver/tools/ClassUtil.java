package cn.edu.nudt.secant.jdriver.tools;

/**
 * Created by huang on 6/29/18.
 */
public class ClassUtil {

    public static final String[] JAVA_STANDARD_LIBRARY = {"java/lang", "java/util", "java/io", "java/math",
            "java/net", "javax/swing"};


    public static boolean isStandandLibrary(String classname) {
        if(classname.startsWith("java/")) return true;
        return false;
    }
}
