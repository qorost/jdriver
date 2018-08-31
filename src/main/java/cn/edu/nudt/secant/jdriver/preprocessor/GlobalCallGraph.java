package cn.edu.nudt.secant.jdriver.preprocessor;

import cn.edu.nudt.secant.jdriver.tools.ASMReader;
import cn.edu.nudt.secant.jdriver.tools.JLogger;
import cn.edu.nudt.secant.jdriver.tools.TypeUtil;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by huang on 4/17/18.
 * The Global Call Graph is a graph consisted of MethodNode
 * Check information from the global call graph
 */
public class GlobalCallGraph {

    private DirectedGraph<Object, DefaultEdge> cg;
    //key to the table: hashsum(classNode.classname + methodNode.desc + methodNode.classname)
    //items are MethodItem
    Hashtable<String, Object> methodNodeHashtable;//


    /** for normalization*/
    public static int maxInsnNum = 0;
    public static int maxBranchNum = 0;
    public static int maxOutgoing = 0;
    public static int maxIncoming = 0;

    public boolean contains(String key) {
        return methodNodeHashtable.containsKey(key);
    }


    public boolean contains(ClassNode classNode, MethodNode methodNode) {
        String key = TypeUtil.calcHashSum(classNode.name + methodNode.desc + methodNode.name);
        return  methodNodeHashtable.containsKey(key);
    }

    /**
    * get item by hashkey
    * */
    public Object get(String key) {
        return methodNodeHashtable.get(key);
    }

    public Object get(ClassNode classNode, MethodNode methodNode) {
        String key = TypeUtil.calcHashSum(classNode.name + methodNode.desc + methodNode.name);
        return methodNodeHashtable.get(key);
    }

    /**
     * add class and method to the graph
     * @param classNode class node
     * @param methodNode method node
     */
    public void add(ClassNode classNode, MethodNode methodNode) {
        //System.out.println("adding node " + classNode.classname + methodNode.desc + methodNode.classname);
        String key = TypeUtil.calcHashSum(classNode.name + methodNode.desc + methodNode.name);
        if(!methodNodeHashtable.containsKey(key)) {
            MethodItem methodItem = new MethodItem(classNode, methodNode);
            methodNodeHashtable.put(key, methodItem);
            //if(!cfg.containsVertex(methodNode))
            cg.addVertex(methodItem);
        } else {
            System.out.println("classNode " + classNode.name + " method " + methodNode.name + " already exist!");
        }
    }

    public Set<Object> getMethodItemSet() {
        return cg.vertexSet();
    }


    /**
     * if ecception occurs, add this
     * @param key
     * @param data
     */
    public void add(String key, String data) {
        methodNodeHashtable.put(key, data);
        cg.addVertex(data);
    }


    public void addEdge(Object src, Object dst) {
        cg.addEdge(src, dst);
    }

    public void showGraph() {
        try {
            String fileName = "./tests/graph.csv";
            FileWriter fileWriter = new FileWriter(fileName);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("ClassNode,Method,Access Type,# of inputs, object inputs,primitive inputs #," +
                    "instruction #, branch #,# of called,# of calling, # of fields, # of accessed, # of modified");
            int size = cg.vertexSet().size();
            int methodNum = 0;
            for (Object o : cg.vertexSet()) {
                if (o instanceof MethodItem) {
                    MethodItem methodItem = (MethodItem) o;
                    methodNum++;
                    //m.desc, m.classname, # of parameters, #of primitives in parameter, #
                    //for()
                    //int plength = methodItem.methodNode.parameters.size();
                    Type mtype = TypeUtil.getMethodType(methodItem.methodNode);
                    int access = methodItem.methodNode.access;
                    String accessStr = "";

                    if(TypeUtil.isAccess(access, Opcodes.ACC_PRIVATE))
                        accessStr = "PRIVATE";
                    else if(TypeUtil.isAccess(access, Opcodes.ACC_PUBLIC))
                        accessStr = "PUBLIC";

                    int plength = mtype.getArgumentTypes().length;
                    int onumber = TypeUtil.getObjectNumberInInputParameters(mtype);
                    int pnumber = TypeUtil.getPrimitiveNumberInInputParameters(mtype);
                    int numberofinstr = methodItem.methodNode.instructions.size();
                    int numberofbranches = methodItem.branchNum;
                    int numberofoutgoing = cg.outDegreeOf(o);
                    int numberofincoming = cg.inDegreeOf(o);
                    int[] fields = ASMReader.getNumberofAccessedFields(methodItem.classNode, methodItem.methodNode);
                    String line = String.format("%s,%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d",
                            methodItem.classNode.name, methodItem.methodNode.name, accessStr,
                            plength, onumber, pnumber, numberofinstr,numberofbranches,
                            numberofincoming, numberofoutgoing,
                            fields[0],fields[1],fields[2]);
                    printWriter.println(line);
                }
            }
            printWriter.close();
            String msg = String.format("%d total vertex, %d are methodNodes", size, methodNum);
            JLogger.logInfo(msg);
            //System.out.println(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * update the outgoing/incoming number after cg is built
     */
    public void updateValue() {
        for (Object o : globalCallGraph.cg.vertexSet()) {
            if (o instanceof MethodItem) {
                MethodItem methodItem = (MethodItem) o;
                int inNum = globalCallGraph.cg.inDegreeOf(o);
                int outNum = globalCallGraph.cg.outDegreeOf(o);

                ((MethodItem) o).setIncoming(inNum);
                ((MethodItem) o).setOutgoing(outNum);

                if(methodItem.insnNum > maxInsnNum) maxInsnNum = methodItem.insnNum;
                if(methodItem.branchNum > maxBranchNum) maxBranchNum = methodItem.branchNum;
                if(inNum > maxIncoming) maxIncoming = inNum;
                if(outNum > maxOutgoing) maxOutgoing = outNum;

                ((MethodItem) o).calcMetric();
            }
        }
    }

    public static GlobalCallGraph globalCallGraph;

    public static GlobalCallGraph v() {
        if (null == globalCallGraph) {
            globalCallGraph = new GlobalCallGraph();
        }
        return globalCallGraph;
    }



    private GlobalCallGraph() {
        cg = new DefaultDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);
        methodNodeHashtable = new Hashtable<String, Object>();
    }


}
