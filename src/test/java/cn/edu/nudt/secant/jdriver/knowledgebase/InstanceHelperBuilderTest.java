package cn.edu.nudt.secant.jdriver.knowledgebase;

import cn.edu.nudt.secant.jdriver.JDriver;
import cn.edu.nudt.secant.jdriver.preprocessor.TypeTableBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huang on 7/24/18.
 */
public class InstanceHelperBuilderTest {

    @Test
    public void test() {
        //InstanceHelperBuilder.build();
        String[] args = new String[]{"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar", "-v"};
        JDriver.parseArgs(args);
        TypeTableBuilder.build();


        InstanceHelperBuilder builder = new InstanceHelperBuilder();
        builder.assemble();
        try {
            StandardLibraryHelperBuilder.build("./tests/StandardLibraryHelper.java");
            builder.write("./tests/");
            builder.compile("./tests/InstanceHelper.java");
        } catch (Exception e) {
            e.printStackTrace();
        }

        InstanceHelperBuilder.showUnsupported();
    }
}