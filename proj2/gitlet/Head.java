package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Head implements Serializable {
    private static final File HEAD = Repository.HEAD;
    private String commitSHA;
    private String branchName;

    public Head(String initCommitSHA, String initBranchName) throws IOException {
        this.commitSHA = initCommitSHA;
        this.branchName = initBranchName;
        saveHead();
    }

    public static Head fromFile() {
        if (!HEAD.exists()) {
            throw new RuntimeException("Head: No head file exist.");
        }
        return Utils.readObject(HEAD, Head.class);
    }

    public void setCommitSHA(String commitSHA) throws IOException {
        this.commitSHA = commitSHA;
        saveHead();
    }

    public void setBranchName(String branchName) throws IOException {
        this.branchName = branchName;
        saveHead();
    }

    private void saveHead() throws IOException {
        if (!HEAD.exists()) {
            HEAD.createNewFile();
        }
        Utils.writeObject(HEAD, this);
    }

    public Commit getCommit() {
        return Commit.fromFile(commitSHA);
    }

    public String getBranchName() {
        return branchName;
    }
}
