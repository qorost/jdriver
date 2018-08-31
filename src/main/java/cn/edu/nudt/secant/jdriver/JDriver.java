package cn.edu.nudt.secant.jdriver;

import cn.edu.nudt.secant.jdriver.knowledgebase.InstanceHelperBuilder;
import cn.edu.nudt.secant.jdriver.knowledgebase.InstanceHelperMethod;
import cn.edu.nudt.secant.jdriver.knowledgebase.StandardLibraryHelperBuilder;
import cn.edu.nudt.secant.jdriver.preprocessor.ClassNodesAnalyzer;
import cn.edu.nudt.secant.jdriver.preprocessor.GlobalCallGraph;
import cn.edu.nudt.secant.jdriver.preprocessor.TypeTableBuilder;
import cn.edu.nudt.secant.jdriver.tools.JLogger;
import cn.edu.nudt.secant.jdriver.tools.Options;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import sun.rmi.runtime.Log;

import java.util.Iterator;
import java.util.Set;

import static cn.edu.nudt.secant.jdriver.CUTDriverCodeBuilder.buildClass;
import static cn.edu.nudt.secant.jdriver.CUTDriverCodeBuilder.buildMethod;

/**
 * Created by huang on 3/29/18.
 */
public class JDriver {

    public static boolean DEBUG = false;

    public static ClassLoader classLoader;

    public static int totalClasses = 0;//number of all the classes under test
    public static int nonTestableClasses = 0; // number of testable classes, filtered by ClassNodeAnalyzer.isClassDirectlyTestable
    public static int totalMethods = 0; //number of all the methods in TestableClasses classes
    public static int totalPublicMethods = 0;//number of all public  testable methods in TestableClasses
    public static int totalFailedMethods = 0;//number of failed

    public static int generatedCut = 0;
    public static int failedCut = 0;
    public static int passedCut = 0;



    /**
     * build type table, and global call graph
     */
    public static void preprocess() {
        //preprocess, build type table, global call graph,
        TypeTableBuilder.build();
        //ClassNodesAnalyzer.analyzev2();
        ClassNodesAnalyzer.analyze();
        try {
            StandardLibraryHelperBuilder.build(null);
            InstanceHelperBuilder.build(null);
        } catch (Exception e) {
            System.err.println("Building helper file error!");

        }
        //ClassNodesAnalyzer.showStatics();
        if(DEBUG)
            GlobalCallGraph.v().showGraph();
        //TypeTableBuilder.printSummary();
        if(DEBUG)
            MutSelector.process();
    }


    /**
     * test single method alone
     */
    public static void processMethod() {
        String classname = Options.v().getClassName();
        String methodname = Options.v().getMethodName();
        String desc = Options.v().getDesc();
        if(classname == null) {
            System.err.println("please specify classname by -class");
            return;
        }
        buildMethod(classname, methodname, desc);
    }


    /**
     * testing single classs alone
     */
    public static void processClass() {
        String classname = Options.v().getClassName();
        if(classname == null) {
            System.err.println("please specify classname by -class");
            return;
        }

        preprocess();

        BuildResult result = buildClass(classname);
        if(result == null) {
            System.err.println("\tFailed to build class " + classname);
            return;
        }

        if(result.isTestable) {
            printClassSummary(result);
        } else {
            System.err.println("\tThe input class " + classname + " is not testable!");
        }
    }

    public static void printClassSummary(BuildResult buildResult) {
        String msg = String.format("\n\nSummary for testing class %s:\nTotal Methods: %d\nTotal public methods:%d\n" +
                        "Failed test: %d\n", Options.v().getClassName(), buildResult.totalMethod, buildResult.publicNum,
                buildResult.failedNum);
        if(buildResult.passTest)
            msg += "The generated test file is compiled successfully!";
        else
            msg += "Fail to compile test file: ";
        System.out.println(msg);
    }

    /**
     * testing the sut,
     */
    public static void process() {
        preprocess();
        //
        Set<String> inputClasses = Options.v().getInput();
        if(DEBUG) {
            String cls = "org/apache/commons/imaging/color/ColorXyz.class";
            cls = "org/apache/commons/imaging/formats/jpeg/JpegImageMetadata.class";
            cls = "org/apache/commons/imaging/formats/jpeg/JpegImageParser.class";
            //files fails
            buildClass(cls);
            return;
        }

        totalClasses = inputClasses.size();
        Iterator iterator = inputClasses.iterator();
        while(iterator.hasNext()) {
            String cls = (String) iterator.next();
        //for(String cls: inputClasses) {
            String msg = "Generating driver code for " + cls;
            System.out.println("\n" + msg);
            JLogger.logInfo(msg);
            JLogger.logMethodInfo(msg);

            BuildResult result = buildClass(cls);

            if(result == null) {
                msg = "Failed to build class " + cls;
                System.out.println("\t" + msg);
                JLogger.logInfo(msg);
                continue;
            }

            if(result.isTestable) {
                totalMethods += result.totalMethod;
                totalPublicMethods += result.publicNum;
                totalFailedMethods += result.failedNum;
                if(result.cutGenerated) {
                    msg = "Driver code is built for " + cls;
                    generatedCut += 1;
                    if (!result.passTest) {
                        failedCut += 1;
                        msg += " : Fail compiling!";
                    }
                    else {
                        passedCut += 1;
                        msg += " : Pass compiling!";
                    }
                    JLogger.logInfo(msg);
                }
            } else {
                nonTestableClasses += 1;
                msg = "Class " + cls + " is not testable!";
                System.out.println("\t" + msg);
                JLogger.logInfo(msg);
            }
            //break;
        }
        printSummary();
    }

    public static void printSummary() {
        String jarname = Options.v().getRawInput();
        String msg = String.format("\nSummary for testing %s: \n" +
                "Total class under test: %d, Non Testable %d.\n" +
                "Total methods: %d\n" +
                "Total public methods: %d\n" +
                "Failed Methods: %d\n" +
                "Generated Test class: %d\n" +
                "CUT compiling: (Passed: %d, Failed: %d)\n", jarname, totalClasses,nonTestableClasses,
                totalMethods, totalPublicMethods, totalFailedMethods,generatedCut,passedCut,failedCut);
        System.out.println(msg);
    }



    public static boolean parseArgs(String[] args) {
        //globalTypeGraph = new TypeGraph(); //fixme
        Options options = Options.v();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
            return true;
        } catch (CmdLineException e) {
            String msg = "";
            for(String s: args)
                msg += s + " ";
            System.err.println("Failed to parse args: " + msg);
            parser.printUsage(System.err);
            return false;
        }
    }

    public static void main(String[] args) {
        classLoader = Thread.currentThread().getContextClassLoader();
        if(parseArgs(args)){
            JLogger.configure();
            if(Options.v().getMethodName() != null)
                processMethod();
            else if(Options.v().getClassName() != null)
                processClass();
            else
                process();
        }
    }
}
