package gitlet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
public class Remote implements Serializable {
    /** Current branchmap that links name to head of branch. */
    private Map<String, String> remoteBranchMap;
    public Remote() {
        remoteBranchMap = new HashMap<String, String>();
    }

    public Map<String, String> getRemoteBranchMap() {
        return remoteBranchMap;
    }
}
