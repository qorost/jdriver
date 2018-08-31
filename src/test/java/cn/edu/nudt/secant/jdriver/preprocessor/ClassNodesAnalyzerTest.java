package cn.edu.nudt.secant.jdriver.preprocessor;

import cn.edu.nudt.secant.jdriver.JDriver;
import org.objectweb.asm.tree.ClassNode;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huang on 6/28/18.
 */
public class ClassNodesAnalyzerTest {

    public void startAnalyzing() {
        String[] args = new String[]{"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar"};
        //args = new String[]{"-i","./tests/a.jar"};
        //JDriver.main(args);
        JDriver.parseArgs(args);
        TypeTableBuilder.build();

        ClassNodesAnalyzer.analyze();
    }

    /**
     * show testable classes/methods statics
     */
    //@Test
    public void testTestable() {
        startAnalyzing();
        ClassNodesAnalyzer.showTestable();
    }

    @Test
    public void testMethodParameter() {
        startAnalyzing();
        ClassNodesAnalyzer.showInvolvedStandardLibary();
    }

    /**
     * This is to show the methods
     */
    public void testMethods() {
        startAnalyzing();

        ClassNodesAnalyzer.showMethods();
    }


    //@Test
    public void test() {
        String[] args = new String[]{"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar"};
        //args = new String[]{"-i","./tests/a.jar"};
        //JDriver.main(args);
        JDriver.parseArgs(args);
        TypeTableBuilder.build();

        ClassNodesAnalyzer.analyze();
        //ClassNodesAnalyzer.analyzev2();
        ClassNodesAnalyzer.showStatics();
        String msg = ClassNodesAnalyzer.isSuperClass
                ("org/apache/commons/imaging/formats/tiff/datareaders/ImageDataReader",
                "java/lang/Object")? "Yes, it is": "No, not";
        System.out.println(msg);

        ClassNodesAnalyzer.showStandardLibraries();


        String cls = "org/apache/commons/imaging/formats/tiff/photometricinterpreters/PhotometricInterpreter";
        Object o = ClassNodesAnalyzer.getClassNode(cls);
        if(o == null) {
            System.out.println("not found class: " + cls);
        }

        if(o instanceof ClassNode) {
            System.out.println("Found!");
        } else {
            System.out.println("not found class: " + cls);
        }
    }
}