package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class Commit implements Serializable {
    /** Message associated the the commit. */
    private String message;
    /** Time UTC when the Commit was made. */
    private String timeStamp;
    /** Hash of the commit. */
    private String hash;
    /** Hashmap that maps from sha of blob. */
    private Map<String, File> shaToBlob;
    /** Hashmap that maps from name to sha of blob. */
    private Map<String, String> nameToSha;
    /** Main parent. */
    private Commit parent;
    /** Second parent. */
    private Commit mergeParent;

    public Commit(String messageInput, Commit parentInput) {
        this.message = messageInput;
        this.parent = parentInput;
        shaToBlob = new HashMap<String, File>();
        nameToSha = new HashMap<String, String>();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE "
                + "MMM d hh:mm:ss yyyy Z");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (parentInput == null) {
            timeStamp = formatter.format(new Date(0));
        } else {
            timeStamp = formatter.format(new Date(System.currentTimeMillis()));
            clone(this.parent);
        }
        setHash(Utils.sha1(Utils.serialize(this)));


    }

    public Commit(String messInp, Commit parentInp, Commit mergeParentInp) {
        this.message = messInp;
        this.parent = parentInp;
        shaToBlob = new HashMap<String, File>();
        nameToSha = new HashMap<String, String>();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE "
               + "MMM d hh:mm:ss yyyy Z");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (parentInp == null) {
            timeStamp = formatter.format(new Date(0));
        } else {
            timeStamp = formatter.format(new Date(System.currentTimeMillis()));
            clone(this.parent);
        }
        this.mergeParent = mergeParentInp;
        setHash(Utils.sha1(Utils.serialize(this)));
    }

    public Commit getMergeParent() {
        return mergeParent;
    }
    public void clone(Commit other) {
        for (Map.Entry<String, File> entry : parent.getShaToBlob().entrySet()) {
            shaToBlob.put(entry.getKey(),
                    entry.getValue());
        }

        for (Map.Entry<String, String> entry
                : parent.getNameToSha().entrySet()) {
            nameToSha.put(entry.getKey(),
                    entry.getValue());
        }

    }



    public Commit kthParent(int k) {
        Commit currentCommit = this;
        for (int i = 0; i < k; i++) {
            currentCommit = currentCommit.getParent();
        }
        return currentCommit;
    }

    public Map<String, String> getNameToSha() {
        return nameToSha;
    }

    public Map<String, File> getShaToBlob() {
        return shaToBlob;
    }

    public void addNameShaPair(String name, String sha) {
        nameToSha.put(name, sha);
    }

    public void addShaBlobPair(String sha, File blob) {
        shaToBlob.put(sha, blob);
    }

    public String getMessage() {
        return message;
    }


    public String getHash() {
        return hash;
    }

    public void setHash(String hashInput) {
        this.hash = hashInput;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public Commit getParent() {
        return parent;
    }
}
