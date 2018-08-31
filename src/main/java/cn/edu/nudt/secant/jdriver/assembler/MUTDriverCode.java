package cn.edu.nudt.secant.jdriver.assembler;

import cn.edu.nudt.secant.jdriver.assembler.jexceptions.DrivecodeException;
import cn.edu.nudt.secant.jdriver.assembler.jexceptions.MUTBuildPrimitiveInputsFailException;
import cn.edu.nudt.secant.jdriver.assembler.jexceptions.MUTDriverCodeException;
import cn.edu.nudt.secant.jdriver.assembler.jexceptions.MethodNotDirectTestableException;
import cn.edu.nudt.secant.jdriver.knowledgebase.HelperMethod;
import cn.edu.nudt.secant.jdriver.preprocessor.DependencyAnalyzer;
import cn.edu.nudt.secant.jdriver.tools.TypeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.PrintWriter;
import java.util.ArrayList;

import static cn.edu.nudt.secant.jdriver.assembler.statement.StatementBuilder.*;
import static cn.edu.nudt.secant.jdriver.assembler.value.ValueBuilder.PRMITIVE_BYTE_SIZE;

/**
 * Our final target is to convert the inputs of method in the sequences into an array of type array.\
 * Each item of the array stands for parameters for that method.
 * We recover the primitive typed values from the input with the fieldInputs which stored the type informaiton.
 *
 *
 * Created by huang on 3/29/18.
 */
public class MUTDriverCode extends DriverCode{

    public ClassNode classnode;
    public MethodNode methodnode;
    public ArrayList<Object> methodSequences;
    public DependencyAnalyzer danalyzer;

    public ArrayList<ArrayList<Type>> fieldInputs; // inputs for method sequences
    public ArrayList<Type> mutInputs;//inputs for this method
    public ArrayList<Object> foreignLibraries;

    public String declaration;
    public String splitStmts;
    public String end = "}";

    public MUTDriverCode(ClassNode classNode,
                         MethodNode methodNode,
                         DependencyAnalyzer dependencyAnalyzer,
                         ArrayList<Object> sequences) {
        super();
        classnode = classNode;
        methodnode = methodNode;
        danalyzer = dependencyAnalyzer;
        methodSequences = sequences;
        foreignLibraries = new ArrayList<>();
        foreignLibraries.add(classNode);
    }

    /**
     * Reassign Methods for the following situations:
     *     if nothing for fields,
     *     if there contains object in parameters
     *
     * @throws MUTDriverCodeException
     */
    public void refineMethods() throws MUTDriverCodeException {

    }

    /**
    * build driver code
    * */
    public void build() throws MUTDriverCodeException {

        buildDeclarationStmt();
        //1. assemble the primitive inputs
        boolean isOnlyFile = buildPrimitiveInputs();

        if(isOnlyFile) {
            //fixme, takes file as input, the method can process file directly.
            System.out.println("building for single string !!!");


        } else {
            //1.2 split the data array into input/fields according to the generated fieldInputs above
            buildInputSplitStatements();
            //2. build statements with the inputs
            buildBodyStmts();
        }
    }

    /**
     * depreciated
     * special made for String methods/File Related Methods
     * public static void test_foo(String in) {
     *     try {
     *
     *
     *     }
     *
     * }
     *
     * @throws DrivecodeException
     */
    public void buildFileMethods() throws DrivecodeException {
        try {


        } catch (Exception e) {
            throw new DrivecodeException("Fail to build driver code for method");
        }
    }


    //
    private static String DECLARATIONS= "public static void %s(String filename) ";
    public void buildDeclarationStmt() {
        //fixme, replace with one bytes array
        String methodName =  methodnode.name + "_test";
        declaration = String.format(DECLARATIONS, methodName);
    }

