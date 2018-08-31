package cn.edu.nudt.secant.jdriver.assembler.statement;

/**
 * This class is to make expression to generate standard class instances.
 */
public class StandardLibraryExpressBuilder {

    public static String[] STANDARD_CLASSES = {
            "java.io.PrintWriter",
            "java.io.OutputStream",
            "java.lang.Throwable",
            "java.awt.color.ICC_Profile",
            "java.awt.Rectangle",
            "java.io.RandomAccessFile",
            "java.awt.image.BufferedImage",
            "java.util.Date",
            "java.lang.String[]",
            "java.awt.color.ColorSpace",
            "java.io.File",
            "java.nio.ByteOrder",
            "java.util.List",
            "java.util.Calendar",
            "java.awt.image.ColorModel",
            "java.io.ByteArrayInputStream",
            "java.util.Map",
            "java.lang.Object",
            "java.lang.StringBuilder",
            "java.lang.String",
            "java.io.InputStream",
    };





    /**
     * given a classname, test if it is in the string;
     * @param classname
     * @return the classname in the STATIC_STRING
     */
    public static String includeClass(String classname) {
        for(String s: STANDARD_CLASSES) {
            if(s.contains(classname))
                return s;
        }
        return null;
    }



}
