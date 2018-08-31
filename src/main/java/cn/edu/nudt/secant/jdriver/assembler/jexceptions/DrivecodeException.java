package cn.edu.nudt.secant.jdriver.assembler.jexceptions;

/**
 * Created by huang on 4/11/18.
 */
public class DrivecodeException extends Exception {
    private static final long serialVersionUID = -1L;

    private String reason = "";

    public DrivecodeException(final String msg) {
        super(msg);
        reason = msg;
    }

    public DrivecodeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public String getReason() {return reason;}

}
