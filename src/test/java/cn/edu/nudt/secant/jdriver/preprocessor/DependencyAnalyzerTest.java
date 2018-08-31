package cn.edu.nudt.secant.jdriver.preprocessor;

import cn.edu.nudt.secant.jdriver.tools.ASMReader;
import cn.edu.nudt.secant.jdriver.tools.JarFileIO;
import cn.edu.nudt.secant.jdriver.tools.Options;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * Created by huang on 6/29/18.
 */
public class DependencyAnalyzerTest {


    /**
     * test for "There is error in finding MethodNode: ()Ljava/nio/ByteOrder; : getByteOrder own: org/apache/commons/imaging/formats/psd/PsdImageParser"
     */
    @Test
    public void tesDependencyAnalyzer () {

        String cls = "org/apache/commons/imaging/formats/psd/PsdImageParser.class";
        String input = "./tests/commons-imaging-1.0-SNAPSHOT.jar";
        HashSet<String> inputClasses = new HashSet<>();
        JarFileIO.extractJar(input, inputClasses);
        Options.addToClassPath(input);

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream bytecode = classLoader.getResourceAsStream(cls);
            ClassNode classNode = ASMReader.getClassNode(cls);

            DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(classNode);
            dependencyAnalyzer.build();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}