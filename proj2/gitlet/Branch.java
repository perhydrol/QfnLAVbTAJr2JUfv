package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Branch implements Serializable {
    /**
     * The SHA-1 hash of the initial commit for this branch.
     */
    private final String STAR_COMMIT_SHA;
    /**
     * The name of the branch.
     */
    private final String name;
    /**
     * The SHA-1 hash of the most recent commit in this branch.
     */
    private String END_COMMIT_SHA;
    /**
     * The SHA-1 hash of the current commit pointed to by this branch.
     */
    private String currentCommitSHA;

    /**
     * Constructs a Branch object with the given name and initial commit SHA.
     *
     * @param name          The name of the branch.
     * @param starCommitSHA The SHA-1 hash of the initial commit.
     */
    public Branch(String name, String starCommitSHA) {
        STAR_COMMIT_SHA = starCommitSHA;
        END_COMMIT_SHA = starCommitSHA;
        currentCommitSHA = starCommitSHA;
        this.name = name;
    }

    /**
     * Loads a Branch object from the filesystem based on its name.
     *
     * @param branchName The name of the branch.
     * @return The Branch object corresponding to the given name.
     * @throws RuntimeException if the branch file does not exist or cannot be read.
     */
    public static Branch fromFile(String branchName) {
        File path = Utils.join(Repository.HEADS_DIR, branchName);
        if (path.exists()) {
            return Utils.readObject(path, Branch.class);
        } else {
            throw new RuntimeException("Branch file does not exist: " + path.getAbsolutePath());
        }
    }

    public static void merge(Branch current, Branch target) {
        Commit splitPoint=Commit.getSplitPoint(current.STAR_COMMIT_SHA,target.STAR_COMMIT_SHA);
    }

    public static void merge(String current, String target) {
        merge(Branch.fromFile(current), Branch.fromFile(target));
    }

    /**
     * Returns the SHA-1 hash of the initial commit.
     *
     * @return The initial commit SHA-1 hash.
     */
    public String getSTAR_COMMIT_SHA() {
        return STAR_COMMIT_SHA;
    }

    /**
     * Returns the SHA-1 hash of the end commit.
     *
     * @return The end commit SHA-1 hash.
     */
    public String getEND_COMMIT_SHA() {
        return END_COMMIT_SHA;
    }

    /**
     * Returns the SHA-1 hash of the current commit.
     *
     * @return The current commit SHA-1 hash.
     */
    public String getCurrentCommitSHA() {
        return currentCommitSHA;
    }

    public String getName() {
        return name;
    }

    /**
     * Saves this branch to the filesystem. The branch is stored in a file
     * named after the branch's name, inside the heads' directory.
     *
     * @throws RuntimeException if there is an issue creating or writing the branch file.
     */
    public void saveBranch() {
        File path = Utils.join(Repository.HEADS_DIR, this.name);
        try {
            if (!path.exists()) {
                path.createNewFile();
            }
            Utils.writeObject(path, this);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save branch: " + path.getAbsolutePath(), e);
        }
    }

    /**
     * Updates the end commit SHA and the current commit SHA to the given commit SHA.
     *
     * @param commitSHA The SHA-1 hash of the new commit.
     */
    public void newCommit(String commitSHA) {
        END_COMMIT_SHA = commitSHA;
        currentCommitSHA = commitSHA;
    }

    /**
     * Moves the current commit pointer to the given commit SHA.
     *
     * @param commitSHA The SHA-1 hash of the new commit.
     */
    public void moveCurrentCommit(String commitSHA) {
        currentCommitSHA = commitSHA;
    }

    /**
     * Retrieves the tree object corresponding to the current commit.
     *
     * @return The Tree object associated with the current commit.
     */
    public Tree getTree() {
        Commit commit = Commit.fromFile(currentCommitSHA);
        String treeSHA = commit.getTreeSHA();
        return Tree.fromFile(treeSHA);
    }
}
