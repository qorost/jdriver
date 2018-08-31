package cn.edu.nudt.secant.jdriver.knowledgebase;

import cn.edu.nudt.secant.jdriver.scheduler.ClassNodeUtil;
import cn.edu.nudt.secant.jdriver.preprocessor.TypeTableBuilder;
import cn.edu.nudt.secant.jdriver.tools.TypeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import static cn.edu.nudt.secant.jdriver.assembler.HelperCode.makeDesc;
import static cn.edu.nudt.secant.jdriver.coder.StatementBuilder.makeNewVoidConstructorStmt;

/**
 * Created by huang on 7/19/18.
 */
public class InstanceHelperMethod extends HelperMethod {



    public static boolean DEBUG = true;
    public Type instanceType;

    public MethodNode methodNode;
    public FieldNode fieldNode;

    public ClassNode classNode;
    public String typeName;
    //public String desc;//interface description
    public ArrayList<Object> methods;//invovled methods, by default MethodNode
    public ArrayList<String> stmts;
    public ArrayList<Type> methodArguments;//store the types of input as an array.



    public void init(Type t) throws Exception{
        instanceType = t;
        typeName = instanceType.getClassName();
        typeName = typeName.substring(typeName.lastIndexOf(".") + 1);

        desc = "";
        //fixme, check if it belows to sut
        stmts = new ArrayList<>();
        methods = new ArrayList<>();
        methodArguments = new ArrayList<>();

        ClassNode typeClassNode = TypeTableBuilder.classTable.get(instanceType);
        //test if the classNode is buildable
        if(!ClassNodeUtil.isClassNodeBuilderable(typeClassNode))
            throw new HelperCodeException("Type: " + typeClassNode.name + " is not able to build");

        addImport(classNode.name);//add import for the method/field
        addImport(typeClassNode.name);//add import for the instanceType
    }

    public InstanceHelperMethod(Type t, FieldNode f) throws Exception {
        super(t.getClassName(), "", "");

        methodname = "get_" + typeName;
        if(f!=null)
            methodname += f.name;
        else
            throw new HelperCodeException("Invalid fieldNode for initializing InstanceHelperMethod");

        classNode = TypeTableBuilder.getFieldClassNode(f);
        init(t);

        if(!TypeUtil.isAccess(f.access, Opcodes.ACC_PUBLIC))
            throw new HelperCodeException("The method: " + f.name + " is not public");
        buildField();

    }

    /**
     * It accepts an method to build the given type t.
     * @param t the target type
     * @param m the method that construct m
     * @throws Exception
     */
    public InstanceHelperMethod(Type t, MethodNode m, int i) throws Exception {

        super(t.getClassName(), "", "");
        methodNode = m;
        classNode = TypeTableBuilder.getMethodClassNode(methodNode);

        init(t);

        methodname = "get_" + typeName;
        if(i > 0)
            methodname += i;

        if(DEBUG)
            System.out.println("Making type " + instanceType.getClassName() + " with method " + m.name + "(" + classNode.name + ")");


        //test if the method is public
        if(!TypeUtil.isAccess(m.access, Opcodes.ACC_PUBLIC))
            throw new HelperCodeException("The method: " + methodNode.name + " is not public");
        //Test if it is ok to build
        //if(TypeUtil.isAbletoGenerate(Type.getMethodType(m.desc))) {
        build();
    }

    public void addImport(String classNodeName) {
        if(classNodeName.contains("/"))
            classNodeName = classNodeName.replace("/",".");
        if(impt == null) {
            HashSet<String> imptSet = new HashSet<>();
            impt = imptSet;
        } else if(impt instanceof String) {
            String tmp = (String) impt;
            HashSet<String> imptSet = new HashSet<>();
            imptSet.add(classNodeName);
            imptSet.add(tmp);
            impt = imptSet;
        } else if(impt instanceof HashSet) {
            HashSet<String> imptSet = (HashSet<String>) impt;
            imptSet.add(classNodeName);
        }
    }

    public void addImport(HelperMethod m) {
        if(m.impt instanceof String) {
            addImport((String) m.impt);
        }

        if(m.impt instanceof HashSet) {
            for(String s: (HashSet<String>) m.impt) {
                addImport(s);
            }
        }
    }


    public void buildField() throws HelperCodeException {

        desc = "()" + makeDesc(instanceType);
        String declaration = "public static " + typeName + " " + methodname + "()";

        String returnStmt = "return ";
        String className = classNode.name;
        className = className.substring(className.lastIndexOf("/") + 1);
        returnStmt += className + "." + fieldNode.name + ";";
        stmts.add(returnStmt);

        methodbody = "\t" + declaration + "\n";
        for(String stmt: stmts)
            methodbody += "\t\t" + stmt + "\n";
        methodbody += "\t}";

    }

