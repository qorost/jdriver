package cn.edu.nudt.secant.jdriver.preprocessor;

import cn.edu.nudt.secant.jdriver.JDriver;
import org.junit.Test;
import org.objectweb.asm.Type;

import static org.junit.Assert.*;

/**
 * Created by huang on 7/18/18.
 */
public class MethodArgumentAnalyzerTest {

    @Test
    public void testMethods() {
        TestHelper.buildTypeTable();
        //ClassNodesAnalyzer.analyze();
        MethodArgumentAnalyzer.analyze();
    }

}