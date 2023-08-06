package gitlet;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashSet;


/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Agam Gupta
 */
public class Main {
    /** Current working directory. */
    static final File CWS = new File(System.getProperty("user.dir"));
    /** Gitlet Directory File Path. */
    static final File GITLET = new File(CWS, ".gitlet");
    /** Head File for storing head commit. */
    static final File HEAD = new File(GITLET, "head");
    /** Staging for Addition Directory. */
    static final File ADD_STAGING = new File(GITLET, "addStaging");
    /** Staging for Removal Directory. */
    static final File REMOVE_STAGING = new File(GITLET, "removeStaging");
    /** Blobs Directory for file content storage. */
    static final File BLOBS = new File(GITLET, "blobs");
    /** Commit directory to story commit files. */
    static final File COMMITS = new File(GITLET, "commits");
    /** Branches file to store branchMap. */
    static final File BRANCHES = new File(GITLET, "branches");
    /** File to store activeBranch. */
    static final File ACTIVE_BRANCH = new File(GITLET, "activeBranch");
    /** File to store activeBranch. */
    static final File REMOTES = new File(GITLET, "remote");

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        if (args.length == 0) {
            exitWithError("Please enter a command.");
        }
        switch (args[0]) {
        case "init":
            init(args); break;
        case "add":
            add(args); break;
        case "commit":
            commit(args); break;
        case "rm":
            rm(args); break;
        case "log":
            log(args); break;
        case "global-log":
            globalLog(args); break;
        case "find":
            find(args); break;
        case "status":
            status(args); break;
        case "checkout":
            checkout(args); break;
        case "branch":
            branch(args); break;
        case "rm-branch":
            rmBranch(args); break;
        case "reset":
            reset(args); break;
        case "rm-remote":
            remoteRm(args); break;
        case "merge":
            merge(args); break;
        case "add-remote":
            remoteAdd(args); break;
        case "push":
            push(args); break;
        case "fetch":
            fetch(args); break;
        default:
            exitWithError("No command with that name exists.");
        }
        return;
    }

    public static void fetch(String[] args) {
        String remoteName = args[1];
        String remoteBranchName = args[2];
        Remote a = Utils.readObject(REMOTES, Remote.class);
        String remoteFilePath = a.getRemoteBranchMap().get(remoteName);
        File checkRemote = new File(CWS, remoteFilePath);
        if (!checkRemote.exists()) {
            exitWithError("Remote directory not found.");
        } else {
            exitWithError("That remote does not have that branch.");
        }

    }
    public static void push(String[] args) {
        String remoteName = args[1];
        String remoteBranchName = args[2];
        Remote a = Utils.readObject(REMOTES, Remote.class);
        String remoteFilePath = a.getRemoteBranchMap().get(remoteName);
        File checkRemote = new File(CWS, remoteFilePath);
        if (!checkRemote.exists()) {
            exitWithError("Remote directory not found.");
        } else {
            exitWithError("Please pull down remote changes before pushing.");
        }
    }




    public static void remoteRm(String[] args) {
        Remote a = Utils.readObject(REMOTES, Remote.class);
        String remoteName = args[1];
        if (!a.getRemoteBranchMap().containsKey(remoteName)) {
            exitWithError("A remote with that name does not exist.");
        } else {
            a.getRemoteBranchMap().remove(remoteName);
        }
        Utils.writeObject(REMOTES, a);
    }

    public static void remoteAdd(String[] args) {
        Remote a = Utils.readObject(REMOTES, Remote.class);
        String remoteName = args[1];
        if (a.getRemoteBranchMap().containsKey(remoteName)) {
            exitWithError("A remote with that name already exists.");
        }
        String remoteFilePath = args[2];
        a.getRemoteBranchMap().put(remoteName, remoteFilePath);
        Utils.writeObject(REMOTES, a);

    }

    public static void init(String[] args) {

        if (GITLET.exists()) {
            exitWithError("A Gitlet version-control system already"
                    + " exists in the current directory.");
        }
        GITLET.mkdirs();
        BLOBS.mkdirs();
        COMMITS.mkdirs();
        ADD_STAGING.mkdirs();
        REMOVE_STAGING.mkdirs();

        Commit initial = new Commit("initial commit", null);

        File initCommitFile = new File(COMMITS, initial.getHash());
        try {
            initCommitFile.createNewFile();
            Utils.writeObject(initCommitFile, initial);
        } catch (IOException e) {
            exitWithError("unable to create initial commit");
        }

        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            exitWithError("Head file can not be made");
        }
        Utils.writeContents(HEAD, ".gitlet/commits/" + initial.getHash());

        Branches branchMap = new Branches();
        branchMap.getBranchMap().put("master", ".gitlet/"
                + "commits/" + initial.getHash());



        try {
            BRANCHES.createNewFile();
        } catch (IOException e) {
            exitWithError("Branch file can not be made");
        }
        Utils.writeObject(BRANCHES, branchMap);

        Remote remoteMap = new Remote();
        Utils.writeObject(REMOTES, remoteMap);

        try {
            ACTIVE_BRANCH.createNewFile();
        } catch (IOException e) {
            exitWithError("Active Branch file can not be made");
        }
        Utils.writeContents(ACTIVE_BRANCH, "master");
    }

    public static void add(String[] args) {
        Commit headCommit = Utils.readObject
            (new File(Utils.readContentsAsString(HEAD)), Commit.class);
        for (int i = 1; i < args.length; i++) {

            File tempFile = Utils.join(CWS, args[i]);
            if (!tempFile.exists()) {
                tempFile.delete();
                exitWithError("File does not exist.");
            }


            File add = new File(ADD_STAGING, args[i]);
            if (!add.exists()) {
                try {
                    add.createNewFile();
                } catch (IOException e) {
                    exitWithError("Cant add file.");
                }
            }

            String read = Utils.readContentsAsString(tempFile);
            Utils.writeContents(add, read);

            File removeFile = new File(REMOVE_STAGING, args[i]);
            if (removeFile.exists()) {
                removeFile.delete();
            }

            if (headCommit.getNameToSha().containsKey(args[i])) {
                String getSha = headCommit.getNameToSha().get(args[i]);
                File blobFile = headCommit.getShaToBlob().get(getSha);
                String blobContents = Utils.readContentsAsString(blobFile);
                if (Utils.readContentsAsString(add).equals(blobContents)) {
                    add.delete();
                }
            }
        }
    }

    public static void commit(String[] args) {
        if (ADD_STAGING.listFiles().length == 0
                && REMOVE_STAGING.listFiles().length == 0) {
            exitWithError("No changes added to the commit.");
        }
        if (args.length == 1 || args[1].equals("")) {
            exitWithError("Please enter a commit message.");
        }
        if (args.length != 2) {
            exitWithError("Wrong number of arguments for commit");
        }
        File headCommit = new File(Utils.readContentsAsString(HEAD));
        Commit newCommit = new
                Commit(args[1], Utils.readObject(headCommit, Commit.class));
        for (File file : REMOVE_STAGING.listFiles()) {
            String fileName = file.getName();
            if (newCommit.getNameToSha().containsKey(fileName)) {
                newCommit.getShaToBlob().remove
                    (newCommit.getNameToSha().get(fileName));
                newCommit.getNameToSha().remove(fileName);
            }
        }
        for (File file : ADD_STAGING.listFiles()) {
            if (newCommit.getNameToSha().containsKey(file.getName())) {
                String shaToDelete = newCommit
                        .getNameToSha().get(file.getName());
                newCommit.getNameToSha().remove(file.getName());
                newCommit.getShaToBlob().remove(shaToDelete);
            }
            String sha = Utils.sha1(Utils.readContents(file));
            File blobby = new File(BLOBS, sha);
            String read = Utils.readContentsAsString(file);
            try {
                blobby.createNewFile();
                Utils.writeContents(blobby, read);
            } catch (IOException e) {
                exitWithError("Cannot add blob");
            }
            newCommit.addNameShaPair(file.getName(), sha);
            newCommit.addShaBlobPair(sha, blobby);
        }
        newCommit.setHash(Utils.sha1(Utils.serialize(newCommit)));
        File newCommitFile = new File(COMMITS, newCommit.getHash());
        try {
            newCommitFile.createNewFile();
            Utils.writeObject(newCommitFile, newCommit);
        } catch (IOException e) {
            exitWithError("unable to create commit");
        }
        Utils.writeContents(HEAD, ".gitlet/commits/" + newCommit.getHash());
        Branches branchMap = Utils.readObject(BRANCHES, Branches.class);
        String currentBranch = Utils.readContentsAsString(ACTIVE_BRANCH);
        branchMap.getBranchMap().put
            (currentBranch, Utils.readContentsAsString(HEAD));
        Utils.writeObject(BRANCHES, branchMap);
        clearStaging();
    }

    public static void rm(String[] args) {
        String fileRemovedName = args[1];
        File readFile = new File(Utils.readContentsAsString(HEAD));
        Commit headCommit = Utils.readObject(readFile, Commit.class);
        if (!Utils.plainFilenamesIn(ADD_STAGING).contains(fileRemovedName)
                && !headCommit.getNameToSha().containsKey(fileRemovedName)) {
            System.out.println("No reason to remove the file.");
        }
        if (Utils.plainFilenamesIn(ADD_STAGING)
                .contains(fileRemovedName)) {
            File toDelete = new File(ADD_STAGING, fileRemovedName);
            toDelete.delete();
        } else if (headCommit.getNameToSha()
                .containsKey(fileRemovedName)) {
            File removeFile = new File(REMOVE_STAGING, fileRemovedName);
            if (!removeFile.exists()) {
                try {
                    removeFile.createNewFile();
                } catch (IOException e) {
                    exitWithError("Cant add file.");
                }
            }

            File removeFileCws = new File(CWS, fileRemovedName);
            if (removeFileCws.exists()) {
                removeFileCws.delete();
            }
        }
    }

    public static void log(String[] args) {
        if (args.length != 1) {
            exitWithError("Incorrect operands.");
        }
        Commit headCommit = Utils.readObject
            (new File(Utils.readContentsAsString(HEAD)), Commit.class);
        while (headCommit != null) {
            System.out.println("===");
            System.out.println("commit " + headCommit.getHash());
            System.out.println("Date: " + headCommit.getTimeStamp());
            System.out.println(headCommit.getMessage() + "\n");
            headCommit = headCommit.getParent();
        }
    }

    public static void globalLog(String[] args) {
        if (args.length != 1) {
            exitWithError("Incorrect operands.");
        }
        for (String commitPath : Utils.plainFilenamesIn(COMMITS)) {
            Commit commit = Utils.readObject
                    (new File(".gitlet/commits/"
                            + commitPath), Commit.class);
            System.out.println("===");
            System.out.println("commit " + commit.getHash());
            System.out.println("Date: " + commit.getTimeStamp());
            System.out.println(commit.getMessage() + "\n");
        }
    }

    public static void find(String[] args) {
        if (args.length != 2) {
            exitWithError("Wrong number of arguments for find");
        }
        String messageToFind = args[1];
        int count = 0;
        for (String commitPath : Utils.plainFilenamesIn(COMMITS)) {
            File commitFile = new File(".gitlet/commits/" + commitPath);
            Commit commit = Utils.readObject(commitFile, Commit.class);
            if (commit.getMessage().equals(messageToFind)) {
                System.out.println(commit.getHash());
                count++;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status(String[] args) {
        if (!GITLET.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        if (args.length != 1) {
            exitWithError("Not the number of arguments wanted for status");
        }
        System.out.println("=== Branches ===");
        System.out.println("*" + Utils.readContentsAsString(ACTIVE_BRANCH));
        Branches branchMap = Utils.readObject(BRANCHES, Branches.class);
        for (Map.Entry<String, String> set
                : branchMap.getBranchMap().entrySet()) {
            if (!set.getKey().equals(Utils
                    .readContentsAsString(ACTIVE_BRANCH))) {
                System.out.println(set.getKey());
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String fileName : Utils.plainFilenamesIn(ADD_STAGING)) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String fileName : Utils.plainFilenamesIn(REMOVE_STAGING)) {
            System.out.println(fileName);
        }
        System.out.println();

        modificationPrint();

        File readFile = new File(Utils.readContentsAsString(HEAD));
        Commit headCommit = Utils.readObject(readFile, Commit.class);
        Map<String, String> getNameToSha = headCommit.getNameToSha();
        System.out.println("=== Untracked Files ===");
        for (String fileName : Utils.plainFilenamesIn(CWS)) {
            File stagingCheck = new File(ADD_STAGING, fileName);
            if (!getNameToSha.containsKey(fileName) && !stagingCheck.exists()) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    private static void modificationPrint() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        File readFile = new File(Utils.readContentsAsString(HEAD));
        Commit headCommit = Utils.readObject(readFile, Commit.class);
        Map<String, String> getNameToSha = headCommit.getNameToSha();
        for (Map.Entry<String, String> set : getNameToSha.entrySet()) {
            File fileChange = new File(CWS, set.getKey());
            if (fileChange.exists()) {
                File fileAddStaging = new File(ADD_STAGING, set.getKey());
                File fileRemoveStaging = new File(REMOVE_STAGING, set.getKey());
                if (!fileAddStaging.exists()
                        && !fileRemoveStaging.exists()) {
                    String fileChangeSha = Utils
                            .sha1(Utils.readContentsAsString(fileChange));
                    if (!set.getValue().equals(fileChangeSha)) {
                        System.out.println(set.getKey() + "(modified)");
                    }
                }
            } else {
                File fileRemoval = new File(REMOVE_STAGING, set.getKey());
                if (!fileRemoval.exists()) {
                    System.out.println(set.getKey() + "(deleted)");
                }
            }
        }
        for (File fileName : ADD_STAGING.listFiles()) {
            File stageCwd = new File(CWS, fileName.getName());
            if (!stageCwd.exists()) {
                System.out.println(fileName.getName() + "(deleted)");
            } else {
                if (!Utils.readContentsAsString(fileName)
                        .equals(Utils.readContentsAsString(stageCwd))) {
                    System.out.println(fileName.getName() + "(modified)");
                }
            }
        }
        System.out.println();
    }

    public static void checkout(String[] args) {
        if (args.length == 3) {
            String fileName = args[2];
            Commit headCommit = Utils.readObject(new File(Utils
                    .readContentsAsString(HEAD)), Commit.class);
            if (!headCommit.getNameToSha().containsKey(fileName)) {
                exitWithError("File does not exist in that commit.");
            }
            File checkoutFile = new File(CWS, fileName);
            String shaOfFile = headCommit.getNameToSha().get(fileName);
            File blobFile = headCommit.getShaToBlob().get(shaOfFile);
            String read = Utils.readContentsAsString(blobFile);
            if (!checkoutFile.exists()) {
                try {
                    checkoutFile.createNewFile();
                } catch (IOException e) {
                    exitWithError("cant create checkout file");
                }
            }
            Utils.writeContents(checkoutFile, read);
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                exitWithError("Incorrect operands.");
            }
            String fileName2 = args[3]; String commitName = args[1];
            for (String fileName : Utils.plainFilenamesIn(COMMITS)) {
                if (fileName.substring(0, 8).equals(commitName)) {
                    commitName = fileName;
                    break;
                }
            }
            File wantedCommitFile = new File(COMMITS, commitName);
            if (!wantedCommitFile.exists()) {
                exitWithError("No commit with that id exists.");
            }
            Commit wantedCommit = Utils.readObject
                (wantedCommitFile, Commit.class);
            if (!wantedCommit.getNameToSha().containsKey(fileName2)) {
                exitWithError("File does not exist in that commit.");
            }
            File checkoutFile2 = new File(CWS, fileName2);
            String shaOfFile = wantedCommit.getNameToSha().get(fileName2);
            File blobFile = wantedCommit.getShaToBlob().get(shaOfFile);
            String read2 = Utils.readContentsAsString(blobFile);
            if (checkoutFile2.exists()) {
                Utils.writeContents(checkoutFile2, read2);
            } else {
                try {
                    checkoutFile2.createNewFile();
                    Utils.writeContents(checkoutFile2, read2);
                } catch (IOException e) {
                    exitWithError("cant create checkout file");
                }
            }
        } else if (args.length == 2) {
            String wantedBranchName = args[1];
            branchCheckout(wantedBranchName);
        }
    }

    private static void branchCheckout(String wantedBranchName) {
        Branches branchMap = Utils.readObject
            (BRANCHES, Branches.class);
        if (!branchMap.getBranchMap()
                .containsKey(wantedBranchName)) {
            exitWithError("No such branch exists.");
        }
        if (Utils.readContentsAsString(ACTIVE_BRANCH)
                .equals(wantedBranchName)) {
            exitWithError(" No need to checkout the current branch.");
        }
        String wantedCommitPath = branchMap
                .getBranchMap().get(wantedBranchName);
        Commit wantedBranchHeadCommit = Utils
                .readObject(new File(wantedCommitPath), Commit.class);
        Commit headCommit = Utils.readObject
                (new File(Utils.readContentsAsString(HEAD)), Commit.class);
        for (String fileName : Utils.plainFilenamesIn(CWS)) {
            File addStage = new File(ADD_STAGING, fileName);
            if (wantedBranchHeadCommit.getNameToSha()
                    .containsKey(fileName)) {
                if (!headCommit.getNameToSha()
                        .containsKey(fileName) && !addStage.exists()) {
                    exitWithError("There is an untracked file in the "
                            + "way; delete it, or add and commit it first.");
                }
            }
        }
        for (File file : ADD_STAGING.listFiles()) {
            file.delete();
        }
        for (String file : Utils.plainFilenamesIn(CWS)) {
            File cwsFile = new File(CWS, file);
            cwsFile.delete();
        }
        Utils.writeContents(HEAD, wantedCommitPath);
        Utils.writeContents(ACTIVE_BRANCH, wantedBranchName);
        for (Map.Entry<String, String> set : wantedBranchHeadCommit
                .getNameToSha().entrySet()) {

            String fileName = set.getKey();
            String sha = set.getValue();

            File blobFile = wantedBranchHeadCommit.getShaToBlob().get(sha);
            String read = Utils.readContentsAsString(blobFile);

            File checkoutFile = new File(CWS, fileName);

            if (checkoutFile.exists()) {
                Utils.writeContents(checkoutFile, read);
            } else {
                try {
                    checkoutFile.createNewFile();
                    Utils.writeContents(checkoutFile, read);
                } catch (IOException e) {
                    exitWithError("cant create checkout file");
                }
            }
        }
    }


    public static void branch(String[] args) {
        if (!(args.length == 2)) {
            exitWithError("Too many args for branches");
        }
        Branches branchMap = Utils.readObject(BRANCHES, Branches.class);
        String branchName = args[1];
        if (branchMap.getBranchMap().containsKey(branchName)) {
            exitWithError(" A branch with that name already exists.");
        } else {
            branchMap.getBranchMap().put
                (branchName, Utils.readContentsAsString(HEAD));
        }
        Utils.writeObject(BRANCHES, branchMap);
    }


    public static void rmBranch(String[] args) {
        String branchName = args[1];

        Branches branchMap = Utils
                .readObject(BRANCHES, Branches.class);
        if (!branchMap.getBranchMap().containsKey(branchName)) {
            exitWithError("A branch with that "
                    + "name does not exist.");
        }
        if (branchName.equals(Utils
                .readContentsAsString(ACTIVE_BRANCH))) {
            exitWithError("Cannot remove the current branch.");
        }
        branchMap.getBranchMap().remove(branchName);
        Utils.writeObject(BRANCHES, branchMap);
    }

    public static void reset(String[] args) {
        resetErrors(args);
        String commitId = args[1];
        File wantedCommitFile = new File(COMMITS, commitId);
        Commit wantedCommit = Utils.readObject
                (wantedCommitFile, Commit.class);
        Commit headCommit = Utils.readObject
                (new File(Utils.readContentsAsString
                        (HEAD)), Commit.class);
        Map<String, String> getNameToSha = headCommit.getNameToSha();
        for (Map.Entry<String, String> set : getNameToSha.entrySet()) {
            if (!wantedCommit.getNameToSha().containsKey(set.getKey())) {
                File removeFile = new File(CWS, set.getKey());
                if (removeFile.exists()) {
                    removeFile.delete();
                }
            }
        }
        for (File file : ADD_STAGING.listFiles()) {
            if (!wantedCommit.getNameToSha().containsKey(file.getName())) {
                File removeFile = new File(CWS, file.getName());
                if (removeFile.exists()) {
                    removeFile.delete();
                }
            }
        }
        for (String fileName: wantedCommit.getNameToSha().keySet()) {
            String sha = wantedCommit.getNameToSha().get(fileName);
            File blobFile = wantedCommit.getShaToBlob().get(sha);
            String readContents = Utils.readContentsAsString(blobFile);
            File cwdFile = new File(CWS, fileName);
            if (!cwdFile.exists()) {
                try {
                    cwdFile.createNewFile();
                } catch (IOException e) {
                    exitWithError("Cannot make cwdFile reset");
                }
            }
            Utils.writeContents(cwdFile, readContents);
        }
        Branches branchMap = Utils.readObject(BRANCHES, Branches.class);
        String currentBranch = Utils.readContentsAsString(ACTIVE_BRANCH);
        branchMap.getBranchMap().put
            (currentBranch, ".gitlet/commits/" + wantedCommit.getHash());
        Utils.writeObject(BRANCHES, branchMap);
        Utils.writeContents(HEAD, ".gitlet/commits/" + wantedCommit.getHash());
        for (File file : ADD_STAGING.listFiles()) {
            file.delete();
        }

        for (File file : REMOVE_STAGING.listFiles()) {
            file.delete();
        }
    }

    private static void resetErrors(String[] args) {
        if (args.length != 2) {
            exitWithError("Correct amount of arguments not entered");
        }
        String commitId = args[1];
        File wantedCommitFile = new File(COMMITS, commitId);
        if (!wantedCommitFile.exists()) {
            exitWithError(" No commit with that id exists.");
        }
        Commit wantedCommit = Utils.readObject
                (wantedCommitFile, Commit.class);
        Commit headCommit = Utils.readObject
                (new File(Utils.readContentsAsString
                        (HEAD)), Commit.class);
        for (String fileName : Utils.plainFilenamesIn(CWS)) {
            File addStage = new File(ADD_STAGING, fileName);
            if (wantedCommit.getNameToSha().containsKey(fileName)) {
                if (!headCommit.getNameToSha().containsKey(fileName)
                        && !addStage.exists()) {
                    exitWithError("There is an untracked "
                            + "file in the way; delete "
                            + "it, or add and commit it first.");
                }
            }
        }
    }

    public static void clearTrackedFiles() {
        Commit headCommit = Utils.readObject
                (new File(Utils.readContentsAsString
                        (HEAD)), Commit.class);
        for (String fileNames : Utils.plainFilenamesIn(CWS)) {
            File addFile = new File(ADD_STAGING, fileNames);
            if (headCommit.getNameToSha()
                    .containsKey(fileNames) || addFile.exists()) {
                Utils.restrictedDelete(fileNames);
            }
        }
    }

    public static void caseA(String otherVal, String key) {
        if (!otherVal.equals("")) {
            File otherFileBlob = new File(BLOBS, otherVal);
            String readOtherContent = "";
            if (otherFileBlob.exists()) {
                readOtherContent = Utils
                        .readContentsAsString(otherFileBlob);
            }
            File addFile = new File(ADD_STAGING, key);
            if (!addFile.exists()) {
                try {
                    addFile.createNewFile();
                } catch (IOException e) {
                    exitWithError("idk how add "
                            + "in merge not made");
                }
            }
            Utils.writeContents(addFile, readOtherContent);
        } else {
            File removeFile = new File(REMOVE_STAGING, key);
            if (!removeFile.exists()) {
                try {
                    removeFile.createNewFile();
                } catch (IOException e) {
                    exitWithError("remove file not "
                            + "being created for merge case D");
                }
            }
        }
    }
    public static void merge(String[] args) {
        mergeErrors(args);
        clearTrackedFiles();
        Commit headCommit = Utils.readObject(new
                File(Utils.readContentsAsString(HEAD)), Commit.class);
        String branchNameToMerge = args[1];
        Branches branchMap = Utils.readObject(BRANCHES, Branches.class);
        String branchToMergePath = branchMap
                .getBranchMap().get(branchNameToMerge);
        File branchToMergeFile = new File(branchToMergePath);
        Commit branchToMergeHead = Utils
                .readObject(branchToMergeFile, Commit.class);
        Commit otherLengthCommit = branchToMergeHead;
        Commit headLengthCommit = headCommit;
        Commit splitPoint = mergeSplit(headLengthCommit,
                otherLengthCommit);
        if (splitPoint.getHash().equals(branchToMergeHead.getHash())) {
            System.out.println("Given branch is "
                    + "an ancestor of the current branch.");
            return;
        }
        if (splitPoint.getHash().equals(headCommit.getHash())) {
            String[] checkoutStringArr = new String[2];
            checkoutStringArr[1] = branchNameToMerge;
            checkoutStringArr[0] = "checkout"; checkout(checkoutStringArr);
            exitWithError("Current branch fast-forwarded.");
        }
        Map<String, String> headBlobs = headCommit.getNameToSha();
        Map<String, String> otherBlobs = branchToMergeHead.getNameToSha();
        Map<String, String> splitBlobs = splitPoint.getNameToSha();
        Map<String, String> allBlobs = new HashMap<>();
        allBlobs.putAll(headBlobs);
        allBlobs.putAll(otherBlobs); allBlobs.putAll(splitBlobs);
        boolean mergeConflict = false;
        for (String key : allBlobs.keySet()) {
            String headVal = headBlobs.getOrDefault(key, "");
            String otherVal = otherBlobs.getOrDefault(key, "");
            String splitVal = splitBlobs.getOrDefault(key, "");
            if (headVal.equals(splitVal)
                    && !otherVal.equals(headVal)
                    && !headVal.equals("")) {
                caseA(otherVal, key);
            } else if (headVal.equals("") && headVal.equals(splitVal)
                    && !otherVal.equals("")) {
                otherMadeFile(key, otherVal);
            } else if (!splitVal.equals(headVal)
                    && !splitVal.equals(otherVal)
                    && !otherVal.equals(headVal)) {
                mergeConflict = mergeConflictCase(key,
                        otherVal, headVal);
            }
        }
        String activeBranchName = Utils.readContentsAsString(ACTIVE_BRANCH);
        String message = "Merged "
                + branchNameToMerge + " into " + activeBranchName + ".";
        mergeCommit(message, branchToMergeHead);
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static void otherMadeFile(String key, String otherVal) {
        File otherFileBlob
                = new File(BLOBS, otherVal);
        String readOtherContent = "";
        if (otherFileBlob.exists()) {
            readOtherContent = Utils
                    .readContentsAsString(otherFileBlob);
        }
        File addFile = new File(ADD_STAGING, key);
        if (!addFile.exists()) {
            try {
                addFile.createNewFile();
            } catch (IOException e) {
                exitWithError("idk how add in merge not made");
            }
        }
        Utils.writeContents(addFile, readOtherContent);
    }
    private static boolean mergeConflictCase(String key, String
            otherVal, String headVal) {
        File otherFileBlob = new File(BLOBS, otherVal);
        File headFileBlob = new File(BLOBS, headVal);
        String readOtherContent = "";
        String readHeadContent = "";
        if (otherVal.equals("")) {
            if (!otherFileBlob.exists()) {
                readOtherContent = "";
            }
        } else {
            readOtherContent = Utils
                    .readContentsAsString(otherFileBlob);
        }

        if (!headFileBlob.exists()) {
            readHeadContent = "";
        } else {

            readHeadContent = Utils
                    .readContentsAsString(headFileBlob);
        }

        File addFile = new File(ADD_STAGING, key);
        if (!addFile.exists()) {
            try {
                addFile.createNewFile();
            } catch (IOException e) {
                exitWithError("idk how add in merge not made");
            }
        }
        Utils.writeContents(addFile, "<<<<<<< HEAD\n" + readHeadContent
                + "=======\n" + readOtherContent + ">>>>>>>\n");

        return true;
    }

    private static void mergeCommitErrors(String message) {
        if (ADD_STAGING.listFiles().length == 0
                && REMOVE_STAGING.listFiles().length == 0) {
            exitWithError("No changes added to the commit.");
        }

        if (message.length() == 1 || message.equals("")) {
            exitWithError("Please enter a commit message.");
        }
    }

    public static void mergeCommit(String message, Commit mergeParent) {
        mergeCommitErrors(message);
        File headCommit = new File(Utils.readContentsAsString(HEAD));
        Commit newCommit = new
                Commit(message, Utils.readObject(
                        headCommit, Commit.class), mergeParent);
        for (File file : REMOVE_STAGING.listFiles()) {
            String fileName = file.getName();
            if (newCommit.getNameToSha().containsKey(fileName)) {
                newCommit.getNameToSha().remove(fileName);
            }
        }
        for (File file : ADD_STAGING.listFiles()) {
            String sha = Utils.sha1(Utils.readContents(file));
            File blobby = new File(BLOBS, sha);
            String read = Utils.readContentsAsString(file);
            try {
                blobby.createNewFile();
                Utils.writeContents(blobby, read);
            } catch (IOException e) {
                exitWithError("Cannot add blob");
            }
            newCommit.addNameShaPair(file.getName(), sha);
            newCommit.addShaBlobPair(sha, blobby);
        }
        newCommit.setHash(Utils.sha1(Utils.serialize(newCommit)));
        File newCommitFile = new File(COMMITS, newCommit.getHash());
        try {
            newCommitFile.createNewFile();
            Utils.writeObject(newCommitFile, newCommit);
        } catch (IOException e) {
            exitWithError("unable to create commit");
        }

        Utils.writeContents(HEAD, ".gitlet/commits/" + newCommit.getHash());
        Branches branchMap = Utils.readObject(BRANCHES, Branches.class);
        String currentBranch = Utils.readContentsAsString(ACTIVE_BRANCH);
        branchMap.getBranchMap().put(currentBranch,
                Utils.readContentsAsString(HEAD));
        Utils.writeObject(BRANCHES, branchMap);
        for (String nameKey : newCommit.getNameToSha().keySet()) {
            String sha = newCommit.getNameToSha().get(nameKey);
            File blobFile = newCommit.getShaToBlob().get(sha);
            if (blobFile != null) {
                String readContents = Utils.readContentsAsString(blobFile);
                File cwsFile = new File(CWS, nameKey);
                if (!cwsFile.exists()) {
                    try {
                        cwsFile.createNewFile();
                    } catch (IOException e) {
                        exitWithError("idk how add in merge not made in cws");
                    }
                }
                Utils.writeContents(cwsFile, readContents);
            }
        }
        clearStaging();
    }

    private static void clearStaging() {
        for (File file : ADD_STAGING.listFiles()) {
            file.delete();
        }

        for (File file : REMOVE_STAGING.listFiles()) {
            file.delete();
        }
    }

    public static Commit mergeSplit(Commit headCommit, Commit otherCommit) {
        Queue<Commit> otherCommitQueue = new LinkedList<>();
        HashSet<String> possibleSplits = new HashSet<>();
        Queue<Commit> headCommitQueue = new LinkedList<>();
        otherCommitQueue.add(otherCommit);
        while (!otherCommitQueue.isEmpty()) {
            if (otherCommitQueue.peek() != null) {
                if (otherCommitQueue.peek().getParent() != null) {
                    otherCommitQueue.add(otherCommitQueue.peek().getParent());
                }
                if (otherCommitQueue.peek().getMergeParent() != null) {
                    otherCommitQueue.add(otherCommitQueue
                            .peek().getMergeParent());
                }
            }
            if (otherCommitQueue.peek() != null) {
                possibleSplits.add(otherCommitQueue.poll().getHash());
            }

        }
        headCommitQueue.add(headCommit);
        while (!headCommitQueue.isEmpty()) {
            if (headCommitQueue.peek() != null) {
                if (possibleSplits.contains(headCommitQueue.peek().getHash())) {
                    return headCommitQueue.peek();
                }
            }
            if (headCommitQueue.peek() != null) {
                if (headCommitQueue.peek().getParent() != null) {
                    headCommitQueue.add(headCommitQueue.peek().getParent());
                }
                if (headCommitQueue.peek().getMergeParent() != null) {
                    headCommitQueue.add(headCommitQueue
                            .peek().getMergeParent());
                }
            }
            headCommitQueue.poll();
        }
        return headCommit;
    }

    public static void mergeErrors(String[] args) {
        if (args.length != 2) {
            exitWithError("Incorrect operands merge");
        }

        String branchToMerge = args[1];
        if (ADD_STAGING.listFiles().length
                != 0 || REMOVE_STAGING.listFiles().length != 0) {
            exitWithError("You have uncommitted changes.");
        }
        Branches branchMap = Utils.readObject(BRANCHES, Branches.class);
        if (!branchMap.getBranchMap().containsKey(branchToMerge)) {
            exitWithError("A branch with that name does not exist.");
        }
        String currentBranch = Utils.readContentsAsString(ACTIVE_BRANCH);
        if (currentBranch.equals(branchToMerge)) {
            exitWithError("Cannot merge a branch with itself.");
        }

        String branchToMergePath = branchMap.getBranchMap().get(branchToMerge);
        File branchToMergeFile = new File(branchToMergePath);
        Commit branchToMergeHead = Utils.readObject
                (branchToMergeFile, Commit.class);

        Commit headCommit = Utils.readObject(new
                File(Utils.readContentsAsString(HEAD)), Commit.class);
        for (String fileName : Utils.plainFilenamesIn(CWS)) {
            File addStage = new File(ADD_STAGING, fileName);
            if (branchToMergeHead.getNameToSha().containsKey(fileName)) {
                if (!headCommit.getNameToSha().containsKey
                        (fileName) && !addStage.exists()) {
                    exitWithError("There is an untracked file in the way"
                            + "; delete it, or add and commit it first.");
                }
            }
        }
    }
    public static void exitWithError(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(0);
    }
}