    /**
     * make helper method for the given type:
     * 1. choose an constructor/factory method from the TypeTable
     * 2. with this method, make a method methodbody to build all
     * (1) discover all the related helper method, ensure the input could only contain a String to specify File
     * (2)
     *
     */
    public void build() throws HelperCodeException {
        MethodNode constructorNode = null;
        if(!TypeUtil.isAccess(methodNode.access, Opcodes.ACC_STATIC) && !methodNode.name.contains("<init>")) {
            //1. make instance for the instance of the class
            //ClassNode classNode = TypeTableBuilder.getMethodClassNode(methodNode);
            if(!ClassNodeUtil.isClassNodeBuilderable(classNode))
                throw new HelperCodeException("not able to build classNode");
            constructorNode = ClassNodeUtil.getSimpleConstructor(classNode);
            if(constructorNode != null) {
                String classname = classNode.name.substring(classNode.name.lastIndexOf(".") + 1);
                classname = classname.substring(classname.lastIndexOf("/") + 1);
                String stmt = makeNewVoidConstructorStmt(classname, "c");//fixme
                if(!TypeUtil.isAccess(constructorNode.access, Opcodes.ACC_PUBLIC))
                    throw new HelperCodeException("classnode is non-public");
                addImport(classNode.name);
                stmts.add(stmt);
            } else
                throw  new HelperCodeException("unable to found a classnode constructor");
        } else {
            //what if the method is static

            //what if it
        }

        for (Type pt : Type.getMethodType(methodNode.desc).getArgumentTypes()) {
            //fixme, only support primitive and string object now.
            int sort = pt.getSort();
            if (sort > Type.DOUBLE) {
                if (sort == Type.OBJECT) {
                    //if it is in standard library
                    HelperMethod item = null;
                    if(TypeUtil.isStandardLibrary(pt.getClassName())) {
                        item = StandardLibraryClasses.getHelper(pt);
                        //fixme for instance helper
                    } else {
                        item = InstanceHelperClasses.getInstanceHelper(pt);
                    }

                    if (item != null) {
                        Type methodType = Type.getMethodType(item.desc);
                        methodArguments.add(methodType);
                        methods.add(item);
                        continue;
                    }
                }
                //fixme
                InstanceHelperBuilder.addunsuported(pt);
                throw new HelperCodeException("doesn't support this type now");
            } else
                methodArguments.add(pt);
        }

        int number_of_string_arg = 0;
        for(Type t: methodArguments) {
            if(t.getSort() == Type.METHOD) {
                for(Type mt: t.getArgumentTypes()) {
                    if(mt.getSort() == Type.OBJECT)
                        number_of_string_arg += 1;
                }
            }
            if(t.getSort() == Type.OBJECT)
                number_of_string_arg += 1;
        }
        if(number_of_string_arg > 1) {
            //is_args_valid_for_building = false;
            throw new HelperCodeException("fail to build argument");
        }

        if(number_of_string_arg == 1) {
            this.isFileString = true;
        }

        System.out.println("The size of the stmts in build, before methodArgument: " + stmts.size());
        makeMethodBody(methodArguments);
    }

    public void makeMethodBody(ArrayList<Type> methodArguments) throws HelperCodeException{


        String declaration = "public static " + typeName + " " + methodname + "(";
        Iterator methodsIterator = methods.iterator();

        desc = "(";

        for(int i=0; i < methodArguments.size(); i++) {
            Type mt = methodArguments.get(i);
            if(mt.getSort() == Type.METHOD) {
                //fixme, impt for method
                String argStmt = "";
                if(methodsIterator.hasNext()) {
                    HelperMethod item = (HelperMethod) methodsIterator.next();

                    addImport(item);
                    //fixme
                    //addImport(item.impt);

                    if(false) {
                        Type methodType = Type.getMethodType(methodNode.desc);
                        Type returnType = methodType.getReturnType();
                        if(returnType != null) {
                            System.out.println(returnType.getClassName());
                        }
                    }

                    Type returnType = mt.getReturnType();
                    String className = null;
                    if(returnType != null)
                        className = TypeUtil.getTypeClassName(returnType);
                    if(className == null)
                        throw new HelperCodeException("Type className null");

                    if(item instanceof HelperMethod.StandardHelperMethod) {
                        argStmt = className + " arg" + i + " = " + "StandardLibraryHelper." + item.methodname + "(";
                    } else
                        argStmt = className + " arg" + i + " = " + item.methodname + "(";
                } else
                    throw new HelperCodeException("invalid method");

                Type[] ptypes = mt.getArgumentTypes();
                int len = ptypes.length;
                for(int j=0; j< len; j++) {
                    Type pt = ptypes[j];
                    String argName = "arg" + i + "_tmp" + j;
                    String tmp = pt.getClassName();
                    if(tmp == null)
                        throw new HelperCodeException("Type className null");
                    String className = tmp.substring(tmp.lastIndexOf(".") + 1);
                    declaration += className + " " + argName + ", ";
                    argStmt += argName + ", ";

                    desc += makeDesc(pt);
                }

                argStmt = argStmt.substring(0,argStmt.lastIndexOf(","));
                argStmt += ");";
                stmts.add(argStmt);


            } else {
                String tmp = mt.getClassName();
                declaration += tmp + " " + "arg" + i + ", ";
                //fixme, desc
                desc += makeDesc(mt);
            }
        }

        desc += ")";
        desc += makeDesc(instanceType);

        declaration += "){";
        declaration = declaration.replace(", ){", "){");
        declaration = declaration.replace("){", ")  throws Exception {");


        String returnStmt = "return ";
        if(methodNode.name.contains("<init>")) {
            returnStmt +=  "new " + typeName + "(";//)";
        } else {
            //fixme, need
            if(!TypeUtil.isAccess(methodNode.access, Opcodes.ACC_STATIC)) {
                System.out.println("The size of the stmts is " + stmts.size());
                returnStmt += "c.";
            } else {
                String className = classNode.name;
                className = className.substring(className.lastIndexOf("/") + 1);
                returnStmt += className + ".";
            }
            returnStmt += methodNode.name + "(";//)";
        }


        int len = Type.getMethodType(methodNode.desc).getArgumentTypes().length;
        for(int i=0; i< len; i++)
            returnStmt += "arg" + i + ", ";
        returnStmt += ");";
        returnStmt = returnStmt.replace(", )", ")");
        stmts.add(returnStmt);


        methodbody = "\t" + declaration + "\n";
        for(String stmt: stmts)
            methodbody += "\t\t" + stmt + "\n";
        methodbody += "\t}";
    }

}