    /**
     * read bytes from string file, and
     * Split the input bytes into inputs/fields
     */
    public void buildInputSplitStatements() {
        int fsize = getFieldsInputSize();
        int msize = getMethodInputSize();
        int totalsize = fsize + msize;

        //open file and declare byte array, data


        String tmp = String.format("byte[] fields = Helper.splitFields(data, %s);",Integer.toString(fsize));
        stmts.add(0,tmp);
        tmp = String.format("byte[] inputs = Helper.splitInput(data, %s,%s);", Integer.toString(fsize),Integer.toString(totalsize));
        if(stmts.size() >= 1)
            stmts.add(1, tmp);

        tmp = "byte[] data = Helper.readBytes(filename);\n" +
                "        if(data == null) {\n" +
                "            System.out.println(\"Fail to read bytes from file!, quit!\");\n" +
                "            return;\n" +
                "        }";
        stmts.add(0, tmp);
    }

    /**
     * build primitive inputs, iterate over method sequences, add primitive field/method inputs to
     * fieldInputs/mutInputs separately.
     */
    public boolean buildPrimitiveInputs() throws MUTDriverCodeException {
        //fixme, add support for public primitive
        boolean isFileOperation = false;
        boolean nonPrimitiveExist = false;
        fieldInputs = new ArrayList<>();
        for(Object o: methodSequences) {
            if(o instanceof HelperMethod) {
                HelperMethod helperMethod = (HelperMethod)o;
                fieldInputs.add(buildPrimitiveInputs(helperMethod));
                if(helperMethod.isFileString)
                    isFileOperation = true;
            } else {
                ArrayList<Type> tmp = null;
                if(o instanceof MethodNode)
                    tmp = buildPrimitiveInputs((MethodNode)o);
                else if(o instanceof FieldNode)
                    tmp = buildPrimitiveInputs((FieldNode)o);
                fieldInputs.add(tmp);
                if(tmp.size() > 0)
                    nonPrimitiveExist = true;
            }
        }
        //for method under test
        //fixme what to do with non-primitive input parameter
        mutInputs = buildPrimitiveInputs(methodnode);



        //fixme, Test if the input is valid
        //String can't live with other types
        if(isFileOperation && nonPrimitiveExist) {
            //any other input is invalid
            throw new MUTBuildPrimitiveInputsFailException("Primitive Exists with String");
        }

        return isFileOperation;
    }


    /**
     * given a HelperMethod, build primitive inputs
     * @param m
     * @return
     */
    public ArrayList<Type> buildPrimitiveInputs(HelperMethod m) {
        Type methodType = Type.getMethodType(m.desc);
        return buildPrimitiveInputs(methodType);
    }

    /**
     * given a methodNode, make fieldInputs
     * @param m
     * @return
     */
    public ArrayList<Type> buildPrimitiveInputs(MethodNode m) {
        if(m != null) {
            Type methodType = TypeUtil.getMethodType(m);
            return buildPrimitiveInputs(methodType);
        }
        return null;
    }

    /**
     * given a method type, make fieldInputs
     * @param methodType
     * @return
     */
    public ArrayList<Type> buildPrimitiveInputs(Type methodType) {
        ArrayList<Type> methodTypes = new ArrayList<>();
        if(methodType != null) {
            Type[] argTypes = methodType.getArgumentTypes();
            for (Type t : argTypes) {
                methodTypes.add(t);
            }
            return methodTypes;
        }
        return null;
    }

    /**
     * given a fieldNode, make fieldInputs
     * @param f
     * @return
     */
    public ArrayList<Type> buildPrimitiveInputs(FieldNode f) {
        ArrayList<Type> methodTypes = new ArrayList<>();
        methodTypes.add(Type.getType(f.desc));
        return methodTypes;
    }

    /**
     * get the size of the types in the fieldInputs
     * @return
     */
    public int getFieldsInputSize() {
        int size = 0;
        for (ArrayList<Type> typeArrayList : fieldInputs)
            size += calcInputBytesSize(typeArrayList);
        return size;
    }

