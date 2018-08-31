package cn.edu.nudt.secant.jdriver;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huang on 6/14/18.
 */
public class MutSelectorTest {

    @Test
    public void test() {
        String[] args = new String[]{"-i","./tests/commons-imaging-1.0-SNAPSHOT.jar", "-m", "c"};

        //args = new String[]{"-i","./tests/a.jar"};
        JDriver.parseArgs(args);
        JDriver.preprocess();
        MutSelector selector = MutSelector.v();

        //selector.show();

    }

}