package cn.edu.nudt.secant.jdriver.tools;

import org.objectweb.asm.tree.ClassNode;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineParser;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by huang on 3/30/18.
 */
public class ASMReaderTest {



    @Test
    public void test() {
        try {
            //ASMReader.getClassNode("");
            Options options = Options.v();
            CmdLineParser parser = new CmdLineParser(options);
            //parser.parseArgument();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}