    public int getMethodInputSize() {
        Type method = Type.getMethodType(methodnode.desc);
        return calcInputBytesSize(method.getArgumentTypes());
    }

    /**
     * calculate input bytes size
     * @param typeArrayList
     * @return
     */
    public int calcInputBytesSize(ArrayList<Type> typeArrayList) {
        Type[] types = typeArrayList.toArray(new Type[typeArrayList.size()]);
        return calcInputBytesSize(types);
    }

    public int calcInputBytesSize(Type[] typeArrayList) {
        int rtn = 0;
        for (Type t: typeArrayList) {
            if(t.getSort() < Type.ARRAY)//what for complicated
                rtn += PRMITIVE_BYTE_SIZE[t.getSort()];
            //fixme what for other?

        }
        return rtn;
    }




    public int buildMethodRecoveryPrimitiveStmts(ArrayList<Type> typeArrayList, ClassNode classNode, MethodNode methodNode,
                                                 String inputname, int fieldPosition) {
        String name = TypeUtil.getMethodNodeName(classNode, methodNode);
        return buildMethodRecoveryPrimitiveStmts(typeArrayList, name, inputname, fieldPosition);
    }

    /**
     * build statements to recover values used in given method
     * @param typeArrayList  an array of types, the input types of given method
     * @param methodName the classname of the method
     * @param inputname specify the data is from "inputs" or "fields"
     * @param fieldPosition the position in the input bytes
     * @return the modified field position
     */
    public int buildMethodRecoveryPrimitiveStmts(ArrayList<Type> typeArrayList, String methodName,
                                           String inputname, int fieldPosition) {
        for(int j=0; j < typeArrayList.size(); j++) {
            //Test if t is primitive
            Type t = typeArrayList.get(j);
            if(t.getSort() >= Type.ARRAY)
                return -1;

            if(methodName.contains("/")) {
                methodName = methodName.substring(methodName.lastIndexOf("/") + 1);
            }

            //Make statement to declare variable for the jth input
            String varname = String.format("%s_%d", methodName, j);
            //fixme, if the method has been called before, rewrite is redefinition to true
            String[] tStmts = makePrmitiveRecoveryStmt(inputname, t, varname,fieldPosition, false);

            fieldPosition += PRMITIVE_BYTE_SIZE[t.getSort()];

            for(String stmt: tStmts)
                stmts.add(stmt);

        }
        return fieldPosition;
    }


    /**
     * This is to change values for public primitive fields, refer to "fieldnode" in method sequences
     * @param fieldNode
     * @param classname if this is static, classname set to the classname of the class, else set to the classname of your
     *                  instance, e.g "cut"
     * @param fieldPosition
     * @return field position
     */
    public int buildFieldRecoveryPrimitiveStmts(FieldNode fieldNode, String classname, int fieldPosition) {
        Type fieldType = Type.getType(fieldNode.desc);
        int sort = fieldType.getSort();

        String expr = String.format("Helper.get_%s(fields, %d);",PRIMITIVE_TYPE_NAME[sort],fieldPosition);

        //AClass.fieldname = get_type(fields, fieldposition);
        //stmt = classname + "." + fieldNode.classname + " = " + expr;
        String[] tStmts = makePrmitiveRecoveryStmt("fields", fieldType,"cut." + fieldNode.name,fieldPosition,true);
        //String[] tStmts = makeFieldPrmitiveRecoveryStmt(fieldType, fieldPosition);

        for(String stmt: tStmts)
            stmts.add(stmt);
        fieldPosition += PRMITIVE_BYTE_SIZE[sort];
        return fieldPosition;
    }


