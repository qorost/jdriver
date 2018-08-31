package cn.edu.nudt.secant.jdriver.knowledgebase;

import cn.edu.nudt.secant.jdriver.tools.JCompiler;
import cn.edu.nudt.secant.jdriver.tools.Options;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * This build stmts for standard Libraries
 * Created by huang on 7/18/18.
 */
public class StandardLibraryHelperBuilder extends HelperBuilder {

    public StandardLibraryHelperBuilder() {
        super();
    }

    public void assemble() {
        HashSet<HelperMethod> methods = new HashSet<>();
        for(HelperMethod.StandardHelperMethod m: StandardLibraryClasses.methodItems)
            methods.add(m);
        super.assemble(methods);

        //reset imports
//        importStmts = new HashSet<>();
//
//        importStmts.add("import java.lang.*;");
//        importStmts.add("import java.io.*;");
//        importStmts.add("import java.nio.*;");
//        importStmts.add("import java.awt.*;");
//        importStmts.add("import java.awt.*;");


    }

    public void write(String filename) throws Exception {
        super.write(filename, "StandardLibraryHelper");
    }

//    public void write(String filename) throws Exception {
//        if(!filename.endsWith("StandardLibraryHelper.java")) {
//            filename += "/StandardLibraryHelper.java";
//            filename = filename.replace("//","/");
//        }
//        File file = new File(filename);
//        file.getParentFile().mkdirs();
//        PrintWriter writer = new PrintWriter(file, "UTF-8");
//
//        for(String imports: importStmts) {
//            writer.write("import " + imports + ";\n");
//        }
//
//        writer.write("\n\n");
//        writer.write("public class StandardLibraryHelper{\n");
//
//        for(String method: methodStmts) {
//            writer.write(method);
//            writer.write("\n");
//        }
//        writer.write("}");
//        writer.close();
//    }


    public static void build(String filename) throws Exception {
        StandardLibraryHelperBuilder builder = new StandardLibraryHelperBuilder();
        builder.assemble();
        if(filename == null) {
            filename = Options.v().getOutput() + "/StandardLibraryHelper.java";
            filename = filename.replace("//","/");
        }
        builder.write(filename);
    }

    public boolean compile(String filename) {
        return JCompiler.compileHelperFile(filename);
    }
}
