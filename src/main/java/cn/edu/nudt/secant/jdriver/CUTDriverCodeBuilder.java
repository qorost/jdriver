package cn.edu.nudt.secant.jdriver;

import cn.edu.nudt.secant.jdriver.assembler.CUTDriverCode;
import cn.edu.nudt.secant.jdriver.assembler.jexceptions.*;
import cn.edu.nudt.secant.jdriver.assembler.HelperCode;
import cn.edu.nudt.secant.jdriver.assembler.MUTDriverCode;
import cn.edu.nudt.secant.jdriver.methodsequencer.*;
import cn.edu.nudt.secant.jdriver.preprocessor.ClassNodesAnalyzer;
import cn.edu.nudt.secant.jdriver.preprocessor.DependencyAnalyzer;
import cn.edu.nudt.secant.jdriver.tools.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by huang on 3/29/18.
 */
public class CUTDriverCodeBuilder {

    public String classname;
    public ClassNode classnode;
    public DependencyAnalyzer dependencyAnalyzer;
    public CUTDriverCode cutDriverCode;

    public List<MethodNode> methodsUnderTest;

    private int failedNum = 0;//number of failed number
    private int publicNum = 0;//numer of all public methods
    private int totalmethodNum = 0; //number of total methods
    private boolean testable = true;//specify whether it is testable;
    private boolean cutGenerated = false;


    public CUTDriverCodeBuilder(InputStream bytecode, String className) throws CUTInitException {
        classname = className;
        try {
            classnode = ASMReader.getClassNode(bytecode);
        } catch (Exception e) {
            throw new ClassNodeInitException("Fail to get classnode");
        }
        init();
    }

    public CUTDriverCodeBuilder(ClassNode classNode) throws CUTInitException {
        classnode = classNode;
        init();
    }

    public CUTDriverCodeBuilder(String path) throws CUTInitException {
        try {
            classnode = ASMReader.getClassNode(path);
        } catch (Exception e) {
            throw new ClassNodeInitException("Fail to get classnode");
        }
        init();
    }

    /**
     * an important role is to test if this class is ok for testing
     * @throws DrivecodeException
     */
    public void init() throws CUTInitException{
        if(classnode != null) {
            //if it is not used directly
            if(!ClassNodesAnalyzer.isClassDirectlyTestable(classnode)) {
                testable = false;
                throw new ClassNotDirectTestableException("classnode is not testable");
            }

            //fixme, what about Exception class?

            classname = classnode.name;
            cutDriverCode = new CUTDriverCode(classnode);
            methodsUnderTest = new ArrayList<>();
            buildDependencyAnalyzer();
            failedNum = 0;
            totalmethodNum = classnode.methods.size();
        } else {
            throw new ClassNodeInitException("classnode is null, fail to initialize.");
        }
    }


    public void buildDependencyAnalyzer() {
        dependencyAnalyzer = DependencyAnalyzer.build(classnode);
        //dependencyAnalyzer.printSummary();
    }


    /**
     * add all the methods to methodsUnderTest
     */
    public void setAllMethodUnderTest() {
        for(Object m: classnode.methods) {
            if(m instanceof  MethodNode) {
                if(TypeUtil.isAccess(((MethodNode) m).access,Opcodes.ACC_PUBLIC))
                    methodsUnderTest.add((MethodNode)m);
            }
        }
        publicNum = methodsUnderTest.size();
        //methodsUnderTest = classnode.methods;
    }

    public int getPublicNum() {return publicNum;}

    public int getFailedNum() {return failedNum;}

    public int getTotalmethodNum() {return totalmethodNum;}

    public boolean getTestable() {return testable;}

    public boolean getCutGenerated() {return cutGenerated;}

    /**
     * add the input method to methodsUnderTest
     * @param method a methodnode
     */
    public void setSingleMethod(MethodNode method) {
        methodsUnderTest.add(method);
    }

    /**
     * add a set of methods to methodsUnderTest
     * @param methods Set<MethodNode>
     */
    public void setSingleMethods(Set<MethodNode> methods) {
        for(MethodNode m: methods)
            methodsUnderTest.add(m);
    }


    /**
     * build mutDriverCode for given method, and add it to cutDriverCode
     * @param method
     * @throws DrivecodeException
     */
    public void build(MethodNode method) throws MUTDriverCodeException {
//        if(method == null) {
//            //System.err.println("Invalid Input Method");
//            throw new MethodNotDirectTestableException("Input method not valid");
//        }
//        if(!TypeUtil.isAccess(method.access, Opcodes.ACC_PUBLIC)) {
//            throw new MethodNotDirectTestableException("Invaldid Access!");
//        }
        ArrayList<Object> methodSequences = MethodSequenceBuilder.build(classnode,method,dependencyAnalyzer);

        //fixme, method sequences, refinement
        MUTDriverCode mutDriverCode = MUTDriverCode.build(classnode, method, dependencyAnalyzer, methodSequences);
        cutDriverCode.addMut(mutDriverCode);
    }

