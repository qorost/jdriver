package cn.edu.nudt.secant.jdriver.assembler.jexceptions;

/**
 * Created by huang on 6/28/18.
 * This specifies the classnode is not testable, this happens when it is not public
 */
public class ClassNotDirectTestableException extends CUTInitException {
    private static final long serialVersionUID = -1L;

    public ClassNotDirectTestableException(final String msg) {
        super(msg);
    }
}
