package gitlet;

import java.io.File;

/**
 * Represents the HEAD in a version control system, tracking the current commit and branch.
 * <p>
 * The Head class manages the current commit and branch pointers, enabling operations like
 * branch switching and commit management. The state is persisted in the HEAD file.
 * </p>
 */
public class Head {
    // File references to the HEAD and HEAD_LOG files in the repository.
    private static final File HEAD = Repository.HEAD;
    private static final File HEAD_LOG = Repository.HEAD_LOG;

    private String currentCommitSHA;
    private String currentBranchName;

    /**
     * Constructs a Head object with the specified commit sha and branch sha.
     *
     * @param currentCommitSHA  The sha-1 hash of the current commit.
     * @param currentBranchName The sha-1 hash of the current branch.
     */
    public Head(String currentCommitSHA, String currentBranchName) {
        this.currentCommitSHA = currentCommitSHA;
        this.currentBranchName = currentBranchName;
    }

    /**
     * Loads the Head object from the HEAD file.
     *
     * @return A Head object initialized with the current commit and branch sha-1 hashes.
     * @throws RuntimeException if the HEAD file does not exist or is unreadable.
     */
    public static Head fromFile() {
        if (HEAD.exists()) {
            String headFile = Utils.readContentsAsString(HEAD);
            String[] temp = headFile.split(";");
            return new Head(temp[0], temp[1]);
        } else {
            throw new RuntimeException();
        }
    }

    /**
     * Saves the current state of the Head object to the HEAD file.
     */
    public void saveHEAD() {
        String content = currentCommitSHA + ";" + currentBranchName;
        Utils.writeContents(HEAD, content);
    }

    /**
     * Retrieves the current branch as a Branch object.
     *
     * @return The Branch object corresponding to the current branch sha-1 hash.
     */
    public Branch getCurrentBranch() {
        return Branch.fromFile(currentBranchName);
    }

    /**
     * Sets the current branch to the specified Branch object and updates the HEAD file.
     *
     * @param branch The Branch object to set as the current branch.
     */
    public void setCurrentBranch(Branch branch) {
        setCurrentBranch(branch.getName());
    }

    /**
     * Sets the current branch sha-1 hash and updates the HEAD file.
     *
     * @param currentBranchName The sha-1 hash of the branch to set as current.
     */
    public void setCurrentBranch(String currentBranchName) {
        this.currentBranchName = currentBranchName;
        saveHEAD();
    }

    /**
     * Retrieves the Tree object representing the current state of the working directory.
     *
     * @return The Tree object associated with the current commit.
     */
    public Tree getCurrentTree() {
        Commit temp = Commit.fromFile(currentCommitSHA);
        return Tree.fromFile(temp.getTreeSHA());
    }

    public String getCurrentCommitSHA() {
        return currentCommitSHA;
    }

    /**
     * Sets the current commit to the specified Commit object and updates the HEAD file.
     *
     * @param currentCommit The Commit object to set as the current commit.
     */
    public void setCurrentCommitSHA(Commit currentCommit) {
        this.currentCommitSHA = currentCommit.getSHA();
        saveHEAD();
    }

    /**
     * Sets the current commit sha-1 hash and updates the HEAD file.
     *
     * @param currentCommitSHA The sha-1 hash of the commit to set as current.
     */
    public void setCurrentCommitSHA(String currentCommitSHA) {
        this.currentCommitSHA = currentCommitSHA;
        saveHEAD();
    }
}
