package cn.edu.nudt.secant.jdriver.assembler.statement;

import cn.edu.nudt.secant.jdriver.assembler.jexceptions.CUTFailMUTDriveCodeException;
import cn.edu.nudt.secant.jdriver.assembler.jexceptions.MUTDriverCodeException;
import cn.edu.nudt.secant.jdriver.assembler.value.ValueBuilder;
import cn.edu.nudt.secant.jdriver.tools.Options;
import cn.edu.nudt.secant.jdriver.tools.TypeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;

import static cn.edu.nudt.secant.jdriver.assembler.statement.ExpressionBuilder.makeClassDeclaration;
import static cn.edu.nudt.secant.jdriver.assembler.statement.ExpressionBuilder.makeConstructorCallExpr;
import static cn.edu.nudt.secant.jdriver.assembler.statement.ExpressionBuilder.makeMethodCallExpr;
import static cn.edu.nudt.secant.jdriver.assembler.value.ValueBuilder.PRMITIVE_BYTE_SIZE;

/**
 * Created by huang on 4/12/18.
 */
public class StatementBuilder {

    public static final String[] BYTEBUFFER_METHODS = {
            "",
            "(ByteBuffer.wrap(tmp).get() != 0)",//bool
            "ByteBuffer.wrap(tmp).getChar()", //char
            "ByteBuffer.wrap(tmp).get()", //byte
            "ByteBuffer.wrap(tmp).getShort()", //short
            "ByteBuffer.wrap(tmp).getInt()", //int
            "ByteBuffer.wrap(tmp).getLong()", //long
            "ByteBuffer.wrap(tmp).getFloat()", //float
            "ByteBuffer.wrap(tmp).getDouble()", //double
    };

    public static final String[] PRIMITIVE_TYPE_NAME = {
            "",
            "boolean",
            "char",
            "byte",
            "short",
            "int",
            "long",
            "float",
            "double"
    };

    public static final String TRY_START = "try{";
    public static final String[] TRY_END = {
            "} catch (Exception e) {",
            "    e.printStackTrace();",
            "}"
    };




    /**
    * declare two temporary variables,
    * position to record the where the pointer in ByteBuffer is,
    * tmp, is to store the bytes;
    * */
    public static String[] makePreparationforRecovery() {
        String[] prepare = {"int position = 0;",
                "byte[] tmp;"};
        return prepare;
    }

    /**
    * make primitive recovery statement,
    * e.g.
    * tmp = Arrays.copyOfRange(inputs, position, position + 4 );
	* position += 4;
    * typename varname = ByteBuffer.wrap(tmp).getInt();
    * */
    public static String[] makePrmitiveRecoveryStmt(String input, Type t, String varname, int position, boolean isredefinition) {
        int typeSort = t.getSort();
        if(typeSort >= Type.ARRAY)
            return null;
        int size = ValueBuilder.PRMITIVE_BYTE_SIZE[typeSort];

        String stmt2 = String. format("position += %d;", size);
        String stmt3;

        /** call helper function to recover primitive value*/
        if(Options.v().isHelperAlone()) {
            String methodname = "Helper.get_" + PRIMITIVE_TYPE_NAME[typeSort];
            if(isredefinition)
                stmt3 = String.format("%s = %s(%s, position);", varname, methodname,input);
            else
                stmt3 = String.format("%s %s = %s(%s, position);", t.getClassName(), varname, methodname,input);
            return new String[]{stmt3,stmt2};
        } else {
            String stmt1 = String.format("tmp = Arrays.copyOfRange(%s, %d, %d);", input, position, position+size);

            if(isredefinition)
                stmt3 = String.format("%s = %s;", varname, BYTEBUFFER_METHODS[typeSort]);
            else
                stmt3 = String.format("%s %s = %s;", t.getClassName(), varname, BYTEBUFFER_METHODS[typeSort]);
            return new String[]{stmt1, stmt2, stmt3};
        }
    }


    /**
     * make primitive recovery statement for field node
     * @param t
     * @param position
     * @return
     */
    public static String[] makeFieldPrmitiveRecoveryStmt(Type t, int position) {
        int typeSort = t.getSort();
        if(typeSort >= Type.ARRAY)
            return null;
        int size = ValueBuilder.PRMITIVE_BYTE_SIZE[typeSort];


        String stmt1 = String. format("position += %d;", size);

        String stmt = "";
        String expr = String.format("Helper.get_%s(fields, %d);",PRIMITIVE_TYPE_NAME[typeSort],position);
        //AClass.fieldname = get_type(fields, fieldposition);

        //stmt = classname + "." + fieldNode.classname + " = " + expr;

        String stmt2 = String. format("position += %d;", size);
        return null;
    }

    public static String makeAssignStmt(String leftExpr, String rightExpr) {
        return leftExpr + " = " + rightExpr + ";";
    }

    /*
    * delcare a cut with constructor
    * */
    public static String makeCUTInstantiationStmt(ClassNode classNode ,String methodName, int paralength) {
        String leftExpr = makeClassDeclaration(classNode.name, true);
        String rightExpr;
        if(methodName.contains("<init>")) {
            methodName = classNode.name.replace("/", ".");
            rightExpr = makeConstructorCallExpr(methodName, paralength);
        } else {
            rightExpr = "";
        }
        return makeAssignStmt(leftExpr, rightExpr);
    }


    public static String makeMethodCallStmt(ClassNode classNode, MethodNode methodNode, int size, boolean isCutInitialized) throws MUTDriverCodeException {
        String name = TypeUtil.getMethodNodeName(classNode, methodNode);
        String className = TypeUtil.getClassNodeName(classNode);
        if(TypeUtil.isAccess(methodNode.access, Opcodes.ACC_STATIC)) {
            //static method
            return makeMethodCallStmt(className, name, size);
        }
        if(methodNode.name.contains("<init>"))
            return makeCUTInstantiationStmt(classNode,methodNode.name,size);
        if(isCutInitialized)
            return makeMethodCallStmt("cut", name, size);

        throw new CUTFailMUTDriveCodeException("Invoking method before cut initialized");
        //return null;
    }

    /**
     * make a method call statement, e.g. given a method "foo", generated "cut.foo(foo_1, foo2....)"
     * @param name
     * @param size
     * @return
     */
    public static String makeMethodCallStmt(String prefix, String name, int size) {
        return makeMethodCallExpr(prefix, name, size) + ";";
    }

    public static String makeImportStmt(ClassNode classNode) {
        return "import " + classNode.name.replace("/", ".") + ";";
    }

    public static String makeImportStmt(String classname) {
        if(!classname.contains("."))
            return null;
        return "import " + classname + ";";
    }

    public static void addTryBlock(ArrayList<String> stmts) {
        stmts.add(0, TRY_START);
        for(String stmt: TRY_END) {
            stmts.add(stmt);
        }
    }

    public static String addClassDeclaration(String name) {
        return String.format("Class %s {", name);
    }

    //The statement involved for standard library

}
