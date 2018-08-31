package cn.edu.nudt.secant.jdriver.methodsequencer;

import cn.edu.nudt.secant.jdriver.assembler.jexceptions.DrivecodeException;
import cn.edu.nudt.secant.jdriver.assembler.jexceptions.MUTDriverCodeException;

/**
 * Created by huang on 4/11/18.
 * This indicates exception occurs when building method sequences
 */
public class MSBuilderException extends MUTDriverCodeException {

    public MSBuilderException(final String msg) {
        super(msg);
    }

}
