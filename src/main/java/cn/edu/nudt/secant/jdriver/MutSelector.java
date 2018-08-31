package cn.edu.nudt.secant.jdriver;

import cn.edu.nudt.secant.jdriver.assembler.MUTDriverCode;
import cn.edu.nudt.secant.jdriver.preprocessor.GlobalCallGraph;
import cn.edu.nudt.secant.jdriver.preprocessor.MethodItem;
import cn.edu.nudt.secant.jdriver.tools.ASMReader;
import cn.edu.nudt.secant.jdriver.tools.Options;
import cn.edu.nudt.secant.jdriver.tools.TypeUtil;
import jdk.internal.org.objectweb.asm.Opcodes;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.swing.text.html.Option;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by huang on 4/10/18.
 * MutSelector is used to rank methods under test
 */
public class MutSelector {

    public ArrayList<MethodItem> methodNodeArrayList;
    public ArrayList<MethodItem> ignoredMethods;
    public ArrayList<MethodItem> abstractMethods;
    public ArrayList<MethodItem> construtorMethods;
    public ArrayList<MethodItem> filerelatedMethods;
    public ArrayList<MethodItem> entryMethods;



    public void show() {

        String mode = Options.v().getModeString();

        System.out.println("max outgoing: " + GlobalCallGraph.maxOutgoing);
        System.out.println("max incoming: " + GlobalCallGraph.maxIncoming);
        System.out.println("max insnNum: " + GlobalCallGraph.maxInsnNum);
        System.out.println("max branchNum: " + GlobalCallGraph.maxBranchNum);

        System.out.println("Sorting according to " + mode);
        for(MethodItem m: methodNodeArrayList) {
            String msg = String.format("method: %s has a priority value of %s", m.toString(), m.getModeValue());
            System.out.println(msg);
        }
    }

    /**
     * sort the methods according to their attributes.
     */
    public void sortMethods() {

        abstractMethods = new ArrayList<>();
        construtorMethods = new ArrayList<>();
        filerelatedMethods = new ArrayList<>();
        entryMethods = new ArrayList<>();

        Iterator iterator = methodNodeArrayList.iterator();
        while(iterator.hasNext()) {
            MethodItem m = (MethodItem) iterator.next();

            if (m.fileoperations >= 1) {
                filerelatedMethods.add(m);
            }

            if (TypeUtil.isAccessAbstract(m.methodNode.access)) {
                abstractMethods.add(m);
                //continue;
            }

            if (TypeUtil.isAccessAbstract(m.classNode.access)) {
                abstractMethods.add(m);
                //System.out.println("found a method in an abstract class: " + m.toString());
                //continue;
            }

            /**
             * single branch methods
             * add some criteria from remove important methods.
             */
            if (m.branchNum < 1) {
                ignoredMethods.add(m);
            }

            if(m.incoming == 0) {
                if(TypeUtil.isAccess(m.methodNode.access, Opcodes.ACC_PUBLIC))
                    entryMethods.add(m);
            }
        }
        analyzeEntryMethods();
    }


    /**
     * analyze entry methods
     */
    public void analyzeEntryMethods() {
        System.out.println("There are " + entryMethods.size() + " entry points.");
        for(MethodItem m: entryMethods) {
            if(m.fileoperations > 0) {
                String msg = String.format("Method: %s(%s) has file operations", m.methodNode.name, m.classNode.name);
                System.out.println(msg);
            }
        }
    }

    /**
     * remove unnecessary methods, branchInsn == 0,
     */
    public void removeMethods() {

        System.out.println("There are " + methodNodeArrayList.size() + " in the list");

        methodNodeArrayList.removeAll(ignoredMethods);
        methodNodeArrayList.removeAll(construtorMethods);
        methodNodeArrayList.removeAll(abstractMethods);

        System.out.println("After removing, there are "
                + methodNodeArrayList.size() + " in the list \n"
                + ignoredMethods.size() + " items in the ignored list \n"
                + abstractMethods.size() + " items in abstract \n"
                + construtorMethods.size() + " items are constructors\n"
        );
//        System.out.println("These methods are removed: ");
        for(MethodItem m: ignoredMethods) {
            String tmp = String.format("method: %s(%s) has %d branchInsn, %d insntructions", m.methodNode.name, m
                    .classNode.name, m.branchNum, m.insnNum);
            //System.out.println(tmp);
        }
        showFileRelatedMethods();
    }

    public void showFileRelatedMethods() {
        System.out.println("These methods" + filerelatedMethods.size() +" have file operations: ");
        if(filerelatedMethods != null) {
            for(MethodItem m: filerelatedMethods) {
                String tmp = String.format("method %s(%s) has %d file operations.", m.methodNode.name, m.classNode
                        .name, m.fileoperations);
                System.out.println(tmp);
            }
        }
    }




    /**
     * depreciated 1st edition
     * selecting method for test,
     */
    public static void process() {
        ArrayList<MethodNode> methodNodeArrayList = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Set<String> inputClasses = Options.v().getInput();
        for(String cls: inputClasses) {
            InputStream bytecode = classLoader.getResourceAsStream(cls);
            try {
                ClassNode classNode = ASMReader.getClassNode(bytecode);
                for(Object o: classNode.methods) {
                    MethodNode methodNode = (MethodNode) o;
                    if(methodNode.name.contains("<init>")) {
                        //skip init
                        continue;
                    }
                    //fixme, here to add the algorithm for
                    methodNodeArrayList.add(methodNode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public static MutSelector selector;

    public static MutSelector v() {
        if (null == selector) {
            selector = new MutSelector();
        }
        return selector;
    }

    private MutSelector() {
        methodNodeArrayList = new ArrayList<>();
        ignoredMethods = new ArrayList<>();
        for(Object o: GlobalCallGraph.v().getMethodItemSet()) {
            if( o instanceof  MethodItem) {
                methodNodeArrayList.add((MethodItem) o);
            }
        }

        sortMethods();

        if(false) {
            removeMethods();
            return;
        }


        char mode = Options.v().getMode();
        if(mode == 'c')
            Collections.sort(methodNodeArrayList);
        else
            Collections.sort(methodNodeArrayList, Collections.reverseOrder());
    }
}
