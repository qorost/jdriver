package cn.edu.nudt.secant.jdriver.methodsequencer;

public class InstanceFailureMSBuilderException extends MSBuilderException{

    public String classname;

    public InstanceFailureMSBuilderException(String msg, String name) {
        super(msg);
        classname = name;
    }
}
