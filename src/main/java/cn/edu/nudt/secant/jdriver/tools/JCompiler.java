package cn.edu.nudt.secant.jdriver.tools;

import cn.edu.nudt.secant.jdriver.knowledgebase.StandardLibraryHelperBuilder;

import javax.lang.model.SourceVersion;
import javax.swing.text.html.Option;
import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Run compilation
 * Created by huang on 7/2/18.
 */
public class JCompiler {


    public static Object compileFile(HashSet<String> sourcefiles, HashSet<String> libraries) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        final DiagnosticCollector< JavaFileObject > diagnostics = new DiagnosticCollector<>();
        final StandardJavaFileManager manager = compiler.getStandardFileManager(
                diagnostics, null, null );

        try {
            //Arrays.asList(new File(Options.v().getRawInput()), new File(Options.v().getOutput()))
            ArrayList<File> dependencies = new ArrayList<>();

            for(String lib: libraries)
                dependencies.add(new File(lib));
            manager.setLocation(StandardLocation.CLASS_PATH, dependencies);
        } catch (IOException e) {
            System.err.println("Invalid bin directory");
            e.printStackTrace();
            return false;
        }

        //prepare source files
        ArrayList<File> sourceArray = new ArrayList<>();
        for(String input: sourcefiles) {
            sourceArray.add(new File(input));
        }


        final Iterable< ? extends JavaFileObject> sources =
                manager.getJavaFileObjectsFromFiles(sourceArray);//Arrays.asList( file )

        //execute compilation
        final JavaCompiler.CompilationTask task = compiler.getTask( null, manager, diagnostics,
                null, null, sources );
        task.call();

        try {
            task.wait();
        } catch (Exception e) {
            //fixme;
        }

        if(diagnostics.getDiagnostics().isEmpty()) {
            //System.out.println("\tFile " + testfile + " is compiled successfully.");
            return true;

        } else {
            return diagnostics;
        }
    }


    public static void printDiagnostics(DiagnosticCollector< JavaFileObject > diagnostics) {

        for (final Diagnostic<? extends JavaFileObject> diagnostic :
                diagnostics.getDiagnostics()) {

            String msg = String.format("\t%s, line %d in %s\n",
                    diagnostic.getMessage(null),
                    diagnostic.getLineNumber(),
                    diagnostic.getSource().getName());

            System.err.print(msg);
        }

    }


    public static boolean compileHelperFile(String helperfile) {

        System.out.println("compiling instance helper file " + helperfile);
        HashSet<String> libraries = new HashSet<>();

        if(Options.v().getRawInput() != null)
            libraries.add(Options.v().getRawInput());
        if(Options.v().getOutput() != null)
            libraries.add(Options.v().getOutput());

        HashSet<String> sourcefiles = new HashSet<>();

        if(!helperfile.startsWith("Standard")) {
            String standLibaryHelper = helperfile.replace("InstanceHelper.java", "StandardLibraryHelper.java");
            if (!Files.exists(Paths.get(standLibaryHelper))) {
                try {
                    StandardLibraryHelperBuilder.build(standLibaryHelper);
                } catch (Exception e) {
                    //
                    System.out.println("Fail to buld standard library helper file");
                    return false;
                }
            }
            sourcefiles.add(standLibaryHelper);
        }

        sourcefiles.add(helperfile);
        Object result = compileFile(sourcefiles, libraries);

        if(result instanceof Boolean) {
            System.out.println("File " + helperfile + " is compiled successfully");
            return (boolean) result;
        } else {
            System.out.println("Fail to compile instance helper file " + helperfile);
            DiagnosticCollector< JavaFileObject > diagnostics = (DiagnosticCollector< JavaFileObject >) result;
            if(Options.v().isVerbose()) {
                printDiagnostics(diagnostics);
            }
            return false;
        }
    }

    /**
     * build the test file
     * @param testfile path of the testfile
     * @return true if success
     */
    public static boolean compileTestFile(String testfile) {

        HashSet<String> libraries = new HashSet<>();
        libraries.add(Options.v().getRawInput());
        libraries.add(Options.v().getOutput());

        HashSet<String> sourcefiles = new HashSet<>();

        File file = new File(testfile);
        String dirname = file.getParent();
        String helpfile = dirname + "/Helper.java";
        String instancehelpfile = dirname + "/InstanceHelper.java";

        sourcefiles.add(testfile);
        sourcefiles.add(helpfile);
        //sourcefiles.add(instancehelpfile);

        Object result = compileFile(sourcefiles, libraries);

        if(result instanceof Boolean) {
            System.out.println("File " + testfile + " is compiled successfully");
            return (boolean) result;
        }
        else
        {
            DiagnosticCollector< JavaFileObject > diagnostics = (DiagnosticCollector< JavaFileObject >) result;

            if(diagnostics.getDiagnostics().isEmpty()) {
                System.out.println("\tFile " + testfile + " is compiled successfully.");
                return true;

            } else {
                System.err.println("\tFail to compile file " + testfile + ".");
                if(Options.v().isVerbose()) {
                    printDiagnostics(diagnostics);
                }
            }
            return false;
        }
    }


    public static void executeTestFile(String testfile) {

    }
}
