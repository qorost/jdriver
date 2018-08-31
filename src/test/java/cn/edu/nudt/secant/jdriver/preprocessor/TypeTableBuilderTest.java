package cn.edu.nudt.secant.jdriver.preprocessor;

import cn.edu.nudt.secant.jdriver.JDriver;
import org.junit.Test;
import org.objectweb.asm.Type;

import static org.junit.Assert.*;

/**
 * Created by huang on 6/15/18.
 */
public class TypeTableBuilderTest {

    @Test
    public void testConstructor() {
        String[] args = new String[]{"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar"};
        //args = new String[]{"-i","./tests/a.jar"};
        //JDriver.main(args);
        JDriver.parseArgs(args);
        TypeTableBuilder.build();

        String classpath = "org/apache/commons/imaging/formats/jpeg/JpegImageMetadata";
        Type t= Type.getObjectType(classpath);
        System.out.println("Finding type for " + t.toString());
        TypeTableBuilder.getConstructor(t);
        //TypeTableBuilder.showClass(classpath);
        // TypeTableBuilder.showClassTable();

        TypeTableBuilder.showClassTypeStatics();
        TypeTableBuilder.showClassFieldStatics();
    }

    //@Test
    public void testSingleClass() {
        String[] args = new String[]{"-i","./tests/InstanceTests/instance.jar"};
        JDriver.parseArgs(args);
        TypeTableBuilder.build();
    }

}