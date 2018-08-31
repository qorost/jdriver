package cn.edu.nudt.secant.jdriver.assembler;

import cn.edu.nudt.secant.jdriver.tools.Options;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import static cn.edu.nudt.secant.jdriver.assembler.HelperCode.HELPERMETHODS;
import static cn.edu.nudt.secant.jdriver.assembler.HelperCode.IMPORTS;
import static cn.edu.nudt.secant.jdriver.assembler.statement.StatementBuilder.makeImportStmt;

/**
 * Created by huang on 3/29/18.
 */
public class CUTDriverCode extends DriverCode {


    public ArrayList<MUTDriverCode> mutDriverCodeArrayList;
    public ArrayList<String> importStmts;
    public ArrayList<String> mainStmts;
    public String classStart;
    public final String classEnd = "}";
    public ClassNode cut;
    private String testclasname = "";
    private String outputfilename = "";

    public CUTDriverCode(ClassNode classNode) {
        super();
        mutDriverCodeArrayList = new ArrayList<>();
        importStmts = new ArrayList<>();
        mainStmts = new ArrayList<>();
        cut = classNode;

        String classname = "";

        if(classNode.name.contains("/"))
            classname = classNode.name.substring(classNode.name.lastIndexOf("/")+1);
        else
            classname = classNode.name;

        if(Options.v().getMethodName() == null)
            testclasname = classname + "Test";
        else
            testclasname = classname + "SingleTest";
        classStart = "public class " + testclasname + " {";
    }


    /**
     * add mutdrivercode
     * @param mutDriverCode method under test
     */
    public void addMut(MUTDriverCode mutDriverCode) {
        mutDriverCodeArrayList.add(mutDriverCode);
    }


    public String getOuptfile() {
        return outputfilename;
    }

    /**
     * write the cut code to java file
     * @param dirname driver code directory
     * @throws Exception
     */
    public String toFile(String dirname) throws IOException{

        String filename = dirname;
        if(!dirname.endsWith("/"))
            filename += "/";
        filename += testclasname + ".java";
        outputfilename = filename;

        File file = new File(filename);
        file.getParentFile().mkdirs();

        PrintWriter writer = new PrintWriter(file, "UTF-8");

        //fixme, add time stamp
        String timeStamp = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy ").format(new Date());
        String classcomment = String.format("/**\n" +
                " * Test classs generated for %s.\n" +
                " * Created on %s\n" +
                " */\n", testclasname, timeStamp);
        writer.write(classcomment);

        //writer.print(IMPORTS);
        for(String stmt: importStmts) {
            writer.write(stmt + "\n");
        }

        writer.print("//Driver code for Class " + cut.name + ";\n");
        writer.write(classStart + "\n");

        //define mut
        for(MUTDriverCode mutDriverCode: mutDriverCodeArrayList) {
            mutDriverCode.write(writer);
        }

        //main
        for(String stmt: mainStmts) {
            writer.write(stmt + "\n");
        }

        writer.write(classEnd + "\n");
        writer.close();

        return filename;
    }


    /**
     * add subStmts to the total stmts
     * @param subStmts
     */
    public void addStmtArray(ArrayList<String> subStmts) {
        for(String stmt: subStmts)
            stmts.add(stmt);
    }

    public void addImportStmtSet(Set<String> stmtSet) {
        for(String stmt: stmtSet)
            stmts.add(stmt);
    }


    /**
     * test if the given class has been already defined in file heads
     * @param classname
     * @return
     */
    public boolean isImportNecessary(String classname) {
        if (classname.contains("/"))
            classname = classname.replace("/", ".");

        //ignore "AClass.class"
        if (!classname.contains(".")) {
            return false;
        }
        //exclude it
        for(String stmt: importStmts) {
            if(stmt.contains(classname))
                return false;
        }
        return true;
    }

    /**
    * make import statements with the classNodes used in the statements
    * */
    public void makeImportStmts() {
        for(String stmt: IMPORTS)
            importStmts.add(stmt);
        for(MUTDriverCode mut: mutDriverCodeArrayList) {
            for(Object o: mut.foreignLibraries) {
                //fixme, remove "AClass" without package
                if(o instanceof ClassNode) {
                    ClassNode classNode = (ClassNode) o;

                    if (isImportNecessary(classNode.name))
                        importStmts.add(makeImportStmt(classNode));
                } else if( o instanceof String) {
                    String tmp = makeImportStmt((String) o);

                    if(tmp != null) {
                        if(isImportNecessary(tmp))
                            importStmts.add(tmp);
                    }
                    else
                        System.err.println("Error building import statement for " + (String) o);
                }
            }
        }
    }

    //fixme asseble exception?
    public void assemble() {
        makeImportStmts();
        stmts.addAll(0, importStmts);

        stmts.add(classStart);

        //add mut
        for(MUTDriverCode mut: mutDriverCodeArrayList) {
            stmts.add(mut.declaration);
            addStmtArray(mut.stmts);
            stmts.add(mut.end);
        }


        boolean isSingle = mutDriverCodeArrayList.size()==1? true: false;
        if(isSingle) {
            System.out.println("\tAssembling: this is a single driver code");
            makeSingleMainStmt();
        }
        else {
            System.out.println("\tAssembling: There are " + mutDriverCodeArrayList.size() + " items in the " +
                    "MutDriverCode list;");
            makeMainStmt();
        }
        stmts.add(classEnd);
    }


    public void makeSingleMainStmt() {
        //fixme, make main statement for single method status

        String methodname = mutDriverCodeArrayList.get(0).methodnode.name + "_test";
        String mainbody = String.format("    public static void main(String[] args) {\n" +
                "        if(args.length == 1) {\n" +
                "            %s(args[0]);\n" +
                "        }\n" +
                "    }",  methodname );
        mainStmts.add(mainbody);
    }


    public void makeMainStmt() {
        mainStmts = new ArrayList<>();

        //fixme
        String mainbody = String.format("\n    public static void main(String[] args) {\n" +
                "        Method[] methods = %s.class.getDeclaredMethods();\n" +
                "        Helper.processargs(args, methods);\n" +
                "    }", testclasname);

        if(!Options.v().isHelperAlone())
            mainStmts.add(HELPERMETHODS);
        else {
            //replace
            mainbody.replace("processargs", "Helper.processargs");
        }
        mainStmts.add(mainbody);
    }


    //fixme, delete this before release
    /**
     * The following methods are for generating template, igore them
     * @param args
     */
    /**
     * This is for debugging driver code entrance, should not be used directly
     * @param args
     * @param methods
     */
    public static void processargs(String[] args, Method[] methods) {
        if(args.length > 2) {
            int i = Integer.valueOf(args[0]);
            if(i < 0)
                return;

            Path path = Paths.get(args[1]);
            try {
                byte[] data = Files.readAllBytes(path);
                if(i < methods.length) {
                    Method m = methods[i];
                    m.invoke(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] splitFields(byte[] data, int end) {
        if(end < data.length)
            return Arrays.copyOfRange(data, 0, end);
        else
            return null;
    }

    public static byte[] splitInput(byte[] data, int start, int end) {
        if(start < data.length && (start+end) < data.length)
            return Arrays.copyOfRange(data, start,end - start);
        else
            return null;
    }



    public static void main(String[] args) {
        Class cut = CUTDriverCode.class;
        Method[] methods = cut.getMethods();
        processargs(args, methods);
    }

    public static void mainsingle(String[] args) {
        if(args.length == 1) {
            //main(args[0]);
        }
    }

    public static void mainsinglefile(String[] args) {
        if(args.length == 1) {
            try {
                //foo_test(args[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
