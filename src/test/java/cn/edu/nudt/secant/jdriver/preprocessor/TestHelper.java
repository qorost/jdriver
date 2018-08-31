package cn.edu.nudt.secant.jdriver.preprocessor;

import cn.edu.nudt.secant.jdriver.JDriver;
import org.junit.Test;
import org.objectweb.asm.Type;

/**
 * Created by huang on 7/18/18.
 */
public class TestHelper {

    public static void buildTypeTable() {
        String[] args = new String[]{"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar"};
        JDriver.parseArgs(args);
        TypeTableBuilder.build();

    }
}
