package cn.edu.nudt.secant.jdriver.knowledgebase;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huang on 7/18/18.
 */
public class StandardLibraryHelperBuilderTest {

    @Test
    public void testStandardHelperBuilding() {
        StandardLibraryHelperBuilder builder = new StandardLibraryHelperBuilder();
        builder.assemble();
        try {
            builder.write("./tests/");
            builder.compile("./tests/StandardLibraryHelper.java");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}