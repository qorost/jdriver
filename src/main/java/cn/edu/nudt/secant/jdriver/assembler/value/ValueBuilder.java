package cn.edu.nudt.secant.jdriver.assembler.value;

/**
 * Created by huang on 4/12/18.
 */
public class ValueBuilder {
    public static final int[] PRMITIVE_BYTE_SIZE = {
            0, //void
            Integer.SIZE/8, //boolean
            Character.SIZE/8, //char
            Byte.SIZE/8, //byte
            Short.SIZE/8, //short
            Integer.SIZE/8, //int
            Long.SIZE/8, //long
            Float.SIZE/8, //float
            Double.SIZE/8 //double
    };




}
