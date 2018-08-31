package cn.edu.nudt.secant.jdriver.methodsequencer;

/**
 * If you found a method, but it's not corresponding to your criteria
 * In our situation, we want the found method to have only primitive types in input.
 * Created by huang on 6/28/18.
 */
public class NoProperMethodException extends  MSBuilderException {
    public NoProperMethodException(String msg) {super(msg);}
}
