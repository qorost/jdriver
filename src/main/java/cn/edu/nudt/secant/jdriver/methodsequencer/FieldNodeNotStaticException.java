package cn.edu.nudt.secant.jdriver.methodsequencer;

/**
 * Created by huang on 7/5/18.
 * This indicates performing buildStatic on NonStatic field
 */
public class FieldNodeNotStaticException extends MSBuilderException {

    public FieldNodeNotStaticException(String msg) {
        super(msg);
    }
}