    /**
    * build statements
    * */
    public void buildBodyStmts() throws MUTDriverCodeException{
        //fields
        int fieldPosition = 0;
        String preparation[] = makePreparationforRecovery();
        for(String stmt: preparation)
            stmts.add(stmt);

        //for fields
        boolean isCutInitialized = false;
        for(int i=0; i< fieldInputs.size(); i++) {
            Object o = methodSequences.get(i);
            if(o instanceof MethodNode) {
                MethodNode mnode = (MethodNode) o;//a method in the method sequence
                if (mnode == null)//fixme
                    continue;
                if(!TypeUtil.isAccess(mnode.access, Opcodes.ACC_PUBLIC)) {
                    throw new MethodNotDirectTestableException("Method (" + mnode.name +") in Method Sequences is non-public");
                }

                ArrayList<Type> typeArrayList = fieldInputs.get(i);

                String methodName = mnode.name;
                if (methodName.contains("<init>"))
                    methodName = classnode.name;

                stmts.add("\n");
                String commentStmt = "//recovery parameters for " + methodName;
                stmts.add(commentStmt);
                int size = typeArrayList.size();
                fieldPosition = buildMethodRecoveryPrimitiveStmts(typeArrayList, methodName, "fields", fieldPosition);
                if(fieldPosition < 0)
                    throw new MUTDriverCodeException("Invalid type recovery");

                String stmt = makeMethodCallStmt(classnode, mnode, size,isCutInitialized);
                if(stmt != null) {
                    if(stmt.contains(" cut = "))
                        isCutInitialized = true;
                    stmts.add(stmt);
                }
                //check if the stmt is valid

//                if (i == 0 && !TypeUtil.isAccess(methodnode.access, Opcodes.ACC_STATIC)) {
//                    //this is constructor for cut
//                    stmts.add(makeCUTInstantiationStmt(classnode, methodName, size));
//                } else {
//                    stmts.add(makeMethodCallStmt(classnode, mnode, size));
//                }
            } else if (o instanceof FieldNode) {
                String classname = "cut";
                FieldNode fnode = (FieldNode) o;
                if(TypeUtil.isAccess(fnode.access, Opcodes.ACC_STATIC))
                    classname = classnode.name;
                fieldPosition = buildFieldRecoveryPrimitiveStmts(fnode, classname, fieldPosition);
            } else {
                //fixme, String
            }
        }

        //for method under test
        // add try
        stmts.add("\n");
        stmts.add("//calling the mut " + methodnode.name);
        stmts.add("position = 0;");//reset position

        //System.out.println("DEBUG: before adding recovery primitive");
        int rtn = buildMethodRecoveryPrimitiveStmts(mutInputs, methodnode.name, "inputs",0);
        if(rtn < 0) {
            throw new MUTDriverCodeException("improper argument in method under test");
        }
        //System.out.println("DEBUG: AFTER adding recovery primitive");

        stmts.add(TRY_START);
        String methodCallStmt = "    " + makeMethodCallStmt(classnode, methodnode, mutInputs.size(), isCutInitialized);
        if(TypeUtil.isAccess(methodnode.access, Opcodes.ACC_STATIC)) {
            String className = classnode.name.substring(classnode.name.lastIndexOf("/") + 1);
            methodCallStmt = methodCallStmt.replace("cut", className);
        }
        stmts.add(methodCallStmt);
        for(String stmt: TRY_END)
            stmts.add(stmt);
    }

    public void print() {
        System.out.println(declaration + "{");
        System.out.println(splitStmts);
        super.print();
        System.out.println(end);
    }

    public void write(PrintWriter writer) {
        writer.write("    " + declaration + "{\n");
        //writer.write(splitStmts);

        for(String stmt: stmts) {
            writer.write("        " + stmt + "\n");
        }
        writer.write("    }\n");
    }

    public static MUTDriverCode build(ClassNode classNode,
                                      MethodNode methodNode,                                       
                                      DependencyAnalyzer dependencyAnalyzer,
                                      ArrayList<Object> sequences) throws MUTDriverCodeException {
        MUTDriverCode mutDriverCode = new MUTDriverCode(classNode, methodNode, dependencyAnalyzer, sequences);
        mutDriverCode.build();
        //mutDriverCode.print();
        return mutDriverCode;
    }
}
