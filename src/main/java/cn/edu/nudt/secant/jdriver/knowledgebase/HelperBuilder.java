package cn.edu.nudt.secant.jdriver.knowledgebase;

import cn.edu.nudt.secant.jdriver.assembler.HelperCode;
import cn.edu.nudt.secant.jdriver.tools.JCompiler;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by huang on 7/25/18.
 */
public class HelperBuilder {

    public HashSet<String> importStmts = new HashSet<>();
    public ArrayList<String> methodStmts = new ArrayList<>();


    //public void buildMethodStmts(HelperMethod m){}

    public void addImport(String impt) {
        if(impt.contains("$"))
            return;
        if(impt.startsWith("java"))
            return;
        importStmts.add(impt);
    }

    public void addImport(HelperMethod m) {
        if(m.impt instanceof String) {
            addImport((String) m.impt);
        }

        if(m.impt instanceof HashSet) {
            for(String s: (HashSet<String>) m.impt) {
                addImport(s);
            }
        }
    }

    public void assemble(HashSet<HelperMethod> methodHashSet ) {
        importStmts = new HashSet<>();
        methodStmts = new ArrayList<>();

        for(HelperMethod m: methodHashSet) {
            //add import statement
            addImport(m);
            methodStmts.add(m.methodbody);
        }

        //importStmts = new HashSet<>();

        importStmts.add("import java.lang.*;");
        importStmts.add("import java.io.*;");
        importStmts.add("import java.nio.*;");
        importStmts.add("import java.awt.*;");
        importStmts.add("import java.util.List;");
        importStmts.add("import java.util.*;");
        importStmts.add("import java.nio.charset.*;");
        importStmts.add("import java.awt.image.*;");
        importStmts.add("import java.awt.color.*;");

    }


    public void write(String filename, String helperClassName) throws Exception {
        String javaFileName = helperClassName + ".java";
        if(!filename.endsWith(javaFileName)) {
            filename += "/" + javaFileName;
            filename = filename.replace("//","/");
        }
        File file = new File(filename);
        file.getParentFile().mkdirs();
        PrintWriter writer = new PrintWriter(file, "UTF-8");

        for(String imports: importStmts) {
            if(imports.startsWith("import"))
                writer.write(imports + "\n");
            else
                writer.write("import " + imports + ";\n");
        }

        writer.write("\n\n");
        writer.write("public class " + helperClassName +"{\n");

        for(String method: methodStmts) {
            writer.write(method);
            writer.write("\n");
        }
        writer.write("}");
        writer.close();
        System.out.println("Helper file generated as: " + filename);
    }

    public boolean compile(String filename) {
        return false;
    }

}
