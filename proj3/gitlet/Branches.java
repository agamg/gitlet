package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Branches implements Serializable {
    /** Current branchmap that links name to head of branch. */
    private Map<String, String> currBranchMap;
    public Branches() {
        currBranchMap = new HashMap<String, String>();
    }

    public Map<String, String> getBranchMap() {
        return currBranchMap;
    }

}
