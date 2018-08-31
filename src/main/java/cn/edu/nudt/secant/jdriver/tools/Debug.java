package cn.edu.nudt.secant.jdriver.tools;

/**
 * Created by huang on 4/11/18.
 */
public class Debug {
    private static final boolean DEBUG = false;
    private static final String NEWLINE = "\r\n";

    public static void debug(final String message) {
        if(DEBUG) {
            System.out.println(message);
        }
    }

    public static void debug() {
        if(DEBUG) {
            System.out.print(NEWLINE);
        }
    }

}
