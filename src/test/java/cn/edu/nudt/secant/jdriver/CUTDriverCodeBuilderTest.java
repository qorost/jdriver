package cn.edu.nudt.secant.jdriver;

import org.junit.Test;

import static org.junit.Assert.*;

public class CUTDriverCodeBuilderTest {

    //@Test
    public void testCommons() {
        String[] args = new String[]{"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar", "-m", "c"};
        JDriver.parseArgs(args);
        JDriver.process();
    }

    //@Test
    public void testATest() {
        boolean simple = false;
        String cls = "org/apache/commons/imaging/color/ColorXyz.class";
        String jarname = "./tests/commons-imaging-1.0-SNAPSHOT.jar";
        cls = "org/apache/commons/imaging/formats/jpeg/JpegImageMetadata.class";
        String outputdir = "./tests/method_output/";

        if(true) {
            jarname = "./tests/a.jar";

            cls = "AClass.class";
        }
        String[] args = new String[]{"-i", jarname ,"-o",outputdir};
        //make type table
        JDriver.parseArgs(args);
        JDriver.preprocess();

        CUTDriverCodeBuilder.buildClass(cls);

    }

    @Test
    public void testSingleMethod() {
        boolean simple = false;

        String cls = "org/apache/commons/imaging/color/ColorXyz.class";
        cls = "org/apache/commons/imaging/formats/jpeg/JpegImageParser";
        String methodname = "getBufferedImage";
        String outputdir = "./tests/output/method_output/";
        String[] args = new String[]{"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar", "-class",cls, "-method",
                methodname, "-o", outputdir};

        //cls = "./tests/output/org/apache/commons/imaging/common/ImageBuilderc.class";

        if(simple) {
            args = new String[]{"-i", "./tests/a.jar", "-class","AClass.class","-method","foo","-o",outputdir};
            cls = "AClass";
            methodname = "foo";
        }

        JDriver.parseArgs(args);
        JDriver.preprocess();
        JDriver.processMethod();
    }

}