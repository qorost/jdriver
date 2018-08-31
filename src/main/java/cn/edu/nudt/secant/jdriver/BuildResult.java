package cn.edu.nudt.secant.jdriver;

/**
 * Created by huang on 6/28/18.
 */
public class BuildResult {

    public int failedNum;
    public int publicNum;
    public int totalMethod;

    public boolean isTestable;
    public boolean passTest;
    public boolean cutGenerated;


    public BuildResult() {}

    public BuildResult(int publicnum, int failed, int total, boolean passed) {
        failedNum = failed;
        publicNum = publicnum;
        totalMethod = total;
        isTestable = true;
        passTest = passed;
        cutGenerated = true;
    }

    public BuildResult(boolean testable, boolean yes) {
        isTestable = testable;
        cutGenerated =  yes;
    }

    public void setCutGenerated(boolean yes) {
        cutGenerated = yes;
    }
}
