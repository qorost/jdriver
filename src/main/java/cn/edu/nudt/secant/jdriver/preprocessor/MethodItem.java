package cn.edu.nudt.secant.jdriver.preprocessor;

import cn.edu.nudt.secant.jdriver.tools.ASMReader;
import cn.edu.nudt.secant.jdriver.tools.Options;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * Created by huang on 6/7/18.
 */
public class MethodItem implements Comparable<MethodItem>{

    public ClassNode classNode;
    public MethodNode methodNode;
    public int branchNum;
    public int insnNum;
    public int outgoing;
    public int incoming;
    public int fileoperations;
    public double metric;


    public MethodItem(ClassNode classNode, MethodNode methodNode) {
        this.classNode = classNode;
        this.methodNode = methodNode;
        branchNum = ASMReader.getNumberofBranches(classNode, methodNode);
        insnNum = methodNode.instructions.size();
        outgoing = incoming = 0;
        metric = 0.0;//priority
    }

    public void setOutgoing(int num) {
        outgoing = num;
    }

    public void setIncoming(int num) {
        incoming = num;
    }

    public void setFileoperations(int num) {fileoperations = num;}


    public void calcMetric() {
        if(GlobalCallGraph.maxOutgoing == 0 || GlobalCallGraph.maxBranchNum == 0 || GlobalCallGraph.maxInsnNum == 0 ||
                GlobalCallGraph.maxIncoming == 0   ) {
            System.err.println("error occured, a max == 0, not updating metric value");
            //System.exit(-1);
        } else {

            metric = 0.5 * outgoing / GlobalCallGraph.maxOutgoing
                    + 0.5 * branchNum / GlobalCallGraph.maxBranchNum
                    + 0.5 * insnNum / GlobalCallGraph.maxInsnNum
                    - 0.5 * incoming / GlobalCallGraph.maxIncoming;
        }
    }

    /**
     * if the method is employed test other method, it should add some bonus, should we set the bonus as the value of
     * metric?
     * @param useNum
     */
    public void addBonuse(int useNum) {
        metric += 0.5*useNum;
    }

    @Override
    public int compareTo(MethodItem o) {
        //define a metric?
        if(metric == 0)
            calcMetric();
        if(o.metric == 0)
            o.calcMetric();
        boolean result;
        switch (Options.v().getMode()) {
            //fixme, 'c', 't', fails with Comparison method violates its general contract!
            case 'c':
                return incoming - o.incoming;
            case 't':
                return outgoing - o.outgoing;
            case 'b':
                result = branchNum > o.branchNum;
                break;
            case 'i':
                result = insnNum > o.insnNum;
                break;
            case 's':
            default:
                result = metric > o.metric;
                break;
        }
        //return incoming-o.incoming;
        return result ? 1: -1;
    }

    public String toString() {
        return String.format("%s(%s)",methodNode.name, classNode.name);
    }

    public String getModeValue() {
        String value = "";
        switch (Options.v().getMode()) {
            case 'b':
                value = Integer.toString(branchNum);
                break;
            case 'c':
                value = Integer.toString(incoming);
                break;
            case 't':
                value = Integer.toString(outgoing);
                break;
            case 'i':
                value = Integer.toString(insnNum);
                break;
            case 's':
            default:
                value = Double.toString(metric);
                break;
        }
        return value;
    }


    /**
     * test if this is a constructor
     * @return
     */
    public boolean isConstructor() {
        return methodNode.name.equals("<init>");
    }

    /**
     * decide if there are `File` operations in list
     * @return
     */
    public boolean hasFileRelatedOperations() {
        Iterator insnIterator = methodNode.instructions.iterator();
        while(insnIterator.hasNext()) {

        }
        return true;
    }
}