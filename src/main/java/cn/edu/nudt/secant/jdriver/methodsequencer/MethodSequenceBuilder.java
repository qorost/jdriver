package cn.edu.nudt.secant.jdriver.methodsequencer;

import cn.edu.nudt.secant.jdriver.knowledgebase.InstanceHelperClasses;
import cn.edu.nudt.secant.jdriver.knowledgebase.InstanceHelperMethod;
import cn.edu.nudt.secant.jdriver.preprocessor.DependencyAnalyzer;
import cn.edu.nudt.secant.jdriver.preprocessor.TypeTableBuilder;
import cn.edu.nudt.secant.jdriver.tools.Debug;
import cn.edu.nudt.secant.jdriver.tools.TypeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by huang on 4/10/18.
 */
public class MethodSequenceBuilder {

    //public ArrayList<MethodNode> sequences;

    //The method sequences is consisted of methodnode or fieldnode, by default, it is a methodnode,
    public ArrayList<Object> sequences;
    public ClassNode classnode;
    public DependencyAnalyzer danalyzer;
    Set<FieldNode> fieldNodeSet = null;
    MethodNode mCutConstructor;

    public MethodSequenceBuilder(ClassNode classNode, DependencyAnalyzer dependencyAnalyzer) {
        sequences = new ArrayList<>();
        classnode = classNode;
        danalyzer = dependencyAnalyzer;
        mCutConstructor = null;
    }


    /**
     * build method sequences for static method
     * @param methodNode
     * @throws Exception
     */
    public void buildStatic(MethodNode methodNode) throws MSBuilderException {
        if(TypeUtil.isAccess(methodNode.access, Opcodes.ACC_STATIC)) {
            fieldNodeSet = danalyzer.getAccessSet(methodNode);

            for(FieldNode f: fieldNodeSet) {
                if(TypeUtil.isAccess(f.access, Opcodes.ACC_FINAL))
                    fieldNodeSet.remove(f);//remove final fields
                if(!TypeUtil.isAccess(f.access, Opcodes.ACC_STATIC))//remove non-static ones
                    fieldNodeSet.remove(f);//
            }

            for(FieldNode fieldNode: fieldNodeSet)
                select(fieldNode, true);

        } else {
            throw new FieldNodeNotStaticException("The input method node is not static");
        }
    }

//    public Object getConstructorMethod(Type classType) throws MSBuilderException{
//        Set<MethodNode> methodNodeSet = TypeTableBuilder.getConstructor(classType);
//        //fixme, helper method
//        mCutConstructor = selectMethod(methodNodeSet);
//        if(mCutConstructor != null)
//            return mCutConstructor;
//            //sequences.add(mCutConstructor);
//        return null;
//
//    }

    /**
    * build method sequences, which starts with a constructor to instantiate CUT and methods to change the instance status
    * */
    public void buildNonStatic(MethodNode methodNode) throws MSBuilderException{
        //Debug.debug("in building method sequences for " + methodNode.classname + " desc: " + methodNode.desc );
        Type classType = TypeUtil.getClassType(classnode);

        Set<MethodNode> methodNodeSet = TypeTableBuilder.getConstructor(classType);
        if(methodNode != null) {
            //fixme, this is what fails
            mCutConstructor = selectMethod(methodNodeSet, false,true);
            if(mCutConstructor != null)
                sequences.add(mCutConstructor);
        }
        if(mCutConstructor == null) {
            //get a helper method from the InstanceHelperClasses
            InstanceHelperMethod mhelper = InstanceHelperClasses.getInstanceHelper(classType);
            if (mhelper == null) {
                System.err.println("There are " + methodNodeSet.size() + " elements in the set");
                throw new InstanceFailureMSBuilderException("Fail to find a proper way to generate instance for CUT: " + classnode.name, classnode.name);
            }
            sequences.add(mhelper);
        }

        fieldNodeSet = danalyzer.getAccessSet(methodNode);
        for(FieldNode fieldNode: fieldNodeSet) {
            if(!TypeUtil.isAccess(fieldNode.access, Opcodes.ACC_FINAL))//skp final
                select(fieldNode, false);
        }
    }

    public void build(MethodNode methodNode) throws MSBuilderException {
        if(TypeUtil.isAccess(methodNode.access, Opcodes.ACC_PUBLIC)) {
            if(TypeUtil.isAccess(methodNode.access, Opcodes.ACC_STATIC))
                buildStatic(methodNode);
            else
                buildNonStatic(methodNode);
        } else
            throw new MSBuilderException("Input methodnode doesn't have a public access");
    }

    /**
     * The goal of this refine is to extend the method sequences, and build it to convertible method sequences
     * e.g.
     */
    public void refine() {


    }

