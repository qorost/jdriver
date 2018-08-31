package cn.edu.nudt.secant.jdriver.assembler;

import java.util.ArrayList;

/**
 * Created by huang on 3/29/18.
 */
public class DriverCode {
    public ArrayList<String> stmts;

    public DriverCode() {
        stmts = new ArrayList<>();
    }

    public void print() {
        for(String stmt: stmts) {
            System.out.println(stmt);
        }
    }

}
