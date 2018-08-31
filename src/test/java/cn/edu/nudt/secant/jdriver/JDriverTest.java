package cn.edu.nudt.secant.jdriver;

import cn.edu.nudt.secant.jdriver.tools.ASMReader;
import cn.edu.nudt.secant.jdriver.tools.Options;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineParser;
import org.objectweb.asm.tree.ClassNode;

import java.io.InputStream;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * Created by huang on 4/1/18.
 */
public class JDriverTest {

    public ClassNode getClassNode() throws Exception {
        String[] args = {"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar"};
        Options options = Options.v();
        CmdLineParser parser = new CmdLineParser(options);
        parser.parseArgument(args);
        HashSet<String> classes = options.getInput();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String cls = "org/apache/commons/imaging/common/ImageBuilder.class";
        InputStream bytecode = classLoader.getResourceAsStream(cls);
        //System.out.println("bytecode = " + bytecode.toString());
        return ASMReader.getClassNode(bytecode);
    }

    @Test
    public void testsut() {
        String[] args = new String[]{"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar", "-o","./tests/common_output"};
        //args = new String[]{"-i","./tests/a.jar"};
        JDriver.main(args);
    }

    //@Test
    public void testsingleclass() {
        String cls = "org/apache/commons/imaging/icc/IccTagDataTypes$4.class";
        cls = "org/apache/commons/imaging/formats/pnm/PamFileInfo$TupleReader.class";
        cls = "org/apache/commons/imaging/formats/jpeg/JpegImageParser";

        String[] args = new String[]{"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar","-class",
                cls,"-o","./tests/JpegImageParser_output"};

        JDriver.main(args);
    }

    //@Test
    public void testMutSelector() {
        String[] args = new String[]{"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar"};
        //args = new String[]{"-i","./tests/a.jar"};
        JDriver.main(args);
    }


}