    /**
    * select a proper method to modify the field, the selection policy is implemented here.
    * some rules:
    * 1. only one constructor can exist in sequences
    * */
    public void select(FieldNode fieldNode, boolean isStatic) throws MSBuilderException {
        Set<MethodNode> methodNodeSet = danalyzer.getModifySet(fieldNode);
        int setSize = methodNodeSet.size();

        if(TypeUtil.isAccess(fieldNode.access, Opcodes.ACC_PUBLIC)) {
            //can be directly modified, add fieldnode
            Type type = Type.getType(fieldNode.desc);
            if(TypeUtil.isPrimitiveType(type)) {
                sequences.add(fieldNode);
                return;
            }
            else {
                //fixme, the type of the field is not primitive
                //System.out.println("The type of the field is not primitive: " + type.getClassName());
            }
        }

        if( setSize == 0) {
            System.err.println("There is no methods to change field: " + fieldNode.name);
            throw new NoMethodFoundExeption("no methods to modify field " + fieldNode.name);
        }

        //if the existing sequences can modify the target field, pass
        for(Object o: sequences) {
            if(o instanceof MethodNode) {
                MethodNode m = (MethodNode) o;
                if (m.equals(mCutConstructor))//fixme, it is ok to return here
                    continue;
                if (danalyzer.isMethodDirectModifier(m, fieldNode))
                    return;
            }
        }

        //selecting methods regarding to the simpleness of input parameters
        MethodNode selectedMethod = selectMethod(methodNodeSet, isStatic, false);

        if(selectedMethod == null)
            return;

        if (!TypeUtil.isConstructor(selectedMethod) )
            sequences.add(selectedMethod);
    }

    /**
    * select method from the method set
    * */

    /**
     * select method from the method set
     * @param modifySet modifySet
     * @param isStatic is the method static
     * @param isConstructor ask for constructor?
     * @return
     * @throws MSBuilderException
     */
    public MethodNode selectMethod(Set<MethodNode> modifySet, boolean isStatic, boolean isConstructor) throws MSBuilderException{
        if(modifySet == null || modifySet.size()<= 0) {
            throw new NoMethodFoundExeption("No methods in the method set!");
        }
        MethodNode selected = modifySet.iterator().next();

        int selectedNotPrimitiveNumber = TypeUtil.getObjectNumberInInputParameters(Type.getMethodType(selected.desc));

        if(!isConstructor && selected.name.contains("<init>")) {
            selected = null;
            selectedNotPrimitiveNumber = 10000;
        }

        if(modifySet.size() > 1) {
            for (MethodNode methodNode : modifySet) {
                if(!isConstructor && methodNode.name.contains("<init>"))
                    continue;//skip constructor here

                if(isConstructor && !methodNode.name.contains("<init>"))
                    continue;

                if(TypeUtil.isAccess(methodNode.access, Opcodes.ACC_PUBLIC) ) {
                    if(isStatic) {//if it is static, the selected method must be static
                        if(!TypeUtil.isAccess(methodNode.access, Opcodes.ACC_STATIC))
                            continue;
                    }

                    Type mtype = Type.getMethodType(methodNode.desc);
                    int tmpNum = TypeUtil.getObjectNumberInInputParameters(mtype);
                    if (tmpNum < selectedNotPrimitiveNumber) {
                        selected = methodNode;
                        selectedNotPrimitiveNumber = tmpNum;
                    }
                }
            }
        }

        if(selectedNotPrimitiveNumber != 0) {
            //throw new NoProperMethodException("found not proper method");
            //fixme
            return null;
        }
        return selected;
    }

    public void printMethodSequences(MethodNode methodNode) {

        String msg = "\nThe method sequences for: " + methodNode.name;

        if(fieldNodeSet.size() == 0) {
            System.out.println("No fields accessed in the method: " + methodNode.name);
            return;
        }

        if(sequences.size() == 0) {
            System.out.println("MethodNode " + methodNode.name + ": there are no elements in the sequences.");
            return;
        }
        for(Object o: sequences) {
            msg += "\n\t";
            if(o instanceof MethodNode) {
                MethodNode m = (MethodNode) o;
                if (m.name.contains("<init>")) {
                    String[] bits = classnode.name.split("/");
                    msg += bits[bits.length - 1];
                } else
                    msg += m.name;
                msg += "(";
                //msg += m.signature;
                msg += m.desc;
                msg += ");\t";
            } else if(o instanceof FieldNode) {
                msg += "Field:" + ((FieldNode) o).name + "\t";
            }
        }
        //JLogger.logInfo(msg);
        System.out.println(msg);
    }

    public static void printClassNodeSummary(ClassNode classNode) {

    }

    /**
     * The method provide the function to build method sequences
     * @param classNode class under test
     * @param methodNode method under test
     * @param dependencyAnalyzer dependency analyzer
     * @return an array of methodNode
     */
    public static ArrayList<Object> build(ClassNode classNode,MethodNode methodNode, DependencyAnalyzer
            dependencyAnalyzer) throws MSBuilderException {
        Debug.debug("in building type sequences for methodnode: " + methodNode.name + " desc: " + methodNode.desc);
        MethodSequenceBuilder builder = new MethodSequenceBuilder(classNode, dependencyAnalyzer);

        builder.build(methodNode);

        //builder.printMethodSequences(methodNode);
        System.out.println("\tMethod sequences generated for " + methodNode.name);
        return builder.sequences;
    }
}