    /**
     * build the driver code for each public method, and assemble them together, and write to file
     * @return
     * @throws Exception
     */
    public boolean build() throws Exception {
        Iterator methodIterator = methodsUnderTest.iterator();

        while(methodIterator.hasNext()) {
            MethodNode m = (MethodNode) methodIterator.next();

            String logmsg = "\tBuilding MethodNode " + m.name + "(" + classnode.name  + ") : ";
            if(m.name.contains("<init>")) {
                logmsg += "SKIP constructor!";
            } else if(!TypeUtil.isAccess(m.access, Opcodes.ACC_PUBLIC)) {
                logmsg += "SKIP non-public method!";
            } else {
                try {
                    System.out.println("\tBuiling Mut for method: " + m.name);
                    build(m);
                    logmsg += "SUCCESS";
                } catch (MUTDriverCodeException e) {
                    System.out.println("\tFailed to build method "  + m.name + ", Reason : " + e.toString());
                    failedNum += 1;

                    if(e instanceof MSBuilderException) {
                        logmsg += "FAIL method sequences failure";
                        if(e instanceof InstanceFailureMSBuilderException) {
                            String classname = ((InstanceFailureMSBuilderException) e).classname;
                            logmsg += " (instance generation failure:" + classname + ")";
                        }
                        else if(e instanceof NoMethodFoundExeption)
                            logmsg += " (no method found)";
                        else if(e instanceof NoProperMethodException)
                            logmsg += " (no proper method found)";
                    } else if (e instanceof MUTDriverCodeException) {
                        logmsg += "FAIL MUT driver code failure";
                    }

                }
            }
            JLogger.logMethodInfo(logmsg);
        }

        if(Options.v().isHelperAlone()) {
            boolean result = HelperCode.build();
            if(result == false) {
                //System.err.println("Making Helper.java Fails");
                throw new CUTHelperException("\tFail to generate Helper.java");
            }
        }

        if(cutDriverCode.mutDriverCodeArrayList.size() == 0) {
            System.out.println("\tNo mut in the list, quit!");
            throw new EmptyMutException("no mut generated for" +classname +"!");
        }

        cutDriverCode.assemble();
        cutGenerated = true;

        if(Options.v().isVerbose()) {
            System.out.println("\nThe Drive code for cut " + classname);
            cutDriverCode.print();
        }

        String output = Options.v().getOutput();
        String testfile = cutDriverCode.toFile(output);
        System.out.println("\tTest file " + cutDriverCode.getOuptfile() + " is generated successfully!");
        //cutDriverCode.toFile("./tests/AClassTest.java");

        return JCompiler.compileTestFile(testfile);
    }



    /**
     * given a class classname like "AClass.class", build its driver code into file output/AClassTest.java
     * @param cls
     */
    public static BuildResult buildClass(String cls) {
        CUTDriverCodeBuilder cbuilder = null;
        BuildResult buildResult = new BuildResult();
        buildResult.isTestable = true;
        try {
            //ClassNode classNode = ASMReader.getClassNode(cls);
            //System.out.println("Classnode " + cls + " is loaded successfully");
            cbuilder = new CUTDriverCodeBuilder(cls);
            cbuilder.setAllMethodUnderTest();
            boolean passbuild = cbuilder.build();
            buildResult.setCutGenerated(true);
            buildResult.passTest = passbuild;

        } catch (Exception e) {
            if(e instanceof CUTInitException)
                buildResult.isTestable = false;
            if(e instanceof EmptyMutException) {
                //fixme,

            }
            if(e instanceof CUTDriverCodeException) {
                buildResult.setCutGenerated(false);
            } else {
                buildResult.isTestable = false;
                //System.err.println("Exception: " + e.toString());
                //return null;
            }

        }

        if(cbuilder != null) {
            buildResult.failedNum = cbuilder.getFailedNum();
            buildResult.totalMethod = cbuilder.getTotalmethodNum();
            buildResult.publicNum = cbuilder.getPublicNum();
        }
        return buildResult;
    }

    /**
     * given class classname and method classname, build a driver code
     * @param cls Name of the class, should end with ".class" e.g. .class
     * @param method
     */
    public static void buildMethod(String cls, String method, String desc) {
        try {
            ClassNode classNode;
            classNode = ASMReader.getClassNode(cls + ".class");
            System.out.println("Classnode " + cls + " is loaded successfully");
            CUTDriverCodeBuilder cbuilder = new CUTDriverCodeBuilder(classNode);
            if(desc == null) {
                Set<MethodNode> methodNodeHashSet = ASMReader.getMethodNodes(classNode, method);
                if(methodNodeHashSet.size() != 0) {
                    cbuilder.setSingleMethods(methodNodeHashSet);
                    cbuilder.build();
                }
            } else {
                MethodNode methodNode = ASMReader.getMethodNode(classNode, method, desc);
                if(methodNode != null) {
                    cbuilder.setSingleMethod(methodNode);
                    cbuilder.build();
                }
            }
            //fixme, add a new mode for generating method with only string as input, which could be probably related
            // to file related operation
        } catch (Exception e) {
            //fixme
            if(e instanceof IOException)
                System.err.println("fail to load class " + cls + " in processClass");
            if(e instanceof DrivecodeException)
                System.err.println("fail to build driver code");
            //JLogger.logWarning
            //e.printStackTrace();
        }
    }
}
