package cn.edu.nudt.secant.jdriver.tools;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.objectweb.asm.tree.FieldNode;

import java.io.InputStream;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Created by huang on 4/1/18.
 */
public class OptionsTest {



    @Test
    public void test() {

        String[] args = {"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar"};
        Options options = Options.v();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
            //System.out.println("input = " + options.getInput());
            Iterator iterator = options.getInput().iterator();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            while (iterator.hasNext()) {
                String tmp = iterator.next().toString();
                System.out.println(tmp);
                try {
                    InputStream bytecode = classLoader.getResourceAsStream(tmp);

                    ClassNode classNode = new ClassNode();
                    ClassReader cr;
                    try {
                        cr = new ClassReader(bytecode);
                        cr.accept(classNode, 0);
                        Iterator it = classNode.fields.iterator();
                        while(it.hasNext()) {
                            FieldNode fnode = (FieldNode) it.next();
                            System.out.println("\tThe node, desc:" + fnode.desc + ", Type:" + Type.getType(fnode.desc)
                                    .getClassName() + ", Name:" +
                                    fnode.name + ", class:" + fnode.getClass().toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    //ASMReader.getClassNode(tmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        } catch (CmdLineException e) {
            parser.printUsage(System.err);
        }
    }